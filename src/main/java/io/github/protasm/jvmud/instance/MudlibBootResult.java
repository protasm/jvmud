package io.github.protasm.jvmud.instance;

import io.github.protasm.jvmud.engine.world.WorldRuntime;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import java.util.List;

/** Immutable result of booting one explicitly configured mudlib into a native world runtime. */
public record MudlibBootResult(
        WorldRuntime worldRuntime,
        MudlibBoundary mudlibBoundary,
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
