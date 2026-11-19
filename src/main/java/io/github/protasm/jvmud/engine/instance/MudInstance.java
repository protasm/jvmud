package io.github.protasm.jvmud.engine.instance;

import io.github.protasm.jvmud.engine.instance.session.InstanceSession;
import io.github.protasm.jvmud.engine.instance.session.InstanceSessions;
import io.github.protasm.jvmud.engine.instance.session.PersonaAttachment;
import io.github.protasm.jvmud.engine.instance.session.login.ManagedLoginPolicies;
import io.github.protasm.jvmud.engine.instance.session.login.ManagedLoginResult;
import io.github.protasm.jvmud.engine.instance.session.login.ManagedLoginSession;
import io.github.protasm.jvmud.engine.instance.session.login.ManagedPersonaProfile;
import io.github.protasm.jvmud.communication.output.OutgoingTextFormatter;
import io.github.protasm.jvmud.communication.protocol.GMCPCodec;
import io.github.protasm.jvmud.communication.protocol.GMCPMessage;
import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.configuration.MudlibConfigurationReader;
import io.github.protasm.jvmud.engine.instance.efun.EngineEfuns;
import io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntime;
import io.github.protasm.jvmud.engine.model.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.engine.model.mudlib.MudlibProjection;
import io.github.protasm.jvmud.engine.model.world.Capability;
import io.github.protasm.jvmud.engine.model.world.Entity;
import io.github.protasm.jvmud.engine.model.world.Location;
import io.github.protasm.jvmud.engine.model.world.Place;
import io.github.protasm.jvmud.engine.model.world.WorldRuntime;
import io.github.protasm.jvmud.language.execution.LPCExecutionOptions;
import io.github.protasm.jvmud.language.execution.LPCObjectHandle;
import io.github.protasm.jvmud.language.execution.LPCObjectLoadObserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

/** Runtime and serialized world operations for one independently hosted mudlib. */
public final class MudInstance implements InstanceHost {
    private final MudlibRuntime runtime;
    private final InstanceSessions sessions;
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
            MudlibRuntime runtime,
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
        this.sessions = runtime.sessions();
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
        MudlibRuntime runtime = new MudlibRuntime(LPCExecutionOptions.builder()
                .baseIncludePath(normalizedRoot)
                .objectLoadObserver(objectLoadObserver)
                .build());
        try {
            MudlibConfiguration declaredBoundary = MudlibConfigurationReader.read(normalizedRoot, configObjectPath);
            EngineEfuns.registerCore(runtime, declaredBoundary.engineCapabilities());
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Could not read mudlib configuration: " + configObjectPath, e);
        }

        MudlibBootResult result =
                new MudlibBoot(runtime, normalizedRoot, configObjectPath, true, progress).boot();
        if (result.initialPlacePath() == null) {
            throw new IllegalStateException("Mudlib boot did not provide a starting place.");
        }

        Object startingPlaceObject = runtime.loadOrGetObject(result.initialPlacePath());
        MudlibConfiguration boundary = result.configuration();
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
        mud.sessions.setAttachmentListener(mud::attachmentChanged);
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
    public <T> T administer(java.util.function.Function<MudlibRuntime, T> action) {
        return runOnWorldThread(() -> administerOnWorldThread(action));
    }

    /** Advances only this mudlib, on its own execution thread. */
    public void advanceWorldTick() {
        if (stopped) return;
        runOnWorldThread(() -> { advanceWorldTickOnWorldThread(); return null; });
    }

    /**
     * Replaces one shared object's behavior while preserving its identity, fields and world bindings.
     *
     * <p>The path is an LPC object id relative to the active source root, such as
     * {@code room/square}. Preparation and installation run on the instance queue. Failed
     * compilation, linking or state compatibility leaves the working revision available. Reload
     * does not rerun LPC initialization or creation hooks.</p>
     *
     * @param objectPath mudlib-relative LPC object id, with or without a {@code .c} suffix
     * @return handle for the preserved identity and newly installed program revision
     * @throws IllegalArgumentException if the path is blank or escapes the active mudlib root
     */
    public LPCObjectHandle reloadMudlibObject(String objectPath) {
        return runOnWorldThread(() -> reloadMudlibObjectOnWorldThread(objectPath));
    }

