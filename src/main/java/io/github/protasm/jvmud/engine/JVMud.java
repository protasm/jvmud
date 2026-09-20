package io.github.protasm.jvmud.engine;

import io.github.protasm.jvmud.admin.EngineAdminCommandSession;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundaryConfigReader;
import io.github.protasm.jvmud.engine.worker.MudlibProcess;
import io.github.protasm.jvmud.instance.MudlibSpec;
import io.github.protasm.jvmud.persistence.admin.AdminRegistry;
import io.github.protasm.jvmud.transport.admin.AdminServer;
import io.github.protasm.jvmud.transport.admin.AdminTLS;
import io.github.protasm.jvmud.transport.telnet.PlayerGateway;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Application entry point and supervisor. Owns public endpoints, administrator identities and worker lifecycles.
 * LPC execution belongs exclusively to sandboxed worker JVMs; the engine remains available with no mudlibs.
 */
public final class JVMud implements AutoCloseable {
    private final EngineConfiguration configuration;
    private final Map<String, ManagedMudlib> mudlibs = new LinkedHashMap<>();
    private final CountDownLatch stopped = new CountDownLatch(1);
    private volatile boolean closed;
    private volatile boolean started;
    private FileChannel lockChannel;
    private FileLock lock;
    private AdminRegistry registry;
    private SSLContext tls;
    private PlayerGateway players;
    private AdminServer administration;
    private AdminServer localAdministration;

    /** Construction declares application configuration without booting a mudlib or binding a socket. */
    public JVMud(EngineConfiguration configuration) { this.configuration = Objects.requireNonNull(configuration); }

    /** Operating-system entry point; command-line parsing delegates to the engine launcher. */
    public static void main(String[] args) throws IOException { EngineLauncher.run(args); }

