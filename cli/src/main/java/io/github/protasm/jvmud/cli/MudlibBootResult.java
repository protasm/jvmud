package io.github.protasm.jvmud.cli;

import io.github.protasm.jvmud.runtime.WorldRuntime;
import java.util.List;

record MudlibBootResult(
        WorldRuntime worldRuntime,
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
