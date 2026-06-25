package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeException;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.compiler.preproc.Preprocessor;
import io.github.protasm.jvmud.compiler.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.compiler.runtime.RuntimeCallable;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.compiler.runtime.RuntimeFunctionLiteral;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.engine.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.engine.mudlib.MudlibProjection;
import io.github.protasm.jvmud.engine.mudlib.MudlibProjectionRole;
import io.github.protasm.jvmud.engine.time.WorldScheduler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        assertTrue(result.getProblems().isEmpty(), () -> problemMessages(result));
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

        assertTrue(result.getProblems().isEmpty(), () -> problemMessages(result));
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
    void runtimeConcatenatesAdjacentStringLiterals() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/adjacent_strings.c", """
                string message() {
                    return "Your sensitive mind notices a wrongness in the "
                        "fabric of space.\\n";
                }
                """);

        assertEquals(
                "Your sensitive mind notices a wrongness in the fabric of space.\n",
                object.invoke("message"));
    }

    @Test
    void runtimeConcatenatesSeveralAdjacentStringLiterals() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/several_adjacent_strings.c", """
                string message() {
                    return "alpha" "beta"
                        "gamma";
                }
                """);

        assertEquals("alphabetagamma", object.invoke("message"));
    }

    @Test
    void preprocessorLeavesFunctionSymbolLiteralsAsSourceText() {
        String processed = Preprocessor.preprocess("""
                    #define HOOK_NAME loadUIDs
                mixed value() {
                    return ({#'HOOK_NAME, #'previous_object});
                }
                """);

        assertTrue(processed.contains("#'HOOK_NAME"));
        assertTrue(processed.contains("#'previous_object"));
        assertFalse(processed.contains("#define"));
    }

    @Test
    void parserAcceptsTypedFunctionLiteralsAsCallArguments() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                string tagReplaceCallback(string tag) {
                    return tag;
                }

                string value(string layout) {
                    return regreplace(
                        layout,
                        "\\\\[[A-Z0-9_ ]+\\\\]",
                        function string (string tag) { return tagReplaceCallback(tag); },
                        1
                    );
                }
                """);

        assertFalse(
                result.getProblems().stream().anyMatch(problem -> problem.getStage() == CompilationStage.PARSE),
                () -> result.getProblems().toString());
    }

    @Test
    void preprocessorEvaluatesIfExpressionsAfterDirectiveWhitespace() {
        String processed = Preprocessor.preprocess("""
                #if ! defined(enable_commands)
                int enabled() { return 1; }
                #endif
                #if 1
                int always() { return 1; }
                #endif
                """);

        assertTrue(processed.contains("enabled"));
        assertTrue(processed.contains("always"));
        assertFalse(processed.contains("#if"));
        assertFalse(processed.contains("#endif"));
    }

    @Test
    void pipelineAppliesMudlibBoundaryCompatibilityPredefines() {
        RuntimeContext context = new RuntimeContext(null);
        context.setMudlibBoundary(MudlibBoundary.builder()
                .compatibilityPredefine("__VERSION__", "\"JVMud compatibility\"")
                .compatibilityPredefine("__VERSION_MAJOR__", "3")
                .compatibilityPredefine("__VERSION_MINOR__", "6")
                .compatibilityPredefine("__VERSION_MICRO__", "3")
                .compatibilityFunctionPredefine("PROBE", "text_width", "0")
                .build());

        CompilationResult result = new CompilationPipeline("java/lang/Object", context).run("""
                string version() {
                    return __VERSION__;
                }

                int supports_text_width() {
                    return (__VERSION_MAJOR__ >= 3) && (__VERSION_MINOR__ >= 6)
                        && (__VERSION_MICRO__ >= 3) || PROBE(text_width);
                }
                """);

        assertTrue(result.getProblems().isEmpty(), () -> problemMessages(result));
        assertNotNull(result.getBytecode());
    }

    @Test
    void mudlibGlobalFunctionsCanComeFromInheritedGlobalObjectDeclarations() throws IOException {
        Path secure = tempDir.resolve("secure");
        Path simulatedEfuns = secure.resolve("simulated-efuns");
        Files.createDirectories(simulatedEfuns);
        Files.writeString(secure.resolve("simul_efun.c"), """
                virtual inherit "/secure/simulated-efuns/users.c";
                """);
        Files.writeString(simulatedEfuns.resolve("users.c"), """
                public nomask void addLiving(object creature) {
                }
                """);

        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(tempDir, List.of()));
        CoreEfuns.registerCore(context);
        context.setMudlibBoundary(MudlibBoundary.builder()
                .mudlibGlobalObjectPath("secure/simul_efun")
                .engineFunction("jvmud_current_lpc_object", "this_object")
                .build());

        assertNotNull(context.resolveEfun("addLiving", 1));

        CompilationResult result = new CompilationPipeline("java/lang/Object", context).run("""
                void create() {
                    addLiving(this_object());
                }
                """);

        assertTrue(result.getProblems().isEmpty(), () -> problemMessages(result));
        assertNotNull(result.getBytecode());
    }

    @Test
    void preprocessorDoesNotLetFunctionSymbolsConsumeLaterDirectives() {
        String processed = Preprocessor.preprocess("""
                void shutdown() {
                    map(efun::db_handles(), #'db_close); // closes handles
                }

                #if ! defined(enable_commands)
                int enabled() { return 1; }
                #endif
                """);

        assertTrue(processed.contains("#'db_close"));
        assertTrue(processed.contains("enabled"));
        assertFalse(processed.contains("#if"));
        assertFalse(processed.contains("#endif"));
    }

    @Test
    void runtimeParsesQuotedSymbolLiteralsWithoutBreakingCharacterLiterals() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/quoted_symbol.c", """
                mixed symbol() {
                    return 'item;
                }

                int character() {
                    return 'a';
                }
        """);

        assertEquals("item", object.invoke("symbol"));
        assertEquals((int) 'a', object.invoke("character"));
    }

    @Test
    void runtimeDecodesHexEscapesInStringLiterals() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/hex_escape.c", """
                string ansi() {
                    return "\\x1b[0;36m";
                }
                """);

        assertEquals("\u001b[0;36m", object.invoke("ansi"));
    }

    @Test
    void runtimeDecodesUnicodeEscapesInStringLiterals() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/unicode_escape.c", """
                string box_corner() {
                    return "\\u2554";
                }
                """);

        assertEquals("╔", object.invoke("box_corner"));
    }

    @Test
    void runtimeAppliesDriverCommandAliasesBeforeActionLookup() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/command_alias.c", """
                void create() {
                    jvmud_set_driver_hook(9, ([ "n": "north" ]));
                    jvmud_enable_commands();
                    jvmud_add_action("move", "north");
                }

                int move() {
                    jvmud_write(jvmud_current_verb());
                    return 1;
                }
                """);
        runtime.withCommandActor(object.instance(), () -> object.invoke("create"));
        runtime.clearOutputTranscript();

        assertEquals(1, runtime.dispatchCommand(object.instance(), "n"));
        assertEquals("north", runtime.outputTranscript());
    }

    @Test
    void runtimeFallsBackToEnvironmentMovementForExitVerbs() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle actor = runtime.loadSource("smoke/moving_actor.c", """
                void create() {
                    jvmud_enable_commands();
                }
                """);
        LPCObjectHandle room = runtime.loadSource("smoke/exit_room.c", """
                string *exits() {
                    return ({ "north" });
                }

                int move(string ignored) {
                    jvmud_write(jvmud_current_verb());
                    return 1;
                }
                """);
        runtime.moveObject(actor.instance(), room.instance());

        assertEquals(1, runtime.dispatchCommand(actor.instance(), "north"));
        assertEquals("north", runtime.outputTranscript());
    }

    @Test
    void runtimeMemberFindsArrayElementsByValue() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/member_array.c", """
                int contains_first() {
                    return jvmud_member(({ "strength", "dexterity" }), "strength") > -1;
                }

                int contains_second() {
                    return jvmud_member(({ "strength", "dexterity" }), "dexterity") > -1;
                }

                int misses_value() {
                    return jvmud_member(({ "strength", "dexterity" }), "wisdom") == -1;
                }
                """);

        assertEquals(1, object.invoke("contains_first"));
        assertEquals(1, object.invoke("contains_second"));
        assertEquals(1, object.invoke("misses_value"));
    }

    @Test
    void runtimeParsesFunctionReferencesAsCallableValues() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/function_reference.c", """
                function value() {
                    return #'moveHook;
                }

                mixed moveHook(mixed value) {
                    return value + 1;
                }
                """);

        Object callable = object.invoke("value");
        assertTrue(callable instanceof RuntimeCallable);
        assertEquals(8, ((RuntimeCallable) callable).call(new RuntimeContext(null), 7));
    }

    @Test
    void parserAcceptsFunctionReferencesAsCallArgumentsAfterQualifiedCalls() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                void shutdown() {
                    map(efun::db_handles(), #'db_close);
                }
                """);

        assertTrue(result.getProblems().stream()
                .noneMatch(problem -> problem.getStage().name().equals("PARSE")), () -> result.getProblems().toString());
    }

    @Test
    void parserTreatsDuplicateIdenticalModifiersAsIdempotent() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                public nomask nomask int value() {
                    return 7;
                }
                """);

        assertTrue(result.getProblems().isEmpty(), () -> result.getProblems().toString());
        assertNotNull(result.getBytecode());
    }

    @Test
    void runtimeSupportsTypedForInitializerDeclaration() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/typed_for_initializer.c", """
                int value() {
                    int total;

                    for (int i = 0; i < 4; i++)
                        total += i;

                    return total;
                }
                """);

        assertEquals(6, object.invoke("value"));
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
    void runtimeSupportsArraySubtraction() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/array_difference.c", """
                mixed value() {
                    return ({ "a", "b", "a", "c" }) - ({ "a", "d" });
                }
                """);

        assertEquals(List.of("b", "c"), object.invoke("value"));
    }

    @Test
    void runtimeSupportsArraySubtractionFromMixedArrayValue() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mixed_array_difference.c", """
                mixed value() {
                    mixed values;

                    values = ({ "a", "b", "c" });
                    return values - ({ "b" });
                }
                """);

        assertEquals(List.of("a", "c"), object.invoke("value"));
    }

    @Test
    void runtimeSubtractsDynamicMixedArrayValues() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/dynamic_mixed_array_difference.c", """
                mixed value() {
                    mixed left;
                    mixed right;

                    left = ({ "a", "b", "c" });
                    right = ({ "b" });
                    return left - right;
                }
                """);

        assertEquals(List.of("a", "c"), object.invoke("value"));
    }

    @Test
    void runtimeSupportsArrayConcatFromMixedArrayValue() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mixed_array_concat.c", """
                mixed value() {
                    mixed ret;

                    ret = ({ "a", "b" });
                    if (pointerp(ret)) {
                        ret += ({ "c" });
                    }
                    return ret;
                }

                int pointerp(mixed value) {
                    return 1;
                }
                """);

        assertEquals(List.of("a", "b", "c"), object.invoke("value"));
    }

    @Test
    void runtimeSupportsTrailingCommaInArrayLiteral() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/trailing_array_comma.c", """
                mixed value() {
                    return ({ 1, 2, 3, });
                }
                """);

        assertEquals(List.of(1, 2, 3), object.invoke("value"));
    }

    @Test
    void runtimeSupportsTrailingCommaAfterNestedArrayLiteralEntries() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/trailing_nested_array_comma.c", """
                mixed value() {
                    return ({
                        ({ 1, 2, 3 }),
                        ({ 4, 5, 6 }),
                    });
                }
                """);

        assertEquals(List.of(List.of(1, 2, 3), List.of(4, 5, 6)), object.invoke("value"));
    }

    @Test
    void runtimeSupportsMappingMergeFromMixedMappingValue() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mixed_mapping_merge.c", """
                mixed value() {
                    mixed ret;

                    ret = ([ "a": 1 ]);
                    if (mappingp(ret)) {
                        ret += ([ "b": 2 ]);
                    }
                    return ret["b"];
                }

                int mappingp(mixed value) {
                    return 1;
                }
                """);

        assertEquals(2, object.invoke("value"));
    }

    @Test
    void runtimeSupportsTrailingCommaInMappingLiteral() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/trailing_mapping_comma.c", """
                mixed value() {
                    mapping values;

                    values = ([ "a": 1, "b": 2, ]);
                    return values["b"];
                }
                """);

        assertEquals(2, object.invoke("value"));
    }

    @Test
    void runtimeSupportsFloatLiteralsInMappingValues() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mapping_float_values.c", """
                mixed value() {
                    mapping values;

                    values = ([ "experience modifier": 1.1 ]);
                    return values["experience modifier"];
                }
                """);

        assertEquals(1.1f, object.invoke("value"));
    }

    @Test
    void runtimePromotesMixedIntegerAndFloatArithmetic() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/mixed_integer_float_arithmetic.c", """
                float scaled(int level) {
                    return level * 1.5;
                }

                int compares() {
                    return (2 * 1.5 == 3.0) && (1 + 0.5 < 2);
                }
                """);

        assertEquals(6.0f, object.invoke("scaled", 4));
        assertEquals(1, object.invoke("compares"));
    }

    @Test
    void runtimeAddsMixedCollectionsDynamically() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mixed_collection_addition.c", """
                mixed array_concat() {
                    mixed left = ({ "north" });
                    mixed right = ({ "south" });
                    return left + right;
                }

                mixed mapping_merge() {
                    mixed left = ([ "north": 1 ]);
                    mixed right = ([ "south": 1 ]);
                    return left + right;
                }
                """);

        assertEquals(List.of("north", "south"), object.invoke("array_concat"));
        assertEquals(Map.of("north", 1, "south", 1), object.invoke("mapping_merge"));
    }

    @Test
    void runtimeWidensIntegerAssignmentsToFloat() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/integer_to_float_assignment.c", """
                float field = 3;

                float assigned(int value) {
                    float local = 0;

                    local = value - 2;
                    return field + local;
                }
                """);

        assertEquals(8.0f, object.invoke("assigned", 7));
    }

    @Test
    void runtimeSupportsFalseSentinelAndOrFallbackInStringContexts() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/string_false_sentinel.c", """
                string field = 0;
                int calls;

                mixed maybe_text(int present) {
                    if (present) {
                        return "present";
                    }
                    return 0;
                }

                string fallback(int present) {
                    return maybe_text(present) || "fallback";
                }

                string local_zero() {
                    string value = 0;
                    return value;
                }

                string side_effect() {
                    calls += 1;
                    return "unused";
                }

                string short_circuit() {
                    return "kept" || side_effect();
                }

                int call_count() {
                    return calls;
                }
                """);

        assertNull(object.invoke("local_zero"));
        assertEquals("fallback", object.invoke("fallback", 0));
        assertEquals("present", object.invoke("fallback", 1));
        assertEquals("kept", object.invoke("short_circuit"));
        assertEquals(0, object.invoke("call_count"));
    }

    @Test
    void runtimeCoercesMixedValuesInStringContexts() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mixed_string_context.c", """
                mapping data = ([ "text": "kept", "number": 7, "false": 0 ]);

                mixed query(string key) {
                    return data[key];
                }

                string echo(string value) {
                    return value;
                }

                string local(string key) {
                    string value = query(key);
                    return value;
                }

                string returned(string key) {
                    return query(key);
                }

                string argument(string key) {
                    return echo(query(key));
                }
                """);

        assertEquals("kept", object.invoke("local", "text"));
        assertEquals("7", object.invoke("local", "number"));
        assertNull(object.invoke("local", "false"));
        assertEquals("7", object.invoke("returned", "number"));
        assertEquals("7", object.invoke("argument", "number"));
    }

    @Test
    void runtimePropagatesStringContextThroughNestedDynamicConcat() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/dynamic_string_concat.c", """
                object configuration;

                void set_configuration(object value) {
                    configuration = value;
                }

                public nomask varargs string decorate(string text, string textClass, string type, string configuration) {
                    return text;
                }

                string copyright(string colorConfiguration) {
                    return configuration->decorate("Copyright", "heading", "help", colorConfiguration) +
                        configuration->decorate(" body ", "text", "help", colorConfiguration) +
                        configuration->decorate("license", "url", "help", colorConfiguration);
                }
                """);

        object.invoke("set_configuration", object.instance());
        assertEquals("Copyright body license", object.invoke("copyright", "none"));
    }

    @Test
    void runtimeSupportsFalseSentinelAndOrFallbackInObjectContexts() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/object_false_sentinel.c", """
                object target;

                void set_target(object value) {
                    target = value;
                }

                mixed maybe_object(int present) {
                    if (present) {
                        return target;
                    }
                    return 0;
                }

                object fallback(int present) {
                    return maybe_object(present) || target;
                }

                object chained_fallback(int first, int second) {
                    return maybe_object(first) || maybe_object(second) || target;
                }
                """);

        object.invoke("set_target", object.instance());
        assertSame(object.instance(), object.invoke("fallback", 0));
        assertSame(object.instance(), object.invoke("fallback", 1));
        assertSame(object.instance(), object.invoke("chained_fallback", 0, 0));
        assertSame(object.instance(), object.invoke("chained_fallback", 0, 1));
    }

    @Test
    void runtimeSupportsFalseSentinelInCollectionContexts() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_filter", "filter")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/collection_false_sentinel.c", """
                mixed maybe_array(int present) {
                    if (present) {
                        return ({ "kept" });
                    }
                    return 0;
                }

                mixed maybe_mapping(int present) {
                    if (present) {
                        return ([ "name": "kept" ]);
                    }
                    return 0;
                }

                string *array_fallback(int present) {
                    return maybe_array(present) || ({});
                }

                mapping mapping_fallback(int present) {
                    return maybe_mapping(present) || ([]);
                }

                string *array_plus_false() {
                    return ({ "a" }) + 0;
                }

                string *array_minus_false() {
                    return ({ "a" }) - 0;
                }

                mixed maybe_left_array() {
                    return ({ "left" });
                }

                mixed maybe_right_array() {
                    return ({ "right" });
                }

                string *mixed_array_concat() {
                    string *values = maybe_left_array() + maybe_right_array();
                    return values;
                }

                string *mixed_array_difference() {
                    string *values = maybe_left_array() - maybe_right_array();
                    return values;
                }

                string *filter_mixed_array_difference() {
                    return filter(maybe_left_array() - maybe_right_array(), (: 1 :));
                }
                """);

        assertEquals(List.of(), object.invoke("array_fallback", 0));
        assertEquals(List.of("kept"), object.invoke("array_fallback", 1));
        assertEquals(Map.of(), object.invoke("mapping_fallback", 0));
        assertEquals(Map.of("name", "kept"), object.invoke("mapping_fallback", 1));
        assertEquals(List.of("a"), object.invoke("array_plus_false"));
        assertEquals(List.of("a"), object.invoke("array_minus_false"));
        assertEquals(List.of("left", "right"), object.invoke("mixed_array_concat"));
        assertEquals(List.of("left"), object.invoke("mixed_array_difference"));
        assertEquals(List.of("left"), object.invoke("filter_mixed_array_difference"));
    }

    @Test
    void runtimeKeepsTypedCollectionZeroFalsey() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/typed_collection_zero.c", """
                string *fieldValues = 0;
                mapping fieldData = 0;
                string fieldText = 0;

                int field_array_is_false() {
                    return !fieldValues;
                }

                int field_mapping_is_false() {
                    return !fieldData;
                }

                int field_string_is_false() {
                    return !fieldText;
                }

                mixed query_field_values() {
                    return fieldValues;
                }

                mixed query_field_data() {
                    return fieldData;
                }

                mixed query_field_text() {
                    return fieldText;
                }

                string *local_array_zero() {
                    string *values = 0;
                    return values;
                }

                mapping local_mapping_zero() {
                    mapping data = 0;
                    return data;
                }

                string local_string_zero() {
                    string text = 0;
                    return text;
                }

                string *array_plus_false() {
                    return ({ "a" }) + local_array_zero();
                }

                mapping mapping_plus_false() {
                    return ([ "a": 1 ]) + local_mapping_zero();
                }
                """);

        assertEquals(1, object.invoke("field_array_is_false"));
        assertEquals(1, object.invoke("field_mapping_is_false"));
        assertEquals(1, object.invoke("field_string_is_false"));
        assertNull(object.invoke("query_field_values"));
        assertNull(object.invoke("query_field_data"));
        assertNull(object.invoke("query_field_text"));
        assertNull(object.invoke("local_array_zero"));
        assertNull(object.invoke("local_mapping_zero"));
        assertNull(object.invoke("local_string_zero"));
        assertEquals(List.of("a"), object.invoke("array_plus_false"));
        assertEquals(Map.of("a", 1), object.invoke("mapping_plus_false"));
    }

    @Test
    void runtimeSupportsMappingIndexedMutation() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mapping_indexed_mutation.c", """
                mapping counts = ([]);

                mixed increment_missing() {
                    return counts["a"]++;
                }

                mixed increment_existing() {
                    return counts["a"]++;
                }

                mixed prefix_increment() {
                    return ++counts["b"];
                }

                mixed value(string key) {
                    return counts[key];
                }
                """);

        assertEquals(0, object.invoke("increment_missing"));
        assertEquals(1, object.invoke("value", "a"));
        assertEquals(1, object.invoke("increment_existing"));
        assertEquals(2, object.invoke("value", "a"));
        assertEquals(1, object.invoke("prefix_increment"));
        assertEquals(1, object.invoke("value", "b"));
    }

    @Test
    void runtimeSupportsLdmudMultiValueMappings() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_mapping_values", "m_values")
                .engineFunction("jvmud_size", "sizeof")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/multi_value_mapping.c", """
                mapping wall = ([
                    "weakness": "<missing>"; 1,
                    "senses": "Unseeing, unhearing"; 0,
                    "resistance": "<missing> no longer"; 1
                ]);

                mixed default_value() {
                    return wall["weakness"];
                }

                int secondary_value() {
                    return wall["resistance", 1];
                }

                mixed missing_secondary() {
                    return wall["senses", 2];
                }

                int active_count() {
                    int *items = m_values(wall, 1);
                    items -= ({ 0 });
                    return sizeof(items);
                }
                """);

        assertEquals("<missing>", object.invoke("default_value"));
        assertEquals(1, object.invoke("secondary_value"));
        assertEquals(0, object.invoke("missing_secondary"));
        assertEquals(2, object.invoke("active_count"));
    }

    @Test
    void runtimeSupportsLargeCollectionLiteralInitializers() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object =
                runtime.loadSource("smoke/large_collection_literal_initializers.c", largeCollectionLiteralSource(240));

        assertEquals(199, object.invoke("score"));
        assertEquals("tag 199b", object.invoke("tag"));
        assertEquals("item 199 description", object.invoke("description"));
        assertEquals(239, object.invoke("number"));
    }

    @Test
    void runtimeSupportsArraySliceReplacement() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/array_slice_replacement.c", """
                mixed value() {
                    mixed* values;

                    values = ({1, 2, 3, 4});
                    values[1..2] = ({9});
                    return values;
                }
                """);

        assertEquals(List.of(1, 9, 4), object.invoke("value"));
    }

    @Test
    void runtimeSupportsArraySliceDeletion() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/array_slice_deletion.c", """
                mixed value() {
                    mixed* values;

                    values = ({1, 2, 3, 4});
                    values[1..2] = ({});
                    return values;
                }
                """);

        assertEquals(List.of(1, 4), object.invoke("value"));
    }

    @Test
    void runtimeSupportsComputedArraySliceDeletion() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/computed_array_slice_deletion.c", """
                mixed value() {
                    mixed* values;
                    int i;

                    values = ({"a", "b", "..", "c"});
                    i = 2;
                    values[i - 1..i] = ({});
                    return values;
                }
                """);

        assertEquals(List.of("a", "c"), object.invoke("value"));
    }

    @Test
    void runtimeSupportsComputedStringSliceDeletion() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/computed_string_slice_deletion.c", """
                string value(int current, int needed) {
                    string bar;

                    bar = "==========";
                    bar[(10 * current) / needed..] = "";
                    return bar;
                }
                """);

        assertEquals("====", object.invoke("value", 4, 10));
    }

    @Test
    void runtimeSupportsFieldStringSliceReplacement() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/field_string_slice_replacement.c", """
                private string label;

                string value() {
                    label = "abcdef";
                    label[2..4] = "ZZ";
                    return label;
                }
                """);

        assertEquals("abZZf", object.invoke("value"));
    }

    @Test
    void runtimeSupportsFromEndStringSuffixSlice() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/from_end_string_suffix.c", """
                int value() {
                    string path;

                    path = "/tmp/";
                    return path[<1..] == "/";
                }
                """);

        assertEquals(1, object.invoke("value"));
    }

    @Test
    void runtimeSupportsFromEndArrayIndexAndSlices() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/from_end_array_index.c", """
                mixed value() {
                    mixed *values;

                    values = ({ "a", "b", "c", "d" });
                    return ({
                        values[<1],
                        values[<2..],
                        values[..<2],
                        values[<3..<1]
                    });
                }
                """);

        assertEquals(
                List.of("d", List.of("c", "d"), List.of("a", "b", "c"), List.of("b", "c", "d")),
                object.invoke("value"));
    }

    @Test
    void runtimeClampsOutOfRangeReadSlices() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/out_of_range_read_slices.c", """
                mixed value() {
                    mixed *values;

                    values = ({ "a", "b", "c" });
                    return ({
                        "abcdef"[0..76],
                        "abcdef"[9..12],
                        values[1..9],
                        values[6..8]
                    });
                }
                """);

        assertEquals(List.of("abcdef", "", List.of("b", "c"), List.of()), object.invoke("value"));
    }

    @Test
    void emptyMappingsCanFlowIntoArrayContextsAsEmptyArrays() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/empty_mapping_array_context.c", """
                int accepts_array(string *values) {
                    return values == 0 ? -1 : 0;
                }

                int value() {
                    mixed values;

                    values = ([]);
                    return accepts_array(values);
                }
                """);

        assertEquals(0, object.invoke("value"));
        assertEquals(0, runtime.invokeObject(object.instance(), "accepts_array", Map.of()));
    }

    @Test
    void runtimeSupportsFromEndArraySliceAssignment() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/from_end_slice_assignment.c", """
                mixed value() {
                    mixed *values;

                    values = ({ 1, 2, 3, 4 });
                    values[<2..] = ({ 9 });
                    return values;
                }
                """);

        assertEquals(List.of(1, 2, 9), object.invoke("value"));
    }

    @Test
    void runtimeSupportsHexIntegerLiterals() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/hex_integer.c", """
                int value() {
                    return 0x10 + 0X02;
                }
                """);

        assertEquals(18, object.invoke("value"));
    }

    @Test
    void runtimeSupportsStringSubtraction() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/string_difference.c", """
                string value() {
                    return "alpha beta alpha" - "alpha";
                }
                """);

        assertEquals(" beta ", object.invoke("value"));
    }

    @Test
    void runtimeSupportsStringSubtractionFromMixedStringValue() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mixed_string_difference.c", """
                string value() {
                    mixed text;

                    text = "one\\rtwo\\r";
                    return text - "\\r";
                }
                """);

        assertEquals("onetwo", object.invoke("value"));
    }

    @Test
    void runtimeSubtractsDynamicMixedStringValues() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/dynamic_mixed_string_difference.c", """
                string value() {
                    mixed left;
                    mixed right;

                    left = "one\\rtwo\\r";
                    right = "\\r";
                    return left - right;
                }
                """);

        assertEquals("onetwo", object.invoke("value"));
    }

    @Test
    void runtimeSubtractsDynamicMixedNumericValues() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/dynamic_mixed_numeric_difference.c", """
                mixed value() {
                    mixed left;
                    mixed right;

                    left = 9;
                    right = 4;
                    return left - right;
                }
                """);

        assertEquals(5, object.invoke("value"));
    }

    @Test
    void stringLocalAssignmentPropagatesExpectedTypeIntoMixedConcatenation() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/expected_string_local_concat.c", """
                mixed chunk(string value) {
                    return value;
                }

                string value() {
                    string text = chunk("one") + chunk("two");
                    return text;
                }
                """);

        assertEquals("onetwo", object.invoke("value"));
    }

    @Test
    void stringFieldAssignmentPropagatesExpectedTypeIntoMixedConcatenation() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/expected_string_field_concat.c", """
                string text;

                mixed chunk(string value) {
                    return value;
                }

                string value() {
                    text = chunk("field") + chunk("-value");
                    return text;
                }
                """);

        assertEquals("field-value", object.invoke("value"));
    }

    @Test
    void stringFieldInitializerPropagatesExpectedTypeIntoMixedConcatenation() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                string text = left() + right();

                mixed left() {
                    return "field";
                }

                mixed right() {
                    return "-initializer";
                }
                """);

        assertTrue(result.getProblems().isEmpty(), () -> problemMessages(result));
        assertNotNull(result.getBytecode());
    }

    @Test
    void stringReturnPropagatesExpectedTypeIntoMixedConcatenation() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/expected_string_return_concat.c", """
                mixed chunk(string value) {
                    return value;
                }

                string value() {
                    return chunk("return") + chunk("-value");
                }
                """);

        assertEquals("return-value", object.invoke("value"));
    }

    @Test
    void stringArgumentPropagatesExpectedTypeIntoMixedConcatenation() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/expected_string_argument_concat.c", """
                mixed chunk(string value) {
                    return value;
                }

                string echo(string value) {
                    return value;
                }

                string value() {
                    return echo(chunk("arg") + chunk("-value"));
                }
                """);

        assertEquals("arg-value", object.invoke("value"));
    }

    @Test
    void stringLocalAssignmentPropagatesExpectedTypeIntoMixedStringDifference() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/expected_string_difference.c", """
                mixed chunk(string value) {
                    return value;
                }

                string value() {
                    string text = chunk("a\\rb\\r") - chunk("\\r");
                    return text;
                }
                """);

        assertEquals("ab", object.invoke("value"));
    }

    @Test
    void runtimeSupportsDoWhileLoops() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/do_while.c", """
                int value() {
                    int total;
                    int index;

                    do {
                        total += index;
                        index++;
                    } while (index < 4);

                    return total;
                }
                """);

        assertEquals(6, object.invoke("value"));
    }

    @Test
    void doWhileRunsAtLeastOnce() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/do_while_once.c", """
                int value() {
                    int count;

                    do {
                        count++;
                    } while (0);

                    return count;
                }
                """);

        assertEquals(1, object.invoke("value"));
    }

    @Test
    void doWhileHonorsBreakAndContinue() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/do_while_control.c", """
                int value() {
                    int index;
                    int total;

                    do {
                        index++;
                        if (index == 2)
                            continue;
                        if (index == 5)
                            break;
                        total += index;
                    } while (index < 10);

                    return total;
                }
                """);

        assertEquals(8, object.invoke("value"));
    }

    @Test
    void runtimeSupportsStringSwitchCases() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/switch_string.c", """
                int value(string name) {
                    int result;

                    switch(name) {
                        case "alpha":
                            result = 1;
                            break;
                        case "beta":
                            result = 2;
                            break;
                        default:
                            result = 9;
                            break;
                    }

                    return result;
                }
                """);

        assertEquals(2, object.invoke("value", "beta"));
        assertEquals(9, object.invoke("value", "gamma"));
    }

    @Test
    void runtimeSupportsSwitchFallthroughLabels() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/switch_fallthrough.c", """
                int value(string name) {
                    int result;

                    switch(name) {
                        case "alpha":
                        case "beta":
                            result = 7;
                            break;
                        default:
                            result = 3;
                            break;
                    }

                    return result;
                }
                """);

        assertEquals(7, object.invoke("value", "alpha"));
        assertEquals(7, object.invoke("value", "beta"));
        assertEquals(3, object.invoke("value", "omega"));
    }

    @Test
    void switchBreakLeavesOnlyTheSwitch() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/switch_break.c", """
                int value(int number) {
                    int result;

                    switch(number) {
                        case 1:
                            result = 4;
                            break;
                        default:
                            result = 8;
                            break;
                    }

                    result += 1;
                    return result;
                }
                """);

        assertEquals(5, object.invoke("value", 1));
    }

    @Test
    void runtimeSupportsSwitchCaseRanges() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/switch_case_ranges.c", """
                string template(int illuminationLevel) {
                    string templateKey;

                    switch(illuminationLevel) {
                        case 1..2:
                        {
                            templateKey = "near dark template";
                            break;
                        }
                        case 3..4:
                        {
                            templateKey = "low light template";
                            break;
                        }
                        case 7..8:
                        {
                            templateKey = "some light template";
                            break;
                        }
                        default:
                        {
                            templateKey = "template";
                            break;
                        }
                    }

                    return templateKey;
                }
                """);

        assertEquals("near dark template", object.invoke("template", 1));
        assertEquals("near dark template", object.invoke("template", 2));
        assertEquals("low light template", object.invoke("template", 4));
        assertEquals("some light template", object.invoke("template", 7));
        assertEquals("template", object.invoke("template", 6));
    }

    @Test
    void runtimeSupportsLogicalAndAssignment() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/logical_and_assignment.c", """
                int value() {
                    int ret;

                    ret = 1;
                    ret &&= 0;
                    return ret;
                }
                """);

        assertEquals(0, object.invoke("value"));
    }

    @Test
    void runtimeSupportsLogicalOrAssignment() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/logical_or_assignment.c", """
                int value() {
                    int ret;

                    ret = 0;
                    ret ||= 1;
                    return ret;
                }
                """);

        assertEquals(1, object.invoke("value"));
    }

    @Test
    void logicalCompoundAssignmentShortCircuits() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/logical_assignment_short_circuit.c", """
                int calls;

                int touch() {
                    calls++;
                    return 1;
                }

                int andCalls() {
                    int ret;

                    calls = 0;
                    ret = 0;
                    ret &&= touch();
                    return calls;
                }

                int orCalls() {
                    int ret;

                    calls = 0;
                    ret = 1;
                    ret ||= touch();
                    return calls;
                }
                """);

        assertEquals(0, object.invoke("andCalls"));
        assertEquals(0, object.invoke("orCalls"));
    }

    @Test
    void runtimeSupportsQualifiedEfunCalls() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                int sizeof(mixed value) {
                    return 99;
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .engineFunction("jvmud_size", "sizeof")
                .engineFunction("jvmud_lowercase_text", "lower_case")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/qualified_efun.c", """
                int shadowed() {
                    return sizeof(({1, 2, 3}));
                }

                int direct() {
                    return efun::sizeof(({1, 2, 3}));
                }

                int jvmud_full_name() {
                    return jvmud::jvmud_size(({1, 2}));
                }

                string jvmud_text_helper() {
                    return jvmud::jvmud_lowercase_text("LOUD");
                }

                string fallback_alias() {
                    return lower_case("LOUD");
                }
                """);

        assertEquals(99, object.invoke("shadowed"));
        assertEquals(3, object.invoke("direct"));
        assertEquals(2, object.invoke("jvmud_full_name"));
        assertEquals("loud", object.invoke("jvmud_text_helper"));
        assertEquals("loud", object.invoke("fallback_alias"));
    }

    @Test
    void jvmudQualifiedCallsBypassLegacyEfunAliases() {
        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(tempDir, List.of()));
        CoreEfuns.registerCore(context);
        context.setMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_size", "sizeof")
                .build());

        CompilationResult result = new CompilationPipeline("java/lang/Object", context).run("""
                int value() {
                    return jvmud::sizeof(({1}));
                }
                """);

        assertTrue(result.getProblems().stream()
                .anyMatch(problem -> problem.getMessage().contains("Unrecognized JVMud efun 'sizeof'")));
    }

    @Test
    void jvmudQualifiedCallsDoNotAutoPrefixNativeNames() {
        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(tempDir, List.of()));
        CoreEfuns.registerCore(context);

        CompilationResult result = new CompilationPipeline("java/lang/Object", context).run("""
                int value() {
                    return jvmud::size(({1}));
                }
                """);

        assertTrue(result.getProblems().stream()
                .anyMatch(problem -> problem.getMessage().contains("Unrecognized JVMud efun 'size'")));
    }

    @Test
    void efunQualifiedCallsDoNotAutoPrefixNativeNames() {
        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(tempDir, List.of()));
        CoreEfuns.registerCore(context);

        CompilationResult result = new CompilationPipeline("java/lang/Object", context).run("""
                int value() {
                    return efun::size(({1}));
                }
                """);

        assertTrue(result.getProblems().stream()
                .anyMatch(problem -> problem.getMessage().contains("Unrecognized efun 'size'")));
    }

    @Test
    void runtimeSupportsQualifiedPrimaryParentCalls() throws Exception {
        Files.writeString(tempDir.resolve("environment.c"), """
                int initialized;

                void init() {
                    initialized = 7;
                }
                """);
        Files.writeString(tempDir.resolve("generatedEnvironment.c"), """
                inherit "environment.c";

                int run() {
                    environment::init();
                    return initialized;
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.load(tempDir.resolve("generatedEnvironment.c"));

        assertEquals(7, object.invoke("run"));
    }

    @Test
    void runtimeSupportsQualifiedSecondaryParentCalls() throws Exception {
        Files.writeString(tempDir.resolve("living.c"), """
                int create() {
                    return 10;
                }
                """);
        Files.writeString(tempDir.resolve("item.c"), """
                int query(string element) {
                    return element == "weight" ? 4 : 0;
                }
                """);
        Files.writeString(tempDir.resolve("equipment.c"), """
                inherit "living.c";
                inherit "item.c";

                int weight() {
                    return item::query("weight");
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.load(tempDir.resolve("equipment.c"));

        assertEquals(4, object.invoke("weight"));
    }

    @Test
    void qualifiedSecondaryParentCallBypassesCurrentOverride() throws Exception {
        Files.writeString(tempDir.resolve("primary.c"), """
                int marker() {
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("item.c"), """
                int set(string element, mixed data) {
                    return element == "weight" ? data : 0;
                }
                """);
        Files.writeString(tempDir.resolve("blueprint.c"), """
                inherit "primary.c";
                inherit "item.c";

                int set(string element, mixed data) {
                    return item::set(element, data);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.load(tempDir.resolve("blueprint.c"));

        assertEquals(9, object.invoke("set", "weight", 9));
    }

    @Test
    void unsupportedQualifiedCallsReportSemanticProblem() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                mixed value() {
                    return other::sizeof(({1}));
                }
                """);

        assertTrue(result.getProblems().stream()
                .anyMatch(problem -> problem.getMessage().contains("Unsupported qualified call prefix 'other'")));
    }

    @Test
    void runtimeSupportsForeachOverArrays() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/foreach_array.c", """
                int value() {
                    int total;

                    foreach(int item in {1, 2, 3})
                        total += item;

                    return total;
                }
                """);

        assertEquals(6, object.invoke("value"));
    }

    @Test
    void runtimeAcceptsColonAsForeachSeparator() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/foreach_colon.c", """
                string value() {
                    string text;

                    text = "";
                    foreach(string item : {"a", "b", "c"})
                        text += item;

                    return text;
                }
                """);

        assertEquals("abc", object.invoke("value"));
    }

    @Test
    void runtimeSupportsForeachMappingKeyValueLoops() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/foreach_mapping.c", """
                int value() {
                    mapping values;
                    int total;

                    values = ([ "a": 2, "b": 3 ]);
                    foreach(string key, int amount in values)
                        total += amount;

                    return total;
                }
                """);

        assertEquals(5, object.invoke("value"));
    }

    @Test
    void runtimePreservesIntegerMappingKeys() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mapping_integer_keys.c", """
                int value() {
                    mapping values;

                    values = ([ 10: 7 ]);
                    values[20] = 11;

                    return values[10] + values[20];
                }
                """);

        assertEquals(18, object.invoke("value"));
    }

    @Test
    void runtimeReturnsZeroForMissingMappingKeys() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/missing_mapping_key.c", """
                int value() {
                    mapping values;

                    values = ([ ]);
                    return values["missing"];
                }
                """);

        assertEquals(0, object.invoke("value"));
    }

    @Test
    void runtimePreservesObjectTypedMappingKeys() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mapping_object_keys.c", """
                int value() {
                    mapping values;
                    object key;

                    values = ([ ]);
                    values[key] = 13;

                    return values[key];
                }
                """);

        assertEquals(13, object.invoke("value"));
    }

    @Test
    void runtimeSupportsStringIndexAssignment() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/string_index_assignment.c", """
                string value() {
                    string ret;
                    string source;

                    ret = "abc";
                    source = "XYZ";
                    ret[1] = source[2];
                    return ret;
                }
                """);

        assertEquals("aZc", object.invoke("value"));
    }

    @Test
    void runtimeSupportsIndexedCompoundArrayAppend() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/indexed_compound_array_append.c", """
                int value() {
                    mixed* values;

                    values = ({ ({ 1 }) });
                    values[0] += ({ 2, 3 });

                    return jvmud_size(values[0]);
                }
                """);

        assertEquals(3, object.invoke("value"));
    }

    @Test
    void runtimeSupportsIndexedCompoundMappingAppend() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/indexed_compound_mapping_append.c", """
                int value() {
                    mapping values;

                    values = ([ "items": ({ 1 }) ]);
                    values["items"] += ({ 2 });

                    return jvmud_size(values["items"]);
                }
                """);

        assertEquals(2, object.invoke("value"));
    }

    @Test
    void runtimeSupportsNestedIndexedCompoundMappingAppend() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/nested_indexed_compound_mapping_append.c", """
                int value() {
                    mapping bonuses;

                    bonuses = ([ "fire": ([ "resist": ({ 1 }) ]) ]);
                    bonuses["fire"]["resist"] += ({ 2 });

                    return jvmud_size(bonuses["fire"]["resist"]);
                }
                """);

        assertEquals(2, object.invoke("value"));
    }

    @Test
    void foreachVariablesAreScopedToSiblingLoops() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/foreach_sibling_scopes.c", """
                int value() {
                    int total;

                    foreach(int item in ({1, 2}))
                    {
                        total += item;
                    }

                    foreach(int item in ({3, 4}))
                    {
                        total += item;
                    }

                    return total;
                }
                """);

        assertEquals(10, object.invoke("value"));
    }

    @Test
    void foreachMappingVariablesAreScopedToSiblingLoops() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/foreach_mapping_sibling_scopes.c", """
                int value() {
                    mapping values;
                    int total;

                    values = ([ "a": 2, "b": 3 ]);
                    foreach(string key, int amount in values)
                    {
                        total += amount;
                    }

                    foreach(string key, int amount in values)
                    {
                        total += amount;
                    }

                    return total;
                }
                """);

        assertEquals(10, object.invoke("value"));
    }

    @Test
    void foreachHeaderRejectsDuplicateVariableNamesInOneScope() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                int value() {
                    mapping values;

                    values = ([ "a": 2 ]);
                    foreach(string key, int key in values)
                    {
                    }

                    return 1;
                }
                """);

        assertTrue(
                result.getProblems().stream()
                        .anyMatch(problem -> problem.getMessage().contains("Duplicate local 'key' in scope")),
                () -> problemMessages(result));
    }

    @Test
    void forInitializerVariablesAreScopedToSiblingLoops() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/for_initializer_sibling_scopes.c", """
                int value() {
                    int total;

                    for (int i = 0; i < 2; i++)
                    {
                        total += i;
                    }

                    for (int i = 0; i < 3; i++)
                    {
                        total += i;
                    }

                    return total;
                }
                """);

        assertEquals(4, object.invoke("value"));
    }

    @Test
    void forInitializerVariablesAreScopedToSiblingMethods() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/for_initializer_method_scopes.c", """
                int left() {
                    int found;

                    for (int sx = 0; sx < 2; sx++)
                    {
                        found += sx;
                    }

                    return found;
                }

                int right() {
                    int found;

                    for (int sx = 0; sx < 3; sx++)
                    {
                        found += sx;
                    }

                    return found;
                }

                int value() {
                    return left() + right();
                }
                """);

        assertEquals(4, object.invoke("value"));
    }

    @Test
    void forInitializerVariableExpiresAfterLoop() {
        CompilationResult result = new CompilationPipeline("java/lang/Object").run("""
                int value() {
                    for (int i = 0; i < 1; i++)
                    {
                    }

                    return i;
                }
                """);

        assertTrue(
                result.getProblems().stream()
                        .anyMatch(problem -> problem.getMessage().contains("Unrecognized local or field 'i'")),
                () -> problemMessages(result));
    }

    @Test
    void foreachHonorsBreakAndContinue() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/foreach_control.c", """
                int value() {
                    int total;

                    foreach(int item in {1, 2, 3, 4}) {
                        if (item == 2)
                            continue;
                        if (item == 4)
                            break;
                        total += item;
                    }

                    return total;
                }
                """);

        assertEquals(4, object.invoke("value"));
    }

    @Test
    void runtimeSupportsInlineCallableFilterArguments() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_filter", "filter")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_filter.c", """
                mixed value() {
                    return filter(({1, 0, 2, 0, 3}), (: $1 :));
                }
                """);

        assertEquals(List.of(1, 2, 3), object.invoke("value"));
    }

    @Test
    void runtimeRepresentsInlineCallableAsFirstClassValue() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_callable_value.c", """
                function value() {
                    return (: $1 :);
                }
                """);

        Object callable = object.invoke("value");
        assertTrue(callable instanceof RuntimeCallable);
        assertTrue(callable instanceof RuntimeFunctionLiteral);
        assertEquals(1, ((RuntimeFunctionLiteral) callable).arity());
        assertEquals(7, ((RuntimeCallable) callable).call(new RuntimeContext(null), 7));
    }

    @Test
    void runtimeStoresInlineCallableInFunctionLocal() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_callable_local.c", """
                function value() {
                    function callback = (: $1 + $2 :);
                    return callback;
                }
                """);

        Object callable = object.invoke("value");
        assertTrue(callable instanceof RuntimeCallable);
        assertEquals(2, ((RuntimeFunctionLiteral) callable).arity());
        assertEquals(9, ((RuntimeCallable) callable).call(new RuntimeContext(null), 4, 5));
    }

    @Test
    void runtimeInlineCallableReadsObjectState() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_callable_object_state.c", """
                int bonus = 3;

                function value() {
                    return (: $1 + bonus :);
                }
                """);

        Object callable = object.invoke("value");
        assertTrue(callable instanceof RuntimeCallable);
        assertEquals(10, ((RuntimeCallable) callable).call(new RuntimeContext(null), 7));
    }

    @Test
    void runtimeInlineCallableCapturesOuterLocals() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_callable_capture.c", """
                function value(int threshold) {
                    return (: $1 > threshold :);
                }
                """);

        Object callable = object.invoke("value", 2);
        assertTrue(callable instanceof RuntimeCallable);
        assertEquals(1, ((RuntimeFunctionLiteral) callable).arity());
        assertEquals(1, ((RuntimeCallable) callable).call(new RuntimeContext(null), 3));
        assertEquals(0, ((RuntimeCallable) callable).call(new RuntimeContext(null), 1));
    }

    @Test
    void runtimeSupportsInlineCallableReturnShorthand() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_filter", "filter")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_filter_return_shorthand.c", """
                mixed value() {
                    return filter(({1, 0, 2, 0, 3}), (: return $1; :));
                }
                """);

        assertEquals(List.of(1, 2, 3), object.invoke("value"));
    }

    @Test
    void runtimeSupportsInlineCallableBlockBody() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_filter", "filter")
                .engineFunction("jvmud_member", "member")
                .engineFunction("jvmud_is_array", "pointerp")
                .engineFunction("jvmud_size", "sizeof")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_filter_block_body.c", """
                mixed value() {
                    mapping values = ([ "keep": ({ ([ "enabled": 1 ]) }), "drop": ({ }) ]);
                    mapping filtered = filter(values, (: {
                        int isOk = pointerp($2);
                        if (sizeof($2)) {
                            foreach (mapping entry in $2) {
                                isOk &&= member(entry, "enabled");
                            }
                        } else {
                            isOk = 0;
                        }
                        return isOk;
                    } :));
                    return sizeof(filtered);
                }
                """);

        assertEquals(1, object.invoke("value"));
    }

    @Test
    void runtimeSupportsInlineCallableMapArguments() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_map", "map")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_map.c", """
                mixed value() {
                    return map(({1, 2, 3}), (: $1 * $2 :), 10);
                }
                """);

        assertEquals(List.of(10, 20, 30), object.invoke("value"));
    }

    @Test
    void runtimeSupportsMixedStringRelationalComparison() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/mixed_string_compare.c", """
                int value() {
                    mixed left;
                    mixed right;

                    left = "b";
                    right = "a";
                    return left > right;
                }
                """);

        assertEquals(1, object.invoke("value"));
    }

    @Test
    void runtimeSupportsSortArrayWithInlineStringComparator() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_sort_array", "sort_array")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_sort.c", """
                mixed value() {
                    return sort_array(({"b", "a", "c"}), (: $1 > $2 :));
                }
                """);

        assertEquals(List.of("a", "b", "c"), object.invoke("value"));
    }

    @Test
    void runtimeSupportsSortArrayInlineComparatorExtraArguments() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_sort_array", "sort_array")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_sort_extra_arguments.c", """
                mixed value() {
                    mapping order;

                    order = ([ "b": 2, "a": 1, "c": 3 ]);
                    return sort_array(({"b", "a", "c"}), (: $3[$1] > $3[$2] :), order);
                }
                """);

        assertEquals(List.of("a", "b", "c"), object.invoke("value"));
    }

    @Test
    void runtimeSupportsInlineCallableExtraArgumentsAndMappingLookup() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_filter", "filter")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_filter_mapping_lookup.c", """
                mixed value() {
                    mapping values;

                    values = ([ "keep": 1, "drop": 0 ]);
                    return filter(({"keep", "drop"}), (: $2[$1] :), values);
                }
                """);

        assertEquals(List.of("keep"), object.invoke("value"));
    }

    @Test
    void runtimeSupportsInlineCallableSecondExtraArgumentAndMappingLookup() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_filter", "filter")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/inline_filter_two_extras.c", """
                mixed value() {
                    mapping values;

                    values = ([
                        "keep": ([ "exclude": ({ "other" }) ]),
                        "drop": ([ "exclude": ({ "target" }) ])
                    ]);
                    return filter(jvmud_mapping_keys(values),
                        (: jvmud_member($3[$1]["exclude"], $2) == -1 :), "target", values);
                }
                """);

        assertEquals(List.of("keep"), object.invoke("value"));
    }

    @Test
    void runtimeSupportsNestedSortArrayFilterInlineCallables() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_filter", "filter")
                .engineFunction("jvmud_sort_array", "sort_array")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/nested_sort_filter_inline_callables.c", """
                private mapping specificationData = ([
                    "name": "ash blond hair",
                    "type": "genetic",
                    "root": "hair",
                    "bonus strength": 1
                ]);

                mixed value() {
                    return sort_array(filter(jvmud_mapping_keys(specificationData),
                        (: jvmud_size(jvmud_regex_match(({ $1 }), "bonus")) &&
                            (specificationData[$1] > 0) :)), (: $1 > $2 :));
                }
                """);

        assertEquals(List.of("bonus strength"), object.invoke("value"));
    }

    @Test
    void inheritedInlineCallableUsesDeclaringClassHelper() throws IOException {
        Files.writeString(tempDir.resolve("callable_parent.c"), """
                private mapping specificationData = ([
                    "name": "ash blond hair",
                    "type": "genetic",
                    "root": "hair",
                    "bonus strength": 1
                ]);

                mixed query(string element) {
                    if (element == "bonuses")
                        return sort_array(filter(jvmud_mapping_keys(specificationData),
                            (: jvmud_size(jvmud_regex_match(({ $1 }), "bonus")) &&
                                (specificationData[$1] > 0) :)), (: $1 > $2 :));
                    return specificationData[element];
                }
                """);
        Files.writeString(tempDir.resolve("callable_child.c"), """
                inherit "/callable_parent.c";

                mixed childSort() {
                    return sort_array(({ "b", "a" }), (: $1 > $2 :));
                }
                """);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_filter", "filter")
                .engineFunction("jvmud_sort_array", "sort_array")
                .build());

        LPCObjectHandle object = runtime.load(tempDir.resolve("callable_child.c"));

        assertEquals(List.of("a", "b"), object.invoke("childSort"));
        assertEquals(List.of("bonus strength"), object.invoke("query", "bonuses"));
    }

    @Test
    void runtimeSupportsFunctionReferenceFilterArguments() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_filter", "filter")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/function_reference_filter.c", """
                int matches(mixed value) {
                    return value;
                }

                mixed value() {
                    return filter(({1, 0, 2, 0, 3}), #'matches);
                }
                """);

        assertEquals(List.of(1, 2, 3), object.invoke("value"));
    }

    @Test
    void runtimeSupportsFunctionReferenceExtraArgumentsAndMappingLookup() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_filter", "filter")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/function_reference_filter_extra_arguments.c", """
                int selected(string key, mapping values, string expected) {
                    return values[key] == expected;
                }

                mixed value() {
                    mapping values;

                    values = ([ "keep": "yes", "drop": "no" ]);
                    return filter(({"keep", "drop"}), #'selected, values, "yes");
                }
                """);

        assertEquals(List.of("keep"), object.invoke("value"));
    }

    @Test
    void runtimeSupportsNativeFilterIndicesWithInlineCallable() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/native_filter_indices_inline_callable.c", """
                mapping value() {
                    mapping inventory;

                    inventory = ([
                        "sword": ([ "type": "weapon", "subType": "blade" ]),
                        "shield": ([ "type": "armor", "subType": "shield" ]),
                        "bow": ([ "type": "weapon", "subType": "ranged" ]),
                    ]);
                    return jvmud_filter_indices(inventory,
                        (: (($2[$1]["type"] == $3) && (($4 == "all") || ($2[$1]["subType"] == $4))) :),
                        inventory, "weapon", "blade");
                }
                """);

        assertEquals(
                Map.of("sword", Map.of("type", "weapon", "subType", "blade")),
                object.invoke("value"));
    }

    @Test
    void mfunFilterIndicesCompatibilityWrapsNativeCallableEfun() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                mapping filter_indices(mapping values, function callback) {
                    return jvmud_filter_indices(values, callback);
                }

                mapping filter_indices(mapping values, function callback, mixed arg1) {
                    return jvmud_filter_indices(values, callback, arg1);
                }

                mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2) {
                    return jvmud_filter_indices(values, callback, arg1, arg2);
                }

                mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2, mixed arg3) {
                    return jvmud_filter_indices(values, callback, arg1, arg2, arg3);
                }

                mapping filter_indices(mapping values, function callback,
                    mixed arg1, mixed arg2, mixed arg3, mixed arg4) {
                    return jvmud_filter_indices(values, callback, arg1, arg2, arg3, arg4);
                }

                mapping filter_indices(mapping values, function callback,
                    mixed arg1, mixed arg2, mixed arg3, mixed arg4, mixed arg5) {
                    return jvmud_filter_indices(values, callback, arg1, arg2, arg3, arg4, arg5);
                }

                mapping filter_indices(mapping values, function callback,
                    mixed arg1, mixed arg2, mixed arg3, mixed arg4, mixed arg5, mixed arg6) {
                    return jvmud_filter_indices(values, callback, arg1, arg2, arg3, arg4, arg5, arg6);
                }
                """);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/mfun_filter_indices_inline_callable.c", """
                mapping value() {
                    mapping inventory;

                    inventory = ([
                        "sword": ([ "type": "weapon", "subType": "blade" ]),
                        "shield": ([ "type": "armor", "subType": "shield" ]),
                        "bow": ([ "type": "weapon", "subType": "ranged" ]),
                    ]);
                    return filter_indices(inventory,
                        (: (($2[$1]["type"] == $3) && (($4 == "all") || ($2[$1]["subType"] == $4))) :),
                        inventory, "weapon", "blade");
                }
                """);

        assertEquals(
                Map.of("sword", Map.of("type", "weapon", "subType", "blade")),
                object.invoke("value"));
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
    void runtimeSupportsPrefixIncrementAndDecrementExpressionValues() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/prefix_increment.c", """
                int value() {
                    int count;
                    int next;

                    count = 4;
                    next = ++count;
                    return (next * 10) + --count;
                }
                """);

        assertEquals(54, object.invoke("value"));
    }

    @Test
    void runtimeSupportsLocalPostfixIncrementAndDecrementExpressionValues() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/postfix_increment_value.c", """
                int value() {
                    int count;
                    int old;

                    count = 4;
                    old = count++;
                    return (old * 10) + count--;
                }
                """);

        assertEquals(45, object.invoke("value"));
    }

    @Test
    void runtimeSupportsFieldPostfixIncrementAndDecrementExpressionValues() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/postfix_field_increment_value.c", """
                int count;

                int value() {
                    int old;

                    count = 4;
                    old = count++;
                    return (old * 10) + count--;
                }
                """);

        assertEquals(45, object.invoke("value"));
    }

    @Test
    void runtimeSupportsPrefixIncrementInFunctionArguments() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/prefix_increment_argument.c", """
                int wrap(int value) {
                    return value * 10;
                }

                int value() {
                    int count;

                    return wrap(++count) + count;
                }
                """);

        assertEquals(11, object.invoke("value"));
    }

    @Test
    void runtimeSupportsPostfixIncrementInFunctionArguments() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/postfix_increment_argument.c", """
                int wrap(int value) {
                    return value * 10;
                }

                int value() {
                    int count;

                    return wrap(count++) + count;
                }
                """);

        assertEquals(1, object.invoke("value"));
    }

    @Test
    void runtimeSupportsMixedLocalPostfixIncrementExpressionValue() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/postfix_mixed_increment.c", """
                mixed value() {
                    mixed count;
                    mixed old;

                    count = 4;
                    old = count++;
                    return (old * 10) + count;
                }
                """);

        assertEquals(45, object.invoke("value"));
    }

    @Test
    void runtimeSupportsIndexedPrefixIncrementAndDecrement() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/indexed_prefix.c", """
                mixed value() {
                    mixed *values;
                    int first;
                    int second;

                    values = ({ 1, 1 });
                    first = ++values[0];
                    second = --values[1];
                    return (first * 100) + (values[0] * 10) + second;
                }
                """);

        assertEquals(220, object.invoke("value"));
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
    void directVarargsMethodCallsMayPassExtraArguments() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/varargs_extra_arg.c", """
                private nomask varargs int compile(string path, object initiator, string colorConfiguration, int recurse) {
                    return recurse + 40;
                }

                int value() {
                    return compile("/room/start.c", 0, "none", 2, 999);
                }
                """);

        assertEquals(42, object.invoke("value"));
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
    void runtimeConcatenatesDynamicMixedSelectorFormats() throws IOException {
        Path lib = Files.createDirectories(tempDir.resolve("lib"));
        Files.writeString(lib.resolve("selector_base.c"), """
                protected mapping Data = ([
                    "1": ([ "name": "No color support" ]),
                    "2": ([ "name": "3-bit (8 colors)" ]),
                ]);
                protected string Description = "Choose your color configuration";
                protected int NumColumns = 1;
                protected string Type = "Character creation";

                string sprintf(string format, mixed arg1) {
                    return jvmud_format_text(format, arg1);
                }

                string sprintf(string format, mixed arg1, mixed arg2) {
                    return jvmud_format_text(format, arg1, arg2);
                }

                string sprintf(string format, mixed arg1, mixed arg2, mixed arg3) {
                    return jvmud_format_text(format, arg1, arg2, arg3);
                }

                string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
                    mixed arg5) {
                    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5);
                }

                int sizeof(mixed value) {
                    return jvmud_size(value);
                }

                mixed *m_indices(mapping value) {
                    return jvmud_mapping_keys(value);
                }

                int sortMethod(string a, string b) {
                    return jvmud_to_int(a) > jvmud_to_int(b);
                }

                protected string displayDetails(string choice) {
                    return "";
                }

                protected string choiceFormatter(string choice) {
                    return sprintf("%s[\\x1b[0;31;1m%s\\x1b[0m]%s - \\x1b[0;32m%-20s\\x1b[0m%s",
                        "    ", "%s", "", "%-20s", displayDetails(choice));
                }

                string display_message() {
                    string ret = "";
                    if (Data && sizeof(Data)) {
                        ret = sprintf("\\x1b[0;36m%s\\x1b[0m\\x1b[0;37;1m%s\\x1b[0m%s",
                            sprintf("%s - ", Type), Description, ":\\n");
                        string *choices = sort_array(m_indices(Data), "sortMethod");
                        int i = 1;
                        foreach (string choice in choices) {
                            string format = choiceFormatter(choice);
                            ret += sprintf(format, choice, Data[choice]["name"]);
                            if (!(i % NumColumns)) {
                                ret += "\\n";
                            }
                            i++;
                        }
                        if (ret[sizeof(ret) - 1] != '\\n')
                            ret += "\\n";
                        ret += sprintf("You must select a number from 1 to %d.%s\\n",
                            sizeof(choices), "");
                    }
                    return ret;
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_sort_array", "sort_array")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/inherited_selector.c", """
                inherit "/lib/selector_base.c";
                """);

        assertEquals("\u001b[0;36mCharacter creation - \u001b[0m\u001b[0;37;1mChoose your color configuration\u001b[0m:\n"
                        + "    [\u001b[0;31;1m1\u001b[0m] - \u001b[0;32mNo color support                   \u001b[0m\n"
                        + "    [\u001b[0;31;1m2\u001b[0m] - \u001b[0;32m3-bit (8 colors)                   \u001b[0m\n"
                        + "You must select a number from 1 to 2.\n",
                object.invoke("display_message"));
    }

    @Test
    void runtimeSupportsLdmudStringColumnFormatMode() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/ldmud_column_format.c", """
                string sprintf(string format, mixed arg1, mixed arg2) {
                    return jvmud_format_text(format, arg1, arg2);
                }

                string wrapped() {
                    return sprintf("%=-*s", 12, "this is a very long sentence");
                }
                """);

        assertEquals("this is a   \nvery long   \nsentence    ", object.invoke("wrapped"));
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
    void compatibleDuplicateInheritedMethodsResolveToLaterParent() throws Exception {
        Files.writeString(tempDir.resolve("placeholder.c"), """
                protected int execute(string value, int actor, string name) {
                    return 0;
                }
                """);
        Files.writeString(tempDir.resolve("implementation.c"), """
                protected nomask int execute(string value, int actor, string name) {
                    return 7;
                }
                """);
        Files.writeString(tempDir.resolve("combined.c"), """
                inherit "placeholder";
                inherit "implementation";

                int run() {
                    return execute("value", 1, "name");
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        Object combined = runtime.loadOrGetObject("combined");

        assertEquals(7, runtime.invokeObject(combined, "run"));
    }

    @Test
    void qualifiedPrimaryParentCallUsesDirectParentOwnerWhenMethodIsInherited() throws Exception {
        Files.createDirectories(tempDir.resolve("lib"));
        Files.writeString(tempDir.resolve("lib/module.c"), """
                private int initialized = 0;

                public void init() {
                    initialized = 1;
                }

                public int module_initialized() {
                    return initialized;
                }
                """);
        Files.writeString(tempDir.resolve("lib/parent.c"), """
                virtual inherit "/lib/module.c";
                """);
        Files.writeString(tempDir.resolve("child.c"), """
                inherit "/lib/parent.c";

                public int run() {
                    parent::init();
                    return module_initialized();
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        Object child = runtime.loadOrGetObject("child");

        assertEquals(1, runtime.invokeObject(child, "run"));
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
    void parserAcceptsLegacyMultiStarArrayDeclarators() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/multistar_array_declarators.c", """
                string **customIcon(string **baseIcon, string color) {
                    string **ret;

                    ret = baseIcon;
                    return ret;
                }

                mixed value() {
                    string **bannerArt;

                    bannerArt = ({ ({ "a" }), ({ "b" }) });
                    return customIcon(bannerArt, "red");
                }
                """);

        assertEquals(List.of(List.of("a"), List.of("b")), object.invoke("value"));
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
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/missing_call_other.c", """
                mixed optional_call() {
                    return jvmud_invoke_lpc_object("room/missing", "advance", 0);
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
    void runtimeSupportsEmptyLoopBodyStatements() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/empty_loop_bodies.c", """
                int value() {
                    int i;

                    while (i++ < 2);
                    for (; i < 5; i++);

                    return i;
                }
                """);

        assertEquals(5, object.invoke("value"));
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
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle object = runtime.loadSource("smoke/allocate.c", """
                string values;

                mixed value() {
                    values = jvmud_allocate(3);
                    values[1] = "middle";
                    return values[1];
                }
                """);

        assertEquals("middle", object.invoke("value"));
    }

    @Test
    void mfunAllocateAndSscanfCompatibilityWrapNativeEfuns() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                mixed *allocate(int size) {
                    return jvmud_allocate(size);
                }

                int sscanf(mixed input, mixed format, mixed capture1, mixed capture2) {
                    return jvmud_sscanf(input, format, capture1, capture2);
                }
                """);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());
        LPCObjectHandle object = runtime.loadSource("smoke/mfun_legacy_efun_names.c", """
                mixed value(mixed input) {
                    string dir, dest;
                    mixed *values;

                    values = allocate(2);
                    values[0] = "prefix";
                    if (sscanf(input, "%s#%s", dir, dest) != 2)
                        return "bad";
                    values[1] = dir + ":" + dest;
                    return values;
                }
                """);

        assertEquals(List.of("prefix", "north:room/church"), object.invoke("value", "north#room/church"));
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
                private nosave object *cacheList;

                public nomask varargs int value(string name, string title) {
                    return 42;
                }

                static nomask varargs void helper(mixed *values) {
                }

                public nomask void call_out(string method, int delay, varargs mixed *data) {
                }

                protected void setup() {
                }
                """;

        CompilationResult result = new CompilationPipeline("java/lang/Object").run(source);
        assertTrue(result.getProblems().isEmpty(), () -> result.getProblems().toString());

        ASTObject ast = result.getAstObject();
        assertTrue(ast.fields().get("cache").modifiers().isPrivate());
        assertTrue(ast.fields().get("cache").modifiers().isNosave());
        assertTrue(ast.fields().get("cacheList").modifiers().isPrivate());
        assertTrue(ast.fields().get("cacheList").modifiers().isNosave());

        assertTrue(ast.methods().get("value").modifiers().isPublic());
        assertTrue(ast.methods().get("value").modifiers().isNomask());
        assertTrue(ast.methods().get("value").modifiers().isVarargs());

        assertTrue(ast.methods().get("helper").modifiers().isStatic());
        assertTrue(ast.methods().get("helper").modifiers().isNomask());
        assertTrue(ast.methods().get("helper").modifiers().isVarargs());

        assertTrue(ast.methods().get("call_out").parameters().get(2).isVarargs());

        assertTrue(ast.methods().get("setup").modifiers().isProtected());
    }

    @Test
    void parserRecordsVirtualInheritWithoutChangingOrdinaryInherits() throws IOException {
        Files.writeString(tempDir.resolve("base.c"), """
                int base_value() {
                    return 1;
                }
                """);
        Path sourcePath = tempDir.resolve("child.c");
        String source = """
                virtual inherit "base.c";

                int value() {
                    return 42;
                }
                """;
        Files.writeString(sourcePath, source);

        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(tempDir, List.of()));
        CompilationResult result = new CompilationPipeline("java/lang/Object", context)
                .run(sourcePath, source, "child", "/child.c", ParserOptions.defaults());

        assertTrue(result.getProblems().isEmpty(), () -> result.getProblems().toString());

        ASTObject ast = result.getAstObject();
        assertEquals(1, ast.inherits().size());
        assertEquals("\"base.c\"", ast.inherits().get(0).path());
        assertTrue(ast.inherits().get(0).isVirtual());
    }

    @Test
    void parserTreatsVirtualAsOrdinaryIdentifierAwayFromInherit() {
        String source = """
                int virtual = 40;

                int value() {
                    return virtual + 2;
                }
                """;

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/virtual_identifier.c", source);

        assertEquals(42, object.invoke("value"));
    }

    @Test
    void ldmudCatchCompatibilityReturnsZeroAfterProtectedSuccess() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/catch_success.c", """
                int value;
                mixed error;

                int run() {
                    error = catch(value = 42);
                    return error ? -1 : value;
                }
                """);

        assertEquals(42, object.invoke("run"));
    }

    @Test
    void ldmudCatchCompatibilityCapturesRuntimeFailure() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/catch_failure.c", """
                mixed error;

                int run() {
                    int denominator = 0;
                    error = catch(1 / denominator);
                    return error ? 42 : 0;
                }
                """);

        assertEquals(42, object.invoke("run"));
    }

    @Test
    void ldmudCatchCompatibilityAcceptsSequenceBodyAndNologFlag() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle object = runtime.loadSource("smoke/catch_nolog.c", """
                int value;
                mixed error;

                int run() {
                    int denominator = 0;
                    error = catch(value = 12; value = value / denominator; nolog);
                    return error ? value : -1;
                }
                """);

        assertEquals(12, object.invoke("run"));
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
    void globalObjectVarargsMethodsCanBeCalledWithOmittedArguments() throws Exception {
        Files.createDirectories(tempDir.resolve("secure/simulated-efuns"));
        Files.writeString(tempDir.resolve("secure/simulated-efuns/strings.c"), """
                public nomask varargs int add_optional(int value, int extra) {
                    return value + extra;
                }
                """);
        Files.writeString(tempDir.resolve("secure/simul_efun.c"), """
                inherit "/secure/simulated-efuns/strings.c";
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                int value() {
                    return add_optional(42);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mudlibGlobalObjectPath("secure/simul_efun")
                .build());

        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals(42, caller.invoke("value"));
    }

    @Test
    void compatibilityGlobalWrappersPreserveFunctionReferencesInMixedArguments() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/compat.c"), """
                string regreplace(string input, string pattern, mixed replacement, int flags) {
                    return jvmud_regex_replace(input, pattern, replacement, flags);
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                string value() {
                    return regreplace("you ponder.", "^[a-z]", #'upper_case, 1);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .compatibilityGlobalObjectPath("jvmud/compat")
                .engineFunction("jvmud_uppercase_text", "upper_case")
                .build());

        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals("You ponder.", caller.invoke("value"));
    }

    @Test
    void callableEngineFunctionAliasesBypassStringTypedMudlibWrappers() throws Exception {
        Files.createDirectories(tempDir.resolve("secure"));
        Files.writeString(tempDir.resolve("secure/simul_efun.c"), """
                public nomask varargs string regreplace(string inputString, string search,
                    string replace, int flags)
                {
                    return efun::regreplace(inputString, search, replace, flags);
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                string value() {
                    return regreplace("you ponder.", "^[a-z]", #'upper_case, 1);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mudlibGlobalObjectPath("secure/simul_efun")
                .engineFunction("jvmud_regex_replace", "regreplace")
                .engineFunction("jvmud_uppercase_text", "upper_case")
                .build());

        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));

        assertEquals("You ponder.", caller.invoke("value"));
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
        CoreEfuns.registerCore(context);
        context.setMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_size", "sizeof")
                .build());

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
    void usersEngineFunctionAliasIsTypedAsArrayForSizeofAndIndexing() {
        RuntimeContext context = new RuntimeContext(null);
        CoreEfuns.registerCore(context);
        context.setMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_size", "sizeof")
                .engineFunction("jvmud_users", "users")
                .build());

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
    void runtimeRebindsSessionBetweenLpcObjects() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle login = runtime.loadSource("smoke/login.c", """
                int handoff(object target) {
                    return jvmud_rebind_session_lpc_object(target, jvmud_current_lpc_object());
                }
                """);
        LPCObjectHandle player = runtime.loadSource("smoke/player.c", """
                void create() {
                    jvmud_add_action("look_action", "look");
                }

                int look_action(string command) {
                    jvmud_write("player-look");
                    return 1;
                }
                """);
        player.invoke("create");
        StringBuilder output = new StringBuilder();
        MudlibProjection loginProjection = MudlibProjection.combinedPlayerPersona("smoke/login", login.instance());

        runtime.bindSession("s1", login.instance(), "127.0.0.1", output::append, loginProjection);
        assertSame(login.instance(), runtime.lpcObjectForSession("s1").orElseThrow());
        assertEquals(loginProjection, runtime.playerRecordForSession("s1").orElseThrow().mudlibProfileProjection().orElseThrow());

        assertEquals(1, login.invoke("handoff", player.instance()));
        assertSame(player.instance(), runtime.lpcObjectForSession("s1").orElseThrow());
        Object reboundProjection = runtime.personaRecordForProjection(player.instance()).orElseThrow()
                .mudlibBehaviorProjection().orElseThrow();
        assertTrue(reboundProjection instanceof MudlibProjection);
        assertSame(player.instance(), ((MudlibProjection) reboundProjection).object());
        assertEquals(loginProjection, runtime.playerRecordForSession("s1").orElseThrow().mudlibProfileProjection().orElseThrow());
        assertTrue(runtime.personaRecordForProjection(login.instance()).orElseThrow().controllingPlayerId().isEmpty());
        assertTrue(runtime.isInteractive(player.instance()));
        assertFalse(runtime.isInteractive(login.instance()));

        runtime.refreshCommandActions(player.instance());
        assertEquals(1, runtime.dispatchCommand(player.instance(), "look"));
        assertEquals("player-look", output.toString());
    }

    @Test
    void runtimeDisplaysNotifyFailWhenCommandActionsDeclineLine() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle player = runtime.loadSource("smoke/player.c", """
                void create() {
                    jvmud_add_action("party_action", "party");
                }

                int notify_fail(mixed message) {
                    return jvmud_notify_fail(message);
                }

                int party_action(string command) {
                    notify_fail("You are not currently in a party.\\n");
                    return 0;
                }
                """);
        StringBuilder output = new StringBuilder();

        player.invoke("create");
        runtime.bindSession("s1", player.instance(), "127.0.0.1", output::append);

        assertEquals(0, runtime.dispatchCommand(player.instance(), "party"));
        assertEquals("You are not currently in a party.\n", output.toString());
    }

    @Test
    void runtimeRoutesPersonaSessionOutputAndPresenceQueries() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle first = runtime.loadSource("smoke/first_player.c", """
                void write_self() {
                    jvmud_write("first-only");
                }

                void tell(mixed target) {
                    jvmud_write_to_lpc_object(target, "second-only");
                }

                void say_to_place() {
                    jvmud_emit_perceivable(jvmud_current_lpc_object(), "place-only");
                }

                void say_to_place_except(mixed target) {
                    jvmud_emit_perceivable_except(jvmud_current_lpc_object(), "excepted", target);
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

        runtime.clearOutputTranscript();
        first.invoke("tell", unconnected.instance());
        assertEquals("first-only", firstOutput.toString());
        assertEquals("second-only", secondOutput.toString());
        assertEquals("", runtime.outputTranscript());

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

        assertTrue(runtime.writeToSession(firstSession.id(), "session-only\\n"));
        assertTrue(runtime.writeToPlayer(firstPlayer.id(), "player-only\\n"));
        assertTrue(runtime.writeToPersona(firstPersona.id(), "persona-only\\n"));
        assertEquals("session-only\nplayer-only\npersona-only\n", firstOutput.toString());
        assertEquals("", secondOutput.toString());
        assertEquals(firstOutput.toString(), runtime.outputTranscript());

        runtime.unbindSession("s1");

        assertFalse(runtime.writeToSession(firstSession.id(), "after-unbind"));
        assertFalse(runtime.writeToPlayer(firstPlayer.id(), "after-unbind"));
        assertFalse(runtime.writeToPersona(firstPersona.id(), "after-unbind"));
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

        assertTrue(runtime.writeToSession(session.id(), "one two three four five six\\n"));
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
        assertTrue(runtime.writeToPlayer(loginPlayer.id(), "login-control\\n"));
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
        assertTrue(runtime.writeToPersona(persona.id(), "persona-gameplay\\n"));
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

        assertTrue(runtime.writeToPersona(personaRecord.id(), "projected-persona\\n"));
        assertTrue(runtime.writeToPersona(personaRecord.id(), "\\tindented\\n"));
        assertEquals("projected-persona\n\tindented\n", output.toString());
    }

    @Test
    void runtimeCapturesNextSessionInputForPersona() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle player = runtime.loadSource("smoke/input_player.c", """
                string response;

                void ask() {
                    jvmud_write("Name: ");
                    jvmud_capture_session_input("answer", 0);
                }

                void ask_charmode() {
                    jvmud_capture_session_input("answer", 2);
                }

                void ask_secret() {
                    jvmud_capture_session_input("answer", 1);
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
        assertFalse(runtime.capturedSessionInputNoEcho(player.instance()));
        assertEquals("Name: ", output.toString());

        runtime.deliverCapturedSessionInput(player.instance(), "Alice");

        assertEquals("Alice", player.invoke("last_response"));
        assertEquals("Name: Hello Alice\n", output.toString());

        player.invoke("ask_charmode");
        assertTrue(runtime.hasCapturedSessionInput(player.instance()));
        assertFalse(runtime.capturedSessionInputNoEcho(player.instance()));
        runtime.deliverCapturedSessionInput(player.instance(), "Bob");
        assertEquals("Bob", player.invoke("last_response"));

        player.invoke("ask_secret");
        assertTrue(runtime.hasCapturedSessionInput(player.instance()));
        assertTrue(runtime.capturedSessionInputNoEcho(player.instance()));
    }

    @Test
    void runtimeReadsMudlibRootedTextForCompatibilityShims() throws Exception {
        Files.writeString(tempDir.resolve("WELCOME"), "Welcome to JVMud.\n");
        Files.writeString(tempDir.resolve("PAGED"), "one\ntwo\nthree\n");
        Path migrations = tempDir.resolve("secure/simulated-efuns/database/migrations");
        Files.createDirectories(migrations);
        Files.writeString(migrations.resolve("0002_second.sql"), "select 2;\n");
        Files.writeString(migrations.resolve("0001_first.sql"), "select 1;\n");

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .engineFunction("jvmud_filter", "filter")
                .engineFunction("jvmud_regex_replace", "regreplace")
                .engineFunction("jvmud_uppercase_text", "upper_case")
                .build());
        LPCObjectHandle reader = runtime.loadSource("smoke/text_reader.c", """
                mixed welcome() {
                    return jvmud_read_mudlib_text("/WELCOME");
                }

                mixed escaped() {
                    return jvmud_read_mudlib_text("../outside");
                }

                int remove_paged() {
                    return jvmud_remove_mudlib_text("/PAGED");
                }

                int copy_welcome() {
                    return jvmud_copy_mudlib_text("/WELCOME", "/COPY");
                }

                mixed copied() {
                    return jvmud_read_mudlib_text("/COPY");
                }

                int rename_copy() {
                    return jvmud_rename_mudlib_text("/COPY", "/RENAMED");
                }

                mixed renamed() {
                    return jvmud_read_mudlib_text("/RENAMED");
                }

                int create_temp_directory() {
                    return jvmud_create_mudlib_directory("/TMPDIR");
                }

                int remove_temp_directory() {
                    return jvmud_remove_mudlib_directory("/TMPDIR");
                }

                mixed paged() {
                    return jvmud_read_mudlib_text("/PAGED", 2, 2);
                }

                string *migration_names() {
                    return jvmud_list_mudlib_paths("/secure/simulated-efuns/database/migrations/*.sql");
                }

                string *migration_paths() {
                    return jvmud_list_mudlib_paths("/secure/simulated-efuns/database/migrations/*.sql", 0x10);
                }

                string lower() {
                    return jvmud_lowercase_text("MiXeD");
                }

                string capitalized() {
                    return jvmud_capitalize_text("alice");
                }

                string *split() {
                    return jvmud_split_text("alpha##beta##", "##");
                }

                int number() {
                    return jvmud_to_int(jvmud_regex_replace("0010_constructed_research.sql", "([0-9]+)_.*", "\\\\1", 1));
                }

                int square_root() {
                    return jvmud_to_int(jvmud_sqrt("16") * 3.0);
                }

                string number_text() {
                    return jvmud_to_string(12);
                }

                string *regex_matches() {
                    return jvmud_regex_match(({ "alpha.c", "beta.txt", "gamma.c" }), "[.]c$");
                }

                string realms_command_text() {
                    return jvmud_regex_replace("look [##Target##]", "^([^[#]+) +[[#].*", "\\\\1", 1);
                }

                string realms_option_prefix_text() {
                    return jvmud_regex_replace("score [-v]", "^([^-[]+ +)(.*)", "\\\\1", 1);
                }

                string *realms_question_command_alias_matches_literal() {
                    return jvmud_regex_match(({ "?", "look" }), "(^?( -v)*$)");
                }

                string callback_regex_replacement() {
                    return jvmud_regex_replace("you ##Infinitive::ponder##", "##Infinitive::[a-z]+##",
                        #'second_person_verb, 1);
                }

                string callback_efun_regex_replacement() {
                    return jvmud_regex_replace("you ponder.", "^[a-z]", #'jvmud_uppercase_text, 1);
                }

                string callback_engine_function_regex_replacement() {
                    return regreplace("you ponder.", "^[a-z]", #'upper_case, 1);
                }

                string second_person_verb(string match) {
                    return jvmud_regex_replace(match, "##Infinitive::([a-z]+)##", "\\\\1", 1);
                }

                int regex_no_match_is_false() {
                    return jvmud_regex_match(({ "bonus" }), "^-") ? 1 : 0;
                }

                int driver_info_is_false() {
                    return jvmud_driver_info(-44) ? 1 : 0;
                }

                int preferred_lpc_object_lookup() {
                    return jvmud_is_object(jvmud_find_lpc_object("/smoke/text_reader.c"));
                }

                int legacy_lpc_object_lookup() {
                    return jvmud_is_object(jvmud_find_object("/smoke/text_reader.c"));
                }

                int object_conversion_lookup() {
                    return jvmud_is_object(jvmud_to_lpc_object("/smoke/text_reader.c"));
                }

                mixed call_other(mixed target, string method, mixed arg1, mixed arg2) {
                    return jvmud_invoke_lpc_object(target, method, arg1, arg2);
                }

                int add_pair(int left, int right) {
                    return left + right;
                }

                int applied_call_other() {
                    return jvmud_apply_callable(#'call_other, jvmud_current_lpc_object(), "add_pair", ({ 2, 5 }));
                }

                int local_method_probe() {
                    return jvmud_method_exists("local_method_probe");
                }

                int explicit_method_probe() {
                    return jvmud_method_exists("local_method_probe", jvmud_current_lpc_object());
                }

                int missing_method_probe() {
                    return jvmud_method_exists("not_here");
                }

                int java_object_method_probe() {
                    return jvmud_method_exists("toString");
                }

                int method_names_probe() {
                    return jvmud_member(jvmud_lpc_object_methods(jvmud_current_lpc_object()), "local_method_probe") > -1;
                }

                string *mapping_keys() {
                    return jvmud_mapping_keys(([ "dawn": 1, "night": 2 ]));
                }

                int *mapping_values() {
                    return jvmud_mapping_values(([ "dawn": 1, "night": 2 ]));
                }

                mapping mapping_from_keys() {
                    return jvmud_mapping_from_keys(({ "north", "south", "north" }));
                }

                mapping mapping_from_false_keys() {
                    return jvmud_mapping_from_keys(0);
                }

                mapping mapping_delete_result() {
                    mapping values = ([ "keep": 1, "drop": 2 ]);
                    return jvmud_mapping_delete(values, "drop");
                }

                int mapping_delete_mutates() {
                    mapping values = ([ "keep": 1, "drop": 2 ]);
                    jvmud_mapping_delete(values, "drop");
                    return jvmud_member(values, "drop");
                }

                string *unique_directions() {
                    return jvmud_mapping_keys(jvmud_mapping_from_keys(({ "north", "south", "north" })));
                }

                int random_zero() {
                    return jvmud_random(0);
                }

                int random_one() {
                    return jvmud_random(1);
                }

                int random_range() {
                    int value = jvmud_random(4);
                    return (value >= 0) && (value < 4);
                }

                string wrapped() {
                    return jvmud_wrap_text("This is the land loving mother pigeon of all strings.", 10);
                }

                string wrapped_default() {
                    return jvmud_wrap_text("kept", 0);
                }

                string wrapped_empty() {
                    return jvmud_wrap_text("", 10);
                }

                mapping restored_value() {
                    mapping source = ([ "name": "book", "count": 3, "weight": 2.5,
                        "tags": ({ "paper", "ink" }), 7: "numeric key" ]);
                    string saved = jvmud_serialize_lpc_value(source);
                    return jvmud_deserialize_lpc_value(saved);
                }

                mixed *filtered_mapping_values() {
                    mapping values = ([ "keep": ([ "name": "keep", "amount": 7 ]),
                        "drop": ([ "name": "drop", "amount": 3 ]) ]);
                    mapping filtered = filter(values, (: $1 == $2["name"] && $1 == "keep" :));
                    return jvmud_mapping_values(filtered);
                }

                string epoch() {
                    return jvmud_format_time(86400);
                }

                string mutates_split_text_result() {
                    string *words = jvmud_split_text("red blue", " ");
                    words[0] = "green";
                    return words[0] + " " + words[1];
                }
                """);

        assertEquals("Welcome to JVMud.\n", reader.invoke("welcome"));
        assertEquals(0, reader.invoke("escaped"));
        assertEquals("two\nthree\n", reader.invoke("paged"));
        assertEquals(1, reader.invoke("remove_paged"));
        assertEquals(0, reader.invoke("paged"));
        assertEquals(0, reader.invoke("copy_welcome"));
        assertEquals("Welcome to JVMud.\n", reader.invoke("copied"));
        assertEquals(0, reader.invoke("rename_copy"));
        assertEquals("Welcome to JVMud.\n", reader.invoke("renamed"));
        assertEquals(1, reader.invoke("create_temp_directory"));
        assertEquals(1, reader.invoke("remove_temp_directory"));
        assertEquals(List.of("0001_first.sql", "0002_second.sql"), reader.invoke("migration_names"));
        assertEquals(List.of(
                "/secure/simulated-efuns/database/migrations/0001_first.sql",
                "/secure/simulated-efuns/database/migrations/0002_second.sql"),
                reader.invoke("migration_paths"));
        assertEquals("mixed", reader.invoke("lower"));
        assertEquals("Alice", reader.invoke("capitalized"));
        assertEquals(List.of("alpha", "beta", ""), reader.invoke("split"));
        assertEquals("green blue", reader.invoke("mutates_split_text_result"));
        assertEquals(10, reader.invoke("number"));
        assertEquals(12, reader.invoke("square_root"));
        assertEquals("12", reader.invoke("number_text"));
        assertEquals(List.of("alpha.c", "gamma.c"), reader.invoke("regex_matches"));
        assertEquals("look", reader.invoke("realms_command_text"));
        assertEquals("score ", reader.invoke("realms_option_prefix_text"));
        assertEquals(List.of("?"), reader.invoke("realms_question_command_alias_matches_literal"));
        assertEquals("you ponder", reader.invoke("callback_regex_replacement"));
        assertEquals("You ponder.", reader.invoke("callback_efun_regex_replacement"));
        assertEquals("You ponder.", reader.invoke("callback_engine_function_regex_replacement"));
        assertEquals(0, reader.invoke("regex_no_match_is_false"));
        assertEquals(0, reader.invoke("driver_info_is_false"));
        assertEquals(1, reader.invoke("preferred_lpc_object_lookup"));
        assertEquals(1, reader.invoke("legacy_lpc_object_lookup"));
        assertEquals(1, reader.invoke("object_conversion_lookup"));
        assertEquals(7, reader.invoke("applied_call_other"));
        assertEquals(1, reader.invoke("local_method_probe"));
        assertEquals(1, reader.invoke("explicit_method_probe"));
        assertEquals(0, reader.invoke("missing_method_probe"));
        assertEquals(0, reader.invoke("java_object_method_probe"));
        assertEquals(1, reader.invoke("method_names_probe"));
        assertEquals(Set.of("dawn", "night"), Set.copyOf((List<?>) reader.invoke("mapping_keys")));
        assertEquals(Set.of(1, 2), Set.copyOf((List<?>) reader.invoke("mapping_values")));
        assertEquals(Map.of("north", 1, "south", 1), reader.invoke("mapping_from_keys"));
        assertEquals(Map.of(), reader.invoke("mapping_from_false_keys"));
        assertEquals(Map.of("keep", 1), reader.invoke("mapping_delete_result"));
        assertEquals(0, reader.invoke("mapping_delete_mutates"));
        assertEquals(List.of("north", "south"), reader.invoke("unique_directions"));
        assertEquals(0, reader.invoke("random_zero"));
        assertEquals(0, reader.invoke("random_one"));
        assertEquals(1, reader.invoke("random_range"));
        assertEquals("This is\nthe land\nloving\nmother\npigeon of\nall\nstrings.\n", reader.invoke("wrapped"));
        assertEquals("kept\n", reader.invoke("wrapped_default"));
        assertEquals("", reader.invoke("wrapped_empty"));
        assertEquals(Map.of(
                "name", "book",
                "count", 3,
                "weight", 2.5d,
                "tags", List.of("paper", "ink"),
                7, "numeric key"), reader.invoke("restored_value"));
        assertEquals(List.of(Map.of("name", "keep", "amount", 7)), reader.invoke("filtered_mapping_values"));
        assertTrue(((String) reader.invoke("epoch")).contains("1970"));
    }

    @Test
    void extensionlessLoadPrefersSourceFileOverDirectoryWithSameStem() throws Exception {
        Path sourceRoot = tempDir.resolve("source");
        Files.createDirectories(sourceRoot.resolve("secure/master"));
        Files.writeString(sourceRoot.resolve("secure/master.c"), """
                string marker() {
                    return "master source";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(sourceRoot).build());
        LPCObjectHandle object = runtime.load("secure/master");

        assertEquals("master source", object.invoke("marker"));
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
        CoreEfuns.registerCore(runtime);
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
    void missingObjectSourceCanBeSuppliedByMudlibBoundary() throws Exception {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.loadSource("jvmud/mudlib.c", """
                object compile_object(string filename) {
                    object ob;

                    if (filename != "room/generated")
                        return 0;

                    ob = jvmud_clone_lpc_object("obj/template");
                    jvmud_invoke_lpc_object(ob, "set_label", filename);

                    return ob;
                }
                """);
        runtime.loadSource("obj/template.c", """
                string label;

                void set_label(string value) {
                    label = value;
                }

                string query_label() {
                    return label;
                }
                """);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .boundaryObjectPath("jvmud/mudlib")
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_SOURCE_MISSING, "compile_object")
                .build());

        Object supplied = runtime.loadOrGetObject("room/generated");
        Object again = runtime.loadOrGetObject("room/generated");

        assertSame(supplied, again);
        assertEquals("room/generated", runtime.objectId(supplied));
        assertEquals("room/generated", runtime.invokeObject(supplied, "query_label"));
    }

    @Test
    void missingObjectSourceFailureContinuesWhenMudlibDeclines() throws Exception {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.loadSource("jvmud/mudlib.c", """
                object compile_object(string filename) {
                    return 0;
                }
                """);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .boundaryObjectPath("jvmud/mudlib")
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_SOURCE_MISSING, "compile_object")
                .build());

        LPCRuntimeException exception = assertThrows(
                LPCRuntimeException.class,
                () -> runtime.loadOrGetObject("room/missing"));

        assertTrue(exception.getMessage().contains("Source file not found"), exception.getMessage());
    }

    @Test
    void circularSharedObjectLoadReturnsRegisteredInProgressSingleton() throws Exception {
        Files.writeString(tempDir.resolve("alpha.c"), """
                object beta = jvmud_load_lpc_object("beta");

                object beta_ref() {
                    return beta;
                }

                int beta_has_alpha() {
                    return jvmud_invoke_lpc_object(beta, "has_alpha", 0);
                }
                """);
        Files.writeString(tempDir.resolve("beta.c"), """
                object alpha = jvmud_load_lpc_object("alpha");

                int has_alpha() {
                    return alpha ? 1 : 0;
                }

                object alpha_ref() {
                    return alpha;
                }
                """);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);

        Object alpha = runtime.loadOrGetObject("alpha");
        Object beta = runtime.invokeObject(alpha, "beta_ref");

        assertSame(alpha, runtime.invokeObject(beta, "alpha_ref"));
        assertEquals(1, runtime.invokeObject(alpha, "beta_has_alpha"));
    }

    @Test
    void objectDestructionCanNotifyMudlibBeforeCleanup() throws Exception {
        LPCRuntime runtime = destructionRuntime("""
                mixed prepare_destruct(mixed ob) {
                    jvmud_append_mudlib_text("/log/DESTRUCT", "prepare " + jvmud_lpc_object_id(ob) + "\\n");
                    return 0;
                }
                """);
        LPCObjectHandle object = runtime.loadSource("obj/victim.c", """
                void destroy_self() {
                    jvmud_destroy_lpc_object(jvmud_current_lpc_object());
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
                    jvmud_append_mudlib_text("/log/DESTRUCT", "veto " + jvmud_lpc_object_id(ob) + "\\n");
                    return 1;
                }
                """);
        LPCObjectHandle object = runtime.loadSource("obj/vetoed.c", """
                void destroy_self() {
                    jvmud_destroy_lpc_object(jvmud_current_lpc_object());
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
                    jvmud_destroy_lpc_object(jvmud_current_lpc_object());
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
        CoreEfuns.registerCore(runtime);
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
        CoreEfuns.registerCore(runtime);
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
                    jvmud_bind_entity_alias(jvmud_current_lpc_object(), "living", name);
                }

                void enable_commands() {
                    jvmud_enable_commands();
                }

                object this_object() {
                    return jvmud_current_lpc_object();
                }
                """);
        Files.writeString(tempDir.resolve("player.c"), """
                void setup() {
                    set_living_name("Protasm");
                }

                void make_commandable() {
                    enable_commands();
                }

                void configure_commandable(int enabled) {
                    jvmud_configure_lpc_object(this_object(), 0, enabled);
                }

                object lookup(mixed name) {
                    return find_living(name);
                }

                status is_registered() {
                    return living(this_object());
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));

        player.invoke("setup");

        assertEquals(player.instance(), player.invoke("lookup", "protasm"));
        assertEquals(false, player.invoke("is_registered"));

        player.invoke("make_commandable");

        assertEquals(true, player.invoke("is_registered"));
        player.invoke("configure_commandable", 0);
        assertEquals(false, player.invoke("is_registered"));
        player.invoke("configure_commandable", 1);
        assertEquals(true, player.invoke("is_registered"));
    }

    @Test
    void previousObjectReportsCallingObjectAcrossMfunInvocation() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                mixed call_other(mixed target, string method) {
                    return jvmud_invoke_lpc_object(target, method);
                }

                string object_name(mixed ob) {
                    return jvmud_lpc_object_id(ob);
                }

                object previous_object() {
                    return jvmud_previous_lpc_object();
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
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle caller = runtime.load(tempDir.resolve("caller.c"));
        LPCObjectHandle target = runtime.load(tempDir.resolve("target.c"));

        assertEquals("caller", caller.invoke("value"));
        assertEquals("target", target.invoke("self_name"));
    }

    @Test
    void currentAgentDoesNotFallBackToCurrentObject() throws Exception {
        Files.writeString(tempDir.resolve("probe.c"), """
                mixed strict_agent() {
                    return jvmud_current_agent();
                }

                mixed loose_actor() {
                    return jvmud_current_actor();
                }
                """);
        Files.writeString(tempDir.resolve("agent.c"), """
                string id() {
                    return "agent";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);

        LPCObjectHandle probe = runtime.load(tempDir.resolve("probe.c"));
        LPCObjectHandle agent = runtime.load(tempDir.resolve("agent.c"));

        assertNull(probe.invoke("strict_agent"));
        assertSame(probe.instance(), probe.invoke("loose_actor"));
        assertSame(agent.instance(), runtime.withCommandActor(agent.instance(), () -> probe.invoke("strict_agent")));
    }

    @Test
    void findPlayerReturnsInteractiveUserByMudlibName() throws Exception {
        Files.writeString(tempDir.resolve("player.c"), """
                string RealName() {
                    return "Gorthaur";
                }

                object find_player(string name) {
                    return jvmud_find_player(name);
                }
                """);
        Files.writeString(tempDir.resolve("offline.c"), """
                string RealName() {
                    return "Dwight";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);

        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));
        LPCObjectHandle offline = runtime.load(tempDir.resolve("offline.c"));
        runtime.bindSession("s1", player.instance(), "127.0.0.1", ignored -> {});

        assertSame(player.instance(), player.invoke("find_player", "gorthaur"));
        assertSame(player.instance(), player.invoke("find_player", " GORTHAUR "));
        assertNull(player.invoke("find_player", "dwight"));
        assertFalse(runtime.isInteractive(offline.instance()));
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
                    return jvmud_invoke_lpc_object(target, method, arg);
                }

                void move_object(mixed ob, mixed destination) {
                    jvmud_move_entity(ob, destination);
                }

                object this_player() {
                    return jvmud_current_actor();
                }

                object this_object() {
                    return jvmud_current_lpc_object();
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
        CoreEfuns.registerCore(runtime);
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
        CoreEfuns.registerCore(runtime);
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
        CoreEfuns.registerCore(runtime);
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
    void commandDispatchKeepsSelfRegisteredCatchAllActionFromCreate() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void add_action(string method, string verb, int flag) {
                    jvmud_add_action(method, verb, flag);
                }

                void write(mixed value) {
                    jvmud_write(value);
                }
                """);
        Files.writeString(tempDir.resolve("player.c"), """
                void create() {
                    add_action("executeCommand", "", 2);
                }

                int executeCommand(string command) {
                    write("handled " + command + "\\n");
                    return 1;
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "create")
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build());

        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));

        runtime.refreshCommandActions(player.instance());
        runtime.clearOutputTranscript();

        assertEquals(1, runtime.dispatchCommand(player.instance(), "look"));
        assertEquals("handled look\n", runtime.outputTranscript());
    }

    @Test
    void commandDispatchRunsScopedDirectionBeforeDecliningCatchAllAction() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void add_action(string method, string verb) {
                    jvmud_add_action(method, verb);
                }

                void add_action(string method, string verb, int flag) {
                    jvmud_add_action(method, verb, flag);
                }

                void move_object(mixed ob, mixed destination) {
                    jvmud_move_entity(ob, destination);
                }

                object this_player() {
                    return jvmud_current_actor();
                }

                string query_verb() {
                    return jvmud_current_verb();
                }

                void write(mixed value) {
                    jvmud_write(value);
                }
                """);
        Files.writeString(tempDir.resolve("player.c"), """
                void create() {
                    add_action("executeCommand", "", 2);
                }

                int executeCommand(string command) {
                    return 0;
                }
                """);
        Files.writeString(tempDir.resolve("room/start.c"), """
                void init() {
                    add_action("move", "west");
                }

                int move(string ignored) {
                    if (query_verb() != "west") {
                        return 0;
                    }
                    write("west moved\\n");
                    move_object(this_player(), "room/next");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("room/next.c"), """
                string short() {
                    return "next room";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "create")
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build());

        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));
        LPCObjectHandle start = runtime.load(tempDir.resolve("room/start.c"));
        LPCObjectHandle next = runtime.load(tempDir.resolve("room/next.c"));
        runtime.moveObject(player.instance(), start.instance());
        runtime.refreshCommandActions(player.instance());

        assertEquals(1, runtime.dispatchCommand(player.instance(), "west"));
        assertEquals(next.instance(), runtime.environment(player.instance()));
        assertEquals("west moved\n", runtime.outputTranscript());
    }

    @Test
    void lifecyclePrefersMostDerivedNoArgumentHookWhenOptionalInvocationTrimsArguments() throws Exception {
        Files.writeString(tempDir.resolve("base.c"), """
                int marker = 0;

                void create() {
                    marker = 1;
                }

                int value() {
                    return marker;
                }
                """);
        Files.writeString(tempDir.resolve("player.c"), """
                inherit "/base.c";

                void create() {
                    marker = 2;
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "create")
                .build());

        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));

        assertEquals(2, player.invoke("value"));
    }

    @Test
    void commandDispatchTreatsMissingActionMethodAsUnhandled() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void add_action(string method, string verb) {
                    jvmud_add_action(method, verb);
                }
                """);
        Files.writeString(tempDir.resolve("player.c"), """
                string short() {
                    return "player";
                }
                """);
        Files.writeString(tempDir.resolve("room/start.c"), """
                void init() {
                    add_action("missing", "south");
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build());

        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));
        LPCObjectHandle start = runtime.load(tempDir.resolve("room/start.c"));
        runtime.moveObject(player.instance(), start.instance());
        runtime.refreshCommandActions(player.instance());

        assertEquals(0, runtime.dispatchCommand(player.instance(), "south"));
        assertEquals("", runtime.outputTranscript());
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
                    return jvmud_invoke_lpc_object(target, method, arg);
                }

                void move_object(mixed ob, mixed destination) {
                    jvmud_move_entity(ob, destination);
                }

                object this_object() {
                    return jvmud_current_lpc_object();
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
        CoreEfuns.registerCore(runtime);
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
                    return jvmud_current_lpc_object();
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
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());

        LPCObjectHandle room = runtime.load(tempDir.resolve("room.c"));
        room.invoke("setup");

        assertNotNull(room.invoke("find_book"));
    }

    @Test
    void presentDoesNotMatchBlankIdentifiers() throws Exception {
        Files.writeString(tempDir.resolve("thing.c"), """
                int id(string str) {
                    return str == "";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle room = runtime.loadSource("smoke/room.c", "int value() { return 1; }");
        LPCObjectHandle thing = runtime.load(tempDir.resolve("thing.c"));
        runtime.moveObject(thing.instance(), room.instance());

        assertNull(runtime.present("", room.instance()));
    }

    @Test
    void setEntityLocationUpdatesLocationAndInventory() throws Exception {
        Files.writeString(tempDir.resolve("thing.c"), """
                int id(string str) {
                    return str == "thing";
                }
                """);
        Files.writeString(tempDir.resolve("room.c"), """
                object thing;

                void setup() {
                    thing = jvmud_load_lpc_object("thing");
                    jvmud_set_entity_location(thing, jvmud_current_lpc_object());
                }

                object where() {
                    return jvmud_entity_location(thing);
                }

                object find_thing() {
                    return jvmud_find_entity("thing", jvmud_current_lpc_object());
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle room = runtime.load(tempDir.resolve("room.c"));
        room.invoke("setup");

        assertEquals(room.instance(), room.invoke("where"));
        assertNotNull(room.invoke("find_thing"));
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
                    return jvmud_invoke_lpc_object(target, method, arg);
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
        CoreEfuns.registerCore(runtime);
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
                    return jvmud_invoke_lpc_object(target, method, arg1, arg2);
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
        CoreEfuns.registerCore(runtime);
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
                    return jvmud_invoke_lpc_object(target, method, arg);
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
        CoreEfuns.registerCore(runtime);
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
                    return jvmud_invoke_lpc_object(target, method, arg);
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
        CoreEfuns.registerCore(runtime);
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
                    return jvmud_invoke_lpc_object(target, method, arg);
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
        CoreEfuns.registerCore(runtime);
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
        CoreEfuns.registerCore(runtime);
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
        CoreEfuns.registerCore(runtime);
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
    void compilerResolvesMultipleDirectInheritsAndPreservesChildOverride() throws Exception {
        Files.writeString(tempDir.resolve("base_one.c"), """
                int value() {
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("base_two.c"), """
                int other_value() {
                    return 2;
                }
                """);
        Path childPath = tempDir.resolve("child.c");
        String source = """
                inherit "base_one.c";
                inherit "base_two.c";

                int value() {
                    return 42;
                }
                """;
        Files.writeString(childPath, source);

        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(tempDir, List.of()));
        CompilationResult result = new CompilationPipeline("java/lang/Object", context)
                .run(childPath, source, "child", "/child.c", ParserOptions.defaults());

        assertTrue(result.getProblems().isEmpty(), () -> problemMessages(result));
        assertEquals(2, result.getAstObject().inherits().size());
        assertEquals(2, result.getCompilationUnit().directParentUnits().size());
        assertEquals("base_one", result.getCompilationUnit().parentUnit().astObject().name());

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle child = runtime.load(childPath);

        assertEquals(42, child.invoke("value"));
    }

    @Test
    void runtimeReportsTransitiveInheritedLpcPrograms() throws Exception {
        Files.writeString(tempDir.resolve("grandparent.c"), """
                int grand_value() {
                    return 3;
                }
                """);
        Files.writeString(tempDir.resolve("base_one.c"), """
                inherit "grandparent.c";

                int first_value() {
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("base_two.c"), """
                int second_value() {
                    return 2;
                }
                """);
        Path childPath = tempDir.resolve("child.c");
        Files.writeString(childPath, """
                inherit "base_one.c";
                inherit "base_two.c";

                string *inherited_programs() {
                    return jvmud_inherited_programs(jvmud_current_lpc_object());
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle child = runtime.load(childPath);

        assertEquals(List.of("/child.c", "/base_one.c", "/grandparent.c", "/base_two.c"), child.invoke("inherited_programs"));
    }

    @Test
    void runtimeReportsCloneProgramBeforeInheritedLpcPrograms() throws Exception {
        Files.writeString(tempDir.resolve("base.c"), """
                int base_value() {
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("child.c"), """
                inherit "base.c";

                string *inherited_programs() {
                    return jvmud_inherited_programs(jvmud_current_lpc_object());
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        Object child = runtime.cloneObject("child.c");

        assertEquals(List.of("/child.c", "/base.c"), runtime.invokeObject(child, "inherited_programs"));
    }

    @Test
    void compilerLowersInheritedFieldFromSecondaryDirectParent() throws Exception {
        Files.writeString(tempDir.resolve("base_one.c"), """
                int first_value() {
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("base_two.c"), """
                protected int PersistRegion = 1;

                int persist_value() {
                    return PersistRegion;
                }
                """);
        Path childPath = tempDir.resolve("child.c");
        Files.writeString(childPath, """
                inherit "base_one.c";
                inherit "base_two.c";

                int current_persistence() {
                    return PersistRegion;
                }

                int disable_persistence() {
                    PersistRegion = 0;
                    return PersistRegion;
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle child = runtime.load(childPath);

        assertEquals(1, child.invoke("current_persistence"));
        assertEquals(0, child.invoke("disable_persistence"));
    }

    @Test
    void compilerLowersInheritedMethodFromSecondaryDirectParent() throws Exception {
        Files.writeString(tempDir.resolve("base_one.c"), """
                int first_value() {
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("base_two.c"), """
                protected int count = 40;

                protected int secondary_helper(int amount) {
                    count += amount;
                    return count;
                }
                """);
        Path childPath = tempDir.resolve("child.c");
        Files.writeString(childPath, """
                inherit "base_one.c";
                inherit "base_two.c";

                int value() {
                    return secondary_helper(2);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle child = runtime.load(childPath);

        assertEquals(42, child.invoke("value"));
    }

    @Test
    void compilerCallsTransitiveSecondaryInheritedMethodThroughPrimaryParentChain() throws Exception {
        Files.writeString(tempDir.resolve("primary.c"), """
                int primary_value() {
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("secondary.c"), """
                protected int secondary_helper(int amount) {
                    return 40 + amount;
                }
                """);
        Files.writeString(tempDir.resolve("parent.c"), """
                inherit "primary.c";
                inherit "secondary.c";
                """);
        Path childPath = tempDir.resolve("child.c");
        Files.writeString(childPath, """
                inherit "parent.c";

                int value() {
                    return secondary_helper(2);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle child = runtime.load(childPath);

        assertEquals(42, child.invoke("value"));
    }

    @Test
    void compilerCallsTransitiveMethodBehindSecondaryParentChain() throws Exception {
        Files.writeString(tempDir.resolve("primary.c"), """
                int primary_value() {
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("layout.c"), """
                protected int layout_helper(int amount) {
                    return 40 + amount;
                }
                """);
        Files.writeString(tempDir.resolve("decorators.c"), """
                inherit "layout.c";
                """);
        Files.writeString(tempDir.resolve("files.c"), """
                inherit "decorators.c";
                """);
        Path childPath = tempDir.resolve("child.c");
        Files.writeString(childPath, """
                inherit "primary.c";
                inherit "files.c";

                int value() {
                    return layout_helper(2);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle child = runtime.load(childPath);

        assertEquals(42, child.invoke("value"));
    }

    @Test
    void compilerFlattensVirtualDiamondInheritedFieldsOnce() throws Exception {
        Files.writeString(tempDir.resolve("primary.c"), """
                int primary_value() {
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("state.c"), """
                protected object StateMachineService = 0;
                """);
        Files.writeString(tempDir.resolve("lighting.c"), """
                virtual inherit "state.c";
                """);
        Files.writeString(tempDir.resolve("description.c"), """
                virtual inherit "state.c";
                """);
        Path childPath = tempDir.resolve("child.c");
        Files.writeString(childPath, """
                inherit "primary.c";
                inherit "lighting.c";
                inherit "description.c";

                int value() {
                    return primary_value();
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle child = runtime.load(childPath);

        assertEquals(1, child.invoke("value"));
    }

    @Test
    void compilerKeepsPrimaryAncestorMethodBehindPrimaryParentOverride() throws Exception {
        Files.writeString(tempDir.resolve("ancestor.c"), """
                string kind() {
                    return "ancestor";
                }
                """);
        Files.writeString(tempDir.resolve("parent.c"), """
                inherit "ancestor.c";

                string kind() {
                    return "parent";
                }
                """);
        Path childPath = tempDir.resolve("child.c");
        Files.writeString(childPath, """
                inherit "parent.c";

                string value() {
                    return kind();
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle child = runtime.load(childPath);

        assertEquals("parent", child.invoke("kind"));
        assertEquals("parent", child.invoke("value"));
    }

    @Test
    void compilerReportsAmbiguousInheritedMethodsFromMultipleDirectParents() throws Exception {
        Files.writeString(tempDir.resolve("left.c"), """
                int shared() {
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("right.c"), """
                string shared() {
                    return "right";
                }
                """);
        Path childPath = tempDir.resolve("child.c");
        String source = """
                inherit "left.c";
                inherit "right.c";
                """;
        Files.writeString(childPath, source);

        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(tempDir, List.of()));
        CompilationResult result = new CompilationPipeline("java/lang/Object", context)
                .run(childPath, source, "child", "/child.c", ParserOptions.defaults());

        assertTrue(
                result.getProblems().stream()
                        .anyMatch(problem -> problem.getMessage()
                                .contains("Ambiguous inherited method 'shared' with arity 0")),
                () -> problemMessages(result));
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
                    return jvmud_lpc_object_id(ob);
                }

                int pointerp(mixed value) {
                    return jvmud_is_array(value);
                }

                object this_object() {
                    return jvmud_current_lpc_object();
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
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .build());
        LPCObjectHandle child = runtime.load(tempDir.resolve("room/village/vill_green.c"));

        assertEquals("three", child.invoke("visible_exits"));
    }

    @Test
    void coreTypePredicatesReportIntsAndLiveObjects() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);

        LPCObjectHandle object = runtime.loadSource("smoke/type_predicates.c", """
                int int_status(mixed value) {
                    return jvmud_is_int(value);
                }

                int float_status(mixed value) {
                    return jvmud_is_float(value);
                }

                int object_status(mixed value) {
                    return jvmud_is_object(value);
                }

                mixed *array_value() {
                    return ({ 1, 2 });
                }
                """);

        assertEquals(1, object.invoke("int_status", 7));
        assertEquals(0, object.invoke("int_status", "7"));
        assertEquals(0, object.invoke("int_status", object.invoke("array_value")));
        assertEquals(1, object.invoke("float_status", 1.25));
        assertEquals(0, object.invoke("float_status", 1));
        assertEquals(0, object.invoke("float_status", "1.25"));
        assertEquals(1, object.invoke("object_status", object.instance()));
        assertEquals(0, object.invoke("object_status", "smoke/type_predicates"));
        runtime.destructObject(object.instance());
        assertEquals(0, object.invoke("object_status", object.instance()));
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
    void runtimeResolvesExtensionlessAbsoluteMudlibInherits() throws Exception {
        Path mudlibRoot = tempDir.resolve("realms");
        Files.createDirectories(mudlibRoot.resolve("areas"));
        Files.createDirectories(mudlibRoot.resolve("lib/core"));
        Files.writeString(mudlibRoot.resolve("lib/core/thing.c"), """
                int thing_value() {
                    return 30;
                }
                """);
        Files.writeString(mudlibRoot.resolve("areas/example.c"), """
                inherit "/lib/core/thing";

                int child_value() {
                    return thing_value() + 12;
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
        CoreEfuns.registerCore(runtime);

        LPCObjectHandle object = runtime.loadSource("smoke/sscanf.c", """
                mixed parse(mixed value) {
                    string dir, dest;
                    if (jvmud_sscanf(value, "%s#%s", dir, dest) != 2)
                        return "bad";
                    return dir + ":" + dest;
                }
                """);

        assertEquals("north:room/village/church", object.invoke("parse", "north#room/village/church"));
    }

    @Test
    void sscanfAssignsCapturesWithDynamicFormatString() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);

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
                    if (jvmud_sscanf(value, format(), who, rest) != 2)
                        return 0;
                    return rest == match;
                }

                string parsed(string value) {
                    string who, rest;
                    if (jvmud_sscanf(value, format(), who, rest) != 2)
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
        CoreEfuns.registerCore(runtime);

        LPCObjectHandle object = runtime.loadSource("smoke/sscanf_no_match.c", """
                int parse(string value) {
                    int number;
                    number = 7;
                    if (jvmud_sscanf(value, "r %d", number) == 1)
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
        CoreEfuns.registerCore(runtime);

        LPCObjectHandle object = runtime.loadSource("smoke/sscanf_no_field_match.c", """
                int number;

                int parse(string value) {
                    number = 7;
                    if (jvmud_sscanf(value, "r %d", number) == 1)
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
        CoreEfuns.registerCore(runtime);

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
    void currentObjectSeesInventoryLightInDarkEnvironment() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);

        LPCObjectHandle room = runtime.loadSource("smoke/dark_room.c", "");
        LPCObjectHandle actor = runtime.loadSource("smoke/light_actor.c", """
                int visible_light() {
                    return jvmud_set_light(0);
                }
                """);
        LPCObjectHandle torch = runtime.loadSource("smoke/torch.c", """
                void light() {
                    jvmud_set_light(1);
                }
                """);

        runtime.moveObject(actor.instance(), room.instance());
        runtime.moveObject(torch.instance(), actor.instance());
        torch.invoke("light");

        assertEquals(1, actor.invoke("visible_light"));
    }

    @Test
    void inventoryLightDoesNotPassThroughOpaqueContainers() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);

        LPCObjectHandle room = runtime.loadSource("smoke/dark_room.c", "");
        LPCObjectHandle actor = runtime.loadSource("smoke/light_actor.c", """
                int visible_light() {
                    return jvmud_set_light(0);
                }
                """);
        LPCObjectHandle box = runtime.loadSource("smoke/opaque_box.c", """
                void make_opaque() {
                    jvmud_set_entity_translucent(jvmud_current_lpc_object(), 0);
                }

                int is_translucent() {
                    return jvmud_entity_translucent(jvmud_current_lpc_object());
                }
                """);
        LPCObjectHandle torch = runtime.loadSource("smoke/torch.c", """
                void light() {
                    jvmud_set_light(1);
                }
                """);

        runtime.moveObject(actor.instance(), room.instance());
        runtime.moveObject(box.instance(), actor.instance());
        runtime.moveObject(torch.instance(), box.instance());
        torch.invoke("light");

        assertEquals(1, box.invoke("is_translucent"));
        assertEquals(1, actor.invoke("visible_light"));

        box.invoke("make_opaque");

        assertEquals(0, box.invoke("is_translucent"));
        assertEquals(0, actor.invoke("visible_light"));
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
    void destructedObjectReferencesAreFalseAndIgnoreOptionalCalls() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        LPCObjectHandle victim = runtime.loadSource("smoke/doomed.c", """
                string query_name() {
                    return "Doomed";
                }
                """);
        LPCObjectHandle observer = runtime.loadSource("smoke/observer.c", """
                object victim;
                int branch;
                mixed response;

                void set_victim(object ob) {
                    victim = ob;
                }

                void check_victim() {
                    if (victim)
                        branch = 1;
                    else
                        branch = 2;

                    response = victim->query_name();
                }

                int query_branch() {
                    return branch;
                }

                mixed query_response() {
                    return response;
                }
                """);

        observer.invoke("set_victim", victim.instance());
        observer.invoke("check_victim");
        assertEquals(1, observer.invoke("query_branch"));
        assertEquals("Doomed", observer.invoke("query_response"));

        runtime.destructObject(victim.instance());
        observer.invoke("check_victim");

        assertEquals(2, observer.invoke("query_branch"));
        assertEquals(0, observer.invoke("query_response"));
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
    void mixedLocalsDefaultToZeroForArithmeticCompatibility() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LPCObjectHandle object = runtime.loadSource("smoke/default_mixed_local.c", """
                int value() {
                    mixed unset;
                    return unset + 4;
                }
                """);

        assertEquals(4, object.invoke("value"));
    }

    @Test
    void mixedFieldsDefaultToZeroForArithmeticCompatibility() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());

        LPCObjectHandle object = runtime.loadSource("smoke/default_mixed_field.c", """
                mixed unset;

                int value() {
                    return unset + 4;
                }
                """);

        assertEquals(4, object.invoke("value"));
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
        CoreEfuns.registerCore(runtime);

        LPCObjectHandle object = runtime.loadSource("engine_function/caller.c", """
                int value() {
                    return 42;
                }

                mixed reflected_value() {
                    return jvmud_invoke_lpc_object(jvmud_current_lpc_object(), "value", 0);
                }
                """);

        assertEquals(42, object.invoke("reflected_value"));
    }

    @Test
    void writeEngineFunctionCapturesOutputForCliAndTests() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);

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
        CoreEfuns.registerCore(runtime);

        LPCObjectHandle room = runtime.load(tempDir.resolve("room.c"));
        Object thing = runtime.cloneObject("thing");

        runtime.moveObject(thing, room.instance());

        assertEquals(room.instance(), runtime.environment(thing));
        assertEquals(thing, runtime.firstInventory(room.instance()));
        assertEquals(thing, runtime.present("thing", room.instance()));
        assertEquals(null, runtime.nextInventory(thing));
    }

    @Test
    void cloneLpcObjectEfunCreatesNumberedCloneInstances() throws Exception {
        Files.writeString(tempDir.resolve("thing.c"), """
                string short() {
                    return "clone target";
                }
                """);
        Files.writeString(tempDir.resolve("factory.c"), """
                string make() {
                    object ob;

                    ob = jvmud_clone_lpc_object("thing");
                    return jvmud_lpc_object_id(ob);
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);

        LPCObjectHandle factory = runtime.load(tempDir.resolve("factory.c"));

        assertEquals("thing#clone1", factory.invoke("make"));
        assertEquals("thing#clone2", factory.invoke("make"));
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
        CoreEfuns.registerCore(runtime);
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
        CoreEfuns.registerCore(runtime);
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
        CoreEfuns.registerCore(runtime);
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
                    return jvmud_current_lpc_object();
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
        CoreEfuns.registerCore(runtime);
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
    void rebindFlushesPendingTargetedOutputAndRefreshesCarriedSelectorActions() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void add_action(string method, string verb, int flag) {
                    jvmud_add_action(method, verb, flag);
                }

                int remove_action(int flags, mixed actor) {
                    return jvmud_remove_action(flags, actor);
                }

                object clone_object(string path) {
                    return jvmud_clone_lpc_object(path);
                }

                int exec(object newObject, object oldObject) {
                    return jvmud_rebind_session_lpc_object(newObject, oldObject);
                }

                void move_object(mixed ob, mixed destination) {
                    jvmud_move_entity(ob, destination);
                }

                void tell_object(object target, mixed message) {
                    jvmud_write_to_lpc_object(target, message);
                }

                object this_object() {
                    return jvmud_current_lpc_object();
                }

                void write(mixed value) {
                    jvmud_write(value);
                }
                """);
        Files.writeString(tempDir.resolve("login.c"), """
                void create_selector(object player) {
                    object selector = clone_object("selector");
                    move_object(selector, player);
                    selector->initiateSelector(player);
                    exec(player, this_object());
                }
                """);
        Files.writeString(tempDir.resolve("player.c"), """
                void addCommands() {
                    add_action("executeCommand", "", 2);
                }

                int executeCommand(string command) {
                    write("player " + command + "\\n");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("selector.c"), """
                object User;

                void init() {
                    add_action("applySelection", "", 3);
                }

                void initiateSelector(object user) {
                    User = user;
                    tell_object(user, "selector menu\\n");
                }

                int applySelection(string choice) {
                    remove_action(1, User);
                    write("selected " + choice + "\\n");
                    return 1;
                }
                """);

        StringBuilder output = new StringBuilder();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "create")
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build());
        LPCObjectHandle login = runtime.load(tempDir.resolve("login.c"));
        LPCObjectHandle player = runtime.load(tempDir.resolve("player.c"));
        runtime.bindSession("test/session", login.instance(), "127.0.0.1", output::append);

        login.invoke("create_selector", player.instance());

        assertEquals("selector menu\n", output.toString());

        runtime.withRuntimeContext(() -> {
            runtime.invokeObject(player.instance(), "addCommands");
            return null;
        });
        runtime.refreshCommandActions(player.instance());
        assertEquals(1, runtime.dispatchCommand(player.instance(), "1"));
        assertEquals("selector menu\nselected 1\n", output.toString());

        assertEquals(1, runtime.dispatchCommand(player.instance(), "look"));
        assertEquals("selector menu\nselected 1\nplayer look\n", output.toString());
    }

    @Test
    void newerEmptyVerbActionPrecedesOlderSelectorAction() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void add_action(string method, string verb, int flag) {
                    jvmud_add_action(method, verb, flag);
                }

                void write(mixed value) {
                    jvmud_write(value);
                }
                """);
        Files.writeString(tempDir.resolve("actor.c"), """
                void init() {
                    add_action("parentSelection", "", 3);
                    add_action("childSelection", "", 3);
                }

                int parentSelection(string choice) {
                    write("parent " + choice + "\\n");
                    return 1;
                }

                int childSelection(string choice) {
                    write("child " + choice + "\\n");
                    return 1;
                }
                """);

        StringBuilder output = new StringBuilder();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .mfunObjectPath("jvmud/mfuns")
                .lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED, "init")
                .build());
        LPCObjectHandle actor = runtime.load(tempDir.resolve("actor.c"));
        runtime.bindSession("test/session", actor.instance(), "127.0.0.1", output::append);

        runtime.refreshCommandActions(actor.instance());

        assertEquals(1, runtime.dispatchCommand(actor.instance(), "1"));
        assertEquals("child 1\n", output.toString());
    }

    @Test
    void runtimeRejectsContainmentCycles() throws Exception {
        Files.writeString(tempDir.resolve("thing.c"), """
                string short() {
                    return "thing";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);

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
        CoreEfuns.registerCore(runtime);
        LPCObjectHandle controller = runtime.loadSource("controller.c", """
                void setup() {
                    object thing;
                    thing = jvmud_clone_lpc_object("thing");
                    jvmud_move_entity(thing, jvmud_current_lpc_object());
                    jvmud_write(jvmud_invoke_lpc_object(jvmud_first_entity_at(jvmud_current_lpc_object()), "short", 0));
                }
                """);

        controller.invoke("setup");

        assertEquals("a small thing", runtime.outputTranscript());
        assertEquals(controller.instance(), runtime.environment(runtime.firstInventory(controller.instance())));
    }

    private LPCRuntime temporalRuntime(WorldScheduler scheduler, int defaultIntervalSeconds) {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
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
        CoreEfuns.registerCore(runtime);
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
        CoreEfuns.registerCore(runtime);
        runtime.loadSource("jvmud/mudlib.c", mudlibSource);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .boundaryObjectPath("jvmud/mudlib")
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_DESTRUCTION_REQUESTED, "prepare_destruct")
                .build());
        return runtime;
    }

    private String problemMessages(CompilationResult result) {
        StringBuilder messages = new StringBuilder();
        result.getProblems().forEach(problem -> {
            if (!messages.isEmpty())
                messages.append('\n');
            messages.append(problem.getStage()).append(": ").append(problem.getMessage());
            if (problem.getLine() != null)
                messages.append(" at line ").append(problem.getLine());
            if (problem.getThrowable() != null && problem.getThrowable().getMessage() != null)
                messages.append(" - ").append(problem.getThrowable().getMessage());
        });
        return messages.toString();
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

    /** Builds a smoke source whose collection literals are large enough to require bytecode helpers. */
    private String largeCollectionLiteralSource(int entries) {
        StringBuilder source = new StringBuilder();
        source.append("mapping values = ([\n");
        for (int i = 0; i < entries; i++) {
            source.append("    \"item ")
                    .append(i)
                    .append("\": ([ \"score\": ")
                    .append(i)
                    .append(", \"tags\": ({ \"tag ")
                    .append(i)
                    .append("a\", \"tag ")
                    .append(i)
                    .append("b\" }), \"description\": \"item ")
                    .append(i)
                    .append("\" \" description\" ]),\n");
        }
        source.append("]);\n");
        source.append("mixed *numbers = ({\n");
        for (int i = 0; i < entries; i++) {
            source.append("    ").append(i).append(",\n");
        }
        source.append("});\n");
        source.append("""
                mixed score() {
                    return values["item 199"]["score"];
                }

                mixed tag() {
                    return values["item 199"]["tags"][1];
                }

                mixed description() {
                    return values["item 199"]["description"];
                }

                mixed number() {
                    return numbers[239];
                }
                """);
        return source.toString();
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
