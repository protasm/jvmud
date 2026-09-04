package io.github.protasm.jvmud.transport.telnet;

import io.github.protasm.jvmud.compiler.exec.LPCObjectLoadObserver;
import io.github.protasm.jvmud.instance.InstanceHost;
import io.github.protasm.jvmud.instance.MudlibBoot;
import io.github.protasm.jvmud.instance.MudlibBootProgress;
import io.github.protasm.jvmud.instance.MudlibBootResult;
import io.github.protasm.jvmud.instance.MudlibRouter;
import io.github.protasm.jvmud.engine.time.WorldClock;
import java.io.IOException;
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

        StartupObjectLoadTrace startupLoadTrace = commandLineObjectLoadTrace(options.traceStartupLoads());
        TelnetServer server = new TelnetServer(
                options.bindAddress(),
                options.port(),
                options.mudlibRoot(),
                options.configObjectPath(),
                commandLineBootProgress(),
                startupLoadTrace);
        server.start();
        startupLoadTrace.finishStartup();
        System.out.println(server.preloadSummary());
        startupLoadTrace.printSummaryIfEnabled();
        System.out.println("JVMud mudlib listening on " + server.bindAddress() + ":" + server.port());
        Runtime.getRuntime().addShutdownHook(new Thread(server::close, "jvmud-start-shutdown"));
        server.await();
    }

    static LaunchOptions parseLaunchOptions(String[] args) {
        if (args.length == 1 && ("-help".equals(args[0]) || "--help".equals(args[0]))) {
            return new LaunchOptions(null, DEFAULT_PORT, DEFAULT_BIND_ADDRESS, null, true, false);
        }

        boolean traceStartupLoads = false;
        Path configFile = null;
        for (String arg : args) {
            if ("--trace-startup-loads".equals(arg)) {
                traceStartupLoads = true;
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
        return optionsForConfigFile(configFile, false, traceStartupLoads);
    }

    private static LaunchOptions optionsForConfigFile(Path configFile, boolean help, boolean traceStartupLoads) {
        Path resolvedConfigFile = resolveConfigFile(configFile);
        Path mudlibRoot = mudlibRootForConfigFile(resolvedConfigFile);
        String configObjectPath = mudlibRoot.relativize(resolvedConfigFile).toString()
                .replace('\\', '/');
        return new LaunchOptions(
                mudlibRoot, DEFAULT_PORT, DEFAULT_BIND_ADDRESS, configObjectPath, help, traceStartupLoads);
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
        return "Usage: scripts/jvmud-start [--trace-startup-loads] <mudlib-config-file>\n"
                + "Options: --trace-startup-loads prints every underlying startup object load.\n"
                + "Listens on localhost:4000.";
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
            boolean traceStartupLoads) {}
}
