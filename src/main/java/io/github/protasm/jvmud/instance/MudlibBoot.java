package io.github.protasm.jvmud.instance;

import io.github.protasm.jvmud.compiler.exec.LPCLoadResult;
import io.github.protasm.jvmud.compiler.exec.LPCObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.engine.world.Capability;
import io.github.protasm.jvmud.engine.world.Entity;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundaryConfigReader;
import io.github.protasm.jvmud.engine.world.Place;
import io.github.protasm.jvmud.engine.world.World;
import io.github.protasm.jvmud.engine.world.WorldRuntime;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class MudlibBoot {
    public static final String DEFAULT_CONFIG_PATH = "jvmud/lpmuseum.config";
    public static final String LP245_CONFIG_PATH = "jvmud/lp245.config";
    static final String DEFAULT_BOUNDARY_OBJECT = "jvmud/mudlib";
    static final String DEFAULT_STARTING_ROOM = "room/village/vill_green";
    static final String LOCAL_ACTOR_HANDLE = "local/player";

    private final LPCRuntime runtime;
    private final Path mudlibRoot;
    private final String configPath;
    private final boolean createInitialActor;
    private final boolean loadInitialPlace;

    public MudlibBoot(LPCRuntime runtime, Path mudlibRoot) {
        this(runtime, mudlibRoot, DEFAULT_CONFIG_PATH);
    }

    public MudlibBoot(LPCRuntime runtime, Path mudlibRoot, String configPath) {
        this(runtime, mudlibRoot, configPath, true);
    }

    public MudlibBoot(LPCRuntime runtime, Path mudlibRoot, String configPath, boolean createInitialActor) {
        this(runtime, mudlibRoot, configPath, createInitialActor, true);
    }

    public MudlibBoot(
            LPCRuntime runtime,
            Path mudlibRoot,
            String configPath,
            boolean createInitialActor,
            boolean loadInitialPlace) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        this.configPath = Objects.requireNonNullElse(configPath, DEFAULT_CONFIG_PATH);
        this.createInitialActor = createInitialActor;
        this.loadInitialPlace = loadInitialPlace;
    }

    public MudlibBootResult boot() {
        WorldRuntime worldRuntime = new WorldRuntime(new World("jvmud", "JVMud"));
        runtime.setScheduler(worldRuntime.scheduler());
        List<String> preloadedObjects = new ArrayList<>();
        List<String> skippedPreloads = new ArrayList<>();
        List<String> preloadManifestPreloadedObjects = new ArrayList<>();
        List<String> preloadManifestSkippedPreloads = new ArrayList<>();

        MudlibBoundary boundary = discoverMudlibBoundary(worldRuntime, preloadedObjects, skippedPreloads);
        preloadConfiguredObjects(boundary, preloadedObjects, skippedPreloads);
        preloadManifest(boundary, preloadedObjects, skippedPreloads, preloadManifestPreloadedObjects, preloadManifestSkippedPreloads);

        Object actor = null;
        String initialPlace = null;
        String initialPlacePath = boundary.initialPlacePath().orElse(DEFAULT_STARTING_ROOM);
        String initialPersonaId = LOCAL_ACTOR_HANDLE;
        if (loadInitialPlace && mudlibFileExists(boundary, initialPlacePath)) {
            try {
                Object placeObject = runtime.loadOrGetObject(initialPlacePath);
                Place startingPlace = worldRuntime.createPlace(initialPlacePath, initialPlacePath);
                initialPlace = initialPlacePath;
                if (createInitialActor) {
                    Entity actorEntity = worldRuntime.createEntity(
                            initialPersonaId,
                            "local player",
                            startingPlace,
                            Capability.ACTOR,
                            Capability.PERCEPTIVE);
                    LocalSessionActor localActor =
                            new LocalSessionActor(runtime, worldRuntime, actorEntity, "local player");
                    runtime.registerHostObject(initialPersonaId, localActor);
                    runtime.moveObject(localActor, placeObject);
                    actor = localActor;
                }
            } catch (RuntimeException e) {
                skippedPreloads.add(initialPlacePath);
            }
        }

        return new MudlibBootResult(
                worldRuntime,
                boundary,
                preloadedObjects,
                skippedPreloads,
                preloadManifestPreloadedObjects,
                preloadManifestSkippedPreloads,
                initialPlace,
                actor == null ? null : initialPersonaId,
                actor);
    }

    private MudlibBoundary discoverMudlibBoundary(
            WorldRuntime worldRuntime,
            List<String> preloadedObjects,
            List<String> skippedPreloads) {
        if (mudlibConfigFileExists(configPath)) {
            try {
                MudlibBoundary boundary = MudlibBoundaryConfigReader.read(mudlibRoot, configPath);
                boundary = readConfiguredBoundaryObject(boundary, preloadedObjects, skippedPreloads);
                registerBoundary(worldRuntime, boundary);
                return boundary;
            } catch (IOException | RuntimeException e) {
                registerBoundary(worldRuntime, MudlibBoundary.empty());
                skippedPreloads.add(configPath);
                return MudlibBoundary.empty();
            }
        }

        if (!mudlibFileExists(MudlibBoundary.empty(), DEFAULT_BOUNDARY_OBJECT)) {
            registerBoundary(worldRuntime, MudlibBoundary.empty());
            return MudlibBoundary.empty();
        }

        try {
            LPCObjectHandle handle = runtime.load(DEFAULT_BOUNDARY_OBJECT);
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

        Object playerPrompt = invokeNoArgIfPresent(boundaryObject, "player_prompt");
        if (playerPrompt == null) {
            playerPrompt = invokeNoArgIfPresent(boundaryObject, "command_prompt");
        }
        if (playerPrompt instanceof String prompt && !prompt.isBlank()) {
            builder.playerPrompt(prompt);
        }

        Object lifecycleEvents = invokeNoArgIfPresent(boundaryObject, "handled_lifecycle_events");
        addLifecycleEvents(builder, lifecycleEvents);

        return builder.build();
    }

    private MudlibBoundary readConfiguredBoundaryObject(
            MudlibBoundary configBoundary,
            List<String> preloadedObjects,
            List<String> skippedPreloads) {
        if (configBoundary.boundaryObjectPath().isEmpty()) {
            return configBoundary;
        }

        String boundaryObjectPath = configBoundary.boundaryObjectPath().orElseThrow();
        LPCLoadResult result = runtime.tryLoad(boundaryObjectPath);
        if (!result.succeeded()) {
            skippedPreloads.add(boundaryObjectPath);
            return configBoundary;
        }

        LPCObjectHandle handle = result.handle().orElseThrow();
        preloadedObjects.add(handle.internalName());
        MudlibBoundary objectBoundary = readBoundaryDeclaration(handle.instance());
        return mergeBoundaryDeclarations(configBoundary, objectBoundary);
    }

    /**
     * Combines the file-backed boundary profile with declarations discovered from the mudlib object.
     *
     * <p>The config profile is the authoritative compatibility surface because it can be selected per
     * mudlib without asking mudlib source to know JVMud-native names. Object declarations still provide
     * mudlib-owned hooks and defaults. For additive maps, object declarations are applied first and
     * config declarations second so explicit profile settings win conflicts.</p>
     */
    private MudlibBoundary mergeBoundaryDeclarations(MudlibBoundary configBoundary, MudlibBoundary objectBoundary) {
        MudlibBoundary.Builder builder = MudlibBoundary.builder();

        configBoundary.gameId().ifPresent(builder::gameId);
        configBoundary.gameName().ifPresent(builder::gameName);
        configBoundary.mudlibRootPath().ifPresent(builder::mudlibRootPath);
        configBoundary.boundaryObjectPath()
                .or(() -> objectBoundary.boundaryObjectPath())
                .ifPresent(builder::boundaryObjectPath);
        configBoundary.mfunObjectPath()
                .or(() -> objectBoundary.mfunObjectPath())
                .ifPresent(builder::mfunObjectPath);
        configBoundary.playerObjectPath().ifPresent(builder::playerObjectPath);
        configBoundary.playerPrompt()
                .or(() -> objectBoundary.playerPrompt())
                .ifPresent(builder::playerPrompt);
        builder.maxLineLength(configBoundary.maxLineLength());
        builder.showRuler(configBoundary.showRuler());
        configBoundary.initialPlacePath().ifPresent(builder::initialPlacePath);
        configBoundary.preloadFilePath().ifPresent(builder::preloadFilePath);
        configBoundary.preloadObjectPaths().forEach(builder::preloadObjectPath);
        configBoundary.databaseJdbcUrl().ifPresent(builder::databaseJdbcUrl);
        configBoundary.databaseUser().ifPresent(builder::databaseUser);
        configBoundary.databasePassword().ifPresent(builder::databasePassword);
        configBoundary.temporalTickMethod().ifPresent(builder::temporalTickMethod);
        if (!configBoundary.temporalTickInterval().isZero()) {
            builder.temporalTickInterval(configBoundary.temporalTickInterval());
        }
        objectBoundary.lifecycleEvents().forEach(builder::handle);
        configBoundary.lifecycleEvents().forEach(builder::handle);
        objectBoundary.lifecycleMethods().forEach(builder::lifecycleMethod);
        configBoundary.lifecycleMethods().forEach(builder::lifecycleMethod);
        objectBoundary.directEfunAliases().forEach(builder::directEfunAlias);
        configBoundary.directEfunAliases().forEach(builder::directEfunAlias);
        objectBoundary.compatibilityPredefines().forEach(builder::compatibilityPredefine);
        configBoundary.compatibilityPredefines().forEach(builder::compatibilityPredefine);
        objectBoundary.compatibilityFunctionPredefines().forEach((macroName, replacements) ->
                replacements.forEach((argumentName, replacementText) ->
                        builder.compatibilityFunctionPredefine(macroName, argumentName, replacementText)));
        configBoundary.compatibilityFunctionPredefines().forEach((macroName, replacements) ->
                replacements.forEach((argumentName, replacementText) ->
                        builder.compatibilityFunctionPredefine(macroName, argumentName, replacementText)));

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

    private void preloadManifest(
            MudlibBoundary boundary,
            List<String> preloadedObjects,
            List<String> skippedPreloads,
            List<String> preloadManifestPreloadedObjects,
            List<String> preloadManifestSkippedPreloads) {
        if (boundary.preloadFilePath().isEmpty()) {
            return;
        }

        String preloadFilePath = boundary.preloadFilePath().orElseThrow();
        Path preloadManifest = activeMudlibRoot(boundary).resolve(preloadFilePath);
        if (!Files.isRegularFile(preloadManifest)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(preloadManifest)) {
                String sourcePath = preloadManifestEntry(line);
                if (sourcePath == null) {
                    continue;
                }

                preloadObject(sourcePath, preloadedObjects, skippedPreloads, preloadManifestPreloadedObjects, preloadManifestSkippedPreloads);
            }
        } catch (IOException e) {
            skippedPreloads.add(preloadFilePath);
            preloadManifestSkippedPreloads.add(preloadFilePath);
        }
    }

    private void preloadObject(String sourcePath, List<String> preloadedObjects, List<String> skippedPreloads) {
        preloadObject(sourcePath, preloadedObjects, skippedPreloads, null, null);
    }

    private void preloadObject(
            String sourcePath,
            List<String> preloadedObjects,
            List<String> skippedPreloads,
            List<String> preloadManifestPreloadedObjects,
            List<String> preloadManifestSkippedPreloads) {
        LPCLoadResult result = runtime.tryLoad(sourcePath);
        if (result.succeeded()) {
            LPCObjectHandle handle = result.handle().orElseThrow();
            preloadedObjects.add(handle.internalName());
            if (preloadManifestPreloadedObjects != null) {
                preloadManifestPreloadedObjects.add(handle.internalName());
            }
        } else {
            skippedPreloads.add(sourcePath);
            if (preloadManifestSkippedPreloads != null) {
                preloadManifestSkippedPreloads.add(sourcePath);
            }
        }
    }

    private String preloadManifestEntry(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null;
        }
        return stripExtension(trimmed);
    }

    private boolean mudlibFileExists(MudlibBoundary boundary, String sourcePath) {
        Path activeRoot = activeMudlibRoot(boundary);
        return Files.isRegularFile(activeRoot.resolve(sourcePath + ".c"))
                || Files.isRegularFile(activeRoot.resolve(sourcePath))
                || Files.isRegularFile(mudlibRoot.resolve(sourcePath + ".c"))
                || Files.isRegularFile(mudlibRoot.resolve(sourcePath));
    }

    private boolean mudlibConfigFileExists(String sourcePath) {
        return Files.isRegularFile(mudlibRoot.resolve(sourcePath));
    }

    private Path activeMudlibRoot(MudlibBoundary boundary) {
        return boundary.mudlibRootPath().orElse(mudlibRoot);
    }

    private String stripExtension(String value) {
        return value.endsWith(".c") ? value.substring(0, value.length() - 2) : value;
    }
}
