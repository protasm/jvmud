package io.github.protasm.jvmud.transport.telnet;

import io.github.protasm.jvmud.instance.MudlibRouter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Player listener owned by the engine; accepts connections to an already booted mudlib menu. */
public final class TelnetServer implements AutoCloseable {
    private final String bindAddress;
    private final int requestedPort;
    private final MudlibRouter router;
    private final Set<Socket> connections = ConcurrentHashMap.newKeySet();
    private final ExecutorService sessions = Executors.newVirtualThreadPerTaskExecutor();
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;
    private boolean closed;

    /** Creates a transport component without booting, ticking, or owning any mudlib. */
    public TelnetServer(String bindAddress, int port, MudlibRouter router) {
        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
        requestedPort = port;
        this.router = Objects.requireNonNull(router, "router");
    }

    /** Binds the listener and starts accepting player connections. */
    public synchronized void start() throws IOException {
        if (closed) throw new IllegalStateException("Telnet server is closed.");
        if (running) return;
        serverSocket = new ServerSocket(requestedPort, 50, InetAddress.getByName(bindAddress));
        running = true;
        acceptThread = new Thread(this::acceptLoop, "jvmud-telnet-accept");
        acceptThread.start();
    }

    /** Waits for the listener thread to finish. */
    public void await() throws IOException {
        Thread thread = acceptThread;
        if (thread == null) throw new IllegalStateException("Telnet server has not started.");
        try { thread.join(); }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for telnet server.", e);
        }
    }

    /** Returns the actual bound address, or the requested address before startup. */
    public String bindAddress() {
        return serverSocket == null ? bindAddress : serverSocket.getInetAddress().getHostAddress();
    }

    /** Returns the actual bound port, or the requested port before startup. */
    public int port() { return serverSocket == null ? requestedPort : serverSocket.getLocalPort(); }

    /** Closes sockets to release blocked readers, then waits for session detach work. */
    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        running = false;
        if (serverSocket != null) {
            try { serverSocket.close(); } catch (IOException ignored) { /* Best effort. */ }
        }
        connections.forEach(TelnetServer::closeSocket);
        sessions.shutdown();
        try {
            if (!sessions.awaitTermination(5, TimeUnit.SECONDS)) sessions.shutdownNow();
        } catch (InterruptedException e) {
            sessions.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                connections.add(socket);
                if (!running) {
                    connections.remove(socket);
                    closeSocket(socket);
                    break;
                }
                try {
                    sessions.execute(() -> {
                        try { new TelnetSession(socket, router).run(); }
                        finally { connections.remove(socket); }
                    });
                } catch (java.util.concurrent.RejectedExecutionException e) {
                    connections.remove(socket);
                    closeSocket(socket);
                }
            } catch (IOException e) {
                if (running) System.err.println("Telnet accept failed: " + e.getMessage());
            }
        }
    }

    private static void closeSocket(Socket socket) {
        try { socket.close(); } catch (IOException ignored) { /* Best effort. */ }
    }
}
