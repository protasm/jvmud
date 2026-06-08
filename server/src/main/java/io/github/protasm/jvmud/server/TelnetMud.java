package io.github.protasm.jvmud.server;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.runtime.Capability;
import io.github.protasm.jvmud.runtime.Entity;
import io.github.protasm.jvmud.runtime.Location;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibLifecycleEvent;
import io.github.protasm.jvmud.runtime.Place;
import io.github.protasm.jvmud.runtime.WorldRuntime;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

/** Shared runtime state for a persistent Telnet mud process. */
final class TelnetMud {
    private final LPCRuntime runtime;
    private final WorldRuntime worldRuntime;
    private final Path mudlibRoot;
    private final String startingRoomPath;
    private final Object startingRoomObject;
    private final String playerObjectPath;
    private final String playerSessionConnectedMethod;
    private int nextPersonaId = 1;

    private TelnetMud(
            LPCRuntime runtime,
            WorldRuntime worldRuntime,
            Path mudlibRoot,
            String startingRoomPath,
            Object startingRoomObject,
            String playerObjectPath,
            String playerSessionConnectedMethod) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.worldRuntime = Objects.requireNonNull(worldRuntime, "worldRuntime");
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        this.startingRoomPath = Objects.requireNonNull(startingRoomPath, "startingRoomPath");
        this.startingRoomObject = Objects.requireNonNull(startingRoomObject, "startingRoomObject");
        this.playerObjectPath = playerObjectPath;
        this.playerSessionConnectedMethod = playerSessionConnectedMethod;
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
            throw new IllegalStateException("Mudlib boot did not provide a starting room.");
        }

        Object startingRoomObject = runtime.loadOrGetObject(result.startingRoom());
        MudlibBoundary boundary = result.mudlibBoundary();
        runtime.clearOutputTranscript();
        return new TelnetMud(
                runtime,
                result.worldRuntime(),
                normalizedRoot,
                result.startingRoom(),
                startingRoomObject,
                boundary.playerObjectPath().orElse(null),
                boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_CONNECTED).orElse(null));
    }

    Path mudlibRoot() {
        return mudlibRoot;
    }

    Duration worldTickInterval() {
        return runtime.mudlibBoundary().temporalTickInterval();
    }

    synchronized void advanceWorldTick() {
        worldRuntime.scheduler().advanceBy(1);
        runtime.clearOutputTranscript();
    }

    String startingRoomPath() {
        return startingRoomPath;
    }

    synchronized Persona attachPersona(PrintWriter out, String remoteAddress) {
        int id = nextPersonaId++;
        Persona persona = attachMudlibPlayer(id, out, remoteAddress);
        if (persona != null) {
            return persona;
        }
        return attachHostPersona(id, out, remoteAddress);
    }

    private Persona attachMudlibPlayer(int id, PrintWriter out, String remoteAddress) {
        if (playerObjectPath == null) {
            return null;
        }

        try {
            Object actor = runtime.cloneObject(playerObjectPath);
            String objectId = Objects.requireNonNullElse(runtime.objectId(actor), playerObjectPath + "#" + id);
            String sessionId = "telnet/" + id;
            String name = "player " + id;
            Place startingPlace = placeFor(startingRoomPath);
            worldRuntime.createEntity(
                    objectId,
                    name,
                    startingPlace,
                    Capability.ACTOR,
                    Capability.PERCEPTIVE);
            runtime.moveObject(actor, startingRoomObject);
            runtime.bindSession(sessionId, actor, remoteAddress, text -> {
                out.print(text);
                out.flush();
            });
            runtime.clearOutputTranscript();
            out.println("Attached " + name + " as " + objectId + " in " + startingRoomPath + ".");
            invokePlayerSessionConnected(actor);
            return new Persona(sessionId, objectId, name, actor);
        } catch (RuntimeException | LinkageError e) {
            System.err.println("Could not attach mudlib player object " + playerObjectPath
                    + "; falling back to host persona: " + e.getMessage());
            runtime.clearOutputTranscript();
            return null;
        }
    }

    private Persona attachHostPersona(int id, PrintWriter out, String remoteAddress) {
        String objectId = "persona/" + id;
        String sessionId = "telnet/" + id;
        String name = "player " + id;
        Place startingPlace = placeFor(startingRoomPath);
        Entity entity = worldRuntime.createEntity(
                objectId,
                name,
                startingPlace,
                Capability.ACTOR,
                Capability.PERCEPTIVE);
        LocalSessionActor actor = new LocalSessionActor(runtime, worldRuntime, entity, name);
        runtime.registerHostObject(objectId, actor);
        runtime.moveObject(actor, startingRoomObject);
        runtime.bindSession(sessionId, actor, remoteAddress, text -> {
            out.print(text);
            out.flush();
        });
        runtime.clearOutputTranscript();
        out.println("Attached " + name + " in " + startingRoomPath + ".");
        return new Persona(sessionId, objectId, name, actor);
    }

    private void invokePlayerSessionConnected(Object actor) {
        if (playerSessionConnectedMethod == null) {
            return;
        }
        runtime.invokeObject(actor, playerSessionConnectedMethod);
        runtime.clearOutputTranscript();
    }

    synchronized void detachPersona(Persona persona) {
        if (persona != null) {
            runtime.unbindSession(persona.sessionId());
        }
    }

    synchronized Object dispatch(Persona persona, PrintWriter out, String commandLine) {
        if (runtime.hasCapturedSessionInput(persona.actor())) {
            runtime.clearOutputTranscript();
            Object result = runtime.deliverCapturedSessionInput(persona.actor(), commandLine);
            runtime.clearOutputTranscript();
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

    record Persona(String sessionId, String objectId, String name, Object actor) {
    }
}
