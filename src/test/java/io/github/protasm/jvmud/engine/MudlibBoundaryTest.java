package io.github.protasm.jvmud.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundaryConfigReader;
import io.github.protasm.jvmud.engine.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.engine.world.World;
import io.github.protasm.jvmud.engine.world.WorldRuntime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
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
        assertEquals(80, boundary.maxLineLength());
        assertFalse(boundary.showRuler());
    }

    @Test
    void boundaryNormalizesMudlibObjectPaths() {
        MudlibBoundary boundary = MudlibBoundary.builder()
                .boundaryObjectPath("/jvmud/mudlib.c")
                .mfunObjectPath(" /jvmud/functions.c ")
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "reset")
                .handle(MudlibLifecycleEvent.SCHEDULED_TICK)
                .build();

        assertTrue(boundary.declared());
        assertEquals("jvmud/mudlib", boundary.boundaryObjectPath().orElseThrow());
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
        Path config = tempDir.resolve("jvmud").resolve("lp245.config");
        Files.createDirectories(config.getParent());
        Files.writeString(config, """
                game_id = vanilla-lpmud-245
                game_name = Vanilla LPMUD 2.4.5
                mudlib_root = ..
                mudlib_object = /jvmud/mudlib.c
                mfun_object = /jvmud/mfuns.c
                player_object = /obj/player.c
                player_prompt = "> "
                max_line_length = 40
                show_ruler = true
                initial_place = room/village/vill_green
                preload_file = init_file
                preload_objects = obj/torch, obj/money.c
                database.url = jdbc:mysql://localhost:3306/RealmsLib
                database.user = realmslib
                database.password = realmsdev
                handled_lifecycle_events = scheduled-tick
                lifecycle.object_loaded = reset
                lifecycle.object_source_missing = compile_object
                lifecycle.object_destruction_requested = prepare_destruct
                lifecycle.interaction_scope_started = init
                lifecycle.player_connected = player_connected
                lifecycle.player_session_post_rebind = player_rebound
                lifecycle.player-bound = player_bound
                lifecycle.log_error = log_error
                lifecycle.runtime_error = runtime_error
                lifecycle.scheduled_tick_error = heart_beat_error
                lifecycle.server_shutdown = notify_shutdown
                engine_function.sizeof = jvmud_size
                ldmud_compat_predefine.__VERSION__ = "JVMud LDMud compatibility"
                ldmud_compat_predefine.__VERSION_MAJOR__ = 3
                ldmud_compat_function_predefine.PROBE.text_width = 0
                temporal_tick_method = heart_beat
                temporal_tick_interval = 0.25
                """);

        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(tempDir, "jvmud/lp245.config");

        assertEquals("jvmud/mudlib", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("vanilla-lpmud-245", boundary.gameId().orElseThrow());
        assertEquals("Vanilla LPMUD 2.4.5", boundary.gameName().orElseThrow());
        assertEquals(tempDir.toAbsolutePath().normalize(), boundary.mudlibRootPath().orElseThrow());
        assertEquals("jvmud/mfuns", boundary.mfunObjectPath().orElseThrow());
        assertEquals("obj/player", boundary.playerObjectPath().orElseThrow());
        assertEquals("> ", boundary.playerPrompt().orElseThrow());
        assertEquals(40, boundary.maxLineLength());
        assertTrue(boundary.showRuler());
        assertEquals("room/village/vill_green", boundary.initialPlacePath().orElseThrow());
        assertEquals("init_file", boundary.preloadFilePath().orElseThrow());
        assertTrue(boundary.preloadObjectPaths().contains("obj/torch"));
        assertTrue(boundary.preloadObjectPaths().contains("obj/money"));
        assertEquals("jdbc:mysql://localhost:3306/RealmsLib", boundary.databaseJdbcUrl().orElseThrow());
        assertEquals("realmslib", boundary.databaseUser().orElseThrow());
        assertEquals("realmsdev", boundary.databasePassword().orElseThrow());
        assertTrue(boundary.handles(MudlibLifecycleEvent.SCHEDULED_TICK));
        assertEquals("reset", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED).orElseThrow());
        assertEquals(
                "compile_object",
                boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_SOURCE_MISSING).orElseThrow());
        assertEquals(
                "prepare_destruct",
                boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_DESTRUCTION_REQUESTED).orElseThrow());
        assertEquals("init", boundary.lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED).orElseThrow());
        assertEquals("player_connected", boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_CONNECTED).orElseThrow());
        assertEquals("player_rebound", boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_POST_REBIND).orElseThrow());
        assertEquals("player_bound", boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_OBJECT_BOUND).orElseThrow());
        assertEquals("log_error", boundary.lifecycleMethod(MudlibLifecycleEvent.LOG_ERROR).orElseThrow());
        assertEquals("runtime_error", boundary.lifecycleMethod(MudlibLifecycleEvent.RUNTIME_ERROR).orElseThrow());
        assertEquals("heart_beat_error", boundary.lifecycleMethod(MudlibLifecycleEvent.SCHEDULED_TICK_ERROR).orElseThrow());
        assertEquals("notify_shutdown", boundary.lifecycleMethod(MudlibLifecycleEvent.SERVER_SHUTDOWN).orElseThrow());
        assertEquals("jvmud_size", boundary.engineFunction("sizeof").orElseThrow());
        assertEquals(Map.of("sizeof", "jvmud_size"), boundary.engineFunctionAliases());
        assertEquals(
                Map.of("__VERSION__", "\"JVMud LDMud compatibility\"", "__VERSION_MAJOR__", "3"),
                boundary.compatibilityPredefines());
        assertEquals(Map.of("PROBE", Map.of("text_width", "0")), boundary.compatibilityFunctionPredefines());
        assertEquals("heart_beat", boundary.temporalTickMethod().orElseThrow());
        assertEquals(Duration.ofMillis(250), boundary.temporalTickInterval());
        assertEquals(0, boundary.temporalTickIntervalSeconds());
    }

    @Test
    void configReaderDetectsSiblingJvmudCompatibilityGlobalObject() throws IOException {
        Path config = tempDir.resolve("jvmud").resolve("realmsmud.config");
        Files.createDirectories(config.getParent());
        Path compatibilityObject = config.getParent().resolve("jvmud.c");
        Files.writeString(compatibilityObject, "mixed helper() { return 1; }\n");
        Files.writeString(config, """
                mudlib_root = ../source
                mudlib_global_object = secure/simul_efun
                """);

        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(tempDir, "jvmud/realmsmud.config");

        assertEquals("secure/simul_efun", boundary.mudlibGlobalObjectPath().orElseThrow());
        assertEquals("jvmud/jvmud", boundary.compatibilityGlobalObjectPath().orElseThrow());
        assertEquals(
                compatibilityObject.toAbsolutePath().normalize(),
                boundary.compatibilityGlobalObjectSourcePath().orElseThrow());
    }

    @Test
    void compatibilityPredefinesAreImmutable() {
        MudlibBoundary boundary = MudlibBoundary.builder()
                .compatibilityPredefine("__VERSION_MAJOR__", "3")
                .build();

        assertTrue(boundary.declared());
        assertThrows(
                UnsupportedOperationException.class,
                () -> boundary.compatibilityPredefines().put("__VERSION_MINOR__", "6"));
    }

    @Test
    void compatibilityFunctionPredefinesAreImmutable() {
        MudlibBoundary boundary = MudlibBoundary.builder()
                .compatibilityFunctionPredefine("PROBE", "feature", "1")
                .build();

        assertTrue(boundary.declared());
        assertThrows(
                UnsupportedOperationException.class,
                () -> boundary.compatibilityFunctionPredefines().put("OTHER", Map.of()));
        assertThrows(
                UnsupportedOperationException.class,
                () -> boundary.compatibilityFunctionPredefines().get("PROBE").put("other", "0"));
    }

    @Test
    void configReaderAcceptsNativePersonaObjectSpelling() throws IOException {
        Path config = tempDir.resolve("jvmud").resolve("lpmuseum.config");
        Files.createDirectories(config.getParent());
        Files.writeString(config, """
                game_id = lpmuseum
                mudlib_root = ../source
                persona_object = persona/visitor
                initial_place = place/concourse
                """);

        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(tempDir, "jvmud/lpmuseum.config");

        assertEquals("persona/visitor", boundary.playerObjectPath().orElseThrow());
        assertEquals("place/concourse", boundary.initialPlacePath().orElseThrow());
    }

    @Test
    void rejectsMaxLineLengthsOutsideSupportedRange() {
        assertThrows(IllegalArgumentException.class, () -> MudlibBoundary.builder()
                .maxLineLength(19)
                .build());
        assertThrows(IllegalArgumentException.class, () -> MudlibBoundary.builder()
                .maxLineLength(141)
                .build());
    }

    @Test
    void configReaderParsesBooleanAliasesForRuler() throws IOException {
        Path config = tempDir.resolve("jvmud").resolve("lpmuseum.config");
        Files.createDirectories(config.getParent());
        Files.writeString(config, """
                mudlib_root = ..
                max_line_length = 140
                show_ruler = yes
                """);

        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(tempDir, "jvmud/lpmuseum.config");

        assertEquals(140, boundary.maxLineLength());
        assertTrue(boundary.showRuler());
    }
}
