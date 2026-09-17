package io.github.protasm.jvmud.engine;

import io.github.protasm.jvmud.compiler.exec.LPCObjectLoadObserver;
import io.github.protasm.jvmud.instance.MudInstance;
import io.github.protasm.jvmud.instance.MudlibBootProgress;
import io.github.protasm.jvmud.instance.MudlibBootResult;
import io.github.protasm.jvmud.instance.MudlibSpec;
import io.github.protasm.jvmud.instance.MudlibRouter;
import io.github.protasm.jvmud.transport.admin.AdminServer;
import io.github.protasm.jvmud.transport.telnet.TelnetServer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Application hub owning mudlib lifetimes and transport components.
 * Each mudlib owns its clock and execution queue; no engine tick visits all worlds.
 * Connections choose a mudlib before any player or persona is created.
 */
public final class Engine implements AutoCloseable {
    private final String bindAddress;
    private final int requestedPort;
    private final List<MudlibSpec> specifications;
    private final MudlibBootProgress progress;
    private final LPCObjectLoadObserver loadObserver;
    private final List<MudInstance> mudlibs = new ArrayList<>();
    private TelnetServer telnet;
    private AdminServer administration;
    private boolean closed;

    /** Creates an engine offering one explicitly configured mudlib in its menu. */
    public Engine(String bindAddress, int port, Path root, String configPath) {
        this(bindAddress, port, List.of(new MudlibSpec(root, configPath)));
    }

    /** Creates an engine offering the supplied mudlibs as peers, in declaration order. */
    public Engine(String bindAddress, int port, List<MudlibSpec> specifications) {
        this(bindAddress, port, specifications, MudlibBootProgress.none(), LPCObjectLoadObserver.NONE);
    }

    /** Creates an engine with startup diagnostics; construction does not boot worlds or bind sockets. */
    Engine(String bindAddress, int port, List<MudlibSpec> specifications,
            MudlibBootProgress progress, LPCObjectLoadObserver loadObserver) {
        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
        this.requestedPort = port;
        this.specifications = List.copyOf(specifications);
        if (this.specifications.isEmpty()) throw new IllegalArgumentException("At least one mudlib is required.");
        this.progress = Objects.requireNonNull(progress, "progress");
        this.loadObserver = Objects.requireNonNull(loadObserver, "loadObserver");
    }

    /** Command-line entry point for the JVMud application. */
    public static void main(String[] args) throws IOException {
        EngineLauncher.run(args);
    }

    /** Boots all configured worlds, starts their individual clocks, and opens the menu listener. */
    public synchronized void start() throws IOException {
        if (closed) throw new IllegalStateException("Engine is closed.");
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
            telnet = new TelnetServer(bindAddress, requestedPort, new MudlibRouter(mudlibs));
            telnet.start();
        } catch (IOException | RuntimeException | Error failure) {
            close();
            throw failure;
        }
    }

    /** Opens administration for a named mudlib; a name may be omitted only for a single-mudlib engine. */
    public synchronized int startAdministration(String gameId, int port, Path tokenFile) throws IOException {
        if (telnet == null || closed) throw new IllegalStateException("Start the engine before administration.");
        if (administration != null) throw new IllegalStateException("Administration already started.");
        if (port != 0 && port == port()) throw new IllegalArgumentException("Player and admin ports must be different.");
        MudInstance selected;
        if (gameId == null && mudlibs.size() == 1) selected = mudlibs.getFirst();
        else selected = mudlibs.stream().filter(mud -> mud.gameId().equals(gameId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Select an available admin game id: " + gameId));
        administration = new AdminServer(selected, port, tokenFile);
        return administration.port();
    }

    /** Waits until the listener terminates, without holding the engine lifecycle lock. */
    public void await() throws IOException {
        TelnetServer listener;
        synchronized (this) { listener = telnet; }
        if (listener == null) throw new IllegalStateException("Engine has not started.");
        listener.await();
    }

    /** Returns the bound player port (including the resolved ephemeral port for embedded callers). */
    public synchronized int port() { return telnet == null ? requestedPort : telnet.port(); }

    /** Returns the bound player address. */
    public synchronized String bindAddress() { return telnet == null ? bindAddress : telnet.bindAddress(); }

    /** Returns an immutable snapshot of the booted peer mudlibs. */
    public synchronized List<MudInstance> mudlibs() { return List.copyOf(mudlibs); }

    /** Stops transports and each independent clock before invoking each mudlib's shutdown lifecycle once. */
    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        if (administration != null) administration.close();
        if (telnet != null) telnet.close();
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