    /** Opens a connection on the instance queue, entering the selected login policy if needed. */
    public InstanceSession attachSession(PrintWriter out, String remoteAddress) {
        return runOnWorldThread(() -> attachSessionOnWorldThread(out, remoteAddress));
    }

    /** Binds protocol output to the connection, including time before Persona attachment. */
    public void bindClientProtocolSink(InstanceSession session, BiConsumer<String, String> protocolOutputSink) {
        runOnWorldThread(() -> { bindClientProtocolSinkOnWorldThread(session, protocolOutputSink); return null; });
    }

    /** Queues a client protocol state change. */
    public void setClientProtocolEnabled(InstanceSession session, String protocol, boolean enabled) {
        runOnWorldThread(() -> { setClientProtocolEnabledOnWorldThread(session, protocol, enabled); return null; });
    }

    /** Queues a decoded client protocol message. */
    public void receiveClientProtocolMessage(InstanceSession session, String protocol, String message) {
        runOnWorldThread(() -> { receiveClientProtocolMessageOnWorldThread(session, protocol, message); return null; });
    }

    /** Closes a connection and its Persona attachment after prior world work. */
    public void detachSession(InstanceSession session) {
        runOnWorldThread(() -> { detachSessionOnWorldThread(session); return null; });
    }

    /** Delivers input on the world queue; mudlib command results stay inside the instance. */
    public void dispatch(InstanceSession session, PrintWriter out, String commandLine) {
        runOnWorldThread(() -> { dispatchOnWorldThread(session, out, commandLine); return null; });
    }

    /** Queues prompt delivery after preceding world work. */
    public void printPromptIfReady(InstanceSession session, PrintWriter out) {
        runOnWorldThread(() -> { printPromptIfReadyOnWorldThread(session, out); return null; });
    }

    /** Reads input capture state in order with world work. */
    public boolean isCapturingInput(InstanceSession session) {
        return runOnWorldThread(() -> isCapturingInputOnWorldThread(session));
    }

    /** Reads hidden-input state in order with world work. */
    public boolean isCapturingNoEchoInput(InstanceSession session) {
        return runOnWorldThread(() -> isCapturingNoEchoInputOnWorldThread(session));
    }

    /** Reads connection liveness independently of whether a Persona has been attached. */
    public boolean isOpen(InstanceSession session) {
        return runOnWorldThread(() -> isOpenOnWorldThread(session));
    }

    /** Returns the unique identity used in the engine menu and administration. */
    public String gameId() {
        return gameId;
    }

    /** Returns the display name shown in the engine mudlib menu. */
    public String gameName() {
        return bootResult.configuration().gameName().orElse(gameId);
    }

