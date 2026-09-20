package io.github.protasm.jvmud.execution.instance;

import io.github.protasm.jvmud.language.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.language.exec.LPCObjectLoadObserver;
import io.github.protasm.jvmud.language.exec.LPCObjectHandle;
import io.github.protasm.jvmud.language.exec.LPCRuntime;
import io.github.protasm.jvmud.language.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.execution.model.world.Capability;
import io.github.protasm.jvmud.execution.model.world.Entity;
import io.github.protasm.jvmud.execution.model.world.Location;
import io.github.protasm.jvmud.execution.model.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.execution.model.mudlib.MudlibBoundaryConfigReader;
import io.github.protasm.jvmud.execution.model.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.execution.model.mudlib.MudlibProjection;
import io.github.protasm.jvmud.communication.output.OutgoingTextFormatter;
import io.github.protasm.jvmud.communication.protocol.GMCPCodec;
import io.github.protasm.jvmud.communication.protocol.GMCPMessage;
import io.github.protasm.jvmud.execution.model.world.Place;
import io.github.protasm.jvmud.execution.model.world.WorldRuntime;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

/** Runtime and serialized world operations for one independently hosted mudlib. */
public final class MudInstance implements InstanceHost {
    private final LPCRuntime runtime;
    private final WorldRuntime worldRuntime;
    private final String gameId;
    private final Path mudlibRoot;
    private final String startingPlacePath;
    private final Object startingPlaceObject;
    private final String playerObjectPath;
    private final String sessionPolicy;
    private final String playerPrompt;
    private final String connectedBanner;
    private final String transportControlPrefix;
    private final String locationDiagnosticCommand;
    private final int maxLineLength;
    private final boolean showRuler;
    private final String playerSessionConnectedMethod;
    private final String playerSessionPostRebindMethod;
    private final String playerPersonaResolvedMethod;
    private final String playerEnteredWorldMethod;
    private final String playerSessionDisconnectedMethod;
    private final String runtimeErrorMethod;
    private final MudlibBootResult bootResult;
    private volatile boolean stopped;
    private volatile MudlibExecution execution;
    private int nextPersonaId = 1;
    private int nextSessionId = 1;

