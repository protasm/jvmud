package io.github.protasm.jvmud.communication.transport.telnet;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** Public directory of running mudlibs; visitors browse details without entering a mudlib. */
public final class PlayerDirectory implements AutoCloseable {
    /** Public connection details, without worker access or administration authority. */
    public record Entry(String id, String name, String publicHost, int directPlayerPort, Supplier<String> blurb) {}
    private final ServerSocket listener;
    private final Supplier<List<Entry>> available;
    private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();
    private final ExecutorService tasks = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore slots = new Semaphore(512);
    private volatile boolean closed;

    /** Reserves a player port without accepting sessions until start is called. */
    public PlayerDirectory(String address, int port, Supplier<List<Entry>> available) throws IOException {
        this.available = available;
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

    /** Keeps displayed numbering stable until L refreshes the list; never opens a worker connection. */
    private void serve(Socket client) {
        try (client) {
            client.setSoTimeout(120000);
            var out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
            List<Entry> entries = available.get();
            printList(out, entries);
            while (!closed) {
                out.print("Enter a mudlib number, L to list, or Q to quit: "); out.flush();
                String line = menuLine(client);
                if (line == null) return;
                if (line.equalsIgnoreCase("q")) {
                    out.print("Goodbye! We hope to see you in a mudlib soon.\r\n"); out.flush(); return;
                }
                if (line.equalsIgnoreCase("l")) {
                    entries = available.get(); printList(out, entries); continue;
                }
                int index;
                try { index = Integer.parseInt(line) - 1; }
                catch (NumberFormatException ignored) { index = -1; }
                if (index < 0 || index >= entries.size()) {
                    out.print("Please enter an available mudlib number, L, or Q.\r\n"); continue;
                }
                Entry listed = entries.get(index);
                Entry entry = available.get().stream().filter(e -> e.id().equals(listed.id())).findFirst().orElse(null);
                if (entry == null) {
                    out.print("That mudlib is no longer available. Enter L for the current list.\r\n"); continue;
                }
                String host = entry.publicHost();
                String endpoint = host == null ? "same host, port " + entry.directPlayerPort()
                        : (host.contains(":") ? "[" + host + "]" : host) + ":" + entry.directPlayerPort();
                out.print("\r\n" + entry.name() + " [" + entry.id() + "]\r\n");
                out.print("Telnet: " + endpoint + "\r\n");
                String blurb = entry.blurb().get();
                if (!blurb.isBlank()) out.print(blurb.replace("\r\n", "\n").replace('\r', '\n').replace("\n", "\r\n") + "\r\n");
                out.print("\r\n");
            }
        } catch (IOException ignored) { /* Disconnects and idle timeouts end only this directory visit. */ }
    }

    /** Shows names only; a numbered selection displays the connection details and blurb. */
    private static void printList(PrintWriter out, List<Entry> entries) {
        out.print("JVMud mudlibs:\r\n");
        if (entries.isEmpty()) out.print("No mudlibs are currently available.\r\n");
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            out.print("  " + (i + 1) + ". " + entry.name() + " [" + entry.id() + "]\r\n");
        }
    }

    /** Reads a bounded directory command while consuming Telnet negotiation bytes. */
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
    /** Closes the listener and any visitors still receiving the directory. */
    @Override public void close() {
        closed = true;
        try { listener.close(); } catch (IOException ignored) {}
        sockets.forEach(PlayerDirectory::closeSocket); tasks.shutdownNow();
    }
    private static void closeSocket(Socket socket) { try { socket.close(); } catch (IOException ignored) {} }
}
