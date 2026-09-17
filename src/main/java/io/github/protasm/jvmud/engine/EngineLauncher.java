package io.github.protasm.jvmud.engine;

import io.github.protasm.jvmud.compiler.exec.LPCObjectLoadObserver;
import io.github.protasm.jvmud.cli.update.InstallationServers;
import io.github.protasm.jvmud.instance.MudlibBootProgress;
import io.github.protasm.jvmud.instance.MudlibSpec;
import io.github.protasm.jvmud.transport.admin.AdminWire;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Command-line configuration and diagnostics for the engine application. */
final class EngineLauncher {
    private static final int DEFAULT_PORT = 4000;
    private static final String DEFAULT_BIND_ADDRESS = "localhost";

    private EngineLauncher() {}

    /** Runs the engine until its listener stops, recording orderly shutdown for updates. */
    static void run(String[] args) throws IOException {
        LaunchOptions options;
        try {
            options = parseLaunchOptions(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(usage());
            System.exit(2);
            return;
        }
        if (options.help()) {
            System.out.println(usage());
            return;
        }
        Path applicationRoot = Path.of("").toAbsolutePath();
        EngineLog.install(applicationRoot, options.port());
        StartupObjectLoadTrace trace = commandLineObjectLoadTrace(options.traceStartupLoads());
        try (InstallationServers registration = InstallationServers.register(args, applicationRoot);
                Engine engine = new Engine(options.bindAddress(), options.port(), options.mudlibs(),
                        commandLineBootProgress(), trace)) {
            Thread shutdown = new Thread(() -> {
                engine.close();
                try { registration.close(); }
                catch (IOException e) { System.err.println("Unable to record clean shutdown: " + e.getMessage()); }
            }, "jvmud-engine-shutdown");
            Runtime.getRuntime().addShutdownHook(shutdown);
            try {
                engine.start();
                if (options.adminPort() != null) {
                    Path tokenFile = options.adminTokenFile() == null
                            ? AdminWire.tokenFile(options.adminPort()) : options.adminTokenFile();
                    engine.startAdministration(options.adminGame(), options.adminPort(), tokenFile);
                    System.out.println("JVMud admin listening on 127.0.0.1:" + options.adminPort()
                            + " (credential: " + tokenFile.toAbsolutePath() + ")");
                }
                trace.finishStartup();
                for (var mud : engine.mudlibs()) {
                    System.out.println(mud.gameId() + ": " + Engine.preloadSummary(mud.bootResult()));
                }
                trace.printSummaryIfEnabled();
                System.out.println("JVMud engine listening on " + engine.bindAddress() + ":" + engine.port());
                registration.ready();
                engine.await();
            } finally {
                engine.close();
                registration.close();
                try { Runtime.getRuntime().removeShutdownHook(shutdown); }
                catch (IllegalStateException ignored) { /* JVM shutdown is already in progress. */ }
            }
        }
    }

    /**
     * Parses a manifest and optional listener settings. Defaults to localhost:4000;
     * network exposure requires an explicit bind address. Ports must be 1–65535.
     *
     * @throws IllegalArgumentException for missing values or invalid options
     */
    static LaunchOptions parseLaunchOptions(String[] args) {
        if (args.length == 1 && ("-help".equals(args[0]) || "--help".equals(args[0]))) {
            return new LaunchOptions(java.util.List.of(), DEFAULT_PORT, DEFAULT_BIND_ADDRESS, true, false, null, null, null);
        }

        boolean traceStartupLoads = false;
        java.util.List<MudlibSpec> mudlibs = new java.util.ArrayList<>();
        String adminGame = null;
        String bindAddress = DEFAULT_BIND_ADDRESS;
        int port = DEFAULT_PORT;
        Integer adminPort = null;
        Path adminTokenFile = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--trace-startup-loads".equals(arg)) {
                traceStartupLoads = true;
            } else if ("--bind".equals(arg) || "--port".equals(arg)
                    || "--admin-port".equals(arg) || "--admin-token-file".equals(arg) || "--admin-game".equals(arg)) {
                if (++i >= args.length || args[i].isBlank() || args[i].startsWith("--")) {
                    throw new IllegalArgumentException("Missing value for " + arg + ".");
                }
                if ("--bind".equals(arg)) {
                    bindAddress = args[i];
                } else if ("--admin-game".equals(arg)) {
                    adminGame = args[i];
                } else if ("--admin-token-file".equals(arg)) {
                    adminTokenFile = Path.of(args[i]);
                } else {
                    int value;
                    try {
                        value = Integer.parseInt(args[i]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Port must be an integer from 1 to 65535.");
                    }
                    if (value < 1 || value > 65535) {
                        throw new IllegalArgumentException("Port must be an integer from 1 to 65535.");
                    }
                    if ("--admin-port".equals(arg)) adminPort = value;
                    else port = value;
                }
            } else if (arg.startsWith("-")) {
                throw new IllegalArgumentException("Unknown option: " + arg);
            } else {
                mudlibs.add(MudlibSpec.fromConfig(resolveLaunchConfigFile(Path.of(arg), launchRoot())));
            }
        }

        if (mudlibs.isEmpty()) {
            throw new IllegalArgumentException("Missing mudlib config file.");
        }
        if (adminTokenFile != null && adminPort == null) {
            throw new IllegalArgumentException("--admin-token-file requires --admin-port.");
        }
        if (adminPort != null && adminPort == port) {
            throw new IllegalArgumentException("Player and admin ports must be different.");
        }
        if (adminGame != null && adminPort == null) {
            throw new IllegalArgumentException("--admin-game requires --admin-port.");
        }
        if (adminPort != null && mudlibs.size() > 1 && adminGame == null) {
            throw new IllegalArgumentException("--admin-game is required when administering multiple mudlibs.");
        }
        return new LaunchOptions(java.util.List.copyOf(mudlibs), port, bindAddress, false,
                traceStartupLoads, adminPort, adminTokenFile, adminGame);
    }

