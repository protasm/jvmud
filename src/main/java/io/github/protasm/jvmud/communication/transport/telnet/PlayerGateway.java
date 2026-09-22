package io.github.protasm.jvmud.communication.transport.telnet;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/** Direct mudlib player listener: relays bytes to its worker until disconnection. */
public final class PlayerGateway implements AutoCloseable {
    /** Connection factory retains worker details outside transport. */
    @FunctionalInterface public interface Connect { Socket open(String address) throws IOException; }
    private final ServerSocket listener;
    private final Connect connect;
    private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();
    private final ExecutorService tasks = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore slots = new Semaphore(512);
    private volatile boolean closed;

    /** Reserves a player port without accepting sessions until start is called. */
    public PlayerGateway(String address, int port, Connect connect) throws IOException {
        this.connect = Objects.requireNonNull(connect, "connect");
        listener = new ServerSocket();
        try { listener.bind(new InetSocketAddress(address, port)); }
        catch (IOException e) { listener.close(); throw e; }
    }

    /** Opens player admission after all endpoints in the pair have been bound successfully. */
    public void start() {
        tasks.submit(() -> {
            while (!closed) {
                try {
                    Socket client = listener.accept();
                    if (!slots.tryAcquire()) { client.close(); continue; }
                    sockets.add(client);
                    if (closed) { closeSocket(client); sockets.remove(client); slots.release(); continue; }
                    try { tasks.submit(() -> {
                        try { serve(client); }
                        finally { sockets.remove(client); slots.release(); }
                    }); } catch (RejectedExecutionException e) {
                        closeSocket(client); sockets.remove(client); slots.release();
                    }
                } catch (IOException e) { if (!closed) System.err.println("Player accept failed: " + e.getMessage()); }
            }
        });
    }

    private void serve(Socket client) {
        try (client) {
            try (Socket worker = connect.open(client.getInetAddress().getHostAddress())) {
                sockets.add(worker);
                if (closed) { sockets.remove(worker); return; }
                try {
                    Thread uplink = Thread.ofVirtual().start(() -> copy(client, worker));
                    copy(worker, client);
                    uplink.interrupt();
                } finally { sockets.remove(worker); }
            }
        } catch (IOException ignored) { /* A failed or departed mudlib ends this connection. */ }
    }

    private static void copy(Socket from, Socket to) {
        try { from.getInputStream().transferTo(to.getOutputStream()); }
        catch (IOException ignored) {}
        finally { closeSocket(from); closeSocket(to); }
    }

    /** Actual bound player port, including ephemeral allocations. */
    public int port() { return listener.getLocalPort(); }
    /** Closes the listener and both sides of every active relay. */
    @Override public void close() {
        closed = true;
        try { listener.close(); } catch (IOException ignored) {}
        sockets.forEach(PlayerGateway::closeSocket); tasks.shutdownNow();
    }
    private static void closeSocket(Socket socket) { try { socket.close(); } catch (IOException ignored) {} }
}
