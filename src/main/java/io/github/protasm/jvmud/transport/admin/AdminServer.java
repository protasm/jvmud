package io.github.protasm.jvmud.transport.admin;

import io.github.protasm.jvmud.admin.AdminSession;
import io.github.protasm.jvmud.persistence.admin.AdminRegistry;
import jdk.net.ExtendedSocketOptions;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Engine-owned administration listener. Authentication and grants are checked before creating a command session. */
public final class AdminServer implements AutoCloseable {
    /** Creates a fresh per-administrator command session after authorization. */
    @FunctionalInterface public interface Sessions { AdminSession open() throws IOException; }
    private final Sessions sessions;
    private final String scope;
    private final AdminRegistry registry;
    private final SSLServerSocket tcp;
    private final ServerSocketChannel unix;
    private final Path socketPath;
    private final Set<Connection> connections = ConcurrentHashMap.newKeySet();
    private final ExecutorService tasks = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledExecutorService checks = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "jvmud-admin-authorization"); t.setDaemon(true); return t;
    });
    private final Semaphore slots = new Semaphore(64);
    private volatile boolean closed;

    /** Binds a TLS endpoint; call start only when the target engine or mudlib is ready. */
    public AdminServer(String address, int port, SSLContext tls, AdminRegistry registry, String scope, Sessions sessions) throws IOException {
        this.registry = registry; this.scope = scope; this.sessions = sessions; unix = null; socketPath = null;
        tcp = (SSLServerSocket) tls.getServerSocketFactory().createServerSocket();
        try {
            tcp.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
            tcp.bind(new InetSocketAddress(address, port));
        } catch (IOException | RuntimeException e) { tcp.close(); throw e; }
    }

    /** Binds an owner-only Unix socket. Existing paths are never silently replaced. */
    public AdminServer(Path path, Sessions sessions) throws IOException {
        this.sessions = sessions; scope = "engine"; registry = null; tcp = null; socketPath = path;
        unix = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        try {
            unix.bind(UnixDomainSocketAddress.of(path));
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"));
        } catch (IOException | RuntimeException e) { unix.close(); throw e; }
    }

    /** Starts accepting and periodically disconnects revoked credentials, including idle sessions. */
    public void start() {
        checks.scheduleWithFixedDelay(() -> connections.forEach(c -> {
            if (!c.authorized.getAsBoolean()) c.close();
        }), 1, 1, TimeUnit.SECONDS);
        tasks.submit(() -> {
            while (!closed) {
                try {
                    if (tcp != null) {
                        SSLSocket socket = (SSLSocket) tcp.accept();
                        if (!slots.tryAcquire()) { socket.close(); continue; }
                        socket.setSoTimeout(10000);
                        Connection c = new Connection(socket, new DataInputStream(socket.getInputStream()), new DataOutputStream(socket.getOutputStream()));
                        accept(c, () -> { socket.startHandshake(); socket.setSoTimeout(0); }, false);
                    } else {
                        SocketChannel socket = unix.accept();
                        if (!slots.tryAcquire()) { socket.close(); continue; }
                        Connection c = new Connection(socket, new DataInputStream(Channels.newInputStream(socket)), new DataOutputStream(Channels.newOutputStream(socket)));
                        accept(c, () -> {
                            var peer = socket.getOption(ExtendedSocketOptions.SO_PEERCRED);
                            if (!peer.user().equals(Files.getOwner(socketPath.getParent()))) throw new IOException("OS account mismatch.");
                        }, true);
                    }
                } catch (IOException | RuntimeException e) {
                    if (!closed) System.err.println("Administration accept failed: " + e.getMessage());
                }
            }
        });
    }

    private void accept(Connection c, Handshake handshake, boolean local) {
        connections.add(c);
        if (closed) { c.close(); connections.remove(c); slots.release(); return; }
        ScheduledFuture<?> deadline;
        try { deadline = checks.schedule(c::close, 10, TimeUnit.SECONDS); }
        catch (RejectedExecutionException e) { c.close(); connections.remove(c); slots.release(); return; }
        try { tasks.submit(() -> {
            try (c) {
                handshake.run();
                if (!AdminWire.VERSION.equals(AdminWire.read(c.in, 128))) throw new IOException("Unsupported administration protocol.");
                if (!local) {
                    String name = AdminWire.read(c.in, 256), token = AdminWire.read(c.in, 256);
                    c.authorized = () -> registry.allows(name, token, scope);
                    if (!c.authorized.getAsBoolean()) {
                        AdminWire.write(c.out, "ERROR: Authentication or authorization failed.", 1024); return;
                    }
                }
                deadline.cancel(false);
                try (AdminSession session = sessions.open()) {
                    AdminWire.write(c.out, session.scope(), 1024);
                    while (!closed && c.authorized.getAsBoolean()) {
                        String command = AdminWire.read(c.in, AdminWire.MAX_COMMAND_BYTES);
                        if (!c.authorized.getAsBoolean()) break;
                        AdminSession.Reply reply = session.execute(command);
                        AdminWire.write(c.out, reply.text(), AdminWire.MAX_RESPONSE_BYTES);
                        c.out.writeBoolean(reply.running()); c.out.flush();
                        if (!reply.running()) break;
                    }
                }
            } catch (IOException | RuntimeException ignored) {
                // Authentication failures, disconnected peers and malformed frames end only this connection.
            } finally { deadline.cancel(false); connections.remove(c); slots.release(); }
        }); } catch (RejectedExecutionException e) {
            deadline.cancel(false); c.close(); connections.remove(c); slots.release();
        }
    }

    /** Returns the allocated TCP port; Unix endpoints have no TCP port. */
    public int port() { return tcp == null ? -1 : tcp.getLocalPort(); }

    /** Closes only this endpoint and its sessions; the target's lifecycle belongs to the engine. */
    @Override public void close() {
        closed = true;
        try { if (tcp != null) tcp.close(); else unix.close(); } catch (IOException ignored) {}
        connections.forEach(Connection::close); checks.shutdownNow(); tasks.shutdownNow();
        if (socketPath != null) try { Files.deleteIfExists(socketPath); } catch (IOException ignored) {}
    }

    @FunctionalInterface private interface Handshake { void run() throws IOException; }
    private static final class Connection implements AutoCloseable {
        final Closeable socket; final DataInputStream in; final DataOutputStream out;
        volatile BooleanSupplier authorized = () -> true;
        Connection(Closeable socket, DataInputStream in, DataOutputStream out) { this.socket = socket; this.in = in; this.out = out; }
        @Override public void close() { try { socket.close(); } catch (IOException ignored) {} }
    }
}
