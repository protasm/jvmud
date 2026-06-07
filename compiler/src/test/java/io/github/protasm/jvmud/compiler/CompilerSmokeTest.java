package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibLifecycleEvent;
import io.github.protasm.jvmud.runtime.WorldScheduler;
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
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/array_loop.c", """
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
    void runtimeCoercesMixedNumericOperandsBeforeJvmIntegerOpcodes() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mixed_numeric.c", """
                int total;

                int adjust(mixed i) {
                    if (i < 0) {
                        if (-i > total / 10)
                            i = -total / 10;
                    }

                    total += i;

                    if (total < 0)
                        total = 0;

                    return total + i;
                }
                """);

        assertEquals(14, object.invoke("adjust", 7));
        assertEquals(7, object.invoke("adjust", -2));
    }

    @Test
    void runtimeEvaluatesLogicalOrFalseWhenBothOperandsAreFalse() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/logical_or.c", """
                int blank(mixed str) {
                    if (!str || str == "")
                        return 1;
                    return 0;
                }
                """);

        assertEquals(1, object.invoke("blank", (Object) null));
        assertEquals(1, object.invoke("blank", ""));
        assertEquals(0, object.invoke("blank", "smoketest"));
    }

    @Test
    void runtimeSupportsLpcStringIndexingAndCharacterLiterals() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/string_index.c", """
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
    void runtimeIndexesMixedStringValuesAsCharacterCodes() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mixed_string_index.c", """
                int first(mixed value) {
                    return value[0];
                }
                """);

        assertEquals(97, object.invoke("first", "abc"));
    }

    @Test
    void runtimeTreatsNullReferenceAndZeroAsEqualForLpcCompatibility() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/null_zero.c", """
                string password;

                int unset_is_zero() {
                    return password == 0;
                }

                int set_is_not_zero() {
                    password = "secret";
                    return password != 0;
                }
                """);

        assertEquals(1, object.invoke("unset_is_zero"));
        assertEquals(1, object.invoke("set_is_not_zero"));
    }

    @Test
    void runtimeReturnsZeroForDynamicCallToMissingStringTarget() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/missing_call_other.c", """
                mixed optional_call() {
                    return jvmud_invoke_entity("room/missing", "advance", 0);
                }
                """);

        assertEquals(0, object.invoke("optional_call"));
    }

    @Test
    void runtimeCoercesZeroAssignedToStringFieldAsNull() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/string_zero.c", """
                string saved;

                int store_zero(mixed value) {
                    saved = value;
                    return saved == 0;
                }
                """);

        assertEquals(1, object.invoke("store_zero", 0));
    }

    @Test
    void runtimeSupportsCommaSeparatedForInitializerAndUpdateExpressions() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/for_comma.c", """
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
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/continue.c", """
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
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/bitwise.c", """
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
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/allocate.c", """
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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.load("obj/playerish");

        assertEquals(42, object.invoke("value"));
    }

    @Test
    void runtimeLoadsInstantiatesAndInvokesCompiledSource() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/object.c", """
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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/static.c", source);

        assertEquals(42, object.invoke("value"));

        CompilationResult result = new CompilationPipeline("java/lang/Object").run(source);
        assertTrue(result.getProblems().isEmpty(), () -> result.getProblems().toString());

        ASTObject ast = result.getAstObject();
        assertTrue(ast.fields().get("hidden").isStatic());
        assertTrue(ast.methods().get("value").isStatic());
    }

    @Test
    void preprocessorRecognizesIndentedConditionalsInsideFunctionBodies() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/indented_conditional.c", """
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
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());

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
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        MudlibBoundary boundary = MudlibBoundary.builder()
                .boundaryObjectPath("jvmud/mudlib")
                .mfunObjectPath("jvmud/functions")
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "on_loaded")
                .build();

        runtime.registerMudlibBoundary(boundary);

        assertEquals(boundary, runtime.mudlibBoundary());
    }

    @Test
    void runtimeUsesConfiguredObjectLoadedLifecycleMethod() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "on_loaded")
                .build());

        LPCObjectHandle object = runtime.loadSource("smoke/lifecycle.c", """
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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/functions")
                .build());

        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/functions")
                .build());

        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

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
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LPCObjectHandle first = runtime.loadSource("smoke/first_player.c", """
                void write_self() {
                    jvmud_write("first-only");
                }

                void tell(mixed target) {
                    jvmud_send_to_entity(target, "second-only");
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
        LPCObjectHandle second = runtime.loadSource("smoke/second_player.c", """
                int value() {
                    return 0;
                }
                """);
        LPCObjectHandle unconnected = runtime.loadSource("smoke/unconnected.c", """
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
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LPCObjectHandle player = runtime.loadSource("smoke/input_player.c", """
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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LPCObjectHandle reader = runtime.loadSource("smoke/text_reader.c", """
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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals(1, caller.invoke("show_welcome"));
        assertEquals("Welcome through mfun.\n", runtime.outputTranscript());
        assertEquals("Alice", caller.invoke("normalized"));
    }

    @Test
    void mfunLivingNameCompatibilityRegistersCurrentObject() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                object find_living(mixed name) {
                    return jvmud_find_entity_alias("living", name);
                }

                status living(mixed ob) {
                    return jvmud_entity_commands_enabled(ob);
                }

                void set_living_name(mixed name) {
                    jvmud_bind_entity_alias(jvmud_current_entity(), "living", name);
                }

                void enable_commands() {
                    jvmud_enable_commands();
                }

                object this_object() {
                    return jvmud_current_entity();
                }
                """);
        Files.writeString(tempDir.resolve("player.c"), """
                void setup() {
                    set_living_name("Protasm");
                }

                void make_commandable() {
                    enable_commands();
                }

                object lookup(mixed name) {
                    return find_living(name);
                }

                status is_registered() {
                    return living(this_object());
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));

        player.invoke("setup");

        assertEquals(player.instance(), player.invoke("lookup", "protasm"));
        assertEquals(false, player.invoke("is_registered"));

        player.invoke("make_commandable");

        assertEquals(true, player.invoke("is_registered"));
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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/functions")
                .build());

        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle child = runtime.load(tempDir.resolve("child.c"));

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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle child = runtime.load(tempDir.resolve("room/village/vill_green.c"));

        assertEquals(42, child.invoke("child_value"));
    }

    @Test
    void arrowInvokeOnExpressionUsesNativeDynamicDispatch() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LPCObjectHandle object = runtime.loadSource("smoke/arrow.c", """
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
    void dynamicDispatchPadsTrailingOmittedArguments() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LPCObjectHandle object = runtime.loadSource("smoke/optional_dispatch.c", """
                mixed optional(mixed first, mixed second) {
                    if (second)
                        return first + second;
                    return first + "#missing";
                }

                mixed reflected_value(mixed target) {
                    return target->optional("north");
                }
                """);

        assertEquals("north#missing", object.invoke("reflected_value", object.instance()));
    }

    @Test
    void sscanfAssignsCapturedOutputLocals() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);

        LPCObjectHandle object = runtime.loadSource("smoke/sscanf.c", """
                mixed parse(mixed value) {
                    string dir, dest;
                    if (sscanf(value, "%s#%s", dir, dest) != 2)
                        return "bad";
                    return dir + ":" + dest;
                }
                """);

        assertEquals("north:room/village/church", object.invoke("parse", "north#room/village/church"));
    }

    @Test
    void currentObjectSeesEnvironmentLight() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);

        LPCObjectHandle room = runtime.loadSource("smoke/lit_room.c", """
                void light() {
                    jvmud_set_light(1);
                }
                """);
        LPCObjectHandle actor = runtime.loadSource("smoke/light_actor.c", """
                int visible_light() {
                    return jvmud_set_light(0);
                }
                """);
        LPCObjectHandle mover = runtime.loadSource("smoke/light_mover.c", """
                void move_actor(mixed actor) {
                    jvmud_move_entity(actor, "smoke/lit_room");
                }
                """);

        room.invoke("light");
        mover.invoke("move_actor", actor.instance());

        assertEquals(1, actor.invoke("visible_light"));
    }

    @Test
    void setHeartBeatSchedulesCurrentObjectAtDefaultInterval() {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntime(scheduler, 2);

        LPCObjectHandle object = runtime.loadSource("smoke/default_tick.c", """
                int ticks;

                void start() {
                    set_heart_beat(1);
                }

                void heart_beat() {
                    ticks += 1;
                }

                int query_ticks() {
                    return ticks;
                }
                """);

        object.invoke("start");
        scheduler.advanceTo(1);
        assertEquals(0, object.invoke("query_ticks"));

        scheduler.advanceTo(2);
        assertEquals(1, object.invoke("query_ticks"));

        scheduler.advanceTo(4);
        assertEquals(2, object.invoke("query_ticks"));
    }

    @Test
    void setHeartBeatOverloadSchedulesCurrentObjectAtExplicitInterval() {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntime(scheduler, 1);

        LPCObjectHandle object = runtime.loadSource("smoke/explicit_tick.c", """
                int ticks;

                void start() {
                    set_heart_beat(1, 5);
                }

                void heart_beat() {
                    ticks += 1;
                }

                int query_ticks() {
                    return ticks;
                }
                """);

        object.invoke("start");
        scheduler.advanceTo(4);
        assertEquals(0, object.invoke("query_ticks"));

        scheduler.advanceTo(5);
        assertEquals(1, object.invoke("query_ticks"));

        scheduler.advanceTo(10);
        assertEquals(2, object.invoke("query_ticks"));
    }

    @Test
    void objectsCanTickAtDifferentRates() {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntime(scheduler, 1);

        LPCObjectHandle fast = runtime.loadSource("smoke/fast_tick.c", tickingObjectSource(2));
        LPCObjectHandle slow = runtime.loadSource("smoke/slow_tick.c", tickingObjectSource(5));

        fast.invoke("start");
        slow.invoke("start");
        scheduler.advanceTo(10);

        assertEquals(5, fast.invoke("query_ticks"));
        assertEquals(2, slow.invoke("query_ticks"));
    }

    @Test
    void reschedulingReplacesPreviousRecurringTick() {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntime(scheduler, 1);

        LPCObjectHandle object = runtime.loadSource("smoke/rescheduled_tick.c", """
                int ticks;

                void start_slow() {
                    set_heart_beat(1, 5);
                }

                void start_fast() {
                    set_heart_beat(1, 2);
                }

                void heart_beat() {
                    ticks += 1;
                }

                int query_ticks() {
                    return ticks;
                }
                """);

        object.invoke("start_slow");
        scheduler.advanceTo(4);
        object.invoke("start_fast");

        scheduler.advanceTo(5);
        assertEquals(0, object.invoke("query_ticks"));

        scheduler.advanceTo(6);
        assertEquals(1, object.invoke("query_ticks"));

        scheduler.advanceTo(8);
        assertEquals(2, object.invoke("query_ticks"));
    }

    @Test
    void setHeartBeatZeroCancelsRecurringTick() {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntime(scheduler, 1);

        LPCObjectHandle object = runtime.loadSource("smoke/cancelled_tick.c", """
                int ticks;

                void start() {
                    set_heart_beat(1, 2);
                }

                void stop() {
                    set_heart_beat(0);
                }

                void heart_beat() {
                    ticks += 1;
                }

                int query_ticks() {
                    return ticks;
                }
                """);

        object.invoke("start");
        scheduler.advanceTo(2);
        assertEquals(1, object.invoke("query_ticks"));

        object.invoke("stop");
        scheduler.advanceTo(8);
        assertEquals(1, object.invoke("query_ticks"));
    }

    @Test
    void destructCancelsRecurringTick() {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntime(scheduler, 1);

        LPCObjectHandle object = runtime.loadSource("smoke/destructed_tick.c", """
                int ticks;

                void start() {
                    set_heart_beat(1, 2);
                }

                void heart_beat() {
                    ticks += 1;
                }

                int query_ticks() {
                    return ticks;
                }
                """);

        object.invoke("start");
        scheduler.advanceTo(2);
        assertEquals(1, object.invoke("query_ticks"));

        runtime.destructObject(object.instance());
        scheduler.advanceTo(8);
        assertEquals(1, object.invoke("query_ticks"));
    }

    @Test
    void recurringTickRejectsNegativeIntervals() {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntime(scheduler, 1);

        LPCObjectHandle object = runtime.loadSource("smoke/bad_tick.c", """
                void start() {
                    set_heart_beat(1, -1);
                }

                void heart_beat() {
                }
                """);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> object.invoke("start"));
        assertTrue(exception.getMessage().contains("InvocationTargetException"));
    }

    @Test
    void localsDefaultToZeroAcrossControlFlowBranches() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LPCObjectHandle object = runtime.loadSource("smoke/default_local.c", """
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
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LPCObjectHandle object = runtime.loadSource("smoke/untyped_arg.c", """
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
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);

        LPCObjectHandle object = runtime.loadSource("engine_function/caller.c", """
                int value() {
                    return 42;
                }

                mixed reflected_value() {
                    return jvmud_invoke_entity(jvmud_current_entity(), "value", 0);
                }
                """);

        assertEquals(42, object.invoke("reflected_value"));
    }

    @Test
    void writeEngineFunctionCapturesOutputForCliAndTests() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);

        LPCObjectHandle object = runtime.loadSource("engine_function/writer.c", """
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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);

        LPCObjectHandle room = runtime.load(tempDir.resolve("room.c"));
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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
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

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LPCObjectHandle controller = runtime.loadSource("controller.c", """
                void setup() {
                    object thing;
                    thing = jvmud_spawn_entity("thing");
                    jvmud_move_entity(thing, jvmud_current_entity());
                    jvmud_write(jvmud_invoke_entity(jvmud_first_entity_at(jvmud_current_entity()), "short", 0));
                }
                """);

        controller.invoke("setup");

        assertEquals("a small thing", runtime.outputTranscript());
        assertEquals(controller.instance(), runtime.environment(runtime.firstInventory(controller.instance())));
    }

    private LPCRuntime temporalRuntime(WorldScheduler scheduler, int defaultIntervalSeconds) {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.setScheduler(scheduler);
        runtime.loadSource("jvmud/mfuns.c", """
                void set_heart_beat(int enabled) {
                    jvmud_schedule_recurring_tick(enabled, 0);
                }

                void set_heart_beat(int enabled, int interval_seconds) {
                    jvmud_schedule_recurring_tick(enabled, interval_seconds);
                }
                """);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .temporalTickMethod("heart_beat")
                .temporalTickIntervalSeconds(defaultIntervalSeconds)
                .build());
        return runtime;
    }

    private String tickingObjectSource(int intervalSeconds) {
        return """
                int ticks;

                void start() {
                    set_heart_beat(1, %d);
                }

                void heart_beat() {
                    ticks += 1;
                }

                int query_ticks() {
                    return ticks;
                }
                """.formatted(intervalSeconds);
    }
}
