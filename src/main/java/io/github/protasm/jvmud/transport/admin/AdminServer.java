package io.github.protasm.jvmud.transport.admin;

import io.github.protasm.jvmud.admin.AdminCommandSession;
import io.github.protasm.jvmud.instance.InstanceHost;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Authenticated loopback-only administrative transport for one live host. Socket I/O occurs
 * outside the world's execution lock. Each connection owns its command directory and output.
 */
public final class AdminServer implements AutoCloseable {
    private final InstanceHost host;
    private final ServerSocket listener;
    private final Path tokenFile;
    private final String token;
    private final Set<Socket> connections = ConcurrentHashMap.newKeySet();
    private final ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor();
    private volatile boolean closed;

    /**
     * Binds before writing a new owner-readable credential, then starts accepting clients.
     * Port zero is supported for embedding/tests; command-line launchers require explicit ports.
     * Existing credential files are never overwritten. A stale file must be removed explicitly.
     */
    public AdminServer(InstanceHost host, int port, Path tokenFile) throws IOException {
        this.host = host;
        this.tokenFile = tokenFile.toAbsolutePath();
        byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);
        this.token = HexFormat.of().formatHex(secret);
        listener = new ServerSocket(port, 16, InetAddress.getByName("127.0.0.1"));
        boolean created = false;
        try {
            Files.createDirectories(this.tokenFile.getParent());
            Files.createFile(this.tokenFile, PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rw-------")));
            created = true;
            Files.writeString(this.tokenFile, token, StandardCharsets.UTF_8);
            workers.submit(this::acceptLoop);
        } catch (IOException | RuntimeException e) {
            listener.close();
            workers.shutdownNow();
            if (created) Files.deleteIfExists(this.tokenFile);
            throw e;
        }
    }

    /** Actual loopback port, including the assigned port when constructed with zero. */
    public int port() { return listener.getLocalPort(); }

    private void acceptLoop() {
        while (!closed) {
            try {
                Socket socket = listener.accept();
                connections.add(socket);
                if (closed) {
                    connections.remove(socket);
                    socket.close();
                } else {
                    try {
                        workers.submit(() -> serve(socket));
                    } catch (java.util.concurrent.RejectedExecutionException e) {
                        connections.remove(socket);
                        socket.close();
                    }
                }
            } catch (IOException e) {
                if (!closed) System.err.println("Admin listener: " + e.getMessage());
            }
        }
    }

    private void serve(Socket socket) {
        try (socket;
                var in = new DataInputStream(socket.getInputStream());
                var out = new DataOutputStream(socket.getOutputStream())) {
            socket.setSoTimeout(10000);
            String version = AdminWire.read(in, 128);
            String supplied = AdminWire.read(in, 256);
            if (!AdminWire.VERSION.equals(version) || !MessageDigest.isEqual(
                    token.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                AdminWire.write(out, "ERROR: Authentication failed.", AdminWire.MAX_RESPONSE_BYTES);
                return;
            }
            socket.setSoTimeout(0);
            StringWriter buffer = new StringWriter();
            PrintWriter output = new PrintWriter(buffer, true);
            AdminCommandSession session = host.administer(runtime -> AdminCommandSession.attach(output, runtime, host.mudlibRoot()));
            AdminWire.write(out, "Connected to live JVMud at admin port " + port()
                    + "\nWorld: " + host.mudlibRoot() + "\nType help for commands; quit disconnects.",
                    AdminWire.MAX_RESPONSE_BYTES);
            while (!closed && session.isRunning()) {
                String command = AdminWire.read(in, AdminWire.MAX_COMMAND_BYTES);
                String response = host.administer(runtime -> {
                    if (closed) return "Server administration is shutting down.\n";
                    buffer.getBuffer().setLength(0);
                    session.execute(command);
                    output.flush();
                    return buffer.toString();
                });
                AdminWire.write(out, response, AdminWire.MAX_RESPONSE_BYTES);
                out.writeBoolean(session.isRunning());
                out.flush();
            }
        } catch (IOException | RuntimeException e) {
            // Disconnects and malformed clients end only their own admin session.
        } finally {
            connections.remove(socket);
        }
    }

    /** Disconnects clients and removes only this server's credential; never stops the world. */
    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        try { listener.close(); } catch (IOException ignored) {}
        for (Socket socket : connections) {
            try { socket.close(); } catch (IOException ignored) {}
        }
        workers.shutdownNow();
        try {
            if (Files.readString(tokenFile).equals(token)) Files.deleteIfExists(tokenFile);
        } catch (IOException ignored) {}
    }
}
