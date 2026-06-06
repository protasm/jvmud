package io.github.protasm.jvmud.cli;

import io.github.protasm.jvmud.compiler.exec.LpcLoadResult;
import io.github.protasm.jvmud.compiler.exec.LpcObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LpcRuntime;
import io.github.protasm.jvmud.runtime.Capability;
import io.github.protasm.jvmud.runtime.Entity;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibLifecycleEvent;
import io.github.protasm.jvmud.runtime.Place;
import io.github.protasm.jvmud.runtime.World;
import io.github.protasm.jvmud.runtime.WorldRuntime;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

final class MudlibBoot {
    static final String DEFAULT_BOUNDARY_OBJECT = "jvmud/boundary";
    static final String DEFAULT_STARTING_ROOM = "room/vill_green";
    static final String LOCAL_ACTOR_HANDLE = "local/player";

    private final LpcRuntime runtime;
    private final Path mudlibRoot;

    MudlibBoot(LpcRuntime runtime, Path mudlibRoot) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
    }

    MudlibBootResult boot() {
        WorldRuntime worldRuntime = new WorldRuntime(new World("jvmud", "JVMud"));
        List<String> preloadedObjects = new ArrayList<>();
        List<String> skippedPreloads = new ArrayList<>();

        discoverMudlibBoundary(worldRuntime, preloadedObjects, skippedPreloads);
        preloadInitFile(preloadedObjects, skippedPreloads);

        Object actor = null;
        String startingRoom = null;
        if (mudlibFileExists(DEFAULT_STARTING_ROOM)) {
            try {
                Object room = runtime.loadOrGetObject(DEFAULT_STARTING_ROOM);
                Place startingPlace = worldRuntime.createPlace(DEFAULT_STARTING_ROOM, DEFAULT_STARTING_ROOM);
                Entity actorEntity = worldRuntime.createEntity(
                        LOCAL_ACTOR_HANDLE,
                        "local player",
                        startingPlace,
                        Capability.ACTOR,
                        Capability.PERCEPTIVE);
                LocalSessionActor localActor =
                        new LocalSessionActor(runtime, worldRuntime, actorEntity, "local player");
                runtime.registerHostObject(LOCAL_ACTOR_HANDLE, localActor);
                runtime.moveObject(localActor, room);
                actor = localActor;
                startingRoom = DEFAULT_STARTING_ROOM;
            } catch (RuntimeException e) {
                skippedPreloads.add(DEFAULT_STARTING_ROOM);
            }
        }

        return new MudlibBootResult(
                worldRuntime,
                preloadedObjects,
                skippedPreloads,
                startingRoom,
                actor == null ? null : LOCAL_ACTOR_HANDLE,
                actor);
    }

    private void discoverMudlibBoundary(
            WorldRuntime worldRuntime,
            List<String> preloadedObjects,
            List<String> skippedPreloads) {
        if (!mudlibFileExists(DEFAULT_BOUNDARY_OBJECT)) {
            registerBoundary(worldRuntime, MudlibBoundary.empty());
            return;
        }

        try {
            LpcObjectHandle handle = runtime.load(DEFAULT_BOUNDARY_OBJECT);
            MudlibBoundary boundary = readBoundaryDeclaration(handle.instance());
            registerBoundary(worldRuntime, boundary);
            preloadedObjects.add(handle.internalName());
        } catch (RuntimeException e) {
            registerBoundary(worldRuntime, MudlibBoundary.empty());
            skippedPreloads.add(DEFAULT_BOUNDARY_OBJECT);
        }
    }

    private MudlibBoundary readBoundaryDeclaration(Object boundaryObject) {
        MudlibBoundary.Builder builder = MudlibBoundary.builder()
                .boundaryObjectPath(DEFAULT_BOUNDARY_OBJECT);

        Object mfunObject = invokeNoArgIfPresent(boundaryObject, "mfun_object");
        if (mfunObject instanceof String path && !path.isBlank()) {
            builder.mfunObjectPath(path);
        }

        Object lifecycleEvents = invokeNoArgIfPresent(boundaryObject, "handled_lifecycle_events");
        addLifecycleEvents(builder, lifecycleEvents);

        return builder.build();
    }

    private Object invokeNoArgIfPresent(Object object, String methodName) {
        if (!hasPublicNoArgMethod(object, methodName)) {
            return null;
        }
        return runtime.invokeObject(object, methodName);
    }

    private boolean hasPublicNoArgMethod(Object object, String methodName) {
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                return true;
            }
        }
        return false;
    }

    private void addLifecycleEvents(MudlibBoundary.Builder builder, Object declaredEvents) {
        if (declaredEvents instanceof Collection<?> events) {
            for (Object event : events) {
                addLifecycleEvent(builder, event);
            }
            return;
        }
        addLifecycleEvent(builder, declaredEvents);
    }

    private void addLifecycleEvent(MudlibBoundary.Builder builder, Object declaredEvent) {
        if (!(declaredEvent instanceof String name) || name.isBlank()) {
            return;
        }
        builder.handle(MudlibLifecycleEvent.valueOf(normalizeLifecycleEventName(name)));
    }

    private String normalizeLifecycleEventName(String name) {
        return name.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();
    }

    private void registerBoundary(WorldRuntime worldRuntime, MudlibBoundary boundary) {
        worldRuntime.registerMudlibBoundary(boundary);
        runtime.registerMudlibBoundary(boundary);
    }

    private void preloadInitFile(List<String> preloadedObjects, List<String> skippedPreloads) {
        Path initFile = mudlibRoot.resolve("room/init_file");
        if (!Files.isRegularFile(initFile)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(initFile)) {
                String sourcePath = initFileEntry(line);
                if (sourcePath == null) {
                    continue;
                }

                LpcLoadResult result = runtime.tryLoad(sourcePath);
                if (result.succeeded()) {
                    LpcObjectHandle handle = result.handle().orElseThrow();
                    preloadedObjects.add(handle.internalName());
                } else {
                    skippedPreloads.add(sourcePath);
                }
            }
        } catch (IOException e) {
            skippedPreloads.add("room/init_file");
        }
    }

    private String initFileEntry(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null;
        }
        return stripExtension(trimmed);
    }

    private boolean mudlibFileExists(String sourcePath) {
        return Files.isRegularFile(mudlibRoot.resolve(sourcePath + ".c"))
                || Files.isRegularFile(mudlibRoot.resolve(sourcePath));
    }

    private String stripExtension(String value) {
        return value.endsWith(".c") ? value.substring(0, value.length() - 2) : value;
    }
}
