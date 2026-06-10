package io.github.protasm.jvmud.server;

import io.github.protasm.jvmud.runtime.WorldClock;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
    private static final Path DEFAULT_MUDLIB_ROOT = Path.of("mudlibs", "lp245");
    private static final String DEFAULT_BIND_ADDRESS = "localhost";

    private final String bindAddress;
    private final int requestedPort;
    private final Path mudlibRoot;
    private final String configObjectPath;
    private final ExecutorService sessions;
    private TelnetMud mud;
    private WorldClock worldClock;
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;

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
        System.out.println("JVMud mudlib listening on " + server.bindAddress() + ":" + server.port());
        Runtime.getRuntime().addShutdownHook(new Thread(server::close, "jvmud-start-shutdown"));
        server.await();
    }

    static LaunchOptions parseLaunchOptions(String[] args) {
        Path mudlibRoot = DEFAULT_MUDLIB_ROOT;
        int port = DEFAULT_PORT;
        String bindAddress = DEFAULT_BIND_ADDRESS;
        String configObjectPath = MudlibBoot.DEFAULT_CONFIG_PATH;
        int positional = 0;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-help", "--help" -> {
                    return new LaunchOptions(mudlibRoot, port, bindAddress, configObjectPath, true);
                }
                case "-mudlib-dir", "--mudlib-dir" -> mudlibRoot = Path.of(requireValue(args, ++i, arg));
                case "-port", "--port" -> port = parsePort(requireValue(args, ++i, arg));
                case "-host", "--host" -> bindAddress = requireValue(args, ++i, arg);
                case "-config", "--config" -> configObjectPath = requireValue(args, ++i, arg);
                default -> {
                    if (arg.startsWith("-")) {
                        throw new IllegalArgumentException("Unknown option: " + arg);
                    }
                    switch (positional++) {
                        case 0 -> mudlibRoot = Path.of(arg);
                        case 1 -> port = parsePort(arg);
                        case 2 -> bindAddress = arg;
                        case 3 -> configObjectPath = arg;
                        default -> throw new IllegalArgumentException("Too many positional arguments: " + arg);
                    }
                }
            }
        }

        return new LaunchOptions(mudlibRoot, port, bindAddress, configObjectPath, false);
    }

    private static String requireValue(String[] args, int index, String option) {
        if (index >= args.length || args[index].startsWith("-")) {
            throw new IllegalArgumentException("Missing value for " + option);
        }
        return args[index];
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("Port must be between 0 and 65535: " + value);
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port must be an integer: " + value, e);
        }
    }

    private static String usage() {
        return "Usage: ./jvmud-start [-mudlib-dir mudlibs/lp245] [-port 4000] "
                + "[-host localhost] [-config jvmud/lp245.config]";
    }

    public synchronized void start() throws IOException {
        if (running) {
            return;
        }
        mud = TelnetMud.boot(mudlibRoot, configObjectPath);
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

    @Override
    public synchronized void close() {
        running = false;
        if (worldClock != null) {
            worldClock.close();
            worldClock = null;
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