    /** Serializes trusted administration with player dispatch and world ticks. */
    private synchronized <T> T administerOnWorldThread(
            java.util.function.Function<io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntime, T> action) {
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
        return runtime.configuration().temporalTickInterval();
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

        Path activeRoot = bootResult.configuration().mudlibRootPath().orElse(mudlibRoot)
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
        String methodName = bootResult.configuration().lifecycleMethod(MudlibLifecycleEvent.SERVER_SHUTDOWN).orElse(null);
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

    private synchronized InstanceSession attachSessionOnWorldThread(PrintWriter out, String remoteAddress) {
        if (stopped) throw new IllegalStateException("Mudlib is stopped.");
        return attachSession("telnet/" + nextSessionId++, out, remoteAddress, true);
    }

    private synchronized void bindClientProtocolSinkOnWorldThread(
            InstanceSession session, BiConsumer<String, String> protocolOutputSink) {
        if (isOpenOnWorldThread(session)) {
            sessions.bindProtocolSink(session.sessionId(), protocolOutputSink);
        }
    }

    private synchronized void setClientProtocolEnabledOnWorldThread(
            InstanceSession session, String protocol, boolean enabled) {
        if (!isOpenOnWorldThread(session)) {
            return;
        }
        sessions.setProtocolEnabled(session.sessionId(), protocol, enabled);
        session.attachment().ifPresent(attachment -> runtime.withCommandActor(attachment.behavior(), () ->
                runtime.invokeOptionalObject(attachment.behavior(), "client_protocol_changed", protocol, enabled ? 1 : 0)));
        runtime.clearOutputTranscript();
    }

    private synchronized void receiveClientProtocolMessageOnWorldThread(
            InstanceSession session, String protocol, String message) {
        if (!isOpenOnWorldThread(session) || session.attachment().isEmpty()
                || !"GMCP".equalsIgnoreCase(protocol)) {
            return;
        }
        GMCPMessage decoded;
        try {
            decoded = GMCPCodec.decode(message);
        } catch (IllegalArgumentException ignored) {
            return;
        }
        Object behavior = session.personaObject();
        runtime.withCommandActor(behavior, () -> runtime.invokeOptionalObject(
                behavior, "receive_gmcp", decoded.packageName(), decoded.payload()));
        runtime.clearOutputTranscript();
    }

    synchronized InstanceSession attachSession(
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            boolean announceConnection) {
        Optional<ManagedLoginSession> managedLogin =
                ManagedLoginPolicies.create(sessionPolicy, mudlibRoot, gameName(),
                        text -> writeToPlayerForSession(sessionId, text));
        if (managedLogin.isPresent()) {
            return attachManagedLoginSession(
                    sessionId, out, remoteAddress, announceConnection, managedLogin.orElseThrow());
        }

        int id = nextPersonaId++;
        InstanceSession session = attachMudlibPlayer(id, sessionId, out, remoteAddress, announceConnection, null, "");
        if (session != null) {
            return session;
        }
        throw new IllegalStateException("Mudlib config must define player_object for hosted sessions.");
    }

    private InstanceSession attachManagedLoginSession(
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
        sessions.beginLogin(sessionId, login);
        login.start();
        return sessions.session(sessionId).orElseThrow();
    }

    /**
     * Attaches the Persona selected by an optional host-managed authentication policy.
     *
     * <p>Account interpretation remains mudlib policy: JVMud delivers neutral lifecycle events,
     * and the manifest maps those events to whatever methods the selected mudlib uses.</p>
     */
    private InstanceSession attachAuthenticatedPersona(
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
        InstanceSession session = sessions.session(sessionId).orElseThrow();
        PersonaAttachment attachment = session.attachment().orElseThrow();
        sessions.attach(sessionId, attachment.withProfile(profile));
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
        return session;
    }

    private InstanceSession attachMudlibPlayer(
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
            return sessions.session(sessionId).orElseThrow();
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

    private synchronized void detachSessionOnWorldThread(InstanceSession session) {
        if (!isOpenOnWorldThread(session)) {
            return;
        }
        session.attachment().ifPresent(attachment -> invokePlayerSessionDisconnected(attachment.behavior()));
        runtime.unbindSession(session.sessionId());
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

    private synchronized void dispatchOnWorldThread(InstanceSession session, PrintWriter out, String commandLine) {
        try {
            dispatchUnchecked(session, out, commandLine);
        } catch (RuntimeException | LinkageError e) {
            handleRuntimeError(session, out, "command", commandLine, e);
        }
    }

    private void dispatchUnchecked(InstanceSession session, PrintWriter out, String commandLine) {
        if (!isOpenOnWorldThread(session)) {
            return;
        }
        sessions.touchSession(session.sessionId());
        if (session.login().isPresent()) {
            ManagedLoginResult result = session.login().orElseThrow().handle(commandLine);
            if (result.profile().isPresent()) {
                attachAuthenticatedPersona(session.sessionId(), out,
                        session.remoteAddress().orElse(null),
                        result.profile().orElseThrow());
            } else if (result.shouldDisconnect()) {
                runtime.unbindSession(session.sessionId());
            }
            return;
        }

        if (runtime.hasCapturedSessionInput(session.personaObject())) {
            runtime.clearOutputTranscript();
            runtime.deliverCapturedSessionInput(session.personaObject(), commandLine);
            runtime.clearOutputTranscript();
            runDueScheduledWork();
            if (!isOpenOnWorldThread(session)) {
                return;
            }
            return;
        }

        if (isLocationDiagnosticCommand(commandLine)) {
            printCurrentLocationPath(session, out);
            return;
        }

        runtime.clearOutputTranscript();
        runtime.dispatchCommand(session.personaObject(), commandLine);
        runDueScheduledWork();
        runtime.clearOutputTranscript();
    }

    private boolean isLocationDiagnosticCommand(String commandLine) {
        return locationDiagnosticCommand != null && locationDiagnosticCommand.equals(commandLine.trim());
    }

    private void printCurrentLocationPath(InstanceSession session, PrintWriter out) {
        Object location = runtime.environment(session.personaObject());
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

    /** Reacts to the owner's committed attachment transition without keeping a second actor copy. */
    private void attachmentChanged(InstanceSessions.AttachmentChange change) {
        Object previous = change.previous().map(PersonaAttachment::behavior).orElse(null);
        Object current = change.current().map(PersonaAttachment::behavior).orElse(null);
        // Destruction moves carried objects through their lifecycle before removing the binding.
        // A handoff may already have attached this same behavior to another live connection.
        if (change.cause() != InstanceSessions.AttachmentChange.Cause.BEHAVIOR_DESTROYED
                && previous != null && previous != current && sessions.sessionFor(previous).isEmpty()) {
            runtime.worldProjection().remove(previous);
        }
        if (previous != null && current != null && previous != current) {
            runtime.withCommandActor(current, () -> {
                invokePlayerSessionPostRebind(current);
                runtime.refreshCommandActions(current);
                runDueScheduledWork();
                return null;
            });
        }
    }

    private void invokePlayerSessionPostRebind(Object actor) {
        if (playerSessionPostRebindMethod != null) {
            runtime.invokeOptionalObject(actor, playerSessionPostRebindMethod);
            runtime.clearOutputTranscript();
        }
    }

    private Object handleRuntimeError(
            InstanceSession session,
            PrintWriter out,
            String context,
            String operation,
            Throwable error) {
        reportRuntimeError(context, error);
        runtime.clearOutputTranscript();
        if (runtimeErrorMethod != null && invokeRuntimeErrorHandler(session, context, operation, error)) {
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
            InstanceSession session,
            String context,
            String operation,
            Throwable error) {
        try {
            Object handler = errorHandlerObject(session);
            if (handler == null) {
                return false;
            }
            Object actor = session != null ? session.attachment().map(PersonaAttachment::behavior).orElse(null) : null;
            java.util.function.Supplier<Object> report = () -> runtime.invokeOptionalObject(
                    handler, runtimeErrorMethod, actor != null ? actor : 0, context, operation, error.getMessage());
            if (actor != null) {
                runtime.withCommandActor(actor, report);
            } else {
                report.get();
            }
            runtime.clearOutputTranscript();
            return true;
        } catch (RuntimeException | LinkageError handlerFailure) {
            System.err.println("Mudlib error handler failed: " + handlerFailure.getMessage());
            runtime.clearOutputTranscript();
            return false;
        }
    }

    private Object errorHandlerObject(InstanceSession session) {
        String boundaryObjectPath = bootResult.configuration().boundaryObjectPath().orElse(null);
        if (boundaryObjectPath != null) {
            return runtime.loadOrGetObject(boundaryObjectPath);
        }
        return session != null ? session.attachment().map(PersonaAttachment::behavior).orElse(null) : null;
    }

    private synchronized void printPromptIfReadyOnWorldThread(InstanceSession session, PrintWriter out) {
        if (!isOpenOnWorldThread(session) || session.login().isPresent() || session.attachment().isEmpty()) {
            return;
        }
        if (playerPrompt == null || !isOpenOnWorldThread(session) || runtime.hasCapturedSessionInput(session.personaObject())) {
            return;
        }
        if (showRuler) {
            out.println(OutgoingTextFormatter.ruler(maxLineLength));
        }
        out.print(playerPrompt);
        out.flush();
    }

    private synchronized boolean isCapturingInputOnWorldThread(InstanceSession session) {
        if (isOpenOnWorldThread(session) && session.login().isPresent()) {
            return true;
        }
        return isOpenOnWorldThread(session) && session.attachment().isPresent()
                && runtime.hasCapturedSessionInput(session.personaObject());
    }

    private synchronized boolean isCapturingNoEchoInputOnWorldThread(InstanceSession session) {
        if (isOpenOnWorldThread(session) && session.login().isPresent()) {
            return session.login().orElseThrow().noEcho();
        }
        return isOpenOnWorldThread(session) && session.attachment().isPresent()
                && runtime.capturedSessionInputNoEcho(session.personaObject());
    }

    private synchronized boolean isOpenOnWorldThread(InstanceSession session) {
        return session != null && sessions.session(session.sessionId()).orElse(null) == session;
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

}
