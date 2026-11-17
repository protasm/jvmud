package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.instance.MudInstance;
import io.github.protasm.jvmud.language.compiler.transpiler.LocalTypeOverride;
import io.github.protasm.jvmud.language.execution.LPCExecutionOptions;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Exercises local translations through hosted loading, inheritance and mudlib boot. */
class LocalTypeTranspilationIntegrationTest {
    @TempDir Path root;

    private MudlibRuntime runtime() {
        var rt = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(root).build());
        rt.configureMudlib(MudlibConfiguration.builder().mudlibRootPath(root)
                .localTypeOverride(new LocalTypeOverride("base.c", "run", "list", "object", "object*")).build());
        return rt;
    }

    @Test void changesOnlySelectedLocalAndPreservesGroupedInitializersAndInheritance() throws Exception {
        String source = """
                object list;
                int count;
                mixed next() { count += 1; return 0; }
                mixed run() {
                    object before = next(), list = ({0, 0}), after = next();
                    return ({list[1], count, before, after});
                }
                object other() { object list; return list; }
                """;
        Files.writeString(root.resolve("base.c"), source);
        Files.writeString(root.resolve("child.c"), "inherit \"base\"; mixed answer() { return run(); }");
        var rt = runtime();
        assertEquals(java.util.Arrays.asList(0, 2, null, null), rt.load("child").invoke("answer"));
        assertEquals(source, Files.readString(root.resolve("base.c")));
        Files.writeString(root.resolve("other.c"), "mixed run() { object list = ({0}); return list[0]; }");
        assertFalse(rt.compile(root.resolve("other.c")).succeeded());
    }

    @Test void headersAndNestedBlocksUseTheEnclosingMethod() throws Exception {
        Files.writeString(root.resolve("local.h"), "object list = ({0});\n");
        Files.writeString(root.resolve("base.c"), "mixed run() { {\n#include \"local.h\"\nreturn list[0]; } }\n");
        assertEquals(0, runtime().load("base").invoke("run"));
    }

    private String json() {
        return """
                {"local_type_overrides":[{"file":"base.c","method":"run","local":"list",
                "expected_type":"object","replacement_type":"object*"}]}
                """;
    }

    @Test void localOnlyJsonSurvivesBootAndRequiresManifestSelection() throws Exception {
        Files.writeString(root.resolve("base.c"), "mixed run() { object list = ({0}); return list[0]; }");
        Files.writeString(root.resolve("rules.json"), json());
        Files.writeString(root.resolve("bridge.c"), "string player_prompt() { return \"> \"; }");
        Path config = root.resolve("test.config");
        String body = "mudlib_object = bridge\ninitial_place = base\n";
        Files.writeString(config, body);
        assertThrows(RuntimeException.class, () -> MudInstance.boot(root, "test.config"));
        Files.writeString(config, body + "transpilation.overrides = rules.json\n");
        var mud = MudInstance.boot(root, "test.config");
        assertEquals(1, mud.bootResult().configuration().localTypeOverrides().size());
        assertFalse(mud.bootResult().configuration().transpileUntypedMethods());
        assertEquals(0, mud.<Object>administer(rt -> rt.invokeObject(rt.loadOrGetObject("base"), "run")));
    }

}
