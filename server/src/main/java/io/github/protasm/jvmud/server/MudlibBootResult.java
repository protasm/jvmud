package io.github.protasm.jvmud.server;

import io.github.protasm.jvmud.engine.WorldRuntime;
import io.github.protasm.jvmud.engine.MudlibBoundary;
import java.util.List;

public record MudlibBootResult(
        WorldRuntime worldRuntime,
        MudlibBoundary mudlibBoundary,
        List<String> preloadedObjects,
        List<String> skippedPreloads,
        List<String> preloadManifestPreloadedObjects,
        List<String> preloadManifestSkippedPreloads,
        String startingRoom,
        String actorHandle,
        Object actor) {

    public MudlibBootResult {
        preloadedObjects = List.copyOf(preloadedObjects);
        skippedPreloads = List.copyOf(skippedPreloads);
        preloadManifestPreloadedObjects = List.copyOf(preloadManifestPreloadedObjects);
        preloadManifestSkippedPreloads = List.copyOf(preloadManifestSkippedPreloads);
    }
}
