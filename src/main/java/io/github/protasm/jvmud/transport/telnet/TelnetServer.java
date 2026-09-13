package io.github.protasm.jvmud.transport.telnet;

import io.github.protasm.jvmud.compiler.exec.LPCObjectLoadObserver;
import io.github.protasm.jvmud.cli.update.InstallationServers;
import io.github.protasm.jvmud.instance.InstanceHost;
import io.github.protasm.jvmud.instance.MudlibBoot;
import io.github.protasm.jvmud.instance.MudlibBootProgress;
import io.github.protasm.jvmud.instance.MudlibBootResult;
import io.github.protasm.jvmud.instance.MudlibRouter;
import io.github.protasm.jvmud.engine.time.WorldClock;
import java.io.IOException;
import io.github.protasm.jvmud.transport.admin.AdminServer;
import io.github.protasm.jvmud.transport.admin.AdminWire;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Starts a mudlib as a persistent telnet target for interactive JVMud sessions. */
public final class TelnetServer implements AutoCloseable {
    public static final int DEFAULT_PORT = 4000;
    private static final String DEFAULT_BIND_ADDRESS = "localhost";

    private final String bindAddress;
    private final int requestedPort;
    private final Path mudlibRoot;
    private final String configObjectPath;
    private final MudlibBootProgress bootProgress;
    private final LPCObjectLoadObserver objectLoadObserver;
    private final ExecutorService sessions;
    private InstanceHost mud;
    private WorldClock worldClock;
    private AdminServer adminServer;
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;
    private boolean shutdownNotified;

    public TelnetServer(String bindAddress, int port, Path mudlibRoot, String configObjectPath) {
        this(bindAddress, port, mudlibRoot, configObjectPath, MudlibBootProgress.none(), LPCObjectLoadObserver.NONE);
    }

    private TelnetServer(
            String bindAddress,
            int port,
            Path mudlibRoot,
            String configObjectPath,
            MudlibBootProgress bootProgress,
            LPCObjectLoadObserver objectLoadObserver) {
        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
        this.requestedPort = port;
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        this.configObjectPath = Objects.requireNonNull(configObjectPath, "configObjectPath");
        this.bootProgress = Objects.requireNonNullElse(bootProgress, MudlibBootProgress.none());
        this.objectLoadObserver = Objects.requireNonNullElse(objectLoadObserver, LPCObjectLoadObserver.NONE);
        this.sessions = Executors.newCachedThreadPool(new TelnetThreadFactory("jvmud-session"));
    }

