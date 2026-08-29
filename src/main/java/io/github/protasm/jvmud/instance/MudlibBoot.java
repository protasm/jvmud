package io.github.protasm.jvmud.instance;

import io.github.protasm.jvmud.compiler.exec.LPCLoadResult;
import io.github.protasm.jvmud.compiler.exec.LPCObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundaryConfigReader;
import io.github.protasm.jvmud.engine.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.engine.world.Place;
import io.github.protasm.jvmud.engine.world.MudlibWorldProjection;
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

/** Boots one mudlib from an explicit JVMud manifest into an engine-owned world runtime. */
public final class MudlibBoot {

    private final LPCRuntime runtime;
    private final Path mudlibRoot;
    private final String configPath;
    private final boolean loadInitialPlace;
    private final MudlibBootProgress progress;

    /** Creates a coordinator that loads the manifest's initial place. */
    public MudlibBoot(LPCRuntime runtime, Path mudlibRoot, String configPath) {
        this(runtime, mudlibRoot, configPath, true);
    }

    /** Creates a coordinator with explicit initial-place loading policy. */
    public MudlibBoot(LPCRuntime runtime, Path mudlibRoot, String configPath, boolean loadInitialPlace) {
        this(runtime, mudlibRoot, configPath, loadInitialPlace, MudlibBootProgress.none());
    }

