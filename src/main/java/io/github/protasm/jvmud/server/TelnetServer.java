package io.github.protasm.jvmud.server;

import io.github.protasm.jvmud.engine.time.WorldClock;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Starts a mudlib as a persistent telnet target for interactive JVMud sessions. */
public final class TelnetServer implements AutoCloseable {
    public static final int DEFAULT_PORT = 4000;
    private static final Path DEFAULT_CONFIG_FILE = Path.of("mudlibs", "lpmuseum", "jvmud", "lpmuseum.config");
    private static final String DEFAULT_BIND_ADDRESS = "localhost";

    private final String bindAddress;
    private final int requestedPort;
    private final Path mudlibRoot;
    private final String configObjectPath;
    private final ExecutorService sessions;
    private TelnetHost mud;
    private WorldClock worldClock;
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;
    private boolean shutdownNotified;

    public TelnetServer(String bindAddress, int port, Path mudlibRoot, String configObjectPath) {
        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
        this.requestedPort = port;
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        this.configObjectPath = Objects.requireNonNullElse(configObjectPath, MudlibBoot.DEFAULT_CONFIG_PATH);
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

        TelnetServer server = new TelnetServer(
                options.bindAddress(), options.port(), options.mudlibRoot(), options.configObjectPath());
        server.start();
        System.out.println(server.preloadSummary());
        System.out.println("JVMud mudlib listening on " + server.bindAddress() + ":" + server.port());
        Runtime.getRuntime().addShutdownHook(new Thread(server::close, "jvmud-start-shutdown"));
        server.await();
    }

    static LaunchOptions parseLaunchOptions(String[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException("Too many arguments.");
        }

        if (args.length == 1 && ("-help".equals(args[0]) || "--help".equals(args[0]))) {
            return optionsForConfigFile(DEFAULT_CONFIG_FILE, true);
        }
        if (args.length == 1 && args[0].startsWith("-")) {
            throw new IllegalArgumentException("Unknown option: " + args[0]);
        }

        Path configFile = args.length == 1 ? Path.of(args[0]) : DEFAULT_CONFIG_FILE;
        return optionsForConfigFile(configFile, false);
    }

    private static LaunchOptions optionsForConfigFile(Path configFile, boolean help) {
        Path resolvedConfigFile = resolveConfigFile(configFile);
        Path mudlibRoot = mudlibRootForConfigFile(resolvedConfigFile);
        String configObjectPath = mudlibRoot.relativize(resolvedConfigFile).toString()
                .replace('\\', '/');
        return new LaunchOptions(mudlibRoot, DEFAULT_PORT, DEFAULT_BIND_ADDRESS, configObjectPath, help);
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
        return "Usage: scripts/jvmud-start [mudlib-config-file]\n"
                + "Default: scripts/jvmud-start mudlibs/lpmuseum/jvmud/lpmuseum.config\n"
                + "Listens on localhost:4000.";
    }

    public synchronized void start() throws IOException {
        if (running) {
            return;
        }
        mud = MultiMudTelnetHost.boot(mudlibRoot, configObjectPath);
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

    record LaunchOptions(
            Path mudlibRoot,
            int port,
            String bindAddress,
            String configObjectPath,
            boolean help) {}
}
