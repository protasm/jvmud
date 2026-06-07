package io.github.protasm.jvmud.server;

import io.github.protasm.jvmud.runtime.WorldRuntime;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import java.util.List;

public record MudlibBootResult(
        WorldRuntime worldRuntime,
        MudlibBoundary mudlibBoundary,
        List<String> preloadedObjects,
        List<String> skippedPreloads,
        String startingRoom,
        String actorHandle,
        Object actor) {

    public MudlibBootResult {
        preloadedObjects = List.copyOf(preloadedObjects);
        skippedPreloads = List.copyOf(skippedPreloads);
    }
}
