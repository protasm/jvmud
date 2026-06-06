package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LpcObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LpcRuntime;
import io.github.protasm.jvmud.compiler.exec.LpcRuntimeConfig;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibLifecycleEvent;
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
    void runtimeSupportsWhileLoopsAndArrayConcatAssignment() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle object = runtime.loadSource("smoke/array_loop.c", """
                mixed value() {
                    mixed* source;
                    mixed* result;
                    int index;

                    source = {1, 2, 3};
                    result = {};
                    index = 0;
                    while (index < 3) {
                        result += { source[index] };
                        index += 1;
                    }

                    return result[2];
                }
                """);

        assertEquals(3, object.invoke("value"));
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
    void untypedMethodsDefaultToMixedForLegacyMudlibCompatibility() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle object = runtime.loadSource("smoke/untyped.c", """
                reset(arg) {
                    return arg + 1;
                }

                value() {
                    return reset(41);
                }
                """);

        assertEquals(42, object.invoke("value"));
    }

    @Test
    void runtimeStoresNativeMudlibBoundaryDeclaration() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        MudlibBoundary boundary = MudlibBoundary.builder()
                .boundaryObjectPath("jvmud/boundary")
                .mfunObjectPath("jvmud/functions")
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "on_loaded")
                .build();

        runtime.registerMudlibBoundary(boundary);

        assertEquals(boundary, runtime.mudlibBoundary());
    }

    @Test
    void runtimeUsesConfiguredObjectLoadedLifecycleMethod() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "on_loaded")
                .build());

        LpcObjectHandle object = runtime.loadSource("smoke/lifecycle.c", """
                int counter = 0;

                void reset(mixed arg) {
                    counter = 100;
                }

                void on_loaded(mixed arg) {
                    counter = 42;
                }

                int value() {
                    return counter;
                }
                """);

        assertEquals(42, object.invoke("value"));
    }

    @Test
    void unresolvedFunctionCallsCanDispatchToRegisteredMfunObject() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/functions.c"), """
                mixed mudlib_sum(a, b) {
                    return a + b;
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                mixed value() {
                    return mudlib_sum(20, 22);
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/functions")
                .build());

        LpcObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals(42, caller.invoke("value"));
    }

    @Test
    void mfunCanSliceArraysForMudlibCompatibilityHelpers() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/functions.c"), """
                mixed *slice_array(mixed *arr, int from, int to) {
                    mixed *result;

                    result = {};
                    while (from <= to) {
                        result += { arr[from] };
                        from += 1;
                    }

                    return result;
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                mixed value() {
                    mixed *values;
                    mixed *sliced;

                    values = {1, 2, 3, 4};
                    sliced = slice_array(values, 1, 2);

                    return sliced[1];
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/functions")
                .build());

        LpcObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals(3, caller.invoke("value"));
    }

    @Test
    void mfunShadowsEngineFunctionWithSameNameAndArity() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/functions.c"), """
                mixed write(value) {
                    return "mfun:" + value;
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                mixed value() {
                    return write("shadowed");
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/functions")
                .build());

        LpcObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals("mfun:shadowed", caller.invoke("value"));
        assertEquals("", runtime.outputTranscript());
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
    void runtimeResolvesExtensionlessMudlibRootInherits() throws Exception {
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/room.c"), """
                int base_value() {
                    return 30;
                }
                """);
        Files.writeString(tempDir.resolve("room/vill_green.c"), """
                inherit "room/room";

                int child_value() {
                    return base_value() + 12;
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle child = runtime.load(tempDir.resolve("room/vill_green.c"));

        assertEquals(42, child.invoke("child_value"));
    }

    @Test
    void arrowInvokeOnExpressionUsesNativeDynamicDispatch() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LpcObjectHandle object = runtime.loadSource("smoke/arrow.c", """
                int value() {
                    return 42;
                }

                mixed reflected_value(target) {
                    return target->value();
                }
                """);

        assertEquals(42, object.invoke("reflected_value", object.instance()));
    }

    @Test
    void localsDefaultToZeroAcrossControlFlowBranches() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LpcObjectHandle object = runtime.loadSource("smoke/default_local.c", """
                int value(flag) {
                    int i;
                    if (flag)
                        return 7;
                    while (i < 1)
                        return i;
                    return 9;
                }
                """);

        assertEquals(0, object.invoke("value", 0));
    }

    @Test
    void primitiveArgumentsAreBoxedForUntypedMethodParameters() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LpcObjectHandle object = runtime.loadSource("smoke/untyped_arg.c", """
                mixed identity(value) {
                    return value;
                }

                mixed answer() {
                    return identity(42);
                }
                """);

        assertEquals(42, object.invoke("answer"));
    }

    @Test
    void pipelineAcceptsUntypedObjectMethodsAsMixed() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                value() {
                    return 42;
                }
                """);

        assertTrue(result.getProblems().isEmpty(), () -> result.getProblems().toString());
        assertNotNull(result.getBytecode());
    }

    @Test
    void runtimeDispatchesCoreEngineFunctionsWithCurrentObjectContext() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);

        LpcObjectHandle object = runtime.loadSource("engine_function/caller.c", """
                int value() {
                    return 42;
                }

                mixed reflected_value() {
                    return jvmud_invoke_object(jvmud_current_object(), "value", 0);
                }
                """);

        assertEquals(42, object.invoke("reflected_value"));
    }

    @Test
    void writeEngineFunctionCapturesOutputForCliAndTests() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);

        LpcObjectHandle object = runtime.loadSource("engine_function/writer.c", """
                void describe() {
                    jvmud_write("hello ");
                    jvmud_write("mud");
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
        EngineEfuns.registerCore(runtime);

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
        EngineEfuns.registerCore(runtime);

        Object parent = runtime.cloneObject("thing");
        Object child = runtime.cloneObject("thing");

        runtime.moveObject(child, parent);

        assertThrows(IllegalArgumentException.class, () -> runtime.moveObject(parent, child));
        assertThrows(IllegalArgumentException.class, () -> runtime.moveObject(parent, parent));
        assertEquals(parent, runtime.environment(child));
        assertEquals(null, runtime.environment(parent));
    }

    @Test
    void lpcCodeCanCloneMoveInspectAndCallObjectsThroughEngineFunctions() throws Exception {
        Files.writeString(tempDir.resolve("thing.c"), """
                status id(str) {
                    return str == "thing";
                }

                string short() {
                    return "a small thing";
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LpcObjectHandle controller = runtime.loadSource("controller.c", """
                void setup() {
                    object thing;
                    thing = jvmud_clone_object("thing");
                    jvmud_move_object(thing, jvmud_current_object());
                    jvmud_write(jvmud_invoke_object(jvmud_first_inventory(jvmud_current_object()), "short", 0));
                }
                """);

        controller.invoke("setup");

        assertEquals("a small thing", runtime.outputTranscript());
        assertEquals(controller.instance(), runtime.environment(runtime.firstInventory(controller.instance())));
    }
}
