package io.github.protasm.jvmud.server;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.runtime.Capability;
import io.github.protasm.jvmud.runtime.Entity;
import io.github.protasm.jvmud.runtime.Location;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibLifecycleEvent;
import io.github.protasm.jvmud.runtime.MudlibProjection;
import io.github.protasm.jvmud.runtime.Place;
import io.github.protasm.jvmud.runtime.WorldRuntime;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/** Shared runtime state for a persistent Telnet mud process. */
final class TelnetMud implements TelnetHost {
    private static final String CONNECTED_BANNER = "JVMud telnet. Type /help for commands or /quit to disconnect.\n";

    private final LPCRuntime runtime;
    private final WorldRuntime worldRuntime;
    private final String gameId;
    private final Path mudlibRoot;
    private final String startingPlacePath;
    private final Object startingPlaceObject;
    private final String playerObjectPath;
    private final String playerPrompt;
    private final String playerSessionConnectedMethod;
    private final String playerSessionDisconnectedMethod;
    private final MudlibBootResult bootResult;
    private final Map<Object, String> requestedTransfers = new IdentityHashMap<>();
    private TransferHandler transferHandler = (mud, actor, gameId) -> 0;
    private int nextPersonaId = 1;

    private TelnetMud(
            LPCRuntime runtime,
            WorldRuntime worldRuntime,
            String gameId,
            Path mudlibRoot,
            String startingPlacePath,
            Object startingPlaceObject,
            String playerObjectPath,
            String playerPrompt,
            String playerSessionConnectedMethod,
            String playerSessionDisconnectedMethod,
            MudlibBootResult bootResult) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.worldRuntime = Objects.requireNonNull(worldRuntime, "worldRuntime");
        this.gameId = Objects.requireNonNull(gameId, "gameId");
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        this.startingPlacePath = Objects.requireNonNull(startingPlacePath, "startingPlacePath");
        this.startingPlaceObject = Objects.requireNonNull(startingPlaceObject, "startingPlaceObject");
        this.playerObjectPath = playerObjectPath;
        this.playerPrompt = playerPrompt;
        this.playerSessionConnectedMethod = playerSessionConnectedMethod;
        this.playerSessionDisconnectedMethod = playerSessionDisconnectedMethod;
        this.bootResult = Objects.requireNonNull(bootResult, "bootResult");
    }

    static TelnetMud boot(Path mudlibRoot, String configObjectPath) {
        Path normalizedRoot = mudlibRoot.toAbsolutePath().normalize();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(normalizedRoot)
                .build());
        EngineEfuns.registerCore(runtime);

        MudlibBootResult result =
                new MudlibBoot(runtime, normalizedRoot, configObjectPath, false).boot();
        if (result.startingRoom() == null) {
            throw new IllegalStateException("Mudlib boot did not provide a starting place.");
        }

        Object startingPlaceObject = runtime.loadOrGetObject(result.startingRoom());
        MudlibBoundary boundary = result.mudlibBoundary();
        String gameId = boundary.gameId().orElse(normalizedRoot.getFileName().toString());
        runtime.clearOutputTranscript();
        TelnetMud mud = new TelnetMud(
                runtime,
                result.worldRuntime(),
                gameId,
                normalizedRoot,
                result.startingRoom(),
                startingPlaceObject,
                boundary.playerObjectPath().orElse(null),
                boundary.playerPrompt().orElse(null),
                boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_CONNECTED).orElse(null),
                boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_DISCONNECTED).orElse(null),
                result);
        runtime.setPlayerTransferHandler((actor, targetGameId) ->
                mud.transferHandler.requestTransfer(mud, actor, targetGameId));
        return mud;
    }

    String gameId() {
        return gameId;
    }

    void setTransferHandler(TransferHandler transferHandler) {
        this.transferHandler = transferHandler != null ? transferHandler : (mud, actor, gameId) -> 0;
    }

    void requestTransfer(Object actor, String gameId) {
        requestedTransfers.put(actor, gameId);
    }

    String consumeRequestedTransfer(TelnetPersona persona) {
        return persona != null ? requestedTransfers.remove(persona.actor()) : null;
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

    @Override
    public synchronized void advanceWorldTick() {
        worldRuntime.scheduler().advanceBy(1);
        runtime.clearOutputTranscript();
    }

    String startingPlacePath() {
        return startingPlacePath;
    }

    @Override
    public synchronized TelnetPersona attachPersona(PrintWriter out, String remoteAddress) {
        return attachPersona("telnet/" + nextPersonaId++, out, remoteAddress, true);
    }

    synchronized TelnetPersona attachPersona(
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            boolean announceConnection) {
        int id = nextPersonaId++;
        TelnetPersona persona = attachMudlibPlayer(id, sessionId, out, remoteAddress, announceConnection);
        if (persona != null) {
            return persona;
        }
        return attachHostPersona(id, sessionId, out, remoteAddress, announceConnection);
    }

    synchronized TelnetPersona attachVisitingPersona(
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            String name) {
        int id = nextPersonaId++;
        return attachHostPersona(id, sessionId, out, remoteAddress, name, false);
    }

    private TelnetPersona attachMudlibPlayer(
            int id,
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            boolean announceConnection) {
        if (playerObjectPath == null) {
            return null;
        }

        try {
            Object actor = runtime.cloneObject(playerObjectPath);
            String objectId = Objects.requireNonNullElse(runtime.objectId(actor), playerObjectPath + "#" + id);
            String name = "player " + id;
            Place startingPlace = placeFor(startingPlacePath);
            worldRuntime.createEntity(
                    objectId,
                    name,
                    startingPlace,
                    Capability.ACTOR,
                    Capability.PERCEPTIVE);
            runtime.moveObject(actor, startingPlaceObject);
            MudlibProjection projection = new LegacyPlayerObjectAdapter(playerObjectPath)
                    .combinedProjection(actor);
            runtime.bindSession(sessionId, actor, remoteAddress, text -> {
                out.print(text);
                out.flush();
            }, projection);
            runtime.clearOutputTranscript();
            if (announceConnection) {
                messagePlayerForSession(sessionId, CONNECTED_BANNER);
                messagePlayerForSession(sessionId, "Attached " + name + " as " + objectId
                        + " in " + startingPlacePath + ".\n");
            }
            invokePlayerSessionConnected(actor);
            return new TelnetPersona(this, sessionId, objectId, name, actor, remoteAddress);
        } catch (RuntimeException | LinkageError e) {
            System.err.println("Could not attach mudlib player object " + playerObjectPath
                    + "; falling back to host persona: " + e.getMessage());
            runtime.clearOutputTranscript();
            return null;
        }
    }

    private TelnetPersona attachHostPersona(
            int id,
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            boolean announceConnection) {
        return attachHostPersona(id, sessionId, out, remoteAddress, "player " + id, announceConnection);
    }

    private TelnetPersona attachHostPersona(
            int id,
            String sessionId,
            PrintWriter out,
            String remoteAddress,
            String name,
            boolean announceConnection) {
        String objectId = "persona/" + id;
        Place startingPlace = placeFor(startingPlacePath);
        Entity entity = worldRuntime.createEntity(
                objectId,
                name,
                startingPlace,
                Capability.ACTOR,
                Capability.PERCEPTIVE);
        LocalSessionActor actor = new LocalSessionActor(runtime, worldRuntime, entity, name);
        runtime.registerHostObject(objectId, actor);
        runtime.moveObject(actor, startingPlaceObject);
        runtime.bindSession(sessionId, actor, remoteAddress, text -> {
            out.print(text);
            out.flush();
        });
        runtime.clearOutputTranscript();
        if (announceConnection) {
            messagePlayerForSession(sessionId, CONNECTED_BANNER);
            messagePlayerForSession(sessionId, "Attached " + name + " in " + startingPlacePath + ".\n");
        }
        return new TelnetPersona(this, sessionId, objectId, name, actor, remoteAddress);
    }

    private void messagePlayerForSession(String sessionId, String text) {
        runtime.playerRecordForSession(sessionId)
                .ifPresent(player -> runtime.messagePlayer(player.id(), text));
    }

    private void invokePlayerSessionConnected(Object actor) {
        if (playerSessionConnectedMethod == null) {
            return;
        }
        runtime.invokeObject(actor, playerSessionConnectedMethod);
        runtime.clearOutputTranscript();
    }

    @Override
    public synchronized void detachPersona(TelnetPersona persona) {
        detachPersona(persona, true);
    }

    synchronized void detachPersona(TelnetPersona persona, boolean invokeDisconnectLifecycle) {
        if (persona != null) {
            if (isAttached(persona)) {
                if (invokeDisconnectLifecycle) {
                    invokePlayerSessionDisconnected(persona.actor());
                }
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

    @Override
    public synchronized Object dispatch(TelnetPersona persona, PrintWriter out, String commandLine) {
        if (runtime.hasCapturedSessionInput(persona.actor())) {
            runtime.clearOutputTranscript();
            Object result = runtime.deliverCapturedSessionInput(persona.actor(), commandLine);
            runtime.clearOutputTranscript();
            if (!isAttached(persona)) {
                removeWorldEntity(persona);
                return 1;
            }
            return result;
        }

        runtime.clearOutputTranscript();
        runtime.refreshCommandActions(persona.actor());
        Object result = runtime.dispatchCommand(persona.actor(), commandLine);
        if (Integer.valueOf(0).equals(result) && isLookCommand(commandLine)) {
            runtime.clearOutputTranscript();
            Object environment = runtime.environment(persona.actor());
            if (environment != null) {
                lookAt(environment, out);
                result = 1;
            }
            printRuntimeOutput(out);
        } else {
            runtime.clearOutputTranscript();
        }
        return result;
    }

    @Override
    public synchronized void printPromptIfReady(TelnetPersona persona, PrintWriter out) {
        if (playerPrompt == null || !isAttached(persona) || runtime.hasCapturedSessionInput(persona.actor())) {
            return;
        }
        out.print(playerPrompt);
        out.flush();
    }

    @Override
    public synchronized boolean isAttached(TelnetPersona persona) {
        return persona != null && runtime.sessionRecord(persona.sessionId()).isPresent();
    }

    private void removeWorldEntity(TelnetPersona persona) {
        if (persona != null) {
            worldRuntime.removeEntity(persona.objectId());
        }
    }

    private void lookAt(Object object, PrintWriter out) {
        try {
            printReturnedDescription(runtime.invokeObject(object, "long", new Object[] {null}), out);
        } catch (RuntimeException e) {
            try {
                printReturnedDescription(runtime.invokeObject(object, "long"), out);
            } catch (RuntimeException ignored) {
                printReturnedDescription(runtime.invokeObject(object, "short"), out);
            }
        }
    }

    private void printReturnedDescription(Object description, PrintWriter out) {
        if (description instanceof String text && !text.isEmpty()) {
            out.println(text);
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

    private boolean isLookCommand(String commandLine) {
        String trimmed = commandLine.trim();
        return "look".equals(trimmed) || "l".equals(trimmed) || trimmed.startsWith("look ");
    }

    @FunctionalInterface
    interface TransferHandler {
        int requestTransfer(TelnetMud sourceMud, Object actor, String gameId);
    }
}
