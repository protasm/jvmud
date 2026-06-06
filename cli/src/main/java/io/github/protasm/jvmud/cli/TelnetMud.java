package io.github.protasm.jvmud.cli;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LpcRuntime;
import io.github.protasm.jvmud.compiler.exec.LpcRuntimeConfig;
import io.github.protasm.jvmud.runtime.Capability;
import io.github.protasm.jvmud.runtime.Entity;
import io.github.protasm.jvmud.runtime.Location;
import io.github.protasm.jvmud.runtime.Place;
import io.github.protasm.jvmud.runtime.WorldRuntime;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Objects;

/** Shared runtime state for a persistent Telnet mud process. */
final class TelnetMud {
    private final LpcRuntime runtime;
    private final WorldRuntime worldRuntime;
    private final Path mudlibRoot;
    private final String startingRoomPath;
    private final Object startingRoomObject;
    private int nextPersonaId = 1;

    private TelnetMud(
            LpcRuntime runtime,
            WorldRuntime worldRuntime,
            Path mudlibRoot,
            String startingRoomPath,
            Object startingRoomObject) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.worldRuntime = Objects.requireNonNull(worldRuntime, "worldRuntime");
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        this.startingRoomPath = Objects.requireNonNull(startingRoomPath, "startingRoomPath");
        this.startingRoomObject = Objects.requireNonNull(startingRoomObject, "startingRoomObject");
    }

    static TelnetMud boot(Path mudlibRoot, String configObjectPath) {
        Path normalizedRoot = mudlibRoot.toAbsolutePath().normalize();
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder()
                .baseIncludePath(normalizedRoot)
                .build());
        EngineEfuns.registerCore(runtime);

        MudlibBootResult result =
                new MudlibBoot(runtime, normalizedRoot, configObjectPath, false).boot();
        if (result.startingRoom() == null) {
            throw new IllegalStateException("Mudlib boot did not provide a starting room.");
        }

        Object startingRoomObject = runtime.loadOrGetObject(result.startingRoom());
        runtime.clearOutputTranscript();
        return new TelnetMud(
                runtime,
                result.worldRuntime(),
                normalizedRoot,
                result.startingRoom(),
                startingRoomObject);
    }

    Path mudlibRoot() {
        return mudlibRoot;
    }

    String startingRoomPath() {
        return startingRoomPath;
    }

    synchronized Persona attachPersona(PrintWriter out) {
        int id = nextPersonaId++;
        String objectId = "persona/" + id;
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
        runtime.clearOutputTranscript();
        out.println("Attached " + name + " in " + startingRoomPath + ".");
        return new Persona(objectId, name, actor);
    }

    synchronized Object dispatch(Persona persona, PrintWriter out, String commandLine) {
        runtime.clearOutputTranscript();
        runtime.refreshCommandActions(persona.actor());
        Object result = runtime.dispatchCommand(persona.actor(), commandLine);
        if (Integer.valueOf(0).equals(result) && isLookCommand(commandLine)) {
            Object environment = runtime.environment(persona.actor());
            if (environment != null) {
                lookAt(environment);
                result = 1;
            }
        }
        printRuntimeOutput(out);
        return result;
    }

    private void lookAt(Object object) {
        try {
            runtime.invokeObject(object, "long", new Object[] {null});
        } catch (RuntimeException e) {
            runtime.invokeObject(object, "short");
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

    record Persona(String objectId, String name, Object actor) {
    }
}
