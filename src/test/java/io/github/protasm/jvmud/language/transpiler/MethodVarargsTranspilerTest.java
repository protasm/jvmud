package io.github.protasm.jvmud.language.transpiler;

import static org.junit.jupiter.api.Assertions.*;
import io.github.protasm.jvmud.language.exec.*;
import io.github.protasm.jvmud.language.pipeline.CompilationStage;
import io.github.protasm.jvmud.execution.model.mudlib.*;
import io.github.protasm.jvmud.execution.instance.MudInstance;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checked varargs adaptation keeps ordinary calls strict and preserves argument side effects. */
class MethodVarargsTranspilerTest {
    @TempDir Path root;

    private LPCRuntime runtime() {
        var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
        rt.registerMudlibBoundary(MudlibBoundary.builder().mudlibRootPath(root)
                .methodVarargsOverride(new MethodVarargsOverride("base.c", "run", 0)).build());
        return rt;
    }

    @Test void inheritedAndDirectCallsEvaluateSurplusArgumentsOnceInOrder() throws Exception {
        String original = "int run() { return 7; } int plain() { return 1; }";
        Files.writeString(root.resolve("base.c"), original);
        Files.writeString(root.resolve("child.c"), """
                inherit "base";
                int count;
                int next(int n) { count = count * 10 + n; return n; }
                int answer() { run(next(1)); ::run(next(2), next(3)); return count; }
                """);
        assertEquals(123, runtime().load("child").invoke("answer"));
        assertEquals(original, Files.readString(root.resolve("base.c")));
        Files.writeString(root.resolve("bad.c"), "inherit \"base\"; int answer() { return plain(1); }");
        assertFalse(runtime().compile(root.resolve("bad.c")).succeeded());
        var strict = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
        assertFalse(strict.compile(root.resolve("child.c")).succeeded());
    }

    @Test void rejectsMissingAmbiguousAlreadyVarargsAndChangedSignatures() throws Exception {
        for (String source : new String[] {"void other() {}", "void run(int n) {}",
                "varargs void run() {}", "void run() {} void run(int n) {}", "void run();"}) {
            Files.writeString(root.resolve("base.c"), source);
            var result = runtime().compile(root.resolve("base.c"));
            assertFalse(result.succeeded(), source);
            assertEquals(CompilationStage.TRANSPILE, result.getProblems().get(0).getStage(), result.getProblems().toString());
        }
    }

    private String json() {
        return """
                {"method_varargs_overrides":[{"file":"base.c","method":"run","expected_parameter_count":0}]}
                """;
    }

    @Test void manifestRulesSurviveBootMerge() throws Exception {
        Files.writeString(root.resolve("base.c"), "int run() { return 7; } int answer() { return run(99); }");
        Files.writeString(root.resolve("rules.json"), json());
        Files.writeString(root.resolve("bridge.c"), "string player_prompt() { return \"> \"; }");
        Files.writeString(root.resolve("test.config"), "mudlib_object = bridge\ninitial_place = base\ntranspilation.overrides = rules.json\ncommand_actions.newest_first = true\ncommand_actions.arguments_only = true\n");
        var mud = MudInstance.boot(root, "test.config");
        assertEquals(1, mud.bootResult().mudlibBoundary().methodVarargsOverrides().size());
        assertTrue(mud.bootResult().mudlibBoundary().commandActionsNewestFirst());
        assertTrue(mud.bootResult().mudlibBoundary().commandActionsArgumentsOnly());
        assertEquals(7, mud.<Object>administer(rt -> rt.invokeObject(rt.loadOrGetObject("base"), "answer")));
    }

    @Test void rejectsMalformedDuplicateAndEscapingRules() throws Exception {
        Files.writeString(root.resolve("base.c"), "void run() {}");
        Path config = root.resolve("rules.json");
        String entry = json().substring(json().indexOf('[') + 1, json().lastIndexOf(']'));
        for (String body : new String[] {json().replace("count", "cout"), json().replace(":0", ":-1"),
                json().replace(":0", ":0.5"), json().replace(":0", ":2147483648"),
                json().replace("base.c", "../base.c"), json().replace("base.c", "absent.c"),
                json().replace(entry, entry + "," + entry)}) {
            Files.writeString(config, body);
            assertThrows(IllegalArgumentException.class, () -> TranspilationConfigReader.readConfig(config, root), body);
        }
    }
}
