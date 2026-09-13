package io.github.protasm.jvmud.cli.update;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/** Updates an extracted installation; game content outside mudlib adapters is never replaced. */
public final class DistributionUpdater {
    static final List<String> ENGINE = List.of("lib", "scripts", "metadata", "runtime", "jre", "vendor-runtime", "README.md", "RELEASE-STATUS.md");
    private static final String DEFAULT_MANIFEST = "https://jvmud.org/downloads/latest.json";
    private static final HttpClient HTTP = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30)).build();

    public static void main(String[] args) throws Exception {
        if (args.length == 0) throw new IllegalArgumentException("Installation path required");
        Path root = Path.of(args[0]).toRealPath();
        List<String> options = new ArrayList<>(Arrays.asList(args).subList(1, args.length));
        if (options.contains("--help")) {
            System.out.println("Usage: scripts/jvmud-update [--check] [--manifest <HTTPS URL>]\n"
                    + "Backs up this installation, updates engine and unmodified mudlib jvmud/ files, and restarts its servers.\n"
                    + "Game content and saves outside jvmud/ are never replaced. External files/databases need separate backups.");
            return;
        }
        boolean worker = options.remove("--worker");
        if (!worker && !options.contains("--check")) {
            // Run from an independent classpath/JRE so replacing the installation cannot break the updater.
            Path temporary = Files.createTempDirectory("jvmud-updater-");
            try {
                copyTree(root.resolve("lib"), temporary.resolve("lib"));
                Path javaHome = Path.of(System.getProperty("java.home")).toRealPath();
                if (javaHome.startsWith(root)) {
                    copyTree(javaHome, temporary.resolve("java"));
                    javaHome = temporary.resolve("java");
                }
                List<String> command = new ArrayList<>(List.of(javaHome.resolve("bin/java").toString(), "-cp",
                        temporary.resolve("lib/*").toString(), DistributionUpdater.class.getName(), root.toString(), "--worker"));
                command.addAll(options);
                Process child = new ProcessBuilder(command).inheritIO().start();
                int exit = child.waitFor();
                if (exit != 0) throw new IOException("Update failed (exit " + exit + "). See the recovery details above.");
            } finally { deleteTree(temporary); }
            return;
        }
        boolean check = options.remove("--check");
        String manifest = DEFAULT_MANIFEST;
        if (options.size() == 2 && options.getFirst().equals("--manifest")) { manifest = options.get(1); options.clear(); }
        if (!options.isEmpty()) throw new IllegalArgumentException("Unknown updater arguments: " + options);
        try (FileChannel channel = FileChannel.open(InstallationServers.lockPath(root), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.tryLock()) {
            if (lock == null) throw new IOException("Another update is already running.");
            update(root, URI.create(manifest), check);
        }
    }

    static void update(Path root, URI manifestUri, boolean check) throws Exception {
        Path journal = root.resolve(".jvmud/update-in-progress.json");
        if (Files.exists(journal)) throw new IOException("An interrupted update needs recovery before continuing. See " + journal);
        JsonNode installed = read(root.resolve("metadata/update-index.json"));
        JsonNode latest = InstallationServers.JSON.readTree(fetch(manifestUri));
        String version = latest.path("version").asText();
        if (!version.matches("[A-Za-z0-9][A-Za-z0-9._-]*")) throw new IOException("Invalid release version");
        System.out.println("Installed: " + installed.path("version").asText() + "; latest: " + version);
        if (version.equals(installed.path("version").asText())) { System.out.println("Already current."); return; }
        String target = Files.exists(root.resolve("metadata/runtime.json"))
                ? read(root.resolve("metadata/runtime.json")).path("target").asText() : "bin";
        JsonNode artifact = latest.path("artifacts").path(target);
        if (artifact.isMissingNode()) throw new IOException("No release package for " + target);
        URI archiveUri = manifestUri.resolve(artifact.path("url").asText());
        String expected = artifact.path("sha256").asText();
        if (!expected.matches("[a-fA-F0-9]{64}")) throw new IOException("Missing release SHA-256");
        if (check) { System.out.println("Update available for " + target + ": " + archiveUri); return; }

        List<Server> servers = runningServers(root);
        Path work = Files.createTempDirectory(root.getParent(), ".jvmud-update-");
        try {
            Path archive = work.resolve("release.tar.gz");
            System.out.println("Downloading " + archiveUri);
            download(archiveUri, archive);
            if (!digest(archive).equalsIgnoreCase(expected)) throw new IOException("Download SHA-256 mismatch; servers were not stopped.");
            Path stage = extract(archive, work.resolve("unpacked"), "jvmud-" + version);
            JsonNode replacement = read(stage.resolve("metadata/update-index.json"));
            if (!version.equals(replacement.path("version").asText())) throw new IOException("Archive version mismatch");
            String stagedTarget = Files.exists(stage.resolve("metadata/runtime.json"))
                    ? read(stage.resolve("metadata/runtime.json")).path("target").asText() : "bin";
            if (!target.equals(stagedTarget)) throw new IOException("Archive platform mismatch");
            for (String required : List.of("lib", "scripts/jvmud-start", "scripts/jvmud-update")) {
                if (!Files.exists(stage.resolve(required))) throw new IOException("Incomplete archive: " + required);
            }
            installed = configurationBaselines(root, work, installed, replacement, archiveUri);
            List<String> adapters = adapterChanges(root, stage, installed, replacement);
            rejectExternalLinks(root.resolve("mudlibs"), root);
            // Fetching and conflict checks finish before downtime begins.
            List<Server> stopped = new ArrayList<>();
            List<Process> restarted = new ArrayList<>();
            Path backup = null;
            List<String> touched = new ArrayList<>();
            try {
                for (Server server : servers) { stop(server); stopped.add(server); }
                if (!runningServers(root).isEmpty()) throw new IOException("A server is still running; refusing to snapshot live files.");
                Path backupParent = root.resolveSibling("backup");
                Files.createDirectories(backupParent);
                Files.setPosixFilePermissions(backupParent, PosixFilePermissions.fromString("rwx------"));
                backup = backupParent.resolve(root.getFileName() + "-" + Instant.now().toString().replace(':', '-') + "-" + UUID.randomUUID());
                System.out.println("Backing up the full installation to " + backup);
                copyTree(root, backup);
                // Confirm every regular file copied successfully before replacing anything.
                verifyCopy(root, backup);
                List<String> changes = new ArrayList<>(ENGINE);
                changes.addAll(adapters);
                Files.createDirectories(journal.getParent());
                InstallationServers.JSON.writeValue(journal.toFile(), Map.of("backup", backup.toString(), "paths", changes));
                for (String name : changes) {
                    touched.add(name);
                    replace(root.resolve(name), stage.resolve(name));
                }
                for (Server server : stopped) restart(root, server, restarted);
                Files.deleteIfExists(journal);
                System.out.println("Update complete: " + version + ". Restarted " + restarted.size() + " server(s).");
                System.out.println("Backup: " + backup + "\nRestart logs: " + "each mudlib's jvmud/log/ directory");
            } catch (Exception failure) {
                System.err.println("Update failed: " + failure.getMessage());
                // Never restore files underneath a still-running replacement process.
                boolean allStopped = true;
                for (Process process : restarted) {
                    if (process.isAlive()) { process.destroy(); if (!process.waitFor(30, TimeUnit.SECONDS)) allStopped = false; }
                }
                if (!allStopped) throw new IOException("A replacement server did not stop. Files were left in place; recover from " + backup, failure);
                if (backup != null) {
                    for (String name : touched) replace(root.resolve(name), backup.resolve(name));
                    System.err.println("Previous JVMud files restored. Backup retained: " + backup);
                }
                Files.deleteIfExists(journal);
                for (Server server : stopped) {
                    try { restart(root, server, new ArrayList<>()); }
                    catch (Exception restartFailure) { failure.addSuppressed(restartFailure); System.err.println("Restart failed: " + restartFailure.getMessage()); }
                }
                throw failure;
            }
        } finally { deleteTree(work); }
    }

    record Server(Path record, long pid, String started, Path cwd, Path mudlibRoot, List<String> args, Map<String, String> environment) {}

    static List<Server> runningServers(Path root) throws IOException {
        List<Server> result = new ArrayList<>();
        Path directory = root.resolve(".jvmud/servers");
        if (Files.isDirectory(directory)) {
            try (Stream<Path> paths = Files.list(directory)) {
                for (Path record : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                    JsonNode data = read(record);
                    long pid = data.path("pid").asLong();
                    String started = data.path("started").asText();
                    if (!InstallationServers.isSameProcess(pid, started)) continue;
                    ProcessHandle process = ProcessHandle.of(pid).orElseThrow();
                    if (!belongsTo(root, process))
                        throw new IOException("Server record does not match its process: " + record);
                    if (!data.path("state").asText().equals("ready")) throw new IOException("Server is still starting: " + pid);
                    List<String> args = new ArrayList<>(); data.path("args").forEach(v -> args.add(v.asText()));
                    Map<String, String> env = new LinkedHashMap<>(); data.path("javaEnvironment").fields().forEachRemaining(e -> env.put(e.getKey(), e.getValue().asText()));
                    result.add(new Server(record, pid, started, Path.of(data.path("cwd").asText()), Path.of(data.path("mudlibRoot").asText()), args, env));
                }
            }
        }
        Set<Long> known = new HashSet<>(); result.forEach(s -> known.add(s.pid()));
        try (Stream<ProcessHandle> processes = ProcessHandle.allProcesses()) {
            for (ProcessHandle process : processes.toList()) {
                if (belongsTo(root, process) && !known.contains(process.pid())) throw new IOException("Unregistered server " + process.pid() + "; stop it manually and relaunch with this distribution's jvmud-start.");
            }
        }
        return result;
    }

    private static boolean belongsTo(Path root, ProcessHandle process) throws IOException {
        List<String> args = Arrays.asList(process.info().arguments().orElse(new String[0]));
        if (!args.contains("io.github.protasm.jvmud.transport.telnet.TelnetServer")) return false;
        for (int i = 0; i + 1 < args.size(); i++) {
            if (!Set.of("-cp", "-classpath", "--class-path").contains(args.get(i))) continue;
            for (String entry : args.get(i + 1).split(java.io.File.pathSeparator)) {
                Path path = Path.of(entry.endsWith("*") ? entry.substring(0, entry.length() - 1) : entry);
                if (Files.exists(path) && path.toRealPath().startsWith(root.resolve("lib").toRealPath())) return true;
            }
        }
        return false;
    }

    private static void stop(Server server) throws Exception {
        if (!InstallationServers.isSameProcess(server.pid(), server.started())) throw new IOException("Server changed before shutdown: " + server.pid());
        ProcessHandle process = ProcessHandle.of(server.pid()).orElseThrow();
        System.out.println("Stopping server " + server.pid());
        if (!process.destroy()) throw new IOException("Cannot stop server " + server.pid());
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (process.isAlive() && System.nanoTime() < deadline) Thread.sleep(100);
        if (process.isAlive()) throw new IOException("Server did not stop cleanly within 30 seconds; it was not force-killed.");
        if (!read(server.record()).path("state").asText().equals("stopped")) throw new IOException("Server exited without confirming saves/shutdown: " + server.pid());
    }

    private static void restart(Path root, Server server, List<Process> launched) throws Exception {
        Path logs = server.mudlibRoot().resolve("jvmud/log"); Files.createDirectories(logs);
        Path log = logs.resolve("server-" + server.pid() + "-" + System.nanoTime() + ".log");
        Files.createFile(log, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
        List<String> command = new ArrayList<>(List.of(root.resolve("scripts/jvmud-start").toString())); command.addAll(server.args());
        ProcessBuilder builder = new ProcessBuilder(command).directory(server.cwd().toFile())
                .redirectInput(ProcessBuilder.Redirect.from(Path.of("/dev/null").toFile()))
                .redirectErrorStream(true).redirectOutput(log.toFile());
        builder.environment().putAll(server.environment());
        builder.environment().put("JVMUD_UPDATE_OWNER", Long.toString(ProcessHandle.current().pid()));
        Process process = builder.start(); launched.add(process);
        Path record = root.resolve(".jvmud/servers/" + process.pid() + ".json");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
        while (process.isAlive() && System.nanoTime() < deadline) {
            if (Files.exists(record) && read(record).path("state").asText().equals("ready")) return;
            Thread.sleep(100);
        }
        throw new IOException("Server restart failed; see " + log);
    }

    static List<String> adapterChanges(Path root, Path stage, JsonNode oldIndex, JsonNode newIndex) throws IOException {
        Set<String> paths = new TreeSet<>(); oldIndex.path("adapters").fieldNames().forEachRemaining(paths::add); newIndex.path("adapters").fieldNames().forEachRemaining(paths::add);
        List<String> changes = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        for (String name : paths) {
            if (!name.matches("mudlibs/[^/]+/jvmud/.+") || name.contains("..") || name.contains("\\")) throw new IOException("Invalid adapter path: " + name);
            String oldHash = oldIndex.path("adapters").path(name).asText("");
            String newHash = newIndex.path("adapters").path(name).asText("");
            Path local = root.resolve(name), next = stage.resolve(name);
            if (!newHash.isEmpty() && (!Files.isRegularFile(next, LinkOption.NOFOLLOW_LINKS) || !digest(next).equals(newHash))) throw new IOException("Adapter checksum mismatch: " + name);
            if (oldHash.equals(newHash)) continue;
            String localHash = Files.exists(local, LinkOption.NOFOLLOW_LINKS) ? (Files.isRegularFile(local, LinkOption.NOFOLLOW_LINKS) ? digest(local) : "non-file") : "";
            if (localHash.equals(newHash)) continue;
            if (!localHash.equals(oldHash)) {
                if (Files.isRegularFile(local, LinkOption.NOFOLLOW_LINKS) && name.endsWith(".md")) {
                    System.out.println("Replacing locally edited documentation after full backup: " + name);
                    changes.add(name);
                    continue;
                }
                if (!configurationMatchesBaseline(local, name, oldHash, oldIndex)) {
                    conflicts.add(name);
                    continue;
                }
            }
            changes.add(name);
        }
        if (!conflicts.isEmpty()) throw new IOException("Local adapter/configuration conflicts with release:\n  "
                + String.join("\n  ", conflicts)
                + "\nMerge these files before updating; nothing was stopped.");
        return changes;
    }

    private static boolean configurationMatchesBaseline(Path local, String name, String oldHash, JsonNode index) throws IOException {
        if (!name.endsWith(".config") || !Files.isRegularFile(local, LinkOption.NOFOLLOW_LINKS)) return false;
        JsonNode baseline = index.path("configBaselines").path(name);
        if (!baseline.isTextual() || !textDigest(baseline.asText()).equals(oldHash)) return false;
        List<String> original = configurationLines(baseline.asText());
        return original != null && original.equals(configurationLines(Files.readString(local)));
    }

    // Conservative normalization follows the manifest reader's comment/separator rules.
    // Keep values and repeated-key ordering intact; never normalize whitespace inside values.
    static List<String> configurationLines(String text) {
        List<String> result = new ArrayList<>();
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            int separator = trimmed.indexOf('=');
            if (separator < 0) separator = trimmed.indexOf(':');
            if (separator <= 0) return null;
            String value = trimmed.substring(separator + 1);
            int comment = value.indexOf('#');
            if (comment >= 0) value = value.substring(0, comment);
            result.add(trimmed.substring(0, separator).trim() + "=" + value.trim());
        }
        return result;
    }

    // Hash-only releases need their original configuration text for a safe comparison.
    // The installed index authenticates each recovered file independently of the archive.
    static JsonNode configurationBaselines(Path root, Path work, JsonNode installed, JsonNode replacement, URI archiveUri) throws IOException {
        List<String> missing = new ArrayList<>();
        Iterator<String> names = installed.path("adapters").fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            if (!name.matches("mudlibs/[^/]+/jvmud/.+\\.config") || name.contains("..") || name.contains("\\")) continue;
            String oldHash = installed.path("adapters").path(name).asText();
            String newHash = replacement.path("adapters").path(name).asText("");
            Path local = root.resolve(name);
            if (oldHash.equals(newHash) || !Files.isRegularFile(local, LinkOption.NOFOLLOW_LINKS)) continue;
            String localHash = digest(local);
            if (localHash.equals(oldHash) || localHash.equals(newHash)) continue;
            JsonNode baseline = installed.path("configBaselines").path(name);
            if (!baseline.isTextual() || !textDigest(baseline.asText()).equals(oldHash)) missing.add(name);
        }
        if (missing.isEmpty()) return installed;
        String version = installed.path("version").asText();
        if (!version.matches("[A-Za-z0-9][A-Za-z0-9._-]*")) return installed;
        URI previous = archiveUri.resolve("jvmud-" + version + "-bin.tar.gz");
        try {
            System.out.println("Recovering configuration baseline from " + previous);
            Path archive = work.resolve("previous.tar.gz");
            download(previous, archive);
            Path stage = extract(archive, work.resolve("previous"), "jvmud-" + version);
            var enriched = (com.fasterxml.jackson.databind.node.ObjectNode) installed.deepCopy();
            var baselines = enriched.withObject("/configBaselines");
            for (String name : missing) {
                Path file = stage.resolve(name);
                if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
                        && digest(file).equals(installed.path("adapters").path(name).asText()))
                    baselines.put(name, Files.readString(file));
            }
            return enriched;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Configuration baseline download interrupted", e);
        } catch (Exception e) {
            System.out.println("Could not recover configuration baseline; unresolved differences will remain conflicts: " + e.getMessage());
            return installed;
        }
    }

    private static String textDigest(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    static Path extract(Path archive, Path destination, String expectedRoot) throws Exception {
        Process list = new ProcessBuilder("tar", "-tzf", archive.toString()).redirectError(ProcessBuilder.Redirect.INHERIT).start();
        try (BufferedReader reader = list.inputReader()) {
            for (String line; (line = reader.readLine()) != null;) {
                Path path = Path.of(line);
                if (path.isAbsolute() || !path.normalize().startsWith(expectedRoot) || Arrays.asList(line.split("/")).contains("..")) {
                    list.destroy(); throw new IOException("Unsafe archive path: " + line);
                }
            }
        }
        if (list.waitFor() != 0) throw new IOException("Cannot list downloaded archive");
        Files.createDirectories(destination);
        Process tar = new ProcessBuilder("tar", "-xzf", archive.toString(), "--no-same-owner", "-C", destination.toString()).inheritIO().start();
        if (tar.waitFor() != 0) throw new IOException("Cannot extract downloaded archive");
        Path result = destination.resolve(expectedRoot);
        rejectExternalLinks(result, result);
        return result;
    }

    static void rejectExternalLinks(Path start, Path allowedRoot) throws IOException {
        if (!Files.exists(start)) return;
        try (Stream<Path> files = Files.walk(start)) {
            for (Path file : files.filter(Files::isSymbolicLink).toList()) {
                if (!file.toRealPath().startsWith(allowedRoot.toRealPath())) throw new IOException("External symlink requires manual update/backup: " + file);
            }
        }
    }

    static void copyTree(Path source, Path target) throws IOException {
        if (!Files.exists(source, LinkOption.NOFOLLOW_LINKS)) return;
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path out = target.resolve(source.relativize(dir)); Files.createDirectories(out);
                // Keep staging writable; restore directory modes after writing their children.
                return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path out = target.resolve(source.relativize(file)); Files.createDirectories(out.getParent());
                Files.copy(file, out, LinkOption.NOFOLLOW_LINKS, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult postVisitDirectory(Path dir, IOException failure) throws IOException {
                if (failure != null) throw failure;
                Files.setPosixFilePermissions(target.resolve(source.relativize(dir)), Files.getPosixFilePermissions(dir));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static void verifyCopy(Path source, Path target) throws IOException {
        try (Stream<Path> files = Files.walk(source)) {
            for (Path file : files.toList()) {
                Path copy = target.resolve(source.relativize(file));
                if (Files.isSymbolicLink(file)) {
                    if (!Files.isSymbolicLink(copy) || !Files.readSymbolicLink(file).equals(Files.readSymbolicLink(copy))) throw new IOException("Backup link mismatch: " + file);
                } else if (Files.isRegularFile(file) && !digest(file).equals(digest(copy))) throw new IOException("Backup verification failed: " + file);
            }
        }
    }

    static void replace(Path destination, Path source) throws IOException { deleteTree(destination); copyTree(source, destination); }
    static void deleteTree(Path path) throws IOException {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return;
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Set<PosixFilePermission> modes = Files.getPosixFilePermissions(dir); modes.add(PosixFilePermission.OWNER_WRITE); Files.setPosixFilePermissions(dir, modes);
                return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException { Files.delete(file); return FileVisitResult.CONTINUE; }
            @Override public FileVisitResult postVisitDirectory(Path dir, IOException error) throws IOException { if (error != null) throw error; Files.delete(dir); return FileVisitResult.CONTINUE; }
        });
    }
    static String digest(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(file)) { byte[] bytes = new byte[65536]; for (int n; (n = input.read(bytes)) != -1;) digest.update(bytes, 0, n); }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static JsonNode read(Path path) throws IOException { return InstallationServers.JSON.readTree(path.toFile()); }
    private static HttpRequest request(URI uri) throws IOException {
        if (!uri.getScheme().equals("https") && !(uri.getScheme().equals("http") && Set.of("localhost", "127.0.0.1", "[::1]").contains(uri.getHost())))
            throw new IOException("Updates require HTTPS (HTTP is allowed only on loopback for local testing)");
        return HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(5)).GET().build();
    }
    private static byte[] fetch(URI uri) throws Exception {
        HttpResponse<byte[]> response = HTTP.send(request(uri), HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) throw new IOException("HTTP " + response.statusCode() + " downloading " + uri);
        return response.body();
    }
    private static void download(URI uri, Path file) throws Exception {
        HttpResponse<Path> response = HTTP.send(request(uri), HttpResponse.BodyHandlers.ofFile(file));
        if (response.statusCode() != 200) throw new IOException("HTTP " + response.statusCode() + " downloading " + uri);
    }
}
