package io.github.protasm.jvmud.engine.instance;

import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.model.world.WorldRuntime;

import java.util.List;

/** Boot metadata and the live world assembled for one mudlib; world access belongs to its execution thread. */
public record MudlibBootResult(
        WorldRuntime worldRuntime,
        MudlibConfiguration configuration,
        List<String> preloadedObjects,
        List<String> skippedPreloads,
        List<String> preloadManifestPreloadedObjects,
        List<String> preloadManifestSkippedPreloads,
        String initialPlacePath) {

    public MudlibBootResult {
        preloadedObjects = List.copyOf(preloadedObjects);
        skippedPreloads = List.copyOf(skippedPreloads);
        preloadManifestPreloadedObjects = List.copyOf(preloadManifestPreloadedObjects);
        preloadManifestSkippedPreloads = List.copyOf(preloadManifestSkippedPreloads);
    }
}
