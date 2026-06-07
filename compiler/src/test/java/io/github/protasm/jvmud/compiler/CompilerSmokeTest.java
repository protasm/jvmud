package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LpcObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LpcRuntime;
import io.github.protasm.jvmud.compiler.exec.LpcRuntimeConfig;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
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
    void runtimeSupportsLpcStringIndexingAndCharacterLiterals() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle object = runtime.loadSource("smoke/string_index.c", """
                int slash_code() {
                    return '/';
                }

                int second_char() {
                    string path;
                    path = "/ab";
                    return path[1];
                }

                int has_slash_at_start() {
                    string path;
                    path = "/ab";
                    return path[0] == '/';
                }
                """);

        assertEquals(47, object.invoke("slash_code"));
        assertEquals(97, object.invoke("second_char"));
        assertEquals(1, object.invoke("has_slash_at_start"));
    }

    @Test
    void runtimeSupportsCommaSeparatedForInitializerAndUpdateExpressions() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle object = runtime.loadSource("smoke/for_comma.c", """
                int value() {
                    int i;
                    int a;

                    for (i = 0, a = 1; i < 3; i += 1, a += 2) {
                    }

                    return a;
                }
                """);

        assertEquals(7, object.invoke("value"));
    }

    @Test
    void runtimeSupportsContinueInForAndWhileLoops() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle object = runtime.loadSource("smoke/continue.c", """
                int value() {
                    int i;
                    int sum;

                    for (i = 0; i < 5; i += 1) {
                        if (i == 2)
                            continue;
                        sum += i;
                    }

                    while (i < 8) {
                        i += 1;
                        if (i == 7)
                            continue;
                        sum += i;
                    }

                    return sum;
                }
                """);

        assertEquals(22, object.invoke("value"));
    }

    @Test
    void runtimeSupportsIntegerBitwiseOperatorsAndCompoundAssignments() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle object = runtime.loadSource("smoke/bitwise.c", """
                int value() {
                    int scar;

                    scar = 1;
                    scar *= 3;
                    scar /= 3;
                    scar |= 1 << 3;
                    scar ^= 2;
                    scar &= 15;
                    scar <<= 1;
                    scar >>= 2;
                    scar &= ~2;

                    return scar;
                }
                """);

        assertEquals(5, object.invoke("value"));
    }

    @Test
    void runtimeSupportsAllocateAndLegacyStringDeclaredArrays() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LpcObjectHandle object = runtime.loadSource("smoke/allocate.c", """
                string values;

                mixed value() {
                    values = allocate(3);
                    values[1] = "middle";
                    return values[1];
                }
                """);

        assertEquals("middle", object.invoke("value"));
    }

    @Test
    void quotedIncludesSearchLegacyMudlibHeaderDirectoriesAfterSourceDirectory() throws Exception {
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("obj/living.h"), "#define LIVING_VALUE 7\n");
        Files.writeString(tempDir.resolve("room/log.h"), "#define LOG_VALUE    (35)\n");
        Files.writeString(tempDir.resolve("obj/playerish.c"), """
                #include "log.h"
                #include "living.h"

                int value() {
                    return LOG_VALUE + LIVING_VALUE;
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle object = runtime.load("obj/playerish");

        assertEquals(42, object.invoke("value"));
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
    void parserRecordsStaticDeclarationModifiersWithoutChangingRuntimeBehavior() {
        String source = """
                static int hidden = 40;

                static int value() {
                    return hidden + 2;
                }
                """;

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle object = runtime.loadSource("smoke/static.c", source);

        assertEquals(42, object.invoke("value"));

        CompilationResult result = new CompilationPipeline("java/lang/Object").run(source);
        assertTrue(result.getProblems().isEmpty(), () -> result.getProblems().toString());

        ASTObject ast = result.getAstObject();
        assertTrue(ast.fields().get("hidden").isStatic());
        assertTrue(ast.methods().get("value").isStatic());
    }

    @Test
    void preprocessorRecognizesIndentedConditionalsInsideFunctionBodies() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle object = runtime.loadSource("smoke/indented_conditional.c", """
                #define ENABLED

                int value() {
                  #ifdef ENABLED
                  return 42;
                  #endif
                  return 0;
                }
                """);

        assertEquals(42, object.invoke("value"));
    }

    @Test
    void untypedMethodsProduceHardSemanticErrors() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());

        RuntimeException error = assertThrows(RuntimeException.class, () -> runtime.loadSource("smoke/untyped.c", """
                reset(arg) {
                    return arg + 1;
                }

                mixed value() {
                    return reset(41);
                }
                """));

        assertTrue(error.getMessage().contains("Method 'reset' must declare a return type"));
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
                mixed mudlib_sum(mixed a, mixed b) {
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
    void sizeofRejectsScalarObjects() {
        RuntimeContext context = new RuntimeContext(null);
        EngineEfuns.registerCore(context);
        context.setMfunObjectPath("jvmud/functions");

        CompilationResult result = new CompilationPipeline("java/lang/Object", context).run("""
                object target;

                int value() {
                    return sizeof(target);
                }
                """);

        assertTrue(result.getProblems().stream()
                .anyMatch(problem -> problem.getMessage()
                        .contains("sizeof expects array, mapping, or string argument")));
        assertNull(result.getBytecode());
    }

    @Test
    void usersMfunIsTypedAsArrayForSizeofAndIndexing() {
        RuntimeContext context = new RuntimeContext(null);
        EngineEfuns.registerCore(context);
        context.setMfunObjectPath("jvmud/functions");

        CompilationResult result = new CompilationPipeline("java/lang/Object", context).run("""
                int value() {
                    object *list;

                    list = users();
                    return sizeof(list);
                }
                """);

        assertTrue(result.getProblems().isEmpty(), () -> result.getProblems().toString());
        assertNotNull(result.getBytecode());
    }

    @Test
    void runtimeRoutesPersonaSessionOutputAndPresenceQueries() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LpcObjectHandle first = runtime.loadSource("smoke/first_player.c", """
                void write_self() {
                    jvmud_write("first-only");
                }

                void tell(mixed target) {
                    jvmud_tell_object(target, "second-only");
                }

                mixed query_ip(mixed target) {
                    return jvmud_query_ip_number(target);
                }

                int query_idle_for(mixed target) {
                    return jvmud_query_idle(target);
                }

                int user_count() {
                    return jvmud_size(jvmud_users());
                }
                """);
        LpcObjectHandle second = runtime.loadSource("smoke/second_player.c", """
                int value() {
                    return 0;
                }
                """);
        LpcObjectHandle unconnected = runtime.loadSource("smoke/unconnected.c", """
                int value() {
                    return 0;
                }
                """);
        StringBuilder firstOutput = new StringBuilder();
        StringBuilder secondOutput = new StringBuilder();

        runtime.bindSession("s1", first.instance(), "127.0.0.1", firstOutput::append);
        runtime.bindSession("s2", second.instance(), "10.0.0.2", secondOutput::append);

        first.invoke("write_self");
        first.invoke("tell", second.instance());

        assertEquals("first-only", firstOutput.toString());
        assertEquals("second-only", secondOutput.toString());
        assertEquals("127.0.0.1", first.invoke("query_ip", first.instance()));
        assertEquals(0, first.invoke("query_ip", unconnected.instance()));
        assertTrue((Integer) first.invoke("query_idle_for", first.instance()) >= 0);
        assertEquals(2, first.invoke("user_count"));

        runtime.unbindSession("s2");

        assertEquals(1, first.invoke("user_count"));
    }

    @Test
    void runtimeCapturesNextSessionInputForPersona() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LpcObjectHandle player = runtime.loadSource("smoke/input_player.c", """
                string response;

                void ask() {
                    jvmud_write("Name: ");
                    jvmud_capture_session_input("answer", 0);
                }

                void answer(mixed line) {
                    response = line;
                    jvmud_write("Hello " + response + "\\n");
                }

                string last_response() {
                    return response;
                }
                """);
        StringBuilder output = new StringBuilder();
        runtime.bindSession("s1", player.instance(), "127.0.0.1", output::append);

        player.invoke("ask");

        assertTrue(runtime.hasCapturedSessionInput(player.instance()));
        assertEquals("Name: ", output.toString());

        runtime.deliverCapturedSessionInput(player.instance(), "Alice");

        assertEquals("Alice", player.invoke("last_response"));
        assertEquals("Name: Hello Alice\n", output.toString());
    }

    @Test
    void runtimeReadsMudlibRootedTextForCompatibilityShims() throws Exception {
        Files.writeString(tempDir.resolve("WELCOME"), "Welcome to JVMud.\n");

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LpcObjectHandle reader = runtime.loadSource("smoke/text_reader.c", """
                mixed welcome() {
                    return jvmud_read_mudlib_text("/WELCOME");
                }

                mixed escaped() {
                    return jvmud_read_mudlib_text("../outside");
                }

                string lower() {
                    return jvmud_lowercase_text("MiXeD");
                }

                string capitalized() {
                    return jvmud_capitalize_text("alice");
                }
                """);

        assertEquals("Welcome to JVMud.\n", reader.invoke("welcome"));
        assertEquals(0, reader.invoke("escaped"));
        assertEquals("mixed", reader.invoke("lower"));
        assertEquals("Alice", reader.invoke("capitalized"));
    }

    @Test
    void mfunTextCompatibilityWrapsNativeTextOperations() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("WELCOME"), "Welcome through mfun.\n");
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void write(mixed value) {
                    jvmud_write(value);
                }

                status stringp(mixed value) {
                    return jvmud_is_string(value);
                }

                int cat(string path) {
                    mixed text;

                    text = jvmud_read_mudlib_text(path);
                    if (!stringp(text))
                        return 0;

                    write(text);
                    return 1;
                }

                string lower_case(mixed value) {
                    return jvmud_lowercase_text(value);
                }

                string capitalize(mixed value) {
                    return jvmud_capitalize_text(value);
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                int show_welcome() {
                    return cat("/WELCOME");
                }

                string normalized() {
                    return capitalize(lower_case("ALICE"));
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LpcObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals(1, caller.invoke("show_welcome"));
        assertEquals("Welcome through mfun.\n", runtime.outputTranscript());
        assertEquals("Alice", caller.invoke("normalized"));
    }

    @Test
    void mfunShadowsEngineFunctionWithSameNameAndArity() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/functions.c"), """
                mixed write(mixed value) {
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
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/room.c"), """
                int base_value() {
                    return 30;
                }
                """);
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                inherit "room/room";

                int child_value() {
                    return base_value() + 12;
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LpcObjectHandle child = runtime.load(tempDir.resolve("room/village/vill_green.c"));

        assertEquals(42, child.invoke("child_value"));
    }

    @Test
    void arrowInvokeOnExpressionUsesNativeDynamicDispatch() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LpcObjectHandle object = runtime.loadSource("smoke/arrow.c", """
                int value() {
                    return 42;
                }

                mixed reflected_value(mixed target) {
                    return target->value();
                }
                """);

        assertEquals(42, object.invoke("reflected_value", object.instance()));
    }

    @Test
    void localsDefaultToZeroAcrossControlFlowBranches() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LpcObjectHandle object = runtime.loadSource("smoke/default_local.c", """
                int value(mixed flag) {
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
    void primitiveArgumentsAreBoxedForExplicitMixedMethodParameters() {
        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LpcObjectHandle object = runtime.loadSource("smoke/untyped_arg.c", """
                mixed identity(mixed value) {
                    return value;
                }

                mixed answer() {
                    return identity(42);
                }
                """);

        assertEquals(42, object.invoke("answer"));
    }

    @Test
    void pipelineRejectsUntypedObjectMethods() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                value() {
                    return 42;
                }
                """);

        assertTrue(result.getProblems().stream()
                .anyMatch(problem -> problem.getMessage().contains("Method 'value' must declare a return type")));
        assertNull(result.getBytecode());
    }

    @Test
    void pipelineRejectsUntypedMethodParameters() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                mixed value(arg) {
                    return arg;
                }
                """);

        assertTrue(result.getProblems().stream()
                .anyMatch(problem -> problem.getMessage()
                        .contains("Parameter 'arg' in method 'value' must declare a type")));
        assertNull(result.getBytecode());
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
                int id(mixed str) {
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
                status id(mixed str) {
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
