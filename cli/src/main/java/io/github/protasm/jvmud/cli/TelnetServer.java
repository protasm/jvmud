package io.github.protasm.jvmud.cli;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Minimal telnet listener for interactive JVMud sessions. */
public final class TelnetServer implements AutoCloseable {
    public static final int DEFAULT_PORT = 4000;

    private final String bindAddress;
    private final int requestedPort;
    private final Path mudlibRoot;
    private final String configObjectPath;
    private final ExecutorService sessions;
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;

    public TelnetServer(String bindAddress, int port, Path mudlibRoot, String configObjectPath) {
        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
        this.requestedPort = port;
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        this.configObjectPath = Objects.requireNonNullElse(configObjectPath, MudlibBoot.DEFAULT_CONFIG_PATH);
        this.sessions = Executors.newCachedThreadPool(new TelnetThreadFactory("jvmud-telnet-session"));
    }

    public static void main(String[] args) throws IOException {
        Path mudlib = args.length > 0 ? Path.of(args[0]) : Path.of("mudlib");
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        String bindAddress = args.length > 2 ? args[2] : "127.0.0.1";
        String configObjectPath = args.length > 3 ? args[3] : MudlibBoot.DEFAULT_CONFIG_PATH;

        TelnetServer server = new TelnetServer(bindAddress, port, mudlib, configObjectPath);
        server.start();
        System.out.println("JVMud telnet listening on " + server.bindAddress() + ":" + server.port());
        Runtime.getRuntime().addShutdownHook(new Thread(server::close, "jvmud-telnet-shutdown"));
        server.await();
    }

    public synchronized void start() throws IOException {
        if (running) {
            return;
        }
        serverSocket = new ServerSocket(requestedPort, 50, InetAddress.getByName(bindAddress));
        running = true;
        acceptThread = new Thread(this::acceptLoop, "jvmud-telnet-accept");
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
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
                // Closing is best-effort during shutdown.
            }
        }
        sessions.shutdownNow();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                sessions.execute(new TelnetSession(socket, mudlibRoot, configObjectPath));
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
}
