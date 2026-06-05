package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.driver.DriverEfuns;
import io.github.protasm.jvmud.compiler.exec.LpcObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LpcRuntime;
import io.github.protasm.jvmud.compiler.exec.LpcRuntimeConfig;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class CompilerSmokeTest {
    @TempDir
    Path tempDir;

    @Test
    void pipelineCompilesBasicLanguageFeaturesToBytecode() {
        String source = """
                int counter = 2;

                int value() {
                    int local = counter + 3;
                    if (local > 4)
                        return local;
                    return 0;
                }

                mixed array_value() {
                    mixed* values = {1, 2, 3};
                    return values[1];
                }

                mixed mapping_value() {
                    mapping values = ([ "answer": 42 ]);
                    return values["answer"];
                }
                """;

        CompilationResult result = new CompilationPipeline("java/lang/Object").run(source);

        assertTrue(result.getProblems().isEmpty(), () -> result.getProblems().toString());
        assertNotNull(result.getBytecode());
        assertTrue(result.getBytecode().length > 0);
    }

    @Test
    void runtimeLoadsInstantiatesAndInvokesCompiledSource() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle object = runtime.loadSource("smoke/object.c", """
                int counter = 7;

                int value() {
                    return counter + 5;
                }

                string describe() {
                    return "value=" + value();
                }
                """);

        assertEquals(12, object.invoke("value"));
        assertEquals("value=12", object.invoke("describe"));
    }

    @Test
    void runtimeResolvesInheritedSourceFiles() throws Exception {
        Files.writeString(tempDir.resolve("base.c"), """
                int base_value() {
                    return 30;
                }
                """);
        Files.writeString(tempDir.resolve("child.c"), """
                inherit "base.c";

                int child_value() {
                    return base_value() + 12;
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle child = runtime.load(tempDir.resolve("child.c"));

        assertEquals(42, child.invoke("child_value"));
    }

    @Test
    void runtimeDispatchesCoreDriverEfunsWithCurrentObjectContext() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        DriverEfuns.registerCore(runtime);

        LpcObjectHandle object = runtime.loadSource("efun/caller.c", """
                int value() {
                    return 42;
                }

                mixed reflected_value() {
                    return call_other(this_object(), "value", 0);
                }
                """);

        assertEquals(42, object.invoke("reflected_value"));
    }

    @Test
    void writeEfunCapturesOutputForCliAndTests() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        DriverEfuns.registerCore(runtime);

        LpcObjectHandle object = runtime.loadSource("efun/writer.c", """
                void describe() {
                    write("hello ");
                    write("mud");
                }
                """);

        object.invoke("describe");

        assertEquals("hello mud", runtime.outputTranscript());
    }

    @Test
    void runtimeMovesObjectsThroughEnvironmentAndInventory() throws Exception {
        Files.writeString(tempDir.resolve("thing.c"), """
                int id(str) {
                    if (str == "thing")
                        return 1;
                    return 0;
                }

                string short() {
                    return "a small thing";
                }
                """);
        Files.writeString(tempDir.resolve("room.c"), """
                string short() {
                    return "test room";
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        DriverEfuns.registerCore(runtime);

        LpcObjectHandle room = runtime.load(tempDir.resolve("room.c"));
        Object thing = runtime.cloneObject("thing");

        runtime.moveObject(thing, room.instance());

        assertEquals(room.instance(), runtime.environment(thing));
        assertEquals(thing, runtime.firstInventory(room.instance()));
        assertEquals(thing, runtime.present("thing", room.instance()));
        assertEquals(null, runtime.nextInventory(thing));
    }

    @Test
    void runtimeRejectsContainmentCycles() throws Exception {
        Files.writeString(tempDir.resolve("thing.c"), """
                string short() {
                    return "thing";
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        DriverEfuns.registerCore(runtime);

        Object parent = runtime.cloneObject("thing");
        Object child = runtime.cloneObject("thing");

        runtime.moveObject(child, parent);

        assertThrows(IllegalArgumentException.class, () -> runtime.moveObject(parent, child));
        assertThrows(IllegalArgumentException.class, () -> runtime.moveObject(parent, parent));
        assertEquals(parent, runtime.environment(child));
        assertEquals(null, runtime.environment(parent));
    }

    @Test
    void lpcCodeCanCloneMoveInspectAndCallObjectsThroughDriverEfuns() throws Exception {
        Files.writeString(tempDir.resolve("thing.c"), """
                status id(str) {
                    return str == "thing";
                }

                string short() {
                    return "a small thing";
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        DriverEfuns.registerCore(runtime);
        LpcObjectHandle controller = runtime.loadSource("controller.c", """
                void setup() {
                    object thing;
                    thing = clone_object("thing");
                    move_object(thing, this_object());
                    write(call_other(first_inventory(this_object()), "short", 0));
                }
                """);

        controller.invoke("setup");

        assertEquals("a small thing", runtime.outputTranscript());
        assertEquals(controller.instance(), runtime.environment(runtime.firstInventory(controller.instance())));
    }
}