    public static void main(String[] args) throws IOException {
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

        MudlibServerLog.install(options.mudlibRoot(), options.port());
        StartupObjectLoadTrace startupLoadTrace = commandLineObjectLoadTrace(options.traceStartupLoads());
        TelnetServer server = new TelnetServer(
                options.bindAddress(),
                options.port(),
                options.mudlibRoot(),
                options.configObjectPath(),
                commandLineBootProgress(),
                startupLoadTrace);
        InstallationServers registration = InstallationServers.register(args, options.mudlibRoot());
        try {
            server.start();
            if (options.adminPort() != null) {
                Path tokenFile = options.adminTokenFile() == null
                        ? AdminWire.tokenFile(options.adminPort()) : options.adminTokenFile();
                server.startAdministration(options.adminPort(), tokenFile);
                System.out.println("JVMud admin listening on 127.0.0.1:" + options.adminPort()
                        + " (credential: " + tokenFile.toAbsolutePath() + ")");
            }
        } catch (IOException | RuntimeException e) {
            server.close();
            throw e;
        }
        startupLoadTrace.finishStartup();
        System.out.println(server.preloadSummary());
        startupLoadTrace.printSummaryIfEnabled();
        System.out.println("JVMud mudlib listening on " + server.bindAddress() + ":" + server.port());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.close();
            try { registration.close(); }
            catch (IOException e) { System.err.println("Unable to record clean shutdown: " + e.getMessage()); }
        }, "jvmud-start-shutdown"));
        registration.ready();
        server.await();
    }

    /**
     * Parses a manifest and optional listener settings. Defaults to localhost:4000;
     * network exposure requires an explicit bind address. Ports must be 1–65535.
     *
     * @throws IllegalArgumentException for missing values or invalid options
     */
    static LaunchOptions parseLaunchOptions(String[] args) {
        if (args.length == 1 && ("-help".equals(args[0]) || "--help".equals(args[0]))) {
            return new LaunchOptions(null, DEFAULT_PORT, DEFAULT_BIND_ADDRESS, null, true, false, null, null);
        }

        boolean traceStartupLoads = false;
        Path configFile = null;
        String bindAddress = DEFAULT_BIND_ADDRESS;
        int port = DEFAULT_PORT;
        Integer adminPort = null;
        Path adminTokenFile = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--trace-startup-loads".equals(arg)) {
                traceStartupLoads = true;
            } else if ("--bind".equals(arg) || "--port".equals(arg)
                    || "--admin-port".equals(arg) || "--admin-token-file".equals(arg)) {
                if (++i >= args.length || args[i].isBlank() || args[i].startsWith("--")) {
                    throw new IllegalArgumentException("Missing value for " + arg + ".");
                }
                if ("--bind".equals(arg)) {
                    bindAddress = args[i];
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
            } else if (configFile == null) {
                configFile = Path.of(arg);
            } else {
                throw new IllegalArgumentException("Too many arguments.");
            }
        }

        if (configFile == null) {
            throw new IllegalArgumentException("Missing mudlib config file.");
        }
        if (adminTokenFile != null && adminPort == null) {
            throw new IllegalArgumentException("--admin-token-file requires --admin-port.");
        }
        if (adminPort != null && adminPort == port) {
            throw new IllegalArgumentException("Player and admin ports must be different.");
        }
        return optionsForConfigFile(configFile, port, bindAddress, traceStartupLoads, adminPort, adminTokenFile);
    }

    private static LaunchOptions optionsForConfigFile(
            Path configFile, int port, String bindAddress, boolean traceStartupLoads,
            Integer adminPort, Path adminTokenFile) {
        Path resolvedConfigFile = resolveLaunchConfigFile(configFile, launchRoot());
        Path mudlibRoot = mudlibRootForConfigFile(resolvedConfigFile);
        String configObjectPath = mudlibRoot.relativize(resolvedConfigFile).toString()
                .replace('\\', '/');
        return new LaunchOptions(
                mudlibRoot, port, bindAddress, configObjectPath, false, traceStartupLoads, adminPort, adminTokenFile);
    }

    private static Path mudlibRootForConfigFile(Path configFile) {
        Path normalized = resolveConfigFile(configFile);
        Path configDir = normalized.getParent();
        if (configDir == null) {
            throw new IllegalArgumentException("Config file must have a parent directory: " + configFile);
        }
        if ("jvmud".equals(configDir.getFileName().toString())) {
            Path root = configDir.getParent();
            if (root == null) {
                throw new IllegalArgumentException("Config file must live inside a mudlib root: " + configFile);
            }
            return root;
        }
        return configDir;
    }

    private static Path resolveConfigFile(Path configFile) {
        if (configFile.isAbsolute()) {
            return configFile.normalize();
        }
        return launchRoot().resolve(configFile).normalize();
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
                + "[--admin-port <port>] [--admin-token-file <path>] [--trace-startup-loads] <mudlib-config-file-or-name>\n"
                + "Resolves the config path first, then mudlibs/<name>/jvmud/<name>.config.\n"
                + "Options: --bind selects a listener address (default localhost).\n"
                + "         --port selects a TCP port from 1 to 65535 (default 4000).\n"
                + "         --admin-port enables authenticated local administration on a separate port.\n"
                + "         --admin-token-file overrides the private per-port credential path.\n"
                + "         --trace-startup-loads prints every underlying startup object load.\n"
                + "Use --bind 0.0.0.0 to accept connections on all IPv4 interfaces.";
    }

    public synchronized void start() throws IOException {
        if (running) {
            return;
        }
        mud = MudlibRouter.boot(mudlibRoot, configObjectPath, bootProgress, objectLoadObserver);
        startWorldClock();
        serverSocket = new ServerSocket(requestedPort, 50, InetAddress.getByName(bindAddress));
        running = true;
        acceptThread = new Thread(this::acceptLoop, "jvmud-start-accept");
        acceptThread.start();
    }

    /**
     * Enables authenticated loopback administration of the running primary world.
     * Must use a different port from the player listener; port zero requests an available port
     * for embedded callers. Closing this server closes the admin endpoint and its sessions.
     */
    public synchronized int startAdministration(int port, Path tokenFile) throws IOException {
        if (!running) throw new IllegalStateException("Start the player server before administration.");
        if (adminServer != null) throw new IllegalStateException("Administration already started.");
        if (port != 0 && port == port()) throw new IllegalArgumentException("Player and admin ports must be different.");
        adminServer = new AdminServer(mud, port, tokenFile);
        return adminServer.port();
    }

    public void await() throws IOException {
        Thread thread = acceptThread;
        if (thread == null) {
            throw new IllegalStateException("Telnet server has not been started.");
        }
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for telnet server.", e);
        }
    }

    public String bindAddress() {
        return serverSocket == null ? bindAddress : serverSocket.getInetAddress().getHostAddress();
    }

    public int port() {
        return serverSocket == null ? requestedPort : serverSocket.getLocalPort();
    }

    String preloadSummary() {
        if (mud == null) {
            throw new IllegalStateException("Telnet server has not been started.");
        }
        MudlibBootResult result = mud.bootResult();
        if (result.mudlibBoundary().preloadFilePath().isEmpty()) {
            return "preload manifest: none declared.";
        }

        String preloadFilePath = result.mudlibBoundary().preloadFilePath().orElseThrow();
        StringBuilder summary = new StringBuilder()
                .append("preload manifest ")
                .append(preloadFilePath)
                .append(": compiled ")
                .append(result.preloadManifestPreloadedObjects().size())
                .append(" object(s), skipped ")
                .append(result.preloadManifestSkippedPreloads().size())
                .append(" object(s).");
        if (!result.preloadManifestSkippedPreloads().isEmpty()) {
            summary.append(" Skipped: ")
                    .append(String.join(", ", result.preloadManifestSkippedPreloads()));
        }
        return summary.toString();
    }

    @Override
    public synchronized void close() {
        running = false;
        if (adminServer != null) {
            adminServer.close();
            adminServer = null;
        }
        if (worldClock != null) {
            worldClock.close();
            worldClock = null;
        }
        if (mud != null && !shutdownNotified) {
            shutdownNotified = true;
            mud.shutdown(0);
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
                // Closing is best-effort during shutdown.
            }
        }
        sessions.shutdownNow();
    }

    private void startWorldClock() {
        Duration tickInterval = mud.worldTickInterval();
        if (tickInterval.isZero()) {
            return;
        }
        worldClock = new WorldClock(mud::advanceWorldTick, tickInterval);
        worldClock.start();
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

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                sessions.execute(new TelnetSession(socket, mud));
            } catch (IOException e) {
                if (running) {
                    System.err.println("Telnet accept failed: " + e.getMessage());
                }
            }
        }
    }

    private static final class TelnetThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger count = new AtomicInteger();

        private TelnetThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable task) {
            Thread thread = new Thread(task, prefix + "-" + count.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }

    /** Parsed launcher options; mudlib paths are absent only for a help request. */
    record LaunchOptions(
            Path mudlibRoot,
            int port,
            String bindAddress,
            String configObjectPath,
            boolean help,
            boolean traceStartupLoads,
            Integer adminPort,
            Path adminTokenFile) {}
}
