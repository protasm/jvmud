package io.github.protasm.jvmud.transport.telnet;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** Engine-owned player listener: select a ready mudlib or attach directly, then relay bytes until disconnection. */
public final class PlayerGateway implements AutoCloseable {
    /** Connection factory retains worker details outside transport. */
    @FunctionalInterface public interface Connect { Socket open(String address) throws IOException; }
    /** Public menu entry; contains no runtime objects or administration authority. */
    public record Entry(String id, String name, Connect connect) {}
    private final ServerSocket listener;
    private final Supplier<List<Entry>> available;
    private final Entry direct;
    private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();
    private final ExecutorService tasks = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore slots = new Semaphore(512);
    private volatile boolean closed;

    /** Reserves a player port without accepting sessions until start is called. */
    public PlayerGateway(String address, int port, Supplier<List<Entry>> available, Entry direct) throws IOException {
        this.available = available; this.direct = direct;
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
            Entry selected = direct;
            if (selected == null) {
                client.setSoTimeout(120000);
                var out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
                while (!closed && selected == null) {
                    List<Entry> entries = available.get();
                    out.print("JVMud mudlibs:\r\n");
                    if (entries.isEmpty()) out.print("No mudlibs are currently available.\r\n");
                    for (int i = 0; i < entries.size(); i++) {
                        Entry entry = entries.get(i);
                        out.print("  " + (i + 1) + ". " + entry.name() + " [" + entry.id() + "]\r\n");
                    }
                    out.print("Select a mudlib by number or game id (or quit): "); out.flush();
                    String line = menuLine(client);
                    if (line == null || line.equalsIgnoreCase("quit")) return;
                    selected = entries.stream().filter(e -> e.id().equals(line)).findFirst().orElse(null);
                    if (selected == null) try {
                        int index = Integer.parseInt(line) - 1;
                        if (index >= 0 && index < entries.size()) selected = entries.get(index);
                    } catch (NumberFormatException ignored) {}
                    if (selected == null) out.print("Please choose an available mudlib.\r\n");
                }
            }
            if (selected == null) return;
            client.setSoTimeout(0);
            try (Socket worker = selected.connect().open(client.getInetAddress().getHostAddress())) {
                sockets.add(worker);
                if (closed) { sockets.remove(worker); return; }
                try {
                    Thread uplink = Thread.ofVirtual().start(() -> copy(client, worker));
                    copy(worker, client);
                    uplink.interrupt();
                } finally { sockets.remove(worker); }
            }
        } catch (IOException ignored) { /* A failed or departed mudlib ends this connection; there is no return to the menu. */ }
    }

    private static void copy(Socket from, Socket to) {
        try { from.getInputStream().transferTo(to.getOutputStream()); }
        catch (IOException ignored) {}
        finally { closeSocket(from); closeSocket(to); }
    }

    /** Reads without buffering beyond the selection, so subsequent player bytes belong to the worker. */
    private static String menuLine(Socket socket) throws IOException {
        InputStream input = socket.getInputStream(); StringBuilder text = new StringBuilder();
        for (int c; (c = input.read()) != -1;) {
            if (c == 255) {
                int verb = input.read();
                if (verb >= 251 && verb <= 254) {
                    int option = input.read(); if (option < 0) return null;
                    if (verb == 251 || verb == 253) {
                        socket.getOutputStream().write(new byte[]{(byte)255, (byte)(verb == 251 ? 254 : 252), (byte)option});
                        socket.getOutputStream().flush();
                    }
                } else if (verb == 250) {
                    int previous = -1, bytes = 0;
                    while ((c = input.read()) != -1) {
                        if (++bytes > 4096) throw new IOException("Oversized Telnet negotiation.");
                        if (previous == 255 && c == 240) break; previous = c;
                    }
                }
                continue;
            }
            if (c == '\n') return text.toString().trim();
            if (c == '\r' || c == 0) continue;
            if (c == 8 || c == 127) { if (!text.isEmpty()) text.setLength(text.length() - 1); continue; }
            if (text.length() >= 256) throw new IOException("Menu selection too long.");
            if (c >= 32) text.append((char)c);
        }
        return null;
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
