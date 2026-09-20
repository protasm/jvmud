package io.github.protasm.jvmud.communication.transport.telnet;

import io.github.protasm.jvmud.execution.instance.InstanceHost;
import java.io.IOException;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import io.github.protasm.jvmud.communication.transport.admin.AdminWire;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Private worker listener for one ready mudlib; public player admission belongs to the engine gateway. */
public final class TelnetServer implements AutoCloseable {
    private final String bindAddress;
    private final int requestedPort;
    private final InstanceHost mud;
    private final Set<Socket> connections = ConcurrentHashMap.newKeySet();
    private final ExecutorService sessions = Executors.newVirtualThreadPerTaskExecutor();
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;
    private boolean closed;
    private final String workerSecret;

    /** Creates a loopback worker listener authenticated by the engine's private relay secret. */
    public TelnetServer(InstanceHost mud, String workerSecret) {
        this.bindAddress = "127.0.0.1";
        requestedPort = 0;
        this.mud = Objects.requireNonNull(mud, "mud");
        this.workerSecret = Objects.requireNonNull(workerSecret, "workerSecret");
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
                        try {
                            socket.setSoTimeout(5000);
                            var input = new DataInputStream(socket.getInputStream());
                            String secret = AdminWire.read(input, 256);
                            if (!MessageDigest.isEqual(secret.getBytes(StandardCharsets.UTF_8), workerSecret.getBytes(StandardCharsets.UTF_8))) {
                                closeSocket(socket); return;
                            }
                            String address = AdminWire.read(input, 256);
                            socket.setSoTimeout(0);
                            new TelnetSession(socket, mud, address).run();
                        } catch (IOException e) { closeSocket(socket); }
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