    /** Starts the engine's player, TLS administration and same-account recovery endpoints with zero mudlibs. */
    public synchronized void start() throws IOException {
        if (closed) throw new IllegalStateException("Engine is closed.");
        if (started) return;
        try {
            Path state = configuration.stateDirectory();
            AdminRegistry.privateDirectory(state);
            lockChannel = FileChannel.open(state.resolve("engine.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            lock = lockChannel.tryLock();
            if (lock == null) throw new IOException("Another engine owns this state directory.");
            registry = new AdminRegistry(state);
            tls = AdminTLS.server(state);
            Path socket = localSocket();
            // Owning the state-directory lock proves no live engine owns the previous recovery endpoint.
            Files.deleteIfExists(socket);
            localAdministration = new AdminServer(socket, () -> new EngineAdminCommandSession(this, registry));
            administration = new AdminServer(configuration.bindAddress(), configuration.adminPort(), tls, registry,
                    "engine", () -> new EngineAdminCommandSession(this, registry));
            players = new PlayerGateway(configuration.bindAddress(), configuration.playerPort(), this::available, null);
            started = true;
            localAdministration.start(); administration.start(); players.start();
        } catch (Exception failure) {
            close();
            if (failure instanceof IOException io) throw io;
            throw new IOException("Engine startup failed: " + failure.getMessage(), failure);
        }
    }

    /** Boots one isolated worker and publishes its port pair only after both bindings succeed. */
    public MudlibStatus startMudlib(MudlibSpec spec, int playerPort, int adminPort) throws IOException {
        requireRunning();
        if (playerPort < 0 || playerPort > 65535 || adminPort < 0 || adminPort > 65535 || playerPort != 0 && playerPort == adminPort)
            throw new IllegalArgumentException("Mudlib player and admin ports must be distinct and in range 0–65535.");
        Path root = spec.root().toRealPath();
        Path state = configuration.stateDirectory().toRealPath();
        if (root.startsWith(state) || state.startsWith(root)) throw new IllegalArgumentException("Engine state and mudlib files must occupy separate trees.");
        for (String entry : System.getProperty("java.class.path").split(java.io.File.pathSeparator)) {
            if (Path.of(entry).toRealPath().startsWith(root))
                throw new IllegalArgumentException("A mudlib cannot own the engine's classpath files.");
        }
        if (Path.of(System.getProperty("java.home")).toRealPath().startsWith(root))
            throw new IllegalArgumentException("A mudlib cannot own the engine's Java runtime files.");
        var boundary = MudlibBoundaryConfigReader.read(root, spec.configPath());
        String id = boundary.gameId().orElse(root.getFileName().toString());
        if (!id.matches("[A-Za-z0-9_-]{1,64}")) throw new IllegalArgumentException("Mudlib id must contain 1–64 letters, digits, underscores or hyphens.");
        if (boundary.mudlibRootPath().isPresent() && !boundary.mudlibRootPath().orElseThrow().toRealPath().startsWith(root))
            throw new IllegalArgumentException("Configured mudlib_root must be within the supplied mudlib tree.");
        ManagedMudlib entry;
        synchronized (mudlibs) {
            requireRunning();
            entry = mudlibs.get(id);
            if (entry != null && !entry.spec.equals(spec)) throw new IllegalArgumentException("Mudlib id already belongs to another manifest: " + id);
            if (entry == null) {
                for (ManagedMudlib other : mudlibs.values()) {
                    Path otherRoot = other.spec.root().toRealPath();
                    if (root.startsWith(otherRoot) || otherRoot.startsWith(root))
                        throw new IllegalArgumentException("Mudlib filesystem trees must not overlap.");
                }
                entry = new ManagedMudlib(id, spec); mudlibs.put(id, entry);
            }
        }
        synchronized (entry) {
            if (entry.state == MudlibStatus.State.RUNNING || entry.state == MudlibStatus.State.STARTING)
                throw new IllegalStateException("Mudlib is already running or starting: " + id);
            entry.playerPort = playerPort; entry.adminPort = adminPort; entry.state = MudlibStatus.State.STARTING; entry.detail = "Booting worker.";
            try {
                Files.createDirectories(state.resolve("logs"));
                MudlibProcess worker = new MudlibProcess(spec, state.resolve("logs").resolve(id + ".log"), configuration.traceLoads());
                entry.worker = worker;
                if (!worker.ready().id().equals(id)) throw new IOException("Worker identity differs from its manifest.");
                entry.name = worker.ready().name();
                PlayerGateway.Entry route = new PlayerGateway.Entry(id, entry.name, worker::connectPlayer);
                entry.players = new PlayerGateway(configuration.bindAddress(), playerPort, List::of, route);
                entry.admin = new AdminServer(configuration.bindAddress(), adminPort, tls, registry, "mudlib:" + id, worker::administration);
                entry.playerPort = entry.players.port(); entry.adminPort = entry.admin.port();
                requireRunning();
                if (!worker.handle().isAlive()) throw new IOException("Worker exited before publication.");
                entry.players.start(); entry.admin.start(); entry.state = MudlibStatus.State.RUNNING; entry.detail = worker.ready().summary();
                ManagedMudlib observed = entry;
                worker.onExit().thenRunAsync(() -> workerExited(observed, worker));
                return entry.snapshot();
            } catch (IOException | RuntimeException failure) {
                release(entry); entry.state = MudlibStatus.State.FAILED; entry.detail = failure.getMessage();
                if (failure instanceof IOException io) throw io;
                throw failure;
            }
        }
    }

    private void workerExited(ManagedMudlib entry, MudlibProcess worker) {
        synchronized (entry) {
            if (entry.worker != worker || entry.state != MudlibStatus.State.RUNNING) return;
            entry.state = MudlibStatus.State.FAILED; entry.detail = "Worker exited unexpectedly; see logs/" + entry.id + ".log.";
            release(entry);
        }
    }

    /** Stops only the selected mudlib, closing its listeners and active connections. */
    public void stopMudlib(String id) {
        ManagedMudlib entry = requireMudlib(id);
        synchronized (entry) {
            entry.state = MudlibStatus.State.STOPPING; release(entry);
            entry.state = MudlibStatus.State.STOPPED; entry.detail = "Stopped by administrator.";
        }
    }

    /** Restarts a registered mudlib using its previously allocated port pair. */
    public MudlibStatus restartMudlib(String id) throws IOException {
        ManagedMudlib entry = requireMudlib(id);
        stopMudlib(id); return startMudlib(entry.spec, entry.playerPort, entry.adminPort);
    }

    /** Returns lifecycle snapshots without waiting for a blocked worker. */
    public List<MudlibStatus> mudlibs() {
        synchronized (mudlibs) { return mudlibs.values().stream().map(ManagedMudlib::snapshot).toList(); }
    }

    private List<PlayerGateway.Entry> available() {
        synchronized (mudlibs) {
            return mudlibs.values().stream().filter(e -> e.state == MudlibStatus.State.RUNNING)
                    .map(e -> new PlayerGateway.Entry(e.id, e.name, address -> {
                        MudlibProcess worker = e.worker;
                        if (e.state != MudlibStatus.State.RUNNING || worker == null) throw new IOException("Mudlib unavailable.");
                        return worker.connectPlayer(address);
                    })).toList();
        }
    }

    /** Human-readable engine and worker status, with no credentials. */
    public String describe() {
        StringBuilder text = new StringBuilder("Engine player=" + port() + " admin=" + adminPort() + " local=" + localSocket() + "\n");
        for (MudlibStatus mud : mudlibs()) text.append(mud).append('\n');
        return text.toString();
    }
    /** Engine menu port. */
    public int port() { return players == null ? configuration.playerPort() : players.port(); }
    /** Engine TLS administration port. */
    public int adminPort() { return administration == null ? configuration.adminPort() : administration.port(); }
    /** Same-account bootstrap and recovery socket path. */
    public Path localSocket() { return configuration.stateDirectory().resolve("engine.sock"); }
    /** Public endpoint binding address. */
    public String bindAddress() { return configuration.bindAddress(); }
    /** Blocks on engine lifetime, independently of the number of running mudlibs. */
    public void await() throws IOException {
        try { stopped.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IOException("Interrupted waiting for engine shutdown.", e); }
    }

    /** Closes engine admission, terminates every worker, and releases private state ownership. */
    @Override public synchronized void close() {
        if (closed) return; closed = true;
        if (players != null) players.close();
        if (administration != null) administration.close();
        if (localAdministration != null) localAdministration.close();
        List<ManagedMudlib> entries; synchronized (mudlibs) { entries = List.copyOf(mudlibs.values()); }
        for (ManagedMudlib entry : entries) synchronized (entry) {
            release(entry); entry.state = MudlibStatus.State.STOPPED;
        }
        try { if (lock != null) lock.release(); } catch (IOException ignored) {}
        try { if (lockChannel != null) lockChannel.close(); } catch (IOException ignored) {}
        stopped.countDown();
    }

    private void requireRunning() { if (!started || closed) throw new IllegalStateException("Engine is not running."); }
    private ManagedMudlib requireMudlib(String id) {
        synchronized (mudlibs) {
            ManagedMudlib entry = mudlibs.get(id);
            if (entry == null) throw new IllegalArgumentException("Unknown mudlib: " + id);
            return entry;
        }
    }
    private static void release(ManagedMudlib entry) {
        if (entry.players != null) { entry.players.close(); entry.players = null; }
        if (entry.admin != null) { entry.admin.close(); entry.admin = null; }
        if (entry.worker != null) { entry.worker.close(); entry.worker = null; }
    }
    private static final class ManagedMudlib {
        final String id; final MudlibSpec spec;
        volatile String name; volatile String detail = "";
        volatile MudlibStatus.State state = MudlibStatus.State.STOPPED;
        volatile int playerPort; volatile int adminPort;
        volatile MudlibProcess worker;
        PlayerGateway players; AdminServer admin;
        ManagedMudlib(String id, MudlibSpec spec) { this.id = id; this.name = id; this.spec = spec; }
        MudlibStatus snapshot() {
            MudlibProcess running = worker;
            return new MudlibStatus(id, name, state, playerPort, adminPort, running == null ? -1 : running.handle().pid(), detail);
        }
    }
}
