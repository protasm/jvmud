package io.github.protasm.jvmud.cli;

import io.github.protasm.jvmud.runtime.WorldRuntime;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import java.util.List;

record MudlibBootResult(
        WorldRuntime worldRuntime,
        MudlibBoundary mudlibBoundary,
        List<String> preloadedObjects,
        List<String> skippedPreloads,
        String startingRoom,
        String actorHandle,
        Object actor) {

    MudlibBootResult {
        preloadedObjects = List.copyOf(preloadedObjects);
        skippedPreloads = List.copyOf(skippedPreloads);
    }
}
