package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.compiler.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibLifecycleEvent;
import io.github.protasm.jvmud.runtime.MudlibProjection;
import io.github.protasm.jvmud.runtime.MudlibProjectionRole;
import io.github.protasm.jvmud.runtime.WorldScheduler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
    void includeResolverAllowsParentTraversalWithinMudlibRoot() throws IOException {
        Path roomDir = Files.createDirectories(tempDir.resolve("room"));
        Path mineDir = Files.createDirectories(roomDir.resolve("mine"));
        Files.writeString(roomDir.resolve("std.h"), "#define VALUE 42\n");
        Path sourcePath = mineDir.resolve("tunnel.c");
        Files.writeString(sourcePath, """
                #include "../std.h"

                int value() {
                    return VALUE;
                }
                """);

        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(tempDir, List.of()));
        CompilationResult result = new CompilationPipeline("java/lang/Object", context)
                .run(sourcePath, Files.readString(sourcePath), "room/mine/tunnel", "/room/mine/tunnel.c",
                        ParserOptions.defaults());

        assertTrue(result.getProblems().isEmpty(), () -> result.getProblems().toString());
        assertNotNull(result.getBytecode());
    }

    @Test
    void includeResolverRejectsParentTraversalOutsideMudlibRoot() throws IOException {
        Path roomDir = Files.createDirectories(tempDir.resolve("room"));
        Path mineDir = Files.createDirectories(roomDir.resolve("mine"));
        Path sourcePath = mineDir.resolve("escape.c");
        Files.writeString(sourcePath, """
                #include "../../../outside.h"

                int value() {
                    return 1;
                }
                """);

        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(tempDir, List.of()));
        CompilationResult result = new CompilationPipeline("java/lang/Object", context)
                .run(sourcePath, Files.readString(sourcePath), "room/mine/escape", "/room/mine/escape.c",
                        ParserOptions.defaults());

        assertTrue(result.getProblems().stream()
                .anyMatch(problem -> problem.getMessage().contains("Error scanning source")));
    }

    @Test
    void scannerKeepsEscapedQuotesInsideStringLiterals() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                string quoted() {
                    return "\\"%s\\"";
                }
                """);

        assertTrue(result.getProblems().isEmpty(), () -> result.getProblems().toString());
        assertNotNull(result.getBytecode());
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
    void runtimeSupportsIndexedPostfixIncrementAndDecrement() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/indexed_postfix.c", """
                mixed incremented() {
                    mixed *values;

                    values = {0, 0};
                    values[0]++;
                    values[1]--;

                    return values[0] * 10 + values[1];
                }
                """);

        assertEquals(9, object.invoke("incremented"));
    }

    @Test
    void indexedPostfixMutationEvaluatesIndexExpressionOnce() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/indexed_postfix_once.c", """
                int cursor;
                mixed *values;

                int next_index() {
                    cursor++;
                    return cursor - 1;
                }

                mixed value() {
                    values = {0, 0};
                    values[next_index()]++;
                    return values[0] * 10 + cursor;
                }
                """);

        assertEquals(11, object.invoke("value"));
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
    void runtimeSupportsModuloExpressions() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/modulo.c", """
                int remainder(int seconds) {
                    return seconds % 60;
                }
                """);

        assertEquals(31, object.invoke("remainder", 91));
    }

    @Test
    void directMethodCallsMayOmitTrailingArguments() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/optional_arg.c", """
                string seen;

                void remember(string label, object optional_target) {
                    if (!optional_target)
                        seen = label;
                }

                string value() {
                    remember("defaulted");
                    return seen;
                }
                """);

        assertEquals("defaulted", object.invoke("value"));
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

                int empty_first() {
                    string path;
                    path = "";
                    return path[0];
                }

                int negative_index() {
                    string path;
                    path = "/ab";
                    return path[-1];
                }
                """);

        assertEquals(47, object.invoke("slash_code"));
        assertEquals(97, object.invoke("second_char"));
        assertEquals(1, object.invoke("has_slash_at_start"));
        assertEquals(0, object.invoke("empty_first"));
        assertEquals(0, object.invoke("negative_index"));
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
        assertEquals(0, object.invoke("first", ""));
    }

    @Test
    void runtimeSlicesStringsWithInclusiveEndAndOpenTail() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/string_slice.c", """
                string tail(string value) {
                    return value[1..];
                }

                string middle(string value) {
                    return value[1..3];
                }
                """);

        assertEquals("ello", object.invoke("tail", "hello"));
        assertEquals("ell", object.invoke("middle", "hello"));
    }

    @Test
    void forwardMethodDeclarationsDoNotEmitBytecodeMethods() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/forward_declaration.c", """
                int value();

                int value() {
                    return 42;
                }
                """);

        assertEquals(42, object.invoke("value"));
    }

    @Test
    void unqualifiedMethodCallPrefersChildDefinitionOverInheritedPrototype() throws Exception {
        Files.writeString(tempDir.resolve("base.c"), """
                string short();
                """);
        Files.writeString(tempDir.resolve("child.c"), """
                inherit "base";

                string describe() {
                    return short();
                }

                string short() {
                    return "child short";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        Object child = runtime.loadOrGetObject("child");

        assertEquals("child short", runtime.invokeObject(child, "describe"));
    }

    @Test
    void parserAcceptsRepeatedArrayFieldDeclarators() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/array_field_declarators.c", """
                string *first, *second;

                int value() {
                    first = ({ "a" });
                    second = ({ "b" });
                    return 2;
                }
                """);

        assertEquals(2, object.invoke("value"));
    }

    @Test
    void objectLoadedLifecycleInvokesIntReset() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "reset")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/reset_lifecycle.c", """
                int initialized;

                void reset(int arg) {
                    initialized = arg == 0;
                }

                int value() {
                    return initialized;
                }
                """);

        assertEquals(1, object.invoke("value"));
    }

    @Test
    void objectLoadedLifecycleTreatsZeroAsFalseForStringReset() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "reset")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/string_reset_lifecycle.c", """
                int initialized;

                void reset(string arg) {
                    if (!arg)
                        initialized = 1;
                }

                int value() {
                    return initialized;
                }
                """);

        assertEquals(1, object.invoke("value"));
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
    void explicitBareReturnUsesDefaultValueForDeclaredReturnType() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/bare_return.c", """
                status status_value() {
                    return;
                }

                object object_value() {
                    return;
                }
                """);

        assertEquals(false, object.invoke("status_value"));
        assertNull(object.invoke("object_value"));
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
    void parserPreservesLdmudStyleDeclarationModifiers() {
        String source = """
                private nosave object cache;

                public nomask varargs int value(string name, string title) {
                    return 42;
                }

                protected void setup() {
                }
                """;

        CompilationResult result = new CompilationPipeline("java/lang/Object").run(source);
        assertTrue(result.getProblems().isEmpty(), () -> result.getProblems().toString());

        ASTObject ast = result.getAstObject();
        assertTrue(ast.fields().get("cache").modifiers().isPrivate());
        assertTrue(ast.fields().get("cache").modifiers().isNosave());

        assertTrue(ast.methods().get("value").modifiers().isPublic());
        assertTrue(ast.methods().get("value").modifiers().isNomask());
        assertTrue(ast.methods().get("value").modifiers().isVarargs());

        assertTrue(ast.methods().get("setup").modifiers().isProtected());
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
    void untypedMethodsDefaultToMixedForCompatibility() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LPCObjectHandle object = runtime.loadSource("smoke/untyped.c", """
                reset(arg) {
                    return arg + 1;
                }

                mixed value() {
                    return reset(41);
                }
                """);

        assertEquals(42, object.invoke("value"));
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

                void say_to_place() {
                    jvmud_emit_perceivable(jvmud_current_entity(), "place-only");
                }

                void say_to_place_except(mixed target) {
                    jvmud_emit_perceivable_except(jvmud_current_entity(), "excepted", target);
                }

                void tell_place(mixed place) {
                    jvmud_emit_perceivable_at(place, "all-here");
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

        var firstSession = runtime.sessionRecord("s1").orElseThrow();
        var firstPlayer = runtime.playerRecordForSession("s1").orElseThrow();
        var firstPersona = runtime.personaRecordForProjection(first.instance()).orElseThrow();
        assertEquals("s1", firstSession.id().value());
        assertEquals("127.0.0.1", firstSession.remoteAddress().orElseThrow());
        assertEquals(firstPlayer.id(), firstSession.playerId());
        assertEquals(firstSession.id(), firstPlayer.activeSessionIds().iterator().next());
        assertEquals(firstPersona.id(), firstSession.attachedPersonaId().orElseThrow());
        assertEquals(firstPersona.id(), firstPlayer.activePersonaId().orElseThrow());
        assertEquals(first.instance(), firstPersona.mudlibBehaviorProjection().orElseThrow());
        assertEquals(firstPlayer.id(), firstPersona.controllingPlayerId().orElseThrow());
        assertTrue(firstPersona.entity().isEmpty());

        first.invoke("write_self");
        first.invoke("tell", second.instance());

        assertEquals("first-only", firstOutput.toString());
        assertEquals("second-only", secondOutput.toString());

        runtime.moveObject(first.instance(), unconnected.instance());
        runtime.moveObject(second.instance(), unconnected.instance());

        first.invoke("say_to_place");
        assertEquals("first-only", firstOutput.toString());
        assertEquals("second-onlyplace-only", secondOutput.toString());

        first.invoke("say_to_place_except", second.instance());
        assertEquals("first-only", firstOutput.toString());
        assertEquals("second-onlyplace-only", secondOutput.toString());

        first.invoke("tell_place", unconnected.instance());
        assertEquals("first-onlyall-here", firstOutput.toString());
        assertEquals("second-onlyplace-onlyall-here", secondOutput.toString());

        assertEquals("127.0.0.1", first.invoke("query_ip", first.instance()));
        assertEquals(0, first.invoke("query_ip", unconnected.instance()));
        assertTrue((Integer) first.invoke("query_idle_for", first.instance()) >= 0);
        assertEquals(2, first.invoke("user_count"));

        runtime.unbindSession("s2");

        assertEquals(1, first.invoke("user_count"));
        assertTrue(runtime.sessionRecord("s2").isEmpty());
    }

    @Test
    void runtimeRoutesEngineMessagesToSessionPlayerAndPersona() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle first = runtime.loadSource("smoke/first_player.c", """
                int value() {
                    return 1;
                }
                """);
        LPCObjectHandle second = runtime.loadSource("smoke/second_player.c", """
                int value() {
                    return 2;
                }
                """);
        StringBuilder firstOutput = new StringBuilder();
        StringBuilder secondOutput = new StringBuilder();

        runtime.bindSession("s1", first.instance(), "127.0.0.1", firstOutput::append);
        runtime.bindSession("s2", second.instance(), "10.0.0.2", secondOutput::append);

        var firstSession = runtime.sessionRecord("s1").orElseThrow();
        var firstPlayer = runtime.playerRecordForSession("s1").orElseThrow();
        var firstPersona = runtime.personaRecordForProjection(first.instance()).orElseThrow();

        assertTrue(runtime.messageSession(firstSession.id(), "session-only\\n"));
        assertTrue(runtime.messagePlayer(firstPlayer.id(), "player-only\\n"));
        assertTrue(runtime.messagePersona(firstPersona.id(), "persona-only\\n"));
        assertEquals("session-only\nplayer-only\npersona-only\n", firstOutput.toString());
        assertEquals("", secondOutput.toString());
        assertEquals(firstOutput.toString(), runtime.outputTranscript());

        runtime.unbindSession("s1");

        assertFalse(runtime.messageSession(firstSession.id(), "after-unbind"));
        assertFalse(runtime.messagePlayer(firstPlayer.id(), "after-unbind"));
        assertFalse(runtime.messagePersona(firstPersona.id(), "after-unbind"));
    }

    @Test
    void runtimeWrapsOutgoingSessionMessagesAtConfiguredLineLength() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .maxLineLength(20)
                .build());
        LPCObjectHandle player = runtime.loadSource("smoke/wrapped_player.c", """
                int value() {
                    return 1;
                }
                """);
        StringBuilder output = new StringBuilder();

        runtime.bindSession("s1", player.instance(), "127.0.0.1", output::append);
        var session = runtime.sessionRecord("s1").orElseThrow();

        assertTrue(runtime.messageSession(session.id(), "one two three four five six\\n"));
        assertEquals("one two three four\nfive six\n", output.toString());
        assertEquals(output.toString(), runtime.outputTranscript());
    }

    @Test
    void runtimeMessagesPlayerBeforePersonaAttachmentThenPreservesPlayerOnAttach() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        StringBuilder output = new StringBuilder();

        runtime.bindPlayerSession("s1", " 127.0.0.1 ", output::append);

        var loginSession = runtime.sessionRecord("s1").orElseThrow();
        var loginPlayer = runtime.playerRecordForSession("s1").orElseThrow();
        assertEquals(loginPlayer.id(), loginSession.playerId());
        assertTrue(loginSession.attachedPersonaId().isEmpty());
        assertTrue(loginPlayer.activePersonaId().isEmpty());
        assertEquals("127.0.0.1", loginSession.remoteAddress().orElseThrow());
        assertTrue(runtime.messagePlayer(loginPlayer.id(), "login-control\\n"));
        assertEquals("login-control\n", output.toString());

        LPCObjectHandle player = runtime.loadSource("smoke/login_player.c", """
                int value() {
                    return 1;
                }
                """);
        runtime.bindSession("s1", player.instance(), "127.0.0.1", output::append);

        var attachedSession = runtime.sessionRecord("s1").orElseThrow();
        var attachedPlayer = runtime.playerRecordForSession("s1").orElseThrow();
        var persona = runtime.personaRecordForProjection(player.instance()).orElseThrow();
        assertEquals(loginPlayer.id(), attachedPlayer.id());
        assertEquals(loginSession.connectedAt(), attachedSession.connectedAt());
        assertEquals(persona.id(), attachedSession.attachedPersonaId().orElseThrow());
        assertEquals(persona.id(), attachedPlayer.activePersonaId().orElseThrow());
        assertTrue(runtime.messagePersona(persona.id(), "persona-gameplay\\n"));
        assertEquals("login-control\npersona-gameplay\n", output.toString());
    }

    @Test
    void runtimeBindsCombinedMudlibProjectionToPlayerAndPersonaRecords() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle player = runtime.loadSource("obj/player.c", """
                int value() {
                    return 1;
                }
                """);
        StringBuilder output = new StringBuilder();
        MudlibProjection projection = MudlibProjection.combinedPlayerPersona("obj/player", player.instance());

        runtime.bindSession("s1", player.instance(), "127.0.0.1", output::append, projection);

        var playerRecord = runtime.playerRecordForSession("s1").orElseThrow();
        var personaRecord = runtime.personaRecordForProjection(player.instance()).orElseThrow();
        assertEquals(projection, playerRecord.mudlibProfileProjection().orElseThrow());
        assertEquals(projection, personaRecord.mudlibBehaviorProjection().orElseThrow());
        assertTrue(projection.hasRole(MudlibProjectionRole.PLAYER_PROFILE));
        assertTrue(projection.hasRole(MudlibProjectionRole.PERSONA_BEHAVIOR));
        assertTrue(projection.hasRole(MudlibProjectionRole.COMBINED_PLAYER_PERSONA));

        assertTrue(runtime.messagePersona(personaRecord.id(), "projected-persona\\n"));
        assertTrue(runtime.messagePersona(personaRecord.id(), "\\tindented\\n"));
        assertEquals("projected-persona\n\tindented\n", output.toString());
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

                string epoch() {
                    return jvmud_format_time(86400);
                }
                """);

        assertEquals("Welcome to JVMud.\n", reader.invoke("welcome"));
        assertEquals(0, reader.invoke("escaped"));
        assertEquals("mixed", reader.invoke("lower"));
        assertEquals("Alice", reader.invoke("capitalized"));
        assertTrue(((String) reader.invoke("epoch")).contains("1970"));
    }

    @Test
    void runtimeReadsMudlibTextFromConfiguredSourceRoot() throws Exception {
        Path configRoot = tempDir.resolve("jvmud");
        Path sourceRoot = tempDir.resolve("source");
        Files.createDirectories(configRoot);
        Files.createDirectories(sourceRoot.resolve("doc"));
        Files.writeString(sourceRoot.resolve("doc/help"), "COMMUNICATIONS:\n");

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(configRoot).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mudlibRootPath(sourceRoot)
                .build());

        assertEquals("COMMUNICATIONS:\n", runtime.readMudlibText("/doc/help"));
        assertEquals(0, runtime.readMudlibText("../source/doc/help"));
    }

    @Test
    void runtimeAppendsMudlibTextUnderConfiguredSourceRoot() throws Exception {
        Path configRoot = tempDir.resolve("jvmud");
        Path sourceRoot = tempDir.resolve("source");
        Files.createDirectories(configRoot);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(configRoot).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mudlibRootPath(sourceRoot)
                .build());
        EngineEfuns.registerCore(runtime);
        LPCObjectHandle logger = runtime.loadSource("logger.c", """
                int log_it(string value) {
                    return jvmud_append_mudlib_text("/log/RUNTIME", value + "\\n");
                }

                int bad_path() {
                    return jvmud_append_mudlib_text("../outside", "nope\\n");
                }
                """);

        assertEquals(1, logger.invoke("log_it", "first"));
        assertEquals(1, logger.invoke("log_it", "second"));
        assertEquals(0, logger.invoke("bad_path"));
        assertEquals("first\nsecond\n", Files.readString(sourceRoot.resolve("log/RUNTIME")));
        assertFalse(Files.exists(tempDir.resolve("outside")));
    }

    @Test
    void objectDestructionCanNotifyMudlibBeforeCleanup() throws Exception {
        LPCRuntime runtime = destructionRuntime("""
                mixed prepare_destruct(mixed ob) {
                    jvmud_append_mudlib_text("/log/DESTRUCT", "prepare " + jvmud_entity_id(ob) + "\\n");
                    return 0;
                }
                """);
        LPCObjectHandle object = runtime.loadSource("obj/victim.c", """
                void destroy_self() {
                    jvmud_destroy_entity(jvmud_current_entity());
                }
                """);

        object.invoke("destroy_self");

        assertNull(runtime.objectId(object.instance()));
        assertEquals("prepare obj/victim\n", Files.readString(tempDir.resolve("log/DESTRUCT")));
    }

    @Test
    void objectDestructionCanBeVetoedByMudlib() throws Exception {
        LPCRuntime runtime = destructionRuntime("""
                mixed prepare_destruct(mixed ob) {
                    jvmud_append_mudlib_text("/log/DESTRUCT", "veto " + jvmud_entity_id(ob) + "\\n");
                    return 1;
                }
                """);
        LPCObjectHandle object = runtime.loadSource("obj/vetoed.c", """
                void destroy_self() {
                    jvmud_destroy_entity(jvmud_current_entity());
                }
                """);

        object.invoke("destroy_self");

        assertEquals("obj/vetoed", runtime.objectId(object.instance()));
        assertEquals("veto obj/vetoed\n", Files.readString(tempDir.resolve("log/DESTRUCT")));
    }

    @Test
    void objectDestructionPreparationCanMoveContentsBeforeCleanup() throws Exception {
        LPCRuntime runtime = destructionRuntime("""
                mixed prepare_destruct(mixed ob) {
                    object item;
                    object super;

                    super = jvmud_entity_location(ob);
                    if (super) {
                        while (item = jvmud_first_entity_at(ob))
                            jvmud_move_entity(item, super);
                    }

                    return 0;
                }
                """);
        LPCObjectHandle room = runtime.loadSource("room/workshop.c", "");
        LPCObjectHandle box = runtime.loadSource("obj/box.c", """
                void destroy_self() {
                    jvmud_destroy_entity(jvmud_current_entity());
                }
                """);
        LPCObjectHandle gem = runtime.loadSource("obj/gem.c", "");
        runtime.moveObject(box.instance(), room.instance());
        runtime.moveObject(gem.instance(), box.instance());

        box.invoke("destroy_self");

        assertNull(runtime.objectId(box.instance()));
        assertEquals(room.instance(), runtime.environment(gem.instance()));
    }

    @Test
    void lpcObjectStatePersistenceWritesJsonAndRestoresLegacyProperties() throws Exception {
        Files.createDirectories(tempDir.resolve("players"));

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LPCObjectHandle stateful = runtime.loadSource("obj/stateful.c", """
                string name;
                int level;
                mixed note;

                void populate() {
                    name = "alice";
                    level = 7;
                    note = "hello";
                    jvmud_save_lpc_object_state("players/alice");
                }

                void clear() {
                    name = "";
                    level = 0;
                    note = 0;
                }

                void restore() {
                    jvmud_restore_lpc_object_state("players/alice");
                }

                string query_name() {
                    return name;
                }

                int query_level() {
                    return level;
                }

                mixed query_note() {
                    return note;
                }
                """);

        stateful.invoke("populate");
        String json = Files.readString(tempDir.resolve("players/alice.o"));
        assertTrue(json.contains("\"format\""), json);
        assertTrue(json.contains("\"jvmud.lpc-object-state\""), json);
        assertTrue(json.contains("\"obj.stateful.name\""), json);
        assertTrue(json.contains("\"value\""), json);
        assertTrue(json.contains("\"alice\""), json);

        stateful.invoke("clear");
        stateful.invoke("restore");
        assertEquals("alice", stateful.invoke("query_name"));
        assertEquals(7, stateful.invoke("query_level"));
        assertEquals("hello", stateful.invoke("query_note"));

        Files.writeString(tempDir.resolve("players/alice.o"), """
                obj.stateful.name=string\\:legacy
                obj.stateful.level=int\\:4
                obj.stateful.note=null\\:
                """);
        stateful.invoke("clear");
        stateful.invoke("restore");
        assertEquals("legacy", stateful.invoke("query_name"));
        assertEquals(4, stateful.invoke("query_level"));
        assertNull(stateful.invoke("query_note"));
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

                string ctime(int timestamp) {
                    return jvmud_format_time(timestamp);
                }

                string extract(mixed value, int from) {
                    return jvmud_extract_text(value, from);
                }

                string extract(mixed value, int from, int to) {
                    return jvmud_extract_text(value, from, to);
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                int show_welcome() {
                    return cat("/WELCOME");
                }

                string normalized() {
                    return capitalize(lower_case("ALICE"));
                }

                string epoch() {
                    return ctime(86400);
                }

                string slice() {
                    return extract("abcdef", 1, 3) + ":" + extract("abcdef", 3);
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
        assertTrue(((String) caller.invoke("epoch")).contains("1970"));
        assertEquals("bcd:def", caller.invoke("slice"));
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
    void previousObjectReportsCallingObjectAcrossMfunInvocation() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                mixed call_other(mixed target, string method) {
                    return jvmud_invoke_entity(target, method);
                }

                string object_name(mixed ob) {
                    return jvmud_entity_id(ob);
                }

                object previous_object() {
                    return jvmud_previous_entity();
                }
                """);
        Files.writeString(tempDir.resolve("target.c"), """
                string caller_name() {
                    return object_name(previous_object());
                }

                string self_name() {
                    return object_name(previous_object());
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                string value() {
                    return call_other("target", "caller_name");
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));
        LPCObjectHandle target = runtime.load(tempDir.resolve("target.c"));

        assertEquals("caller", caller.invoke("value"));
        assertEquals("target", target.invoke("self_name"));
    }

    @Test
    void commandDispatchTreatsActorMovementAsHandledWhenMudlibReturnsZeroish() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void add_action(string method, string verb) {
                    jvmud_add_action(method, verb);
                }

                mixed call_other(mixed target, string method, mixed arg) {
                    return jvmud_invoke_entity(target, method, arg);
                }

                void move_object(mixed ob, mixed destination) {
                    jvmud_move_entity(ob, destination);
                }

                object this_player() {
                    return jvmud_current_actor();
                }

                object this_object() {
                    return jvmud_current_entity();
                }
                """);
        Files.writeString(tempDir.resolve("player.c"), """
                mixed move_player(mixed dir_dest) {
                    move_object(this_object(), "room/next");
                }
                """);
        Files.writeString(tempDir.resolve("room/start.c"), """
                void init() {
                    add_action("move", "west");
                }

                mixed move(mixed str) {
                    return this_player()->move_player("west#room/next");
                }
                """);
        Files.writeString(tempDir.resolve("room/next.c"), """
                string short() {
                    return "next room";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build());

        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));
        LPCObjectHandle start = runtime.load(tempDir.resolve("room/start.c"));
        Object next = runtime.load(tempDir.resolve("room/next.c")).instance();
        runtime.moveObject(player.instance(), start.instance());

        runtime.refreshCommandActions(player.instance());

        assertEquals(1, runtime.dispatchCommand(player.instance(), "west"));
        assertEquals(next, runtime.environment(player.instance()));
    }

    @Test
    void commandDispatchSupportsPrefixActions() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void add_action(string method, string verb, int flag) {
                    jvmud_add_action(method, verb, flag);
                }

                void write(mixed value) {
                    jvmud_write(value);
                }
                """);
        Files.writeString(tempDir.resolve("player.c"), """
                int value;

                void init() {
                    add_action("examine", "exa", 1);
                }

                int examine(mixed str) {
                    write("examined " + str + "\\n");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("room/start.c"), """
                string short() {
                    return "start";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build());

        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));
        LPCObjectHandle start = runtime.load(tempDir.resolve("room/start.c"));
        runtime.moveObject(player.instance(), start.instance());

        runtime.refreshCommandActions(player.instance());

        assertEquals(1, runtime.dispatchCommand(player.instance(), "examine book"));
        assertEquals("examined book\n", runtime.outputTranscript());
    }

    @Test
    void commandDispatchKeepsPersistentActorActionsAcrossRefresh() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void add_action(string method, string verb) {
                    jvmud_add_action(method, verb);
                }

                void write(mixed value) {
                    jvmud_write(value);
                }
                """);
        Files.writeString(tempDir.resolve("player.c"), """
                void setup() {
                    add_action("examine", "exa");
                }

                int examine(mixed str) {
                    write("examined " + str + "\\n");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("room/start.c"), """
                void init() {
                    add_action("wave", "wave");
                }

                int wave(mixed str) {
                    write("waved\\n");
                    return 1;
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build());

        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));
        LPCObjectHandle start = runtime.load(tempDir.resolve("room/start.c"));
        runtime.moveObject(player.instance(), start.instance());
        runtime.withCommandActor(player.instance(), () -> player.invoke("setup"));

        runtime.refreshCommandActions(player.instance());
        runtime.clearOutputTranscript();

        assertEquals(1, runtime.dispatchCommand(player.instance(), "exa book"));
        assertEquals("examined book\n", runtime.outputTranscript());

        runtime.clearOutputTranscript();
        assertEquals(1, runtime.dispatchCommand(player.instance(), "wave"));
        assertEquals("waved\n", runtime.outputTranscript());
    }

    @Test
    void addVerbKeepsScopedActionTransientAcrossRefresh() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void add_action(string method) {
                    jvmud_add_action(method);
                }

                void add_verb(string verb) {
                    jvmud_add_verb(verb);
                }

                mixed call_other(mixed target, string method, mixed arg) {
                    return jvmud_invoke_entity(target, method, arg);
                }

                void move_object(mixed ob, mixed destination) {
                    jvmud_move_entity(ob, destination);
                }

                object this_object() {
                    return jvmud_current_entity();
                }

                object this_player() {
                    return jvmud_current_actor();
                }

                void write(mixed value) {
                    jvmud_write(value);
                }
                """);
        Files.writeString(tempDir.resolve("player.c"), """
                mixed move_player(mixed dir_dest) {
                    move_object(this_object(), "room/next");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("room/start.c"), """
                void init() {
                    add_action("move");
                    add_verb("west");
                }

                int move() {
                    write("old room moved\\n");
                    return this_player()->move_player("west#room/next");
                }
                """);
        Files.writeString(tempDir.resolve("room/next.c"), """
                string short() {
                    return "next room";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build());

        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));
        LPCObjectHandle start = runtime.load(tempDir.resolve("room/start.c"));
        LPCObjectHandle next = runtime.load(tempDir.resolve("room/next.c"));
        runtime.moveObject(player.instance(), start.instance());

        runtime.refreshCommandActions(player.instance());
        assertEquals(1, runtime.dispatchCommand(player.instance(), "west"));
        assertEquals(next.instance(), runtime.environment(player.instance()));

        runtime.clearOutputTranscript();
        runtime.refreshCommandActions(player.instance());
        assertEquals(0, runtime.dispatchCommand(player.instance(), "west"));
        assertEquals("", runtime.outputTranscript());
    }

    @Test
    void forwardDeclaredMethodDefinitionUsesDefinitionParameterLocals() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/prototype_args.c", """
                static void helper(string value);

                int result;

                int call_helper(string value) {
                    result = 0;
                    helper(value);
                    return result;
                }

                static void helper(string value) {
                    if (value == "stick")
                        result = 1;
                }
                """);

        assertEquals(1, object.invoke("call_helper", "stick"));
    }

    @Test
    void moveEntityResolvesStringSourcePaths() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void move_object(mixed ob, mixed destination) {
                    jvmud_move_entity(ob, destination);
                }

                object present(mixed id, mixed container) {
                    return jvmud_find_entity(id, container);
                }

                object this_object() {
                    return jvmud_current_entity();
                }
                """);
        Files.writeString(tempDir.resolve("obj/book.c"), """
                int id(string str) {
                    return str == "book";
                }
                """);
        Files.writeString(tempDir.resolve("room.c"), """
                void setup() {
                    move_object("obj/book", this_object());
                }

                object find_book() {
                    return present("book", this_object());
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle room = runtime.load(tempDir.resolve("room.c"));
        room.invoke("setup");

        assertNotNull(room.invoke("find_book"));
    }

    @Test
    void stringConcatCoercesMixedArrayElementsWithoutCasting() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LPCObjectHandle object = runtime.loadSource("smoke/mixed_concat.c", """
                string route(int i) {
                    mixed *dest_dir;

                    dest_dir = ({
                        "room/mountain/hump", "east",
                        "room/forest/forest1", "west"
                    });

                    return dest_dir[i] + "#" + dest_dir[i-1];
                }
                """);

        assertEquals("west#room/forest/forest1", object.invoke("route", 3));
    }

    @Test
    void callOtherZeroArgumentIsPassedWhenTargetAcceptsOneArgument() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                mixed call_other(mixed target, string method, mixed arg) {
                    return jvmud_invoke_entity(target, method, arg);
                }
                """);
        Files.writeString(tempDir.resolve("target.c"), """
                int value;

                void set_value(mixed v) {
                    value = v;
                }

                int query_value() {
                    return value;
                }

                string no_arg() {
                    return "called";
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                void set_zero(object target) {
                    call_other(target, "set_value", 0);
                }

                string call_no_arg(object target) {
                    return call_other(target, "no_arg", 0);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle target = runtime.load(tempDir.resolve("target.c"));
        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        caller.invoke("set_zero", target.instance());

        assertEquals(0, target.invoke("query_value"));
        assertEquals("called", caller.invoke("call_no_arg", target.instance()));
    }

    @Test
    void callOtherPassesMultipleArgumentsThroughEntityInvocation() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                mixed call_other(mixed target, string method, mixed arg1, mixed arg2) {
                    return jvmud_invoke_entity(target, method, arg1, arg2);
                }
                """);
        Files.writeString(tempDir.resolve("target.c"), """
                mixed combine(mixed left, mixed right) {
                    return left + ":" + right;
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                mixed combine(object target) {
                    return call_other(target, "combine", 50, "chat");
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle target = runtime.load(tempDir.resolve("target.c"));
        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals("50:chat", caller.invoke("combine", target.instance()));
    }

    @Test
    void callOtherResolvesRegisteredStringObjectTargets() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                mixed call_other(mixed target, string method, mixed arg) {
                    return jvmud_invoke_entity(target, method, arg);
                }
                """);
        Files.writeString(tempDir.resolve("target.c"), """
                string heard;

                int matched(string value) {
                    heard = value;
                    return 1;
                }

                string query_heard() {
                    return heard;
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                int relay(string value) {
                    return call_other("target", "matched", value);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle target = runtime.load(tempDir.resolve("target.c"));
        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals(1, caller.invoke("relay", "hello"));
        assertEquals("hello", target.invoke("query_heard"));
    }

    @Test
    void callOtherReturnsZeroForMissingMethod() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                mixed call_other(mixed target, string method, mixed arg) {
                    return jvmud_invoke_entity(target, method, arg);
                }
                """);
        Files.writeString(tempDir.resolve("target.c"), """
                string short() {
                    return "target";
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                mixed missing(object target) {
                    return call_other(target, "can_put_and_get", "book");
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle target = runtime.load(tempDir.resolve("target.c"));
        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals(0, caller.invoke("missing", target.instance()));
    }

    @Test
    void callOtherTrimsExtraArgumentsForOptionalInvocation() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                mixed call_other(mixed target, string method, mixed arg) {
                    return jvmud_invoke_entity(target, method, arg);
                }

                void write(mixed value) {
                    jvmud_write(value);
                }
                """);
        Files.writeString(tempDir.resolve("target.c"), """
                void long() {
                    write("target long\\n");
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                void examine(object target) {
                    call_other(target, "long", "target");
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle target = runtime.load(tempDir.resolve("target.c"));
        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        caller.invoke("examine", target.instance());

        assertEquals("target long\n", runtime.outputTranscript());
    }

    @Test
    void transferMfunMovesEntityAndReturnsSuccessCode() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                int transfer(mixed ob, mixed destination) {
                    jvmud_move_entity(ob, destination);
                    return 0;
                }
                """);
        Files.writeString(tempDir.resolve("thing.c"), """
                string short() {
                    return "thing";
                }
                """);
        Files.writeString(tempDir.resolve("room.c"), """
                int value() {
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                int move_it(object thing, object room) {
                    return transfer(thing, room);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle thing = runtime.load(tempDir.resolve("thing.c"));
        LPCObjectHandle room = runtime.load(tempDir.resolve("room.c"));
        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals(0, caller.invoke("move_it", thing.instance(), room.instance()));
        assertEquals(room.instance(), runtime.environment(thing.instance()));
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
    void inheritedMudlibObjectCanDelegateToSharedObjectByStringPath() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                string object_name(mixed ob) {
                    return jvmud_entity_id(ob);
                }

                int pointerp(mixed value) {
                    return jvmud_is_array(value);
                }

                object this_object() {
                    return jvmud_current_entity();
                }
                """);
        Files.writeString(tempDir.resolve("room/room.c"), """
                string *numbers;

                string convert_number(int n) {
                    if (!pointerp(numbers))
                        numbers = query_numbers();
                    if (n > 9)
                        return "lot of";
                    return numbers[n];
                }

                string *query_numbers() {
                    if (!numbers) {
                        if (object_name(this_object()) == "room/room")
                            numbers = ({"no", "one", "two", "three"});
                        else
                            numbers = "room/room"->query_numbers();
                    }
                    return numbers;
                }
                """);
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                inherit "room/room";

                string visible_exits() {
                    return convert_number(3);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());
        LPCObjectHandle child = runtime.load(tempDir.resolve("room/village/vill_green.c"));

        assertEquals("three", child.invoke("visible_exits"));
    }

    @Test
    void runtimeUsesBoundaryMudlibRootForAbsoluteInherits() throws Exception {
        Path mudlibRoot = tempDir.resolve("realms");
        Files.createDirectories(mudlibRoot.resolve("areas"));
        Files.createDirectories(mudlibRoot.resolve("lib/environment"));
        Files.writeString(mudlibRoot.resolve("lib/environment/environment.c"), """
                int environment_value() {
                    return 30;
                }
                """);
        Files.writeString(mudlibRoot.resolve("areas/example.c"), """
                inherit "/lib/environment/environment.c";

                int child_value() {
                    return environment_value() + 12;
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir.resolve("wrong-root"))
                .build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mudlibRootPath(mudlibRoot)
                .build());

        LPCObjectHandle child = runtime.load(mudlibRoot.resolve("areas/example.c"));

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
    void dynamicDispatchReturnsZeroForMissingMethod() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LPCObjectHandle object = runtime.loadSource("smoke/missing_dynamic.c", """
                mixed reflected_value(mixed target) {
                    return target->missing_method();
                }
                """);

        assertEquals(0, object.invoke("reflected_value", object.instance()));
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
    void sscanfAssignsCapturesWithDynamicFormatString() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);

        LPCObjectHandle object = runtime.loadSource("smoke/sscanf_dynamic.c", """
                string type;
                string match;

                void configure(string value) {
                    type = value;
                    match = "grrr";
                }

                string format() {
                    return "%s " + type + " %s\\n";
                }

                int parse(string value) {
                    string who, rest;
                    if (sscanf(value, format(), who, rest) != 2)
                        return 0;
                    return rest == match;
                }

                string parsed(string value) {
                    string who, rest;
                    if (sscanf(value, format(), who, rest) != 2)
                        return "bad";
                    return who + ":" + rest + ":" + match;
                }
                """);

        object.invoke("configure", "says:");

        assertEquals("%s says: %s\n", object.invoke("format"));
        assertEquals("Orc:grrr:grrr", object.invoke("parsed", "Orc says: grrr\n"));
        assertEquals(1, object.invoke("parse", "Orc says: grrr\n"));
        assertEquals(0, object.invoke("parse", "Orc says: hello\n"));
    }

    @Test
    void sscanfDoesNotAssignMissingOutputLocalCaptures() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);

        LPCObjectHandle object = runtime.loadSource("smoke/sscanf_no_match.c", """
                int parse(string value) {
                    int number;
                    number = 7;
                    if (sscanf(value, "r %d", number) == 1)
                        return number;
                    return number;
                }
                """);

        assertEquals(42, object.invoke("parse", "r 42"));
        assertEquals(7, object.invoke("parse", "quit"));
    }

    @Test
    void sscanfDoesNotAssignMissingOutputFieldCaptures() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);

        LPCObjectHandle object = runtime.loadSource("smoke/sscanf_no_field_match.c", """
                int number;

                int parse(string value) {
                    number = 7;
                    if (sscanf(value, "r %d", number) == 1)
                        return number;
                    return number;
                }
                """);

        assertEquals(42, object.invoke("parse", "r 42"));
        assertEquals(7, object.invoke("parse", "quit"));
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
    void setHeartBeatSchedulesCurrentObjectEveryWorldTickByDefault() {
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
        assertEquals(1, object.invoke("query_ticks"));

        scheduler.advanceTo(2);
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
    void deferredCallbackSchedulesCurrentEntityOnce() {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntime(scheduler, 1);

        LPCObjectHandle object = runtime.loadSource("smoke/callout_once.c", """
                int calls;

                void start() {
                    call_out("finish", 3);
                }

                void finish() {
                    calls += 1;
                }

                int query_calls() {
                    return calls;
                }
                """);

        object.invoke("start");
        scheduler.advanceTo(2);
        assertEquals(0, object.invoke("query_calls"));

        scheduler.advanceTo(3);
        assertEquals(1, object.invoke("query_calls"));

        scheduler.advanceTo(10);
        assertEquals(1, object.invoke("query_calls"));
    }

    @Test
    void deferredCallbackDeliversOneArgument() {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntime(scheduler, 1);

        LPCObjectHandle object = runtime.loadSource("smoke/callout_arg.c", """
                int value;

                void start() {
                    call_out("finish", 2, 7);
                }

                void finish(mixed arg) {
                    value = arg;
                }

                int query_value() {
                    return value;
                }
                """);

        object.invoke("start");
        scheduler.advanceTo(2);

        assertEquals(7, object.invoke("query_value"));
    }

    @Test
    void cancelDeferredCallbackCancelsScheduledWork() {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntime(scheduler, 1);

        LPCObjectHandle object = runtime.loadSource("smoke/callout_cancel.c", """
                int calls;
                int removed;

                void start() {
                    call_out("finish", 2);
                    removed = remove_call_out("finish");
                }

                void finish() {
                    calls += 1;
                }

                int query_calls() {
                    return calls;
                }

                int query_removed() {
                    return removed;
                }
                """);

        object.invoke("start");
        scheduler.advanceTo(5);

        assertEquals(0, object.invoke("query_removed"));
        assertEquals(0, object.invoke("query_calls"));
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
    void destructCancelsDeferredCallback() {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntime(scheduler, 1);

        LPCObjectHandle object = runtime.loadSource("smoke/destructed_deferred_callback.c", """
                int calls;

                void start() {
                    call_out("finish", 2);
                }

                void finish() {
                    calls += 1;
                }

                int query_calls() {
                    return calls;
                }
                """);

        object.invoke("start");
        runtime.destructObject(object.instance());
        scheduler.advanceTo(2);

        assertEquals(0, object.invoke("query_calls"));
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
    void recurringTickFailureNotifiesMudlibAndCancelsHeartbeat() throws Exception {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntimeWithErrorHandlers(scheduler);
        LPCObjectHandle object = runtime.loadSource("smoke/failing_tick.c", """
                int beats;

                void start() {
                    set_heart_beat(1);
                }

                void heart_beat() {
                    int divisor;

                    beats += 1;
                    beats = beats / divisor;
                }

                int query_beats() {
                    return beats;
                }
                """);

        object.invoke("start");
        scheduler.advanceTo(1);
        scheduler.advanceTo(3);

        assertEquals(1, object.invoke("query_beats"));
        String runtimeLog = Files.readString(tempDir.resolve("log/RUNTIME"));
        assertTrue(runtimeLog.contains("context=scheduled_tick operation=heart_beat"), runtimeLog);
        assertTrue(runtimeLog.contains("/ by zero"), runtimeLog);
        String heartBeatLog = Files.readString(tempDir.resolve("log/HEART_BEAT"));
        assertEquals(1, countOccurrences(heartBeatLog, "culprit=smoke/failing_tick"));
        assertTrue(heartBeatLog.contains("/ by zero"), heartBeatLog);
    }

    @Test
    void deferredCallbackFailureNotifiesMudlibRuntimeError() throws Exception {
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = temporalRuntimeWithErrorHandlers(scheduler);
        LPCObjectHandle object = runtime.loadSource("smoke/failing_callout.c", """
                int calls;

                void start() {
                    call_out("finish", 2);
                }

                void finish() {
                    int divisor;

                    calls += 1;
                    calls = calls / divisor;
                }

                int query_calls() {
                    return calls;
                }
                """);

        object.invoke("start");
        scheduler.advanceTo(2);

        assertEquals(1, object.invoke("query_calls"));
        String runtimeLog = Files.readString(tempDir.resolve("log/RUNTIME"));
        assertTrue(runtimeLog.contains("context=deferred_callback operation=finish"), runtimeLog);
        assertTrue(runtimeLog.contains("/ by zero"), runtimeLog);
        assertFalse(Files.exists(tempDir.resolve("log/HEART_BEAT")));
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
    void pipelineTreatsUntypedObjectMethodsAsMixed() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                value() {
                    return 42;
                }
                """);

        assertTrue(result.getProblems().isEmpty());
        assertNotNull(result.getBytecode());
    }

    @Test
    void pipelineTreatsUntypedMethodParametersAsMixed() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                mixed value(arg) {
                    return arg;
                }
                """);

        assertTrue(result.getProblems().isEmpty());
        assertNotNull(result.getBytecode());
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
                int id(mixed str, mixed lvl) {
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
    void fieldCanShareNameWithMethod() throws Exception {
        Files.writeString(tempDir.resolve("corpse_shape.c"), """
                int decay;

                void reset() {
                    decay = 2;
                }

                void decay() {
                    decay -= 1;
                }

                string short() {
                    if (decay < 2)
                        return "decayed";

                    return "fresh";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.load(tempDir.resolve("corpse_shape.c"));

        object.invoke("reset");
        assertEquals("fresh", object.invoke("short"));

        object.invoke("decay");

        assertEquals("decayed", object.invoke("short"));
    }

    @Test
    void randomShimDelegatesToEngineRandom() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                int random(int max) {
                    return jvmud_random(max);
                }
                """);
        Files.writeString(tempDir.resolve("roller.c"), """
                int saw_nonzero() {
                    int i;

                    i = 0;
                    while (i < 100) {
                        if (random(2) == 1)
                            return 1;
                        i += 1;
                    }
                    return 0;
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder().mfunObjectPath("jvmud/mfuns").build());

        LPCObjectHandle roller = runtime.load(tempDir.resolve("roller.c"));

        assertEquals(1, roller.invoke("saw_nonzero"));
    }

    @Test
    void presentWithObjectIdentifierFindsSameRoomObjects() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                object present(mixed id) {
                    return jvmud_find_entity(id);
                }
                """);
        Files.writeString(tempDir.resolve("room.c"), """
                """);
        Files.writeString(tempDir.resolve("actor.c"), """
                object seen;

                void remember(object ob) {
                    seen = present(ob);
                }

                object seen_object() {
                    return seen;
                }
                """);
        Files.writeString(tempDir.resolve("target.c"), """
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder().mfunObjectPath("jvmud/mfuns").build());
        LPCObjectHandle room = runtime.load(tempDir.resolve("room.c"));
        LPCObjectHandle actor = runtime.load(tempDir.resolve("actor.c"));
        Object target = runtime.cloneObject("target");
        runtime.moveObject(actor.instance(), room.instance());
        runtime.moveObject(target, room.instance());

        actor.invoke("remember", target);

        assertEquals(target, actor.invoke("seen_object"));
    }

    @Test
    void movingObjectIntoLivingInventoryInvokesInteractionLifecycle() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void enable_commands() {
                    jvmud_enable_commands();
                }

                object environment(mixed ob) {
                    return jvmud_entity_location(ob);
                }

                object this_object() {
                    return jvmud_current_entity();
                }

                void move_object(mixed ob, mixed destination) {
                    jvmud_move_entity(ob, destination);
                }
                """);
        Files.writeString(tempDir.resolve("room.c"), """
                """);
        Files.writeString(tempDir.resolve("death_room.c"), """
                """);
        Files.writeString(tempDir.resolve("actor.c"), """
                void setup() {
                    enable_commands();
                }
                """);
        Files.writeString(tempDir.resolve("death_mark.c"), """
                void init() {
                    move_object(environment(this_object()), "death_room");
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build());
        LPCObjectHandle room = runtime.load(tempDir.resolve("room.c"));
        LPCObjectHandle deathRoom = runtime.load(tempDir.resolve("death_room.c"));
        LPCObjectHandle actor = runtime.load(tempDir.resolve("actor.c"));
        Object deathMark = runtime.cloneObject("death_mark");
        runtime.moveObject(actor.instance(), room.instance());
        runtime.bindSession("test/session", actor.instance(), "127.0.0.1", ignored -> {});
        actor.invoke("setup");

        runtime.moveObject(deathMark, actor.instance());

        assertEquals(deathRoom.instance(), runtime.environment(actor.instance()));
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

                void call_out(string method, int delay) {
                    jvmud_schedule_deferred_callback(method, delay);
                }

                void call_out(string method, int delay, mixed arg) {
                    jvmud_schedule_deferred_callback(method, delay, arg);
                }

                int remove_call_out(string method) {
                    return jvmud_cancel_deferred_callback(method);
                }
                """);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .temporalTickMethod("heart_beat")
                .temporalTickIntervalSeconds(defaultIntervalSeconds)
                .build());
        return runtime;
    }

    private LPCRuntime temporalRuntimeWithErrorHandlers(WorldScheduler scheduler) {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.setScheduler(scheduler);
        runtime.loadSource("jvmud/mfuns.c", """
                void set_heart_beat(int enabled) {
                    jvmud_schedule_recurring_tick(enabled, 0);
                }

                void call_out(string method, int delay) {
                    jvmud_schedule_deferred_callback(method, delay);
                }
                """);
        runtime.loadSource("jvmud/mudlib.c", """
                void runtime_error(mixed actor, mixed context, mixed operation, mixed detail) {
                    jvmud_append_mudlib_text("/log/RUNTIME", "context=" + context + " operation=" + operation + "\\n");
                    jvmud_append_mudlib_text("/log/RUNTIME", detail + "\\n");
                }

                mixed heart_beat_error(mixed culprit, mixed err, mixed prg, mixed curobj, mixed line) {
                    jvmud_append_mudlib_text("/log/HEART_BEAT", "culprit=" + curobj + " program=" + prg + " line=" + line + "\\n");
                    jvmud_append_mudlib_text("/log/HEART_BEAT", err + "\\n");
                    return 0;
                }
                """);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .boundaryObjectPath("jvmud/mudlib")
                .temporalTickMethod("heart_beat")
                .temporalTickIntervalSeconds(1)
                .lifecycleMethod(MudlibLifecycleEvent.RUNTIME_ERROR, "runtime_error")
                .lifecycleMethod(MudlibLifecycleEvent.SCHEDULED_TICK_ERROR, "heart_beat_error")
                .build());
        return runtime;
    }

    private LPCRuntime destructionRuntime(String mudlibSource) {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        runtime.loadSource("jvmud/mudlib.c", mudlibSource);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .boundaryObjectPath("jvmud/mudlib")
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_DESTRUCTION_REQUESTED, "prepare_destruct")
                .build());
        return runtime;
    }

    private int countOccurrences(String text, String needle) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) != -1) {
            count++;
            index += needle.length();
        }
        return count;
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
