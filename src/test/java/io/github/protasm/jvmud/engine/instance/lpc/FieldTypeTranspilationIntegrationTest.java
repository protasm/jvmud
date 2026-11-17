package io.github.protasm.jvmud.engine.instance.lpc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.engine.configuration.MudlibConfiguration;
import io.github.protasm.jvmud.engine.configuration.MudlibConfigurationReader;
import io.github.protasm.jvmud.engine.instance.MudInstance;
import io.github.protasm.jvmud.engine.instance.efun.EngineEfuns;
import io.github.protasm.jvmud.language.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.language.compiler.transpiler.FieldTypeOverride;
import io.github.protasm.jvmud.language.execution.LPCExecutionOptions;
import io.github.protasm.jvmud.language.execution.LPCRuntimeException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Exercises field translations through LPC loading, inheritance, manifests and archived source. */
class FieldTypeTranspilationIntegrationTest {
    @TempDir Path root;

    private FieldTypeOverride rule(String name, String expected, String replacement) {
        return new FieldTypeOverride("probe.c", name, expected, replacement);
    }

    private MudlibRuntime runtime(FieldTypeOverride... rules) {
        var rt = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(root).build());
        var builder = MudlibConfiguration.builder().mudlibRootPath(root);
        for (var rule : rules) builder.fieldTypeOverride(rule);
        rt.configureMudlib(builder.build());
        return rt;
    }

    @Test void changesOnlySelectedFieldInGroupedDeclarationAndPreservesInitializerOrder() throws Exception {
        Path file = root.resolve("probe.c");
        String source = """
                private static string untouched = "before", fuel = 7, after = "after";
                int value() { return fuel * 2; }
                string label() { return untouched + after; }
                string local() { string fuel = "local"; return fuel; }
                """;
        Files.writeString(file, source);
        assertFalse(runtime().compile(file).succeeded());
        var object = runtime(rule("fuel", "string", "int")).load("probe");
        assertEquals(14, object.invoke("value"));
        assertEquals("beforeafter", object.invoke("label"));
        assertEquals("local", object.invoke("local"));
        assertEquals(source, Files.readString(file));
    }

    @Test void supportsArrayTypesAndNestedInitializers() throws Exception {
        Files.writeString(root.resolve("probe.c"), """
                object values = ({1, 2}), other;
                mixed first() { return values[0]; }
                """);
        var object = runtime(rule("values", "object", "mixed *")).load("probe");
        assertEquals(1, object.invoke("first"));
    }

    @Test void appliesToExpandedHeadersAndInheritedObjects() throws Exception {
        Files.writeString(root.resolve("fields.h"), "string fuel;\n");
        Files.writeString(root.resolve("probe.c"), "#include \"fields.h\"\nvoid set() { fuel = 7; } int get() { return fuel; }");
        Files.writeString(root.resolve("child.c"), "inherit \"probe\"; int answer() { set(); return get(); }");
        var object = runtime(rule("fuel", "string", "int")).load("child");
        assertEquals(7, object.invoke("answer"));
    }

    @Test void jsonRulesSurviveHostedBootWithoutEnablingUntypedMethods() throws Exception {
        Files.createDirectories(root.resolve("jvmud"));
        Files.writeString(root.resolve("jvmud/bridge.c"), "string player_prompt() { return \"> \"; }");
        Files.writeString(root.resolve("probe.c"), "string fuel; int answer() { fuel = 8; return fuel; }");
        Files.writeString(root.resolve("jvmud/transpilation.json"), json("probe.c", "fuel", "string", "int"));
        Files.writeString(root.resolve("jvmud/test.config"), """
                mudlib_root = ..
                mudlib_object = jvmud/bridge
                initial_place = probe
                transpilation.overrides = transpilation.json
                """);
        var mud = MudInstance.boot(root, "jvmud/test.config");
        assertFalse(mud.bootResult().configuration().transpileUntypedMethods());
        assertEquals(1, mud.bootResult().configuration().fieldTypeOverrides().size());
        assertEquals(8, mud.<Object>administer(rt -> rt.invokeObject(rt.loadOrGetObject("probe"), "answer")));
        assertThrows(LPCRuntimeException.class, () -> mud.administer(rt -> rt.loadSource("untyped.c", "f() { return 1; }")));
    }

    @Test void overrideFileRequiresExplicitManifestSelectionAndWorksForGlobalHelpers() throws Exception {
        Files.createDirectories(root.resolve("jvmud"));
        Files.writeString(root.resolve("probe.c"), "string fuel; int helper() { fuel = 6; return fuel; }");
        Files.writeString(root.resolve("jvmud/transpilation.json"), json("probe.c", "fuel", "string", "int"));
        Path config = root.resolve("jvmud/test.config");
        Files.writeString(config, "mudlib_root = ..\nmfun_object = probe\ntranspilation.untyped_methods = true\n");
        var boundary = MudlibConfigurationReader.read(root, "jvmud/test.config");
        assertTrue(boundary.fieldTypeOverrides().isEmpty());
        var rt = runtime();
        rt.configureMudlib(boundary);
        assertFalse(rt.compile(root.resolve("probe.c")).succeeded());
        Files.writeString(config, Files.readString(config) + "transpilation.overrides = transpilation.json\n");
        var enabled = runtime();
        enabled.configureMudlib(MudlibConfigurationReader.read(root, "jvmud/test.config"));
        assertEquals(6, enabled.loadSource("caller.c", "int answer() { return helper(); }").invoke("answer"));
    }

    @Test void originalTorchCompilesAndUsesNumericFuelWithoutSourceChanges() throws Exception {
        Path mudlib = Path.of("mudlibs/lp245").toAbsolutePath();
        Path source = mudlib.resolve("obj/torch.c");
        byte[] before = Files.readAllBytes(source);
        var boundary = MudlibConfigurationReader.read(mudlib, "jvmud/lp245.config");
        var rt = new MudlibRuntime(LPCExecutionOptions.builder().baseIncludePath(mudlib).build());
        EngineEfuns.registerCore(rt, boundary.engineCapabilities());
        rt.configureMudlib(boundary);
        rt.setParserOptions(ParserOptions.features(boundary.languageFeatures()));
        var torch = rt.load("obj/torch");
        torch.invoke("reset", 0);
        assertEquals(20, torch.invoke("query_value"));
        torch.invoke("set_fuel", 500);
        assertEquals(5, torch.invoke("query_value"));
        assertArrayEquals(before, Files.readAllBytes(source));
    }

    private String json(String file, String field, String expected, String replacement) {
        return "{\"field_type_overrides\":[{\"file\":\"" + file + "\",\"field\":\"" + field
                + "\",\"expected_type\":\"" + expected + "\",\"replacement_type\":\"" + replacement + "\"}]}";
    }
}
