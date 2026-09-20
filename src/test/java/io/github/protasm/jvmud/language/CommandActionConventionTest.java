package io.github.protasm.jvmud.language;

import static org.junit.jupiter.api.Assertions.*;
import io.github.protasm.jvmud.language.exec.*;
import io.github.protasm.jvmud.language.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.execution.model.mudlib.MudlibBoundary;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Native defaults and independently selected legacy action conventions. */
class CommandActionConventionTest {
    @TempDir Path root;

    @Test void orderingAndCatchAllArgumentsAreIndependentOptIns() {
        for (boolean newest : new boolean[] {false, true}) {
            for (boolean argsOnly : new boolean[] {false, true}) {
                var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
                CoreEfuns.registerCore(rt);
                rt.registerMudlibBoundary(MudlibBoundary.builder()
                        .commandActionsNewestFirst(newest).commandActionsArgumentsOnly(argsOnly).build());
                var actor = rt.loadSource("actor.c", """
                        string seen = "";
                        void setup() {
                            jvmud_add_action("first", "go");
                            jvmud_add_action("second", "go");
                            jvmud_add_action("watch", "", 1);
                        }
                        int first(string text) { seen += "first;"; return 1; }
                        int second(string text) { seen += "second;"; return 1; }
                        int watch(string text) {
                            seen += jvmud_current_verb() + ":" + (text ? text : "<none>") + ";";
                            return 0;
                        }
                        string recorded() { return seen; }
                        void clear() { seen = ""; }
                        """);
                rt.withCommandActor(actor.instance(), () -> actor.invoke("setup"));
                assertEquals(1, rt.dispatchCommand(actor.instance(), "go west"));
                assertEquals(newest ? "go:" + (argsOnly ? "west" : "go west") + ";second;" : "first;",
                        actor.invoke("recorded"));
                actor.invoke("clear");
                assertEquals(0, rt.dispatchCommand(actor.instance(), "unknown"));
                assertEquals("unknown:" + (argsOnly ? "<none>" : "unknown") + ";", actor.invoke("recorded"));
            }
        }
    }
}
