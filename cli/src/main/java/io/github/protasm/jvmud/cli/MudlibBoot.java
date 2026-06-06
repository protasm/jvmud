package io.github.protasm.jvmud.cli;

import io.github.protasm.jvmud.compiler.exec.LpcLoadResult;
import io.github.protasm.jvmud.compiler.exec.LpcObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LpcRuntime;
import io.github.protasm.jvmud.runtime.Capability;
import io.github.protasm.jvmud.runtime.Entity;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibBoundaryConfigReader;
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
    static final String DEFAULT_CONFIG_PATH = "jvmud/config";
    static final String DEFAULT_BOUNDARY_OBJECT = "jvmud/boundary";
    static final String DEFAULT_STARTING_ROOM = "room/village/vill_green";
    static final String LOCAL_ACTOR_HANDLE = "local/player";

    private final LpcRuntime runtime;
    private final Path mudlibRoot;
    private final String configPath;

    MudlibBoot(LpcRuntime runtime, Path mudlibRoot) {
        this(runtime, mudlibRoot, DEFAULT_CONFIG_PATH);
    }

    MudlibBoot(LpcRuntime runtime, Path mudlibRoot, String configPath) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        this.configPath = Objects.requireNonNullElse(configPath, DEFAULT_CONFIG_PATH);
    }

    MudlibBootResult boot() {
        WorldRuntime worldRuntime = new WorldRuntime(new World("jvmud", "JVMud"));
        List<String> preloadedObjects = new ArrayList<>();
        List<String> skippedPreloads = new ArrayList<>();

        MudlibBoundary boundary = discoverMudlibBoundary(worldRuntime, preloadedObjects, skippedPreloads);
        preloadConfiguredObjects(boundary, preloadedObjects, skippedPreloads);
        preloadInitFile(boundary, preloadedObjects, skippedPreloads);

        Object actor = null;
        String initialPlace = null;
        String initialPlacePath = boundary.initialPlacePath().orElse(DEFAULT_STARTING_ROOM);
        String initialPresenceId = boundary.initialPresenceId().orElse(LOCAL_ACTOR_HANDLE);
        if (mudlibFileExists(initialPlacePath)) {
            try {
                Object placeObject = runtime.loadOrGetObject(initialPlacePath);
                Place startingPlace = worldRuntime.createPlace(initialPlacePath, initialPlacePath);
                Entity actorEntity = worldRuntime.createEntity(
                        initialPresenceId,
                        "local player",
                        startingPlace,
                        Capability.ACTOR,
                        Capability.PERCEPTIVE);
                LocalSessionActor localActor =
                        new LocalSessionActor(runtime, worldRuntime, actorEntity, "local player");
                runtime.registerHostObject(initialPresenceId, localActor);
                runtime.moveObject(localActor, placeObject);
                actor = localActor;
                initialPlace = initialPlacePath;
            } catch (RuntimeException e) {
                skippedPreloads.add(initialPlacePath);
            }
        }

        return new MudlibBootResult(
                worldRuntime,
                preloadedObjects,
                skippedPreloads,
                initialPlace,
                actor == null ? null : initialPresenceId,
                actor);
    }

    private MudlibBoundary discoverMudlibBoundary(
            WorldRuntime worldRuntime,
            List<String> preloadedObjects,
            List<String> skippedPreloads) {
        if (mudlibConfigFileExists(configPath)) {
            try {
                MudlibBoundary boundary = MudlibBoundaryConfigReader.read(mudlibRoot, configPath);
                registerBoundary(worldRuntime, boundary);
                return boundary;
            } catch (IOException | RuntimeException e) {
                registerBoundary(worldRuntime, MudlibBoundary.empty());
                skippedPreloads.add(configPath);
                return MudlibBoundary.empty();
            }
        }

        if (!mudlibFileExists(DEFAULT_BOUNDARY_OBJECT)) {
            registerBoundary(worldRuntime, MudlibBoundary.empty());
            return MudlibBoundary.empty();
        }

        try {
            LpcObjectHandle handle = runtime.load(DEFAULT_BOUNDARY_OBJECT);
            MudlibBoundary boundary = readBoundaryDeclaration(handle.instance());
            registerBoundary(worldRuntime, boundary);
            preloadedObjects.add(handle.internalName());
            return boundary;
        } catch (RuntimeException e) {
            registerBoundary(worldRuntime, MudlibBoundary.empty());
            skippedPreloads.add(DEFAULT_BOUNDARY_OBJECT);
            return MudlibBoundary.empty();
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
        builder.handle(MudlibBoundaryConfigReader.lifecycleEvent(name));
    }

    private void registerBoundary(WorldRuntime worldRuntime, MudlibBoundary boundary) {
        worldRuntime.registerMudlibBoundary(boundary);
        runtime.registerMudlibBoundary(boundary);
    }

    private void preloadConfiguredObjects(
            MudlibBoundary boundary, List<String> preloadedObjects, List<String> skippedPreloads) {
        for (String sourcePath : boundary.preloadObjectPaths()) {
            preloadObject(sourcePath, preloadedObjects, skippedPreloads);
        }
    }

    private void preloadInitFile(
            MudlibBoundary boundary, List<String> preloadedObjects, List<String> skippedPreloads) {
        String preloadFilePath = boundary.preloadFilePath().orElse("room/init_file");
        Path initFile = mudlibRoot.resolve(preloadFilePath);
        if (!Files.isRegularFile(initFile)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(initFile)) {
                String sourcePath = initFileEntry(line);
                if (sourcePath == null) {
                    continue;
                }

                preloadObject(sourcePath, preloadedObjects, skippedPreloads);
            }
        } catch (IOException e) {
            skippedPreloads.add(preloadFilePath);
        }
    }

    private void preloadObject(String sourcePath, List<String> preloadedObjects, List<String> skippedPreloads) {
        LpcLoadResult result = runtime.tryLoad(sourcePath);
        if (result.succeeded()) {
            LpcObjectHandle handle = result.handle().orElseThrow();
            preloadedObjects.add(handle.internalName());
        } else {
            skippedPreloads.add(sourcePath);
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

    private boolean mudlibConfigFileExists(String sourcePath) {
        return Files.isRegularFile(mudlibRoot.resolve(sourcePath));
    }

    private String stripExtension(String value) {
        return value.endsWith(".c") ? value.substring(0, value.length() - 2) : value;
    }
}
