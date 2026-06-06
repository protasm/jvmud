package io.github.protasm.jvmud.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MudlibBoundaryTest {
    @TempDir
    Path tempDir;

    @Test
    void emptyBoundaryDeclaresNoCompatibilityObjectsOrLifecycleEvents() {
        MudlibBoundary boundary = MudlibBoundary.empty();

        assertFalse(boundary.declared());
        assertTrue(boundary.boundaryObjectPath().isEmpty());
        assertTrue(boundary.mfunObjectPath().isEmpty());
        assertTrue(boundary.lifecycleEvents().isEmpty());
    }

    @Test
    void boundaryNormalizesMudlibObjectPaths() {
        MudlibBoundary boundary = MudlibBoundary.builder()
                .boundaryObjectPath("/jvmud/boundary.c")
                .mfunObjectPath(" /jvmud/functions.c ")
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "reset")
                .handle(MudlibLifecycleEvent.SCHEDULED_TICK)
                .build();

        assertTrue(boundary.declared());
        assertEquals("jvmud/boundary", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("jvmud/functions", boundary.mfunObjectPath().orElseThrow());
        assertTrue(boundary.handles(MudlibLifecycleEvent.OBJECT_LOADED));
        assertEquals("reset", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED).orElseThrow());
        assertTrue(boundary.handles(MudlibLifecycleEvent.SCHEDULED_TICK));
        assertFalse(boundary.handles(MudlibLifecycleEvent.OBJECT_DESTROYED));
    }

    @Test
    void rejectsBlankMudlibObjectPaths() {
        assertThrows(IllegalArgumentException.class, () -> MudlibBoundary.builder()
                .boundaryObjectPath(" / ")
                .build());
        assertThrows(IllegalArgumentException.class, () -> MudlibBoundary.builder()
                .mfunObjectPath(" ")
                .build());
    }

    @Test
    void lifecycleEventsAreImmutable() {
        MudlibBoundary boundary = MudlibBoundary.builder()
                .handle(MudlibLifecycleEvent.DEFERRED_CALLBACK)
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                boundary.lifecycleEvents().add(MudlibLifecycleEvent.OBJECT_DESTROYED));
    }

    @Test
    void lifecycleMethodsAreImmutable() {
        MudlibBoundary boundary = MudlibBoundary.builder()
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                boundary.lifecycleMethods().put(MudlibLifecycleEvent.OBJECT_DESTROYED, "destruct"));
    }

    @Test
    void worldRuntimeStoresRegisteredMudlibBoundary() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        MudlibBoundary boundary = MudlibBoundary.builder()
                .mfunObjectPath("jvmud/functions")
                .build();

        runtime.registerMudlibBoundary(boundary);

        assertEquals(boundary, runtime.mudlibBoundary());
    }

    @Test
    void configReaderBuildsBoundaryDeclaration() throws IOException {
        Path config = tempDir.resolve("jvmud").resolve("config");
        Files.createDirectories(config.getParent());
        Files.writeString(config, """
                game_id = vanilla-lpmud-245
                game_name = Vanilla LPMUD 2.4.5
                mfun_object = /jvmud/mfuns.c
                initial_place = room/village/vill_green
                initial_presence_id = local/player
                preload_file = room/init_file
                preload_objects = obj/torch, obj/money.c
                handled_lifecycle_events = scheduled-tick
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                temporal_tick_method = heart_beat
                temporal_tick_interval = 2
                """);

        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(tempDir, "jvmud/config");

        assertEquals("jvmud/config", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("vanilla-lpmud-245", boundary.gameId().orElseThrow());
        assertEquals("Vanilla LPMUD 2.4.5", boundary.gameName().orElseThrow());
        assertEquals("jvmud/mfuns", boundary.mfunObjectPath().orElseThrow());
        assertEquals("room/village/vill_green", boundary.initialPlacePath().orElseThrow());
        assertEquals("local/player", boundary.initialPresenceId().orElseThrow());
        assertEquals("room/init_file", boundary.preloadFilePath().orElseThrow());
        assertTrue(boundary.preloadObjectPaths().contains("obj/torch"));
        assertTrue(boundary.preloadObjectPaths().contains("obj/money"));
        assertTrue(boundary.handles(MudlibLifecycleEvent.SCHEDULED_TICK));
        assertEquals("reset", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED).orElseThrow());
        assertEquals("init", boundary.lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED).orElseThrow());
        assertEquals("heart_beat", boundary.temporalTickMethod().orElseThrow());
        assertEquals(2, boundary.temporalTickIntervalSeconds());
    }
}
