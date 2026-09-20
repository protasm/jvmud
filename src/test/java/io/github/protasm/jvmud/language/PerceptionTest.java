package io.github.protasm.jvmud.language;

import static org.junit.jupiter.api.Assertions.*;
import io.github.protasm.jvmud.language.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.language.exec.*;
import io.github.protasm.jvmud.execution.model.mudlib.*;
import io.github.protasm.jvmud.execution.model.time.WorldScheduler;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PerceptionTest {
    @TempDir Path temp;

    @Test void nativeDoorAndPlaceObserveStructuredSpeechWithoutPlayerSessions() {
        LPCRuntime r = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(temp).build());
        CoreEfuns.registerCore(r);
        r.registerMudlibBoundary(MudlibBoundary.builder()
                .lifecycleMethod(MudlibLifecycleEvent.PERCEPTION_RECEIVED, "observe").build());
        String observer = """
                int count;
                int opened;
                mixed actor;
                void observe(mapping event) {
                    count++;
                    actor = event["actor"];
                    if (event["kind"] == "speech" && event["content"] == "mellon") opened = 1;
                }
                int observations() { return count; }
                int is_open() { return opened; }
                mixed speaker() { return actor; }
                """;
        var place = r.loadSource("test/place.c", observer);
        var door = r.loadSource("test/door.c", observer);
        var actor = r.loadSource("test/actor.c", """
                void speak() { jvmud_emit_world_event(jvmud_current_lpc_object(), "speech", "mellon", "Friend!\\n"); }
                void private_output(mixed target) { jvmud_write_to_lpc_object(target, "private"); }
                void direct(mixed target) { jvmud_deliver_world_event(target, "speech", "mellon", "direct\\n"); }
                """);
        r.moveObject(actor.instance(), place.instance());
        r.moveObject(door.instance(), place.instance());
        StringBuilder output = new StringBuilder();
        r.bindSession("door", door.instance(), "local", output::append);
        r.withCommandActor(actor.instance(), () -> actor.invoke("speak"));
        assertEquals(1, door.invoke("is_open"));
        assertEquals(1, place.invoke("is_open"));
        assertSame(actor.instance(), door.invoke("speaker"));
        assertEquals("Friend!\n", output.toString());
        actor.invoke("private_output", door.instance());
        assertEquals(1, door.invoke("observations"));
        actor.invoke("direct", door.instance());
        assertEquals(2, door.invoke("observations"));
        assertEquals(1, place.invoke("observations"));
    }

    @Test void exclusionsNestedResponsesAndDestructionRespectTheDeliverySnapshot() {
        LPCRuntime r = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(temp).build());
        CoreEfuns.registerCore(r);
        r.registerMudlibBoundary(MudlibBoundary.builder()
                .lifecycleMethod(MudlibLifecycleEvent.PERCEPTION_RECEIVED, "observe").build());
        var place = r.loadSource("test/room.c", "int noop() { return 0; }");
        var source = r.loadSource("test/source.c", """
                int count;
                void observe(mapping event) { count++; }
                int observations() { return count; }
                void emit(mixed excluded) { jvmud_emit_perceivable_except(jvmud_current_lpc_object(), "hello", excluded); }
                """);
        var removed = r.loadSource("test/removed.c", """
                void observe(mapping event) { jvmud_write("SHOULD NOT RUN"); }
                """);
        var responder = r.loadSource("test/responder.c", """
                mixed victim;
                int count;
                void setup(mixed ob) { victim = ob; }
                int observations() { return count; }
                void observe(mapping event) {
                    count++;
                    if (event["text"] == "hello") {
                        jvmud_destroy_lpc_object(victim);
                        jvmud_emit_world_event(jvmud_current_lpc_object(), "reply", "yes", "yes");
                    }
                }
                """);
        // Containment insertion order places the responder before its soon-destroyed neighbor.
        r.moveObject(source.instance(), place.instance());
        r.moveObject(responder.instance(), place.instance());
        r.moveObject(removed.instance(), place.instance());
        responder.invoke("setup", removed.instance());
        source.invoke("emit", responder.instance());
        assertEquals(0, responder.invoke("observations"));
        r.clearOutputTranscript();
        source.invoke("emit", 0);
        assertEquals(1, responder.invoke("observations"));
        assertEquals(1, source.invoke("observations"));
        assertFalse(r.outputTranscript().contains("SHOULD NOT RUN"));
    }

    @Test void cyclicCallbacksAreBoundedAndDeliveryRecoversAfterFailure() {
        LPCRuntime r = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(temp).build());
        CoreEfuns.registerCore(r);
        r.registerMudlibBoundary(MudlibBoundary.builder()
                .lifecycleMethod(MudlibLifecycleEvent.PERCEPTION_RECEIVED, "observe").build());
        var loop = r.loadSource("test/loop.c", """
                int stopped;
                int count;
                void stop() { stopped = 1; }
                int observations() { return count; }
                void send() { jvmud_deliver_world_event(jvmud_current_lpc_object(), "text", "loop", "loop"); }
                void observe(mapping event) { count++; if (!stopped) send(); }
                """);
        assertThrows(RuntimeException.class, () -> loop.invoke("send"));
        assertEquals(64, loop.invoke("observations"));
        loop.invoke("stop");
        assertDoesNotThrow(() -> loop.invoke("send"));
        assertEquals(65, loop.invoke("observations"));
    }

    @Test void originalGoPuzzleReceivesSpeechAndAwardsExperience() throws Exception {
        Path root = Path.of("mudlibs/lp245").toAbsolutePath();
        LPCRuntime r = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
        CoreEfuns.registerCore(r);
        r.registerMudlibBoundary(MudlibBoundaryConfigReader.read(root, "jvmud/lp245.config"));
        WorldScheduler scheduler = new WorldScheduler();
        r.setScheduler(scheduler);
        var actor = r.loadSource("test/solver.c", """
                int xp;
                string quest;
                void set_quest(string name) { quest = name; }
                string solved() { return quest; }
                void give_sword() { say("Solver gives short sword to Leo.\\n"); }
                string query_name() { return "Solver"; }
                string query_real_name() { return "solver"; }
                int query_npc() { return 0; }
                void start() { jvmud_enable_commands(); set_living_name("solver"); }
                void add_exp(int n) { xp += n; }
                int experience() { return xp; }
                void speak(string move) { say("Solver says: play " + move + "\\n"); }
                """);
        actor.invoke("start");
        var pub = r.load("room/pub2");
        StringBuilder out = new StringBuilder();
        r.bindSession("solver", actor.instance(), "local", out::append);
        r.moveObject(actor.instance(), pub.instance());
        r.withCommandActor(actor.instance(), () -> actor.invoke("speak", "a5"));
        scheduler.advanceBy(4);
        assertEquals(0, actor.invoke("experience"));
        r.withCommandActor(actor.instance(), () -> actor.invoke("speak", "b1"));
        scheduler.advanceBy(4);
        assertEquals(50, actor.invoke("experience"), out.toString());
        assertTrue(out.toString().contains("Right !"), out.toString());
        r.withCommandActor(actor.instance(), () -> pub.invoke("show_problem"));
        assertTrue(out.toString().contains("7|......."), out.toString());
        var leo = r.load("obj/leo");
        var sword = r.loadSource("test/quest_sword.c", """
                int id(string name) { return name == "short sword" || name == "orc slayer"; }
                """);
        r.moveObject(leo.instance(), pub.instance());
        r.moveObject(sword.instance(), leo.instance());
        r.withCommandActor(actor.instance(), () -> actor.invoke("give_sword"));
        scheduler.advanceBy(4);
        assertEquals("orc_slayer", actor.invoke("solved"));
        assertTrue(out.toString().contains("fullfilled this quest"), out.toString());

    }
}