    private MudInstance(
            LPCRuntime runtime,
            WorldRuntime worldRuntime,
            String gameId,
            Path mudlibRoot,
            String startingPlacePath,
            Object startingPlaceObject,
            String playerObjectPath,
            String sessionPolicy,
            String playerPrompt,
            String connectedBanner,
            String transportControlPrefix,
            String locationDiagnosticCommand,
            int maxLineLength,
            boolean showRuler,
            String playerSessionConnectedMethod,
            String playerSessionPostRebindMethod,
            String playerPersonaResolvedMethod,
            String playerEnteredWorldMethod,
            String playerSessionDisconnectedMethod,
            String runtimeErrorMethod,
            MudlibBootResult bootResult) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.worldRuntime = Objects.requireNonNull(worldRuntime, "worldRuntime");
        this.gameId = Objects.requireNonNull(gameId, "gameId");
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        this.startingPlacePath = Objects.requireNonNull(startingPlacePath, "startingPlacePath");
        this.startingPlaceObject = Objects.requireNonNull(startingPlaceObject, "startingPlaceObject");
        this.playerObjectPath = playerObjectPath;
        this.sessionPolicy = sessionPolicy;
        this.playerPrompt = playerPrompt;
        this.connectedBanner = connectedBanner;
        this.transportControlPrefix = Objects.requireNonNull(transportControlPrefix, "transportControlPrefix");
        this.locationDiagnosticCommand = locationDiagnosticCommand;
        this.maxLineLength = maxLineLength;
        this.showRuler = showRuler;
        this.playerSessionConnectedMethod = playerSessionConnectedMethod;
        this.playerSessionPostRebindMethod = playerSessionPostRebindMethod;
        this.playerPersonaResolvedMethod = playerPersonaResolvedMethod;
        this.playerEnteredWorldMethod = playerEnteredWorldMethod;
        this.playerSessionDisconnectedMethod = playerSessionDisconnectedMethod;
        this.runtimeErrorMethod = runtimeErrorMethod;
        this.bootResult = Objects.requireNonNull(bootResult, "bootResult");
    }

    public static MudInstance boot(Path mudlibRoot, String configObjectPath) {
        return boot(mudlibRoot, configObjectPath, MudlibBootProgress.none(), LPCObjectLoadObserver.NONE);
    }

    /**
     * Boots a persistent mud instance and reports host-visible progress to the supplied callback.
     *
     * @param mudlibRoot filesystem root of the selected mudlib
     * @param configObjectPath mudlib-relative JVMud configuration path
     * @param progress callback for local startup progress events
     * @return booted mud instance ready for session attachment
     */
    public static MudInstance boot(Path mudlibRoot, String configObjectPath, MudlibBootProgress progress) {
        return boot(mudlibRoot, configObjectPath, progress, LPCObjectLoadObserver.NONE);
    }

    /**
     * Boots a persistent mud instance with host-visible preload and object-load diagnostics.
     *
     * @param mudlibRoot filesystem root of the selected mudlib
     * @param configObjectPath mudlib-relative JVMud configuration path
     * @param progress callback for local startup progress events
     * @param objectLoadObserver observer for each shared LPC object load attempt
     * @return booted mud instance ready for session attachment
     */
    public static MudInstance boot(
            Path mudlibRoot,
            String configObjectPath,
            MudlibBootProgress progress,
            LPCObjectLoadObserver objectLoadObserver) {
        Path normalizedRoot = mudlibRoot.toAbsolutePath().normalize();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(normalizedRoot)
                .objectLoadObserver(objectLoadObserver)
                .build());
        try {
            MudlibBoundary declaredBoundary = MudlibBoundaryConfigReader.read(normalizedRoot, configObjectPath);
            CoreEfuns.registerCore(runtime, declaredBoundary.engineCapabilities());
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Could not read mudlib configuration: " + configObjectPath, e);
        }

        MudlibBootResult result =
                new MudlibBoot(runtime, normalizedRoot, configObjectPath, true, progress).boot();
        if (result.initialPlacePath() == null) {
            throw new IllegalStateException("Mudlib boot did not provide a starting place.");
        }

        Object startingPlaceObject = runtime.loadOrGetObject(result.initialPlacePath());
        MudlibBoundary boundary = result.mudlibBoundary();
        String gameId = boundary.gameId().orElse(normalizedRoot.getFileName().toString());
        runtime.clearOutputTranscript();
        MudInstance mud = new MudInstance(
                runtime,
                result.worldRuntime(),
                gameId,
                normalizedRoot,
                result.initialPlacePath(),
                startingPlaceObject,
                boundary.playerObjectPath().orElse(null),
                boundary.sessionPolicy().orElse(null),
                boundary.playerPrompt().orElse(null),
                boundary.connectedBanner().orElse(
                        "JVMud telnet. Type " + boundary.transportControlPrefix()
                                + "help for commands or " + boundary.transportControlPrefix()
                                + "quit to disconnect."),
                boundary.transportControlPrefix(),
                boundary.locationDiagnosticCommand().orElse(null),
                boundary.maxLineLength(),
                boundary.showRuler(),
                boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_CONNECTED).orElse(null),
                boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_POST_REBIND).orElse(null),
                boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_PERSONA_RESOLVED).orElse(null),
                boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_ENTERED_WORLD).orElse(null),
                boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_DISCONNECTED).orElse(null),
                boundary.lifecycleMethod(MudlibLifecycleEvent.RUNTIME_ERROR).orElse(null),
                result);
        return mud;
    }

    /** Starts this instance's independent command queue and configured clock exactly once. */
    public synchronized void startExecution() {
        if (stopped) throw new IllegalStateException("Mudlib is stopped.");
        if (execution == null) execution = new MudlibExecution(gameId, worldTickInterval(), this::advanceWorldTick);
    }

    /** Stops the clock, completes queued work and the shutdown lifecycle, then closes this world queue. */
    @Override
    public void shutdown(Object reason) {
        if (stopped) return;
        MudlibExecution active = execution;
        if (active != null) active.stopClock();
        try { runOnWorldThread(() -> { shutdownOnWorldThread(reason); return null; }); }
        finally { if (active != null) active.close(); }
    }

    /** Unstarted instances remain usable by deterministic compiler and mudlib test harnesses. */
    private <T> T runOnWorldThread(java.util.concurrent.Callable<T> operation) {
        MudlibExecution active = execution;
        if (active != null) return active.call(() -> {
            if (stopped) throw new IllegalStateException("Mudlib is stopped.");
            return operation.call();
        });
        try { return operation.call(); }
        catch (RuntimeException | Error e) { throw e; }
        catch (Exception e) { throw new IllegalStateException("Mudlib operation failed.", e); }
    }

    /** Runs trusted administration on this mudlib's execution thread. */
    public <T> T administer(java.util.function.Function<LPCRuntime, T> action) {
        return runOnWorldThread(() -> administerOnWorldThread(action));
    }

    /** Advances only this mudlib, on its own execution thread. */
    public void advanceWorldTick() {
        if (stopped) return;
        runOnWorldThread(() -> { advanceWorldTickOnWorldThread(); return null; });
    }

    /**
     * Recompiles and replaces one shared mudlib object while this instance remains online.
     *
     * <p>The path is an LPC object id relative to the selected mudlib's active source root, such
     * as {@code system/quests}. Existing callers resolve the replacement through the same stable
     * object id. Host administration code should use this operation only for shared service or
     * content objects whose own reload policy is safe; it is intentionally not exposed as an
     * unauthenticated player transport command.</p>
     *
     * @param objectPath mudlib-relative LPC object id, with or without a {@code .c} suffix
     * @return handle for the newly compiled shared object
     * @throws IllegalArgumentException if the path is blank or escapes the active mudlib root
     */
    public LPCObjectHandle reloadMudlibObject(String objectPath) {
        return runOnWorldThread(() -> reloadMudlibObjectOnWorldThread(objectPath));
    }

    /** Queues attachment to this mudlib before delivering player input. */
    public InstancePersona attachPersona(PrintWriter out, String remoteAddress) {
        return runOnWorldThread(() -> attachPersonaOnWorldThread(out, remoteAddress));
    }

    /** Queues the transport output binding for a selected persona. */
    public void bindClientProtocolSink(InstancePersona persona, BiConsumer<String, String> protocolOutputSink) {
        runOnWorldThread(() -> { bindClientProtocolSinkOnWorldThread(persona, protocolOutputSink); return null; });
    }

    /** Queues a client protocol state change. */
    public void setClientProtocolEnabled(InstancePersona persona, String protocol, boolean enabled) {
        runOnWorldThread(() -> { setClientProtocolEnabledOnWorldThread(persona, protocol, enabled); return null; });
    }

    /** Queues a decoded client protocol message. */
    public void receiveClientProtocolMessage(InstancePersona persona, String protocol, String message) {
        runOnWorldThread(() -> { receiveClientProtocolMessageOnWorldThread(persona, protocol, message); return null; });
    }

    /** Queues session detachment after prior world work. */
    public void detachPersona(InstancePersona persona) {
        runOnWorldThread(() -> { detachPersonaOnWorldThread(persona); return null; });
    }

    /** Delivers input on the world queue; mudlib command results stay inside the instance. */
    public void dispatch(InstancePersona persona, PrintWriter out, String commandLine) {
        runOnWorldThread(() -> { dispatchOnWorldThread(persona, out, commandLine); return null; });
    }

    /** Queues prompt delivery after preceding world work. */
    public void printPromptIfReady(InstancePersona persona, PrintWriter out) {
        runOnWorldThread(() -> { printPromptIfReadyOnWorldThread(persona, out); return null; });
    }

    /** Reads input capture state in order with world work. */
    public boolean isCapturingInput(InstancePersona persona) {
        return runOnWorldThread(() -> isCapturingInputOnWorldThread(persona));
    }

    /** Reads hidden-input state in order with world work. */
    public boolean isCapturingNoEchoInput(InstancePersona persona) {
        return runOnWorldThread(() -> isCapturingNoEchoInputOnWorldThread(persona));
    }

    /** Reads attachment state in order with world work. */
    public boolean isAttached(InstancePersona persona) {
        return runOnWorldThread(() -> isAttachedOnWorldThread(persona));
    }

    /** Returns the unique identity used in the engine menu and administration. */
    public String gameId() {
        return gameId;
    }

    /** Returns the display name shown in the engine mudlib menu. */
    public String gameName() {
        return bootResult.mudlibBoundary().gameName().orElse(gameId);
    }

    /** Serializes trusted administration with player dispatch and world ticks. */
    private synchronized <T> T administerOnWorldThread(
            java.util.function.Function<io.github.protasm.jvmud.language.exec.LPCRuntime, T> action) {
        return action.apply(runtime);
    }

    @Override
    public Path mudlibRoot() {
        return mudlibRoot;
    }

    @Override
    public MudlibBootResult bootResult() {
        return bootResult;
    }

    @Override
    public Duration worldTickInterval() {
        return runtime.mudlibBoundary().temporalTickInterval();
    }

    private synchronized void advanceWorldTickOnWorldThread() {
        if (stopped) return;
        worldRuntime.scheduler().advanceBy(1);
        runtime.clearOutputTranscript();
    }

    private synchronized LPCObjectHandle reloadMudlibObjectOnWorldThread(String objectPath) {
        Objects.requireNonNull(objectPath, "objectPath");
        String normalizedObjectPath = objectPath.trim();
        if (normalizedObjectPath.endsWith(".c")) {
            normalizedObjectPath = normalizedObjectPath.substring(0, normalizedObjectPath.length() - 2);
        }
        while (normalizedObjectPath.startsWith("/")) {
            normalizedObjectPath = normalizedObjectPath.substring(1);
        }
        if (normalizedObjectPath.isBlank()) {
            throw new IllegalArgumentException("objectPath cannot be blank.");
        }

        Path activeRoot = bootResult.mudlibBoundary().mudlibRootPath().orElse(mudlibRoot)
                .toAbsolutePath()
                .normalize();
        Path sourcePath = activeRoot.resolve(normalizedObjectPath + ".c").normalize();
        if (!sourcePath.startsWith(activeRoot)) {
            throw new IllegalArgumentException("objectPath must remain inside the active mudlib root.");
        }
        return runtime.reload(sourcePath);
    }

    private synchronized void shutdownOnWorldThread(Object reason) {
        if (stopped) return;
        stopped = true;
        runtime.disconnectPlayerSessions(playerSessionDisconnectedMethod);
        String methodName = bootResult.mudlibBoundary().lifecycleMethod(MudlibLifecycleEvent.SERVER_SHUTDOWN).orElse(null);
        if (methodName == null) {
            return;
        }
        Object handler = errorHandlerObject(null);
        if (handler == null) {
            return;
        }
        try {
            runtime.invokeOptionalObject(handler, methodName, reason);
        } catch (RuntimeException | LinkageError e) {
            System.err.println("Ignoring mudlib shutdown lifecycle failure: " + e.getMessage());
        } finally {
            runtime.clearOutputTranscript();
        }
    }

    String startingPlacePath() {
        return startingPlacePath;
    }

    private synchronized InstancePersona attachPersonaOnWorldThread(PrintWriter out, String remoteAddress) {
        if (stopped) throw new IllegalStateException("Mudlib is stopped.");
        return attachPersona("telnet/" + nextSessionId++, out, remoteAddress, true);
    }

    private synchronized void bindClientProtocolSinkOnWorldThread(
            InstancePersona persona, BiConsumer<String, String> protocolOutputSink) {
        if (persona != null && isAttached(persona)) {
            runtime.bindSessionProtocolSink(persona.actor(), protocolOutputSink);
        }
    }

    private synchronized void setClientProtocolEnabledOnWorldThread(
            InstancePersona persona, String protocol, boolean enabled) {
        if (persona == null || !isAttached(persona)) {
            return;
        }
        runtime.setSessionProtocolEnabled(persona.actor(), protocol, enabled);
        runtime.withCommandActor(persona.actor(), () -> runtime.invokeOptionalObject(
                persona.actor(), "client_protocol_changed", protocol, enabled ? 1 : 0));
        runtime.clearOutputTranscript();
    }

    private synchronized void receiveClientProtocolMessageOnWorldThread(
            InstancePersona persona, String protocol, String message) {
        if (persona == null || !isAttached(persona) || !"GMCP".equalsIgnoreCase(protocol)) {
            return;
        }
        GMCPMessage decoded;
        try {
            decoded = GMCPCodec.decode(message);
        } catch (IllegalArgumentException ignored) {
            return;
        }
        runtime.withCommandActor(persona.actor(), () -> runtime.invokeOptionalObject(
                persona.actor(), "receive_gmcp", decoded.packageName(), decoded.payload()));
        runtime.clearOutputTranscript();
    }

    synchronized InstancePersona attachPersona(
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            boolean announceConnection) {
        ManagedLoginSession managedLogin =
                ManagedLoginPolicies.create(sessionPolicy, this, sessionId, remoteAddress);
        if (managedLogin != null) {
            return attachManagedLoginSession(
                    sessionId, out, remoteAddress, announceConnection, managedLogin);
        }

        int id = nextPersonaId++;
        InstancePersona persona = attachMudlibPlayer(id, sessionId, out, remoteAddress, announceConnection, null, "");
        if (persona != null) {
            return persona;
        }
        throw new IllegalStateException("Mudlib config must define player_object for hosted sessions.");
    }

    private InstancePersona attachManagedLoginSession(
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            boolean announceConnection,
            ManagedLoginSession login) {
        runtime.bindPlayerSession(sessionId, remoteAddress, text -> {
            out.print(text);
            out.flush();
        });
        runtime.clearOutputTranscript();
        if (announceConnection) {
            writeConnectedBanner(sessionId);
        }
        InstancePersona persona = new InstancePersona(
                this,
                sessionId,
                "player/" + sessionId,
                "login player",
                login,
                remoteAddress);
        login.start();
        return persona;
    }

    /**
     * Attaches the Persona selected by an optional host-managed authentication policy.
     *
     * <p>Account interpretation remains mudlib policy: JVMud delivers neutral lifecycle events,
     * and the manifest maps those events to whatever methods the selected mudlib uses.</p>
     */
    InstancePersona attachAuthenticatedPersona(
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            ManagedPersonaProfile profile) {
        int id = nextPersonaId++;
        Object actor = runtime.cloneObject(playerObjectPath);
        String objectId = Objects.requireNonNullElse(runtime.objectId(actor), playerObjectPath + "#" + id);
        Place startingPlace = placeFor(startingPlacePath);
        Entity nativeActor = worldRuntime.createEntity(
                objectId,
                profile.displayName(),
                startingPlace,
                Capability.ACTOR,
                Capability.PERCEPTIVE);
        runtime.worldProjection().bindEntity(actor, nativeActor);
        MudlibProjection projection = new CombinedPlayerPersonaAdapter(playerObjectPath)
                .combinedProjection(actor);
        runtime.bindSession(sessionId, actor, remoteAddress, text -> {
            out.print(text);
            out.flush();
        }, projection);
        runtime.refreshCommandActions(actor);
        runtime.clearOutputTranscript();
        if (playerPersonaResolvedMethod != null) {
            runtime.invokeObject(
                    actor,
                    playerPersonaResolvedMethod,
                    profile.externalUserId(),
                    profile.displayName(),
                    profile.gender(),
                    profile.attributes());
        }
        if (playerEnteredWorldMethod != null) {
            runtime.invokeObject(actor, playerEnteredWorldMethod, startingPlaceObject);
        }
        runtime.clearOutputTranscript();
        return new InstancePersona(
                this,
                sessionId,
                objectId,
                profile.displayName(),
                profile.externalUserId(),
                profile.gender(),
                actor,
                remoteAddress);
    }

    private InstancePersona attachMudlibPlayer(
            int id,
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            boolean announceConnection,
            String visitingUserId,
            String visitingGender) {
        if (playerObjectPath == null) {
            return null;
        }

        try {
            Object actor = runtime.cloneObject(playerObjectPath);
            String objectId = Objects.requireNonNullElse(runtime.objectId(actor), playerObjectPath + "#" + id);
            String name = "player " + id;
            Place startingPlace = placeFor(startingPlacePath);
            Entity nativeActor = worldRuntime.createEntity(
                    objectId,
                    name,
                    startingPlace,
                    Capability.ACTOR,
                    Capability.PERCEPTIVE);
            runtime.worldProjection().bindEntity(actor, nativeActor);
            MudlibProjection projection = new CombinedPlayerPersonaAdapter(playerObjectPath)
                    .combinedProjection(actor);
            runtime.bindSession(sessionId, actor, remoteAddress, text -> {
                out.print(text);
                out.flush();
            }, projection);
            runtime.refreshCommandActions(actor);
            runtime.clearOutputTranscript();
            if (announceConnection) {
                writeConnectedBanner(sessionId);
            }
            if (visitingUserId != null) {
                runtime.withCommandActor(actor, () ->
                        runtime.invokeOptionalObject(actor, "jvmud_exhibit_logon", visitingUserId, visitingGender));
                runtime.clearOutputTranscript();
                name = visitingUserId;
            } else {
                invokePlayerSessionConnected(actor);
            }
            runDueScheduledWork();
            if (announceConnection
                    && runtime.sessionRecord(sessionId).isPresent()
                    && runtime.lpcObjectForSession(sessionId).orElse(null) == actor
                    && !runtime.hasCapturedSessionInput(actor)) {
                writeToPlayerForSession(sessionId, "Attached " + name + " as " + objectId
                        + " in " + startingPlacePath + ".\n");
            }
            return new InstancePersona(this, sessionId, objectId, name, visitingUserId != null ? visitingUserId : name,
                    visitingGender, actor, remoteAddress);
        } catch (RuntimeException | LinkageError e) {
            runtime.clearOutputTranscript();
            throw new IllegalStateException("Could not attach mudlib player object " + playerObjectPath + ".", e);
        }
    }

    private void writeToPlayerForSession(String sessionId, String text) {
        runtime.playerRecordForSession(sessionId)
                .ifPresent(player -> runtime.writeToPlayer(player.id(), text));
    }

    private void invokePlayerSessionConnected(Object actor) {
        if (playerSessionConnectedMethod == null) {
            return;
        }
        runtime.invokeObject(actor, playerSessionConnectedMethod);
        runtime.clearOutputTranscript();
    }

    private synchronized void detachPersonaOnWorldThread(InstancePersona persona) {
        if (persona != null) {
            if (persona.actor() instanceof ManagedLoginSession) {
                runtime.unbindSession(persona.sessionId());
                return;
            }
            if (isAttached(persona)) {
                invokePlayerSessionDisconnected(persona.actor());
                runtime.unbindSession(persona.sessionId());
            }
            removeWorldEntity(persona);
        }
    }

    private void invokePlayerSessionDisconnected(Object actor) {
        if (playerSessionDisconnectedMethod == null) {
            return;
        }
        try {
            runtime.invokeOptionalObject(actor, playerSessionDisconnectedMethod);
        } catch (RuntimeException e) {
            System.err.println("Ignoring player disconnect lifecycle failure: " + e.getMessage());
        } finally {
            runtime.clearOutputTranscript();
        }
    }

    private synchronized void dispatchOnWorldThread(InstancePersona persona, PrintWriter out, String commandLine) {
        try {
            dispatchUnchecked(persona, out, commandLine);
        } catch (RuntimeException | LinkageError e) {
            handleRuntimeError(persona, out, "command", commandLine, e);
        }
    }

    private void dispatchUnchecked(InstancePersona persona, PrintWriter out, String commandLine) {
        if (persona.actor() instanceof ManagedLoginSession login) {
            ManagedLoginResult result = login.handle(commandLine, out);
            if (result.replacement().isPresent()) {
                persona.replaceWith(result.replacement().orElseThrow());
                return;
            }
            if (result.shouldDisconnect()) {
                runtime.unbindSession(persona.sessionId());
                return;
            }
            return;
        }

        if (runtime.hasCapturedSessionInput(persona.actor())) {
            runtime.clearOutputTranscript();
            runtime.deliverCapturedSessionInput(persona.actor(), commandLine);
            runtime.clearOutputTranscript();
            runDueScheduledWork();
            if (!isAttached(persona)) {
                removeWorldEntity(persona);
                return;
            }
            refreshBoundActor(persona);
            return;
        }

        if (isLocationDiagnosticCommand(commandLine)) {
            printCurrentLocationPath(persona, out);
            return;
        }

        runtime.clearOutputTranscript();
        runtime.dispatchCommand(persona.actor(), commandLine);
        runDueScheduledWork();
        refreshBoundActor(persona);
        runtime.clearOutputTranscript();
    }

    private boolean isLocationDiagnosticCommand(String commandLine) {
        return locationDiagnosticCommand != null && locationDiagnosticCommand.equals(commandLine.trim());
    }

    private void printCurrentLocationPath(InstancePersona persona, PrintWriter out) {
        Object location = runtime.environment(persona.actor());
        String objectId = location != null ? runtime.objectId(location) : null;
        out.println(objectId != null ? "/" + objectId + ".c" : "No current location.");
    }

    private void writeConnectedBanner(String sessionId) {
        if (connectedBanner != null) {
            writeToPlayerForSession(sessionId, connectedBanner + "\n");
        }
    }

    @Override
    public String transportControlPrefix() {
        return transportControlPrefix;
    }

    private void runDueScheduledWork() {
        worldRuntime.scheduler().advanceBy(0);
    }

    private void refreshBoundActor(InstancePersona persona) {
        runtime.lpcObjectForSession(persona.sessionId()).ifPresent(boundActor -> {
            if (boundActor != persona.actor()) {
                String objectId = Objects.requireNonNullElse(runtime.objectId(boundActor), persona.objectId());
                // A mudlib-side session rebind replaces the live actor; the old entity must not
                // reserve its object id or remain addressable as the connected player.
                removeWorldEntity(persona);
                persona.replaceActor(objectId, boundActor);
                invokePlayerSessionPostRebind(boundActor);
                runtime.refreshCommandActions(boundActor);
                runDueScheduledWork();
            }
        });
    }

    private void invokePlayerSessionPostRebind(Object actor) {
        if (playerSessionPostRebindMethod != null) {
            runtime.invokeOptionalObject(actor, playerSessionPostRebindMethod);
            runtime.clearOutputTranscript();
        }
    }

    private Object handleRuntimeError(
            InstancePersona persona,
            PrintWriter out,
            String context,
            String operation,
            Throwable error) {
        reportRuntimeError(context, error);
        runtime.clearOutputTranscript();
        if (runtimeErrorMethod != null && invokeRuntimeErrorHandler(persona, context, operation, error)) {
            return 1;
        }
        out.println("Something goes wrong.");
        return 1;
    }

    /**
     * Emits an operator-facing diagnostic without including the command or captured input, which
     * may contain authentication material. Mudlib hooks remain responsible for player-facing text.
     */
    private void reportRuntimeError(String context, Throwable error) {
        System.err.println("Mudlib runtime error during " + context + ": " + error.getMessage());
    }

    private boolean invokeRuntimeErrorHandler(
            InstancePersona persona,
            String context,
            String operation,
            Throwable error) {
        try {
            Object handler = errorHandlerObject(persona);
            if (handler == null) {
                return false;
            }
            runtime.withCommandActor(persona.actor(), () -> {
                runtime.invokeOptionalObject(
                        handler,
                        runtimeErrorMethod,
                        persona.actor(),
                        context,
                        operation,
                        error.getMessage());
                return null;
            });
            runtime.clearOutputTranscript();
            return true;
        } catch (RuntimeException | LinkageError handlerFailure) {
            System.err.println("Mudlib error handler failed: " + handlerFailure.getMessage());
            runtime.clearOutputTranscript();
            return false;
        }
    }

    private Object errorHandlerObject(InstancePersona persona) {
        String boundaryObjectPath = bootResult.mudlibBoundary().boundaryObjectPath().orElse(null);
        if (boundaryObjectPath != null) {
            return runtime.loadOrGetObject(boundaryObjectPath);
        }
        return persona != null ? persona.actor() : null;
    }

    private synchronized void printPromptIfReadyOnWorldThread(InstancePersona persona, PrintWriter out) {
        if (persona.actor() instanceof ManagedLoginSession) {
            return;
        }
        if (playerPrompt == null || !isAttached(persona) || runtime.hasCapturedSessionInput(persona.actor())) {
            return;
        }
        if (showRuler) {
            out.println(OutgoingTextFormatter.ruler(maxLineLength));
        }
        out.print(playerPrompt);
        out.flush();
    }

    private synchronized boolean isCapturingInputOnWorldThread(InstancePersona persona) {
        if (persona != null && persona.actor() instanceof ManagedLoginSession) {
            return true;
        }
        return isAttached(persona) && runtime.hasCapturedSessionInput(persona.actor());
    }

    private synchronized boolean isCapturingNoEchoInputOnWorldThread(InstancePersona persona) {
        if (persona != null && persona.actor() instanceof ManagedLoginSession login) {
            return login.noEcho();
        }
        return isAttached(persona) && runtime.capturedSessionInputNoEcho(persona.actor());
    }

    private synchronized boolean isAttachedOnWorldThread(InstancePersona persona) {
        return persona != null && runtime.sessionRecord(persona.sessionId()).isPresent();
    }

    private void removeWorldEntity(InstancePersona persona) {
        if (persona != null) {
            runtime.worldProjection().remove(persona.actor());
        }
    }

    private void printRuntimeOutput(PrintWriter out) {
        String output = runtime.outputTranscript();
        runtime.clearOutputTranscript();
        if (!output.isEmpty()) {
            out.print(output);
            if (!output.endsWith("\n")) {
                out.println();
            }
        }
    }

    private Place placeFor(String path) {
        Location existing = worldRuntime.findLocation(path);
        if (existing instanceof Place place) {
            return place;
        }
        if (existing != null) {
            throw new IllegalArgumentException("Starting location is not a place: " + path);
        }
        return worldRuntime.createPlace(path, path);
    }

    void messageLoginPlayer(String sessionId, String text) {
        writeToPlayerForSession(sessionId, text);
    }

}
