package io.github.protasm.jvmud.execution.application;

import io.github.protasm.jvmud.execution.application.update.InstallationServers;
import io.github.protasm.jvmud.execution.instance.MudlibSpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Command-line configuration and diagnostics for the engine application. */
final class EngineLauncher {
    private static final int DEFAULT_PORT = 4000;
    private static final String DEFAULT_BIND_ADDRESS = "localhost";

    private EngineLauncher() {}

    /** Starts engine services first, then attempts each configured mudlib independently. */
    static void run(String[] args) throws IOException {
        LaunchOptions options;
        try { options = parseLaunchOptions(args); }
        catch (IllegalArgumentException e) { System.err.println(e.getMessage()); System.err.println(usage()); System.exit(2); return; }
        if (options.help()) { System.out.println(usage()); return; }
        Path applicationRoot = Path.of("").toAbsolutePath();
        EngineLog.install(applicationRoot, options.port());
        try (InstallationServers registration = InstallationServers.register(args, applicationRoot);
                JVMud engine = new JVMud(new EngineConfiguration(options.bindAddress(), options.port(), options.adminPort(),
                        options.stateDirectory(), options.traceStartupLoads(), options.mudlibDirectory()))) {
            Thread shutdown = new Thread(() -> closeForProcessExit(engine, registration), "jvmud-engine-shutdown");
            Runtime.getRuntime().addShutdownHook(shutdown);
            try {
                engine.start();
                System.out.print(engine.describe());
                System.out.println("Administration TLS fingerprint: " + Files.readString(options.stateDirectory().resolve("admin-tls.sha256")).trim());
                registration.ready();
                for (MudlibSpec spec : options.mudlibs()) {
                    try { System.out.println(engine.startMudlib(spec, 0, 0)); }
                    catch (IOException | RuntimeException e) { System.err.println("Mudlib startup failed: " + e.getMessage()); }
                }
                engine.await();
            } finally {
                engine.close();
                try { Runtime.getRuntime().removeShutdownHook(shutdown); }
                catch (IllegalStateException ignored) { /* Process shutdown already began. */ }
            }
        }
    }

    /**
     * Records shutdown after worker save hooks finish, before the JVM exits.
     * Signal-driven shutdown does not wait for the main thread's resource cleanup.
     */
    private static void closeForProcessExit(JVMud engine, InstallationServers registration) {
        engine.close();
        try { registration.close(); }
        catch (IOException e) { System.err.println("Cannot record completed engine shutdown: " + e.getMessage()); }
    }

    /** Accepts zero or more initial mudlibs; engine administration always has its own port. */
    static LaunchOptions parseLaunchOptions(String[] args) {
        java.util.List<Path> initialMudlibs = new java.util.ArrayList<>();
        int port = DEFAULT_PORT, adminPort = 4001;
        String bind = DEFAULT_BIND_ADDRESS;
        Path state = null;
        Path mudlibDirectory = launchRoot().resolve("mudlibs");
        boolean trace = false, help = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--help") || arg.equals("-help")) help = true;
            else if (arg.equals("--trace-startup-loads")) trace = true;
            else if (java.util.Set.of("--bind", "--port", "--admin-port", "--state-dir", "--mudlib-dir").contains(arg)) {
                if (++i >= args.length || args[i].isBlank() || args[i].startsWith("--")) throw new IllegalArgumentException("Missing value for " + arg);
                if (arg.equals("--bind")) bind = args[i];
                else if (arg.equals("--state-dir")) state = Path.of(args[i]).toAbsolutePath().normalize();
                else if (arg.equals("--mudlib-dir")) mudlibDirectory = Path.of(args[i]).toAbsolutePath().normalize();
                else {
                    int value;
                    try { value = Integer.parseInt(args[i]); }
                    catch (NumberFormatException e) { throw new IllegalArgumentException("Port must be an integer from 1 to 65535."); }
                    if (value < 1 || value > 65535) throw new IllegalArgumentException("Port must be an integer from 1 to 65535.");
                    if (arg.equals("--port")) port = value; else adminPort = value;
                }
            } else if (arg.startsWith("-")) throw new IllegalArgumentException("Unknown option: " + arg);
            else initialMudlibs.add(Path.of(arg));
        }
        if (port == adminPort) throw new IllegalArgumentException("Player and admin ports must be different.");
        if (state == null) state = Path.of(System.getProperty("user.home"), ".jvmud", "engine-" + port);
        java.util.List<MudlibSpec> mudlibs = new java.util.ArrayList<>();
        for (Path argument : initialMudlibs)
            mudlibs.add(MudlibSpec.fromConfig(resolveLaunchConfigFile(argument, launchRoot(), mudlibDirectory)));
        return new LaunchOptions(java.util.List.copyOf(mudlibs), port, bind, help, trace, adminPort, state, mudlibDirectory);
    }

    /** Resolves a host-supplied path or a short name under the launch root's default mudlib directory. */
    static Path resolveLaunchConfigFile(Path argument, Path root) {
        return resolveLaunchConfigFile(argument, root, root.resolve("mudlibs"));
    }

    /** Preserves explicit host launch paths while resolving short names in the configured directory. */
    private static Path resolveLaunchConfigFile(Path argument, Path root, Path mudlibDirectory) {
        Path direct = root.resolve(argument).normalize();
        if (Files.isRegularFile(direct)) {
            return direct;
        }
        Path fallback = mudlibDirectory.resolve(argument + "/jvmud/" + argument + ".config").normalize();
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
        return "Usage: scripts/jvmud-start [--bind <address>] [--port <player-port>] [--admin-port <port>] "
                + "[--state-dir <private-directory>] [--mudlib-dir <directory>] [--trace-startup-loads] [<mudlib-config-or-name>...]\n"
                + "Engine defaults: localhost player port 4000, TLS admin port 4001. Zero mudlibs is valid.\n"
                + "The state directory contains the local recovery socket, TLS identity and administrator registry.\n"
                + "Administration selects mudlibs by name from --mudlib-dir (default: mudlibs under the launch root).\n"
                + "Initial mudlibs receive available player/admin ports, shown in engine status.\n"
                + "Use the engine console start command to request a specific mudlib port pair.";
    }

    /** Application launch settings; initial mudlibs boot only after engine endpoints are ready. */
    record LaunchOptions(java.util.List<MudlibSpec> mudlibs, int port, String bindAddress,
            boolean help, boolean traceStartupLoads, int adminPort, Path stateDirectory, Path mudlibDirectory) {}
}
