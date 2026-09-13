package io.github.protasm.jvmud.transpiler;

import static org.junit.jupiter.api.Assertions.*;
import io.github.protasm.jvmud.compiler.exec.*;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.engine.mudlib.*;
import io.github.protasm.jvmud.instance.MudInstance;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checked local translations preserve declaration identity, scope and normal compiler checks. */
class LocalTypeTranspilerTest {
    @TempDir Path root;

    private LPCRuntime runtime() {
        var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
        rt.registerMudlibBoundary(MudlibBoundary.builder().mudlibRootPath(root)
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

    @Test void missingWrongTypeParametersAndAmbiguousLocalsFailBeforeAnalysis() throws Exception {
        for (String source : new String[] {
                "void other() { object list; }",
                "void run() { string list; }",
                "void run(object list) {}",
                "object list; void run() {}",
                "void run() { object list; { object list; } }",
                "void run() { object list; } void run(int n) { object list; }"}) {
            Files.writeString(root.resolve("base.c"), source);
            var result = runtime().compile(root.resolve("base.c"));
            assertFalse(result.succeeded(), source);
            assertEquals(CompilationStage.TRANSPILE, result.getProblems().get(0).getStage(), result.getProblems().toString());
            assertNull(result.getBytecode());
        }
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
        assertEquals(1, mud.bootResult().mudlibBoundary().localTypeOverrides().size());
        assertFalse(mud.bootResult().mudlibBoundary().transpileUntypedMethods());
        assertEquals(0, mud.<Object>administer(rt -> rt.invokeObject(rt.loadOrGetObject("base"), "run")));
    }

    @Test void rejectsMalformedAndDuplicateLocalRules() throws Exception {
        Files.writeString(root.resolve("base.c"), "void run() { object list; }");
        Path config = root.resolve("rules.json");
        String entry = json().substring(json().indexOf('[') + 1, json().lastIndexOf(']'));
        for (String value : new String[] {
                json().replace("\"method\"", "\"methdo\""),
                json().replace("\"run\"", "\"bad name\""),
                json().replace("object*", "void"),
                json().replace("base.c", "../base.c"),
                json().replace("base.c", "absent.c"),
                "{\"local_type_overrides\":null}",
                "{\"local_type_overrides\":[" + entry + "," + entry + "]}"}) {
            Files.writeString(config, value);
            assertThrows(IllegalArgumentException.class, () -> TranspilationConfigReader.readConfig(config, root), value);
        }
    }
}
