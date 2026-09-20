package io.github.protasm.jvmud.execution.application;

import io.github.protasm.jvmud.language.exec.LPCObjectLoadObserver;
import io.github.protasm.jvmud.execution.instance.MudInstance;
import io.github.protasm.jvmud.execution.instance.MudlibBootProgress;
import io.github.protasm.jvmud.execution.instance.MudlibBootResult;
import io.github.protasm.jvmud.execution.instance.MudlibSpec;
import io.github.protasm.jvmud.communication.transport.telnet.PlayerGateway;
import io.github.protasm.jvmud.communication.transport.telnet.TelnetServer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Test-only in-process fixture for exhaustive Telnet and mudlib behavior checks. Production uses worker JVMs.
 * Each mudlib owns its clock and execution queue; no engine tick visits all worlds.
 * Connections choose a mudlib before any player or persona is created.
 */
final class EmbeddedMudlibHost implements AutoCloseable {
    private final String bindAddress;
    private final int requestedPort;
    private final List<MudlibSpec> specifications;
    private final MudlibBootProgress progress;
    private final LPCObjectLoadObserver loadObserver;
    private final List<MudInstance> mudlibs = new ArrayList<>();
    private PlayerGateway telnet;
    private final List<TelnetServer> workers = new ArrayList<>();
    private boolean closed;

    /** Creates an engine offering one explicitly configured mudlib in its menu. */
    public EmbeddedMudlibHost(String bindAddress, int port, Path root, String configPath) {
        this(bindAddress, port, List.of(new MudlibSpec(root, configPath)));
    }

    /** Creates an engine offering the supplied mudlibs as peers, in declaration order. */
    public EmbeddedMudlibHost(String bindAddress, int port, List<MudlibSpec> specifications) {
        this(bindAddress, port, specifications, MudlibBootProgress.none(), LPCObjectLoadObserver.NONE);
    }

    /** Creates an engine with startup diagnostics; construction does not boot worlds or bind sockets. */
    EmbeddedMudlibHost(String bindAddress, int port, List<MudlibSpec> specifications,
            MudlibBootProgress progress, LPCObjectLoadObserver loadObserver) {
        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
        this.requestedPort = port;
        this.specifications = List.copyOf(specifications);
        if (this.specifications.isEmpty()) throw new IllegalArgumentException("At least one mudlib is required.");
        this.progress = Objects.requireNonNull(progress, "progress");
        this.loadObserver = Objects.requireNonNull(loadObserver, "loadObserver");
    }

    /** Boots all configured worlds, starts their individual clocks, and opens the menu listener. */
    public synchronized void start() throws IOException {
        if (closed) throw new IllegalStateException("JVMud is closed.");
        if (telnet != null) return;
        try {
            var ids = new HashSet<String>();
            for (MudlibSpec specification : specifications) {
                MudInstance mud = MudInstance.boot(specification.root(), specification.configPath(), progress, loadObserver);
                mudlibs.add(mud);
                if (!ids.add(mud.gameId())) {
                    throw new IllegalArgumentException("Duplicate mudlib game id: " + mud.gameId());
                }
            }
            mudlibs.forEach(MudInstance::startExecution);
            List<PlayerGateway.Entry> entries = new ArrayList<>();
            for (MudInstance mud : mudlibs) {
                String secret = java.util.UUID.randomUUID().toString();
                TelnetServer worker = new TelnetServer(mud, secret); workers.add(worker); worker.start();
                entries.add(new PlayerGateway.Entry(mud.gameId(), mud.gameName(), address -> {
                    var socket = new java.net.Socket("127.0.0.1", worker.port());
                    var out = new java.io.DataOutputStream(socket.getOutputStream());
                    io.github.protasm.jvmud.communication.transport.admin.AdminWire.write(out, secret, 256);
                    io.github.protasm.jvmud.communication.transport.admin.AdminWire.write(out, address, 256);
                    return socket;
                }));
            }
            telnet = new PlayerGateway(bindAddress, requestedPort, () -> entries, null);
            telnet.start();
        } catch (IOException | RuntimeException | Error failure) {
            close();
            throw failure;
        }
    }

    /** Returns the bound player port (including the resolved ephemeral port for embedded callers). */
    public synchronized int port() { return telnet == null ? requestedPort : telnet.port(); }

    /** Returns the bound player address. */
    public synchronized String bindAddress() { return bindAddress; }

    /** Returns an immutable snapshot of the booted peer mudlibs. */
    public synchronized List<MudInstance> mudlibs() { return List.copyOf(mudlibs); }

    /** Stops transports and each independent clock before invoking each mudlib's shutdown lifecycle once. */
    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        if (telnet != null) telnet.close();
        workers.forEach(TelnetServer::close);
        for (MudInstance mud : mudlibs) mud.shutdown(0);
    }

    /** Summarizes one mudlib's preload result for startup diagnostics. */
    static String preloadSummary(MudlibBootResult result) {
        if (result.mudlibBoundary().preloadFilePath().isEmpty()) return "preload manifest: none declared.";
        String summary = "preload manifest " + result.mudlibBoundary().preloadFilePath().orElseThrow()
                + ": compiled " + result.preloadManifestPreloadedObjects().size()
                + " object(s), skipped " + result.preloadManifestSkippedPreloads().size() + " object(s).";
        if (!result.preloadManifestSkippedPreloads().isEmpty()) {
            summary += " Skipped: " + String.join(", ", result.preloadManifestSkippedPreloads());
        }
        return summary;
    }
}