    static Path resolveLaunchConfigFile(Path argument, Path root) {
        Path direct = root.resolve(argument).normalize();
        if (Files.isRegularFile(direct)) {
            return direct;
        }
        Path fallback = root.resolve("mudlibs/" + argument + "/jvmud/" + argument + ".config").normalize();
        if (Files.isRegularFile(fallback)) {
            return fallback;
        }
        throw new IllegalArgumentException("Mudlib config file not found. Tried: " + direct + " and " + fallback);
    }

    private static Path launchRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (isRepositoryRoot(current)) {
                return current;
            }
            current = current.getParent();
        }
        return Path.of("").toAbsolutePath().normalize();
    }

    private static boolean isRepositoryRoot(Path path) {
        return Files.exists(path.resolve("pom.xml")) && Files.isDirectory(path.resolve("mudlibs"));
    }

    private static String usage() {
        return "Usage: scripts/jvmud-start [--bind <address>] [--port <port>] "
                + "[--admin-port <port>] [--admin-game <game-id>] [--admin-token-file <path>] [--trace-startup-loads] <mudlib-config-file-or-name>...\n"
                + "Resolves the config path first, then mudlibs/<name>/jvmud/<name>.config.\n"
                + "Options: --bind selects a listener address (default localhost).\n"
                + "         --port selects a TCP port from 1 to 65535 (default 4000).\n"
                + "         --admin-port enables authenticated local administration on a separate port.\n"
                + "         --admin-game selects the mudlib to administer (required with multiple mudlibs).\n"
                + "         --admin-token-file overrides the private per-port credential path.\n"
                + "         --trace-startup-loads prints every underlying startup object load.\n"
                + "Use --bind 0.0.0.0 to accept connections on all IPv4 interfaces.";
    }

    private static MudlibBootProgress commandLineBootProgress() {
        return new MudlibBootProgress() {
            @Override
            public void preloadStarted(PreloadKind kind, String sourcePath) {
                System.out.println(preloadLabel(kind) + " " + sourcePath + ": starting.");
            }

            @Override
            public void preloadFinished(PreloadKind kind, String sourcePath, boolean loaded) {
                String outcome = loaded ? "compiled." : "skipped.";
                System.out.println(preloadLabel(kind) + " " + sourcePath + ": " + outcome);
            }

            @Override
            public void preloadFailed(PreloadKind kind, String sourcePath, Throwable error) {
                System.err.println(preloadLabel(kind) + " " + sourcePath + " failed: "
                        + error.getMessage());
            }
        };
    }

    private static String preloadLabel(MudlibBootProgress.PreloadKind kind) {
        return switch (kind) {
            case CONFIGURED_OBJECT -> "preload configured object";
            case MANIFEST_OBJECT -> "preload manifest object";
        };
    }

    static StartupObjectLoadTrace commandLineObjectLoadTrace(boolean traceStartupLoads) {
        return new StartupObjectLoadTrace(traceStartupLoads);
    }

    private static String loadIndent(int depth) {
        return "  ".repeat(Math.max(0, depth));
    }

    static final class StartupObjectLoadTrace implements LPCObjectLoadObserver {
        private final boolean enabled;
        private boolean active;
        private final Set<String> loadedObjectIds = new HashSet<>();
        private final Set<String> failedObjectIds = new HashSet<>();
        private final Set<String> compiledObjectIds = new HashSet<>();
        private final Set<String> failedCompileObjectIds = new HashSet<>();
        private int loadedAttempts;
        private int failedAttempts;
        private int compiledAttempts;
        private int failedCompileAttempts;

        StartupObjectLoadTrace(boolean enabled) {
            this.enabled = enabled;
            this.active = enabled;
        }

        @Override
        public void objectLoadStarted(String objectId, Path sourcePath, int depth) {
            if (active) {
                System.out.println(loadIndent(depth) + "startup object /" + objectId + ": starting.");
            }
        }

        @Override
        public void objectLoadFinished(String objectId, Path sourcePath, int depth, boolean loaded, long elapsedNanos) {
            if (!active) {
                return;
            }
            if (loaded) {
                loadedAttempts++;
                loadedObjectIds.add(objectId);
            } else {
                failedAttempts++;
                failedObjectIds.add(objectId);
            }
            String outcome = loaded ? "loaded" : "failed";
            double elapsedMillis = elapsedNanos / 1_000_000.0;
            System.out.printf(
                    Locale.ROOT,
                    "%sstartup object /%s: %s in %.1f ms.%n",
                    loadIndent(depth),
                    objectId,
                    outcome,
                    elapsedMillis);
        }

        @Override
        public void objectCompileFinished(String objectId, Path sourcePath, boolean compiled, long elapsedNanos) {
            if (!active) {
                return;
            }
            if (compiled) {
                compiledAttempts++;
                compiledObjectIds.add(objectId);
            } else {
                failedCompileAttempts++;
                failedCompileObjectIds.add(objectId);
            }
            String outcome = compiled ? "compiled" : "failed";
            double elapsedMillis = elapsedNanos / 1_000_000.0;
            System.out.printf(
                    Locale.ROOT,
                    "startup compile /%s: %s in %.1f ms.%n",
                    objectId,
                    outcome,
                    elapsedMillis);
        }

        @Override
        public void objectCompileStarted(String objectId, Path sourcePath) {
            if (active) {
                System.out.println("startup compile /" + objectId + ": starting.");
            }
        }

        @Override
        public void objectLoadFailed(String objectId, Path sourcePath, int depth, Throwable failure) {
            if (active) {
                System.out.println(loadIndent(depth)
                        + "startup object /"
                        + objectId
                        + ": "
                        + failureSummary(failure));
            }
        }

        @Override
        public void objectCompileFailed(String objectId, Path sourcePath, Throwable failure) {
            if (active) {
                System.out.println("startup compile /" + objectId + ": " + failureSummary(failure));
            }
        }

        void finishStartup() {
            active = false;
        }

        String summary() {
            return "startup object load summary: loaded "
                    + loadedObjectIds.size()
                    + " unique object(s) across "
                    + loadedAttempts
                    + " load attempt(s), failed "
                    + failedObjectIds.size()
                    + " unique object(s) across "
                    + failedAttempts
                    + " load attempt(s).\n"
                    + "startup compile summary: compiled "
                    + compiledObjectIds.size()
                    + " unique object(s) across "
                    + compiledAttempts
                    + " compile attempt(s), failed "
                    + failedCompileObjectIds.size()
                    + " unique object(s) across "
                    + failedCompileAttempts
                    + " compile attempt(s).";
        }

        void printSummaryIfEnabled() {
            if (enabled) {
                System.out.println(summary());
            }
        }

        private static String failureSummary(Throwable failure) {
            String message = failure.getMessage();
            if (message == null || message.isBlank()) {
                message = failure.getClass().getName();
            } else {
                message = failure.getClass().getSimpleName() + ": " + message;
            }
            return "failed because " + message;
        }
    }

    /** Launch configuration; every mudlib is an equal menu entry. */
    record LaunchOptions(java.util.List<MudlibSpec> mudlibs, int port, String bindAddress,
            boolean help, boolean traceStartupLoads, Integer adminPort, Path adminTokenFile,
            String adminGame) {}
}
