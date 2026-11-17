package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.instance.MudInstance;
import io.github.protasm.jvmud.language.compiler.transpiler.MethodVarargsOverride;
import io.github.protasm.jvmud.language.execution.LPCExecutionOptions;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Exercises translated varargs calls, argument evaluation and mudlib boot. */
class MethodVarargsTranspilationIntegrationTest {
    @TempDir Path root;

    private MudlibRuntime runtime() {
        var rt = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(root).build());
        rt.configureMudlib(MudlibConfiguration.builder().mudlibRootPath(root)
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
        var strict = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(root).build());
        assertFalse(strict.compile(root.resolve("child.c")).succeeded());
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
        assertEquals(1, mud.bootResult().configuration().methodVarargsOverrides().size());
        assertTrue(mud.bootResult().configuration().commandActionsNewestFirst());
        assertTrue(mud.bootResult().configuration().commandActionsArgumentsOnly());
        assertEquals(7, mud.<Object>administer(rt -> rt.invokeObject(rt.loadOrGetObject("base"), "answer")));
    }

}