    /** Creates a coordinator with explicit initial-place policy and startup progress reporting. */
    public MudlibBoot(
            LPCRuntime runtime,
            Path mudlibRoot,
            String configPath,
            boolean loadInitialPlace,
            MudlibBootProgress progress) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.mudlibRoot = Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        this.configPath = Objects.requireNonNull(configPath, "configPath");
        this.loadInitialPlace = loadInitialPlace;
        this.progress = Objects.requireNonNullElse(progress, MudlibBootProgress.none());
    }

    /** Boots the configured mudlib or throws when required manifest data cannot be loaded. */
    public MudlibBootResult boot() {
        List<String> preloadedObjects = new ArrayList<>();
        List<String> skippedPreloads = new ArrayList<>();
        List<String> preloadManifestPreloadedObjects = new ArrayList<>();
        List<String> preloadManifestSkippedPreloads = new ArrayList<>();

        MudlibBoundary boundary = discoverMudlibBoundary(preloadedObjects, skippedPreloads);
        String worldId = boundary.gameId().orElseGet(() -> mudlibRoot.getFileName().toString());
        String worldName = boundary.gameName().orElse(worldId);
        WorldRuntime worldRuntime = new WorldRuntime(new World(worldId, worldName));
        runtime.setWorldProjection(new MudlibWorldProjection(worldRuntime));
        registerBoundary(worldRuntime, boundary);
        runtime.setScheduler(worldRuntime.scheduler());
        preloadConfiguredObjects(boundary, preloadedObjects, skippedPreloads);
        preloadManifest(boundary, preloadedObjects, skippedPreloads, preloadManifestPreloadedObjects, preloadManifestSkippedPreloads);
        invokeServerStartedHook(boundary);

        String initialPlace = null;
        String initialPlacePath = boundary.initialPlacePath().orElse(null);
        if (loadInitialPlace && initialPlacePath != null) {
            try {
                Object initialPlaceObject = runtime.loadOrGetObject(initialPlacePath);
                Place startingPlace = worldRuntime.createPlace(initialPlacePath, initialPlacePath);
                runtime.worldProjection().bindPlace(initialPlaceObject, startingPlace);
                initialPlace = initialPlacePath;
            } catch (RuntimeException e) {
                throw new IllegalStateException("Could not load configured initial place: " + initialPlacePath, e);
            }
        }

        return new MudlibBootResult(
                worldRuntime,
                boundary,
                preloadedObjects,
                skippedPreloads,
                preloadManifestPreloadedObjects,
                preloadManifestSkippedPreloads,
                initialPlace);
    }

    private MudlibBoundary discoverMudlibBoundary(
            List<String> preloadedObjects,
            List<String> skippedPreloads) {
        if (!mudlibConfigFileExists(configPath)) {
            throw new IllegalStateException("Mudlib configuration does not exist: " + configPath);
        }
        try {
            MudlibBoundary boundary = MudlibBoundaryConfigReader.read(mudlibRoot, configPath);
            runtime.setParserOptions(ParserOptions.features(boundary.languageFeatures()));
            return readConfiguredBoundaryObject(boundary, preloadedObjects, skippedPreloads);
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Invalid mudlib configuration: " + configPath, e);
        }
    }

    private MudlibBoundary readBoundaryDeclaration(Object boundaryObject) {
        MudlibBoundary.Builder builder = MudlibBoundary.builder();

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
            throw new IllegalStateException(
                    "Could not load configured mudlib boundary object: " + boundaryObjectPath,
                    result.error().orElse(null));
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
        configBoundary.mudlibGlobalObjectPath()
                .or(() -> objectBoundary.mudlibGlobalObjectPath())
                .ifPresent(builder::mudlibGlobalObjectPath);
        configBoundary.mudlibGlobalObjectSourcePath()
                .or(() -> objectBoundary.mudlibGlobalObjectSourcePath())
                .ifPresent(builder::mudlibGlobalObjectSourcePath);
        configBoundary.compatibilityGlobalObjectPath()
                .or(() -> objectBoundary.compatibilityGlobalObjectPath())
                .ifPresent(builder::compatibilityGlobalObjectPath);
        configBoundary.compatibilityGlobalObjectSourcePath()
                .or(() -> objectBoundary.compatibilityGlobalObjectSourcePath())
                .ifPresent(builder::compatibilityGlobalObjectSourcePath);
        objectBoundary.compatibilityGlobalOverrides().forEach(builder::compatibilityGlobalOverride);
        configBoundary.compatibilityGlobalOverrides().forEach(builder::compatibilityGlobalOverride);
        configBoundary.playerObjectPath().ifPresent(builder::playerObjectPath);
        configBoundary.sessionPolicy().ifPresent(builder::sessionPolicy);
        configBoundary.playerPrompt()
                .or(() -> objectBoundary.playerPrompt())
                .ifPresent(builder::playerPrompt);
        configBoundary.connectedBanner().ifPresent(builder::connectedBanner);
        builder.transportControlPrefix(configBoundary.transportControlPrefix());
        configBoundary.locationDiagnosticCommand().ifPresent(builder::locationDiagnosticCommand);
        builder.maxLineLength(configBoundary.maxLineLength());
        builder.showRuler(configBoundary.showRuler());
        configBoundary.initialPlacePath().ifPresent(builder::initialPlacePath);
        configBoundary.preloadFilePath().ifPresent(builder::preloadFilePath);
        configBoundary.preloadObjectPaths().forEach(builder::preloadObjectPath);
        configBoundary.includePaths().forEach(builder::includePath);
        configBoundary.databaseJdbcUrl().ifPresent(builder::databaseJdbcUrl);
        configBoundary.databaseUser().ifPresent(builder::databaseUser);
        configBoundary.databasePassword().ifPresent(builder::databasePassword);
        configBoundary.languageFeatures().forEach(builder::languageFeature);
        configBoundary.engineCapabilities().forEach(builder::engineCapability);
        configBoundary.temporalTickMethod().ifPresent(builder::temporalTickMethod);
        if (!configBoundary.temporalTickInterval().isZero()) {
            builder.temporalTickInterval(configBoundary.temporalTickInterval());
        }
        objectBoundary.lifecycleEvents().forEach(builder::handle);
        configBoundary.lifecycleEvents().forEach(builder::handle);
        objectBoundary.lifecycleMethods().forEach(builder::lifecycleMethod);
        configBoundary.lifecycleMethods().forEach(builder::lifecycleMethod);
        objectBoundary.engineFunctionAliases().forEach((mudlibName, engineName) ->
                builder.engineFunction(engineName, mudlibName));
        configBoundary.engineFunctionAliases().forEach((mudlibName, engineName) ->
                builder.engineFunction(engineName, mudlibName));
        configBoundary.mountedMudlibConfigs().forEach(builder::mountedMudlib);
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
            preloadObject(
                    sourcePath,
                    MudlibBootProgress.PreloadKind.CONFIGURED_OBJECT,
                    preloadedObjects,
                    skippedPreloads);
        }
    }

    private void invokeServerStartedHook(MudlibBoundary boundary) {
        String methodName = boundary.lifecycleMethod(MudlibLifecycleEvent.SERVER_STARTED).orElse(null);
        String boundaryPath = boundary.boundaryObjectPath().orElse(null);
        if (methodName == null || boundaryPath == null) {
            return;
        }
        Object boundaryObject = runtime.loadOrGetObject(boundaryPath);
        runtime.invokeOptionalObject(boundaryObject, methodName);
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
            throw new IllegalStateException("Configured preload manifest does not exist: " + preloadFilePath);
        }

        try {
            for (String line : Files.readAllLines(preloadManifest)) {
                String sourcePath = preloadManifestEntry(line);
                if (sourcePath == null) {
                    continue;
                }

                preloadObject(
                        sourcePath,
                        MudlibBootProgress.PreloadKind.MANIFEST_OBJECT,
                        preloadedObjects,
                        skippedPreloads,
                        preloadManifestPreloadedObjects,
                        preloadManifestSkippedPreloads);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read preload manifest: " + preloadFilePath, e);
        }
    }

    private void preloadObject(
            String sourcePath,
            MudlibBootProgress.PreloadKind kind,
            List<String> preloadedObjects,
            List<String> skippedPreloads) {
        preloadObject(sourcePath, kind, preloadedObjects, skippedPreloads, null, null);
    }

    private void preloadObject(
            String sourcePath,
            MudlibBootProgress.PreloadKind kind,
            List<String> preloadedObjects,
            List<String> skippedPreloads,
            List<String> preloadManifestPreloadedObjects,
            List<String> preloadManifestSkippedPreloads) {
        progress.preloadStarted(kind, sourcePath);
        LPCLoadResult result = runtime.tryLoad(sourcePath);
        if (result.succeeded()) {
            LPCObjectHandle handle = result.handle().orElseThrow();
            preloadedObjects.add(handle.internalName());
            if (preloadManifestPreloadedObjects != null) {
                preloadManifestPreloadedObjects.add(handle.internalName());
            }
            progress.preloadFinished(kind, sourcePath, true);
        } else {
            result.error().ifPresent(error -> progress.preloadFailed(kind, sourcePath, error));
            skippedPreloads.add(sourcePath);
            if (preloadManifestSkippedPreloads != null) {
                preloadManifestSkippedPreloads.add(sourcePath);
            }
            progress.preloadFinished(kind, sourcePath, false);
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
