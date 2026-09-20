package io.github.protasm.jvmud.execution.application.worker;

import io.github.protasm.jvmud.communication.admin.AdminSession;
import io.github.protasm.jvmud.execution.instance.MudlibSpec;
import io.github.protasm.jvmud.communication.transport.admin.AdminWire;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Engine-side owner of a worker JVM. The engine never loads this mudlib's LPC classes or runtime objects. */
public final class MudlibProcess implements AutoCloseable {
    private final Process process;
    private final Path scratch;
    private final DataInputStream input;
    private final DataOutputStream output;
    private final ExecutorService control = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "jvmud-worker-control"); t.setDaemon(true); return t;
    });
    private final WorkerWire.Ready ready;
    private volatile boolean closed;

    /** Starts a sandboxed, memory-bounded JVM and requires readiness before returning. */
    public MudlibProcess(MudlibSpec spec, Path log, boolean trace) throws IOException {
        scratch = Files.createTempDirectory("jvmud-worker-");
        List<Path> classpath = classpath();
        String cp = String.join(File.pathSeparator, classpath.stream().map(Path::toString).toList());
        List<String> java = List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Xms32m", "-Xmx256m", "-XX:MaxMetaspaceSize=128m", "-XX:MaxDirectMemorySize=64m",
                "-XX:ActiveProcessorCount=2", "-XX:+ExitOnOutOfMemoryError", "-XX:-UsePerfData",
                "-Djava.net.preferIPv4Stack=true", "-Djava.io.tmpdir=" + scratch, "-Duser.home=" + scratch,
                "-XX:ErrorFile=" + scratch.resolve("hs_err_pid%p.log"),
                "-cp", cp, "io.github.protasm.jvmud.execution.instance.MudlibWorker");
        try {
            ProcessBuilder builder = new ProcessBuilder(WorkerSandbox.command(spec.root(), scratch, log.getParent().getParent(), java, classpath));
            builder.directory(spec.root().toFile());
            builder.environment().clear();
            builder.environment().put("LANG", "en_US.UTF-8");
            process = builder.start();
        } catch (IOException | RuntimeException e) { deleteScratch(); throw e; }
        input = new DataInputStream(process.getInputStream()); output = new DataOutputStream(process.getOutputStream());
        Thread diagnostics = Thread.ofVirtual().name("jvmud-worker-log").start(() -> {
            try (var source = process.getErrorStream(); var target = Files.newOutputStream(log)) {
                byte[] buffer = new byte[8192]; int count; long total = 0;
                while ((count = source.read(buffer)) != -1) {
                    int allowed = (int) Math.min(count, Math.max(0, 1024 * 1024 - total));
                    if (allowed > 0) target.write(buffer, 0, allowed);
                    total += count;
                }
            } catch (IOException ignored) { /* Worker exit closes its diagnostics pipe. */ }
        });
        try {
            ready = bounded(() -> {
                WorkerWire.write(output, new WorkerWire.Boot(spec.root().toString(), spec.configPath(), trace));
                return WorkerWire.read(input, WorkerWire.Ready.class);
            }, 60);
            if (!ready.id().matches("[A-Za-z0-9_-]{1,64}") || ready.playerPort() < 1 || ready.playerPort() > 65535)
                throw new IOException("Worker returned invalid readiness metadata.");
        } catch (IOException e) {
            close();
            try { diagnostics.join(1000); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
            String diagnostic = Files.exists(log) ? Files.readString(log) : "";
            throw new IOException("Mudlib startup failed; inspect " + log + ": " + e.getMessage() + "\n" + diagnostic.substring(0, Math.min(3000, diagnostic.length())), e);
        }
    }

    /** Reports immutable worker readiness metadata. */
    public WorkerWire.Ready ready() { return ready; }
    /** Worker process identity for status reporting and supervision. */
    public ProcessHandle handle() { return process.toHandle(); }
    /** Completes when the worker exits, including an unexpected crash. */
    public CompletableFuture<Process> onExit() { return process.onExit(); }

    /** Opens the authenticated private player channel and preserves the original client address. */
    public Socket connectPlayer(String clientAddress) throws IOException {
        if (closed || !process.isAlive()) throw new IOException("Mudlib is not running.");
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("127.0.0.1", ready.playerPort()), 5000);
            var out = new DataOutputStream(socket.getOutputStream());
            AdminWire.write(out, ready.secret(), 256); AdminWire.write(out, clientAddress, 256);
            return socket;
        } catch (IOException e) { socket.close(); throw e; }
    }

    /** Opens independent command state in the worker; grants have already been checked by the engine. */
    public AdminSession administration() throws IOException {
        String id = UUID.randomUUID().toString();
        request("OPEN", id, "");
        return new AdminSession() {
            public String scope() { return "mudlib:" + ready.id(); }
            public Reply execute(String command) throws IOException {
                WorkerWire.Response response = request("COMMAND", id, command);
                return new Reply(response.text(), response.running());
            }
            public void close() throws IOException { if (!closed && process.isAlive()) request("CLOSE", id, ""); }
        };
    }

    private WorkerWire.Response request(String operation, String id, String command) throws IOException {
        return bounded(() -> {
            WorkerWire.write(output, new WorkerWire.Request(operation, id, command));
            return WorkerWire.read(input, WorkerWire.Response.class);
        }, 15);
    }

    private <T> T bounded(Callable<T> operation, int seconds) throws IOException {
        Future<T> future;
        try { future = control.submit(operation); }
        catch (RejectedExecutionException e) { throw new IOException("Worker is stopped.", e); }
        try { return future.get(seconds, TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IOException("Interrupted waiting for worker.", e); }
        catch (ExecutionException e) { throw new IOException("Worker command failed: " + e.getCause(), e.getCause()); }
        catch (TimeoutException e) {
            process.destroyForcibly(); future.cancel(true);
            throw new IOException("Worker did not respond within " + seconds + " seconds and was terminated.", e);
        }
    }

    /** Gives shutdown hooks a bounded opportunity, then terminates an unresponsive worker. */
    @Override public void close() {
        if (closed) return; closed = true;
        try {
            if (process.isAlive()) {
                control.submit(() -> {
                    try { WorkerWire.write(output, new WorkerWire.Request("STOP", "", "")); } catch (IOException ignored) {}
                });
                if (!process.waitFor(2, TimeUnit.SECONDS)) {
                    process.destroy();
                    if (!process.waitFor(1, TimeUnit.SECONDS)) { process.destroyForcibly(); process.waitFor(2, TimeUnit.SECONDS); }
                }
            }
        } catch (InterruptedException e) { process.destroyForcibly(); Thread.currentThread().interrupt(); }
        finally {
            control.shutdownNow();
            try { input.close(); output.close(); } catch (IOException ignored) {}
            deleteScratch();
        }
    }

    /** Expands manifest Class-Path entries as well as ordinary launcher classpaths. */
    private static List<Path> classpath() throws IOException {
        Set<Path> paths = new LinkedHashSet<>();
        Deque<Path> pending = new ArrayDeque<>();
        for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) pending.add(Path.of(path).toAbsolutePath().normalize());
        while (!pending.isEmpty()) {
            Path path = pending.removeFirst().toRealPath();
            if (!paths.add(path) || !path.toString().endsWith(".jar")) continue;
            try (var jar = new java.util.jar.JarFile(path.toFile())) {
                var manifest = jar.getManifest();
                String dependencies = manifest == null ? null : manifest.getMainAttributes().getValue("Class-Path");
                if (dependencies != null) for (String dependency : dependencies.trim().split("\\s+")) {
                    java.net.URI resolved = path.toUri().resolve(dependency);
                    if (!resolved.getScheme().equals("file")) throw new IOException("Worker classpath must contain local files.");
                    pending.add(Path.of(resolved));
                }
            }
        }
        return List.copyOf(paths);
    }

    private void deleteScratch() {
        try (var paths = Files.walk(scratch)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
        } catch (IOException ignored) { /* A terminated worker may leave diagnostics for manual cleanup. */ }
    }
}
