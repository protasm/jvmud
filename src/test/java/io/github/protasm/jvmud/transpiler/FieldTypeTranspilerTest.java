package io.github.protasm.jvmud.transpiler;

import static org.junit.jupiter.api.Assertions.*;

import io.github.protasm.jvmud.compiler.exec.*;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.compiler.scanner.Scanner;
import io.github.protasm.jvmud.compiler.token.*;
import io.github.protasm.jvmud.engine.mudlib.*;
import io.github.protasm.jvmud.instance.MudInstance;
import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FieldTypeTranspilerTest {
    @TempDir Path root;

    private FieldTypeOverride rule(String name, String expected, String replacement) {
        return new FieldTypeOverride("probe.c", name, expected, replacement);
    }

    private LPCRuntime runtime(FieldTypeOverride... rules) {
        var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(root).build());
        var builder = MudlibBoundary.builder().mudlibRootPath(root);
        for (var rule : rules) builder.fieldTypeOverride(rule);
        rt.registerMudlibBoundary(builder.build());
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

    @Test void mismatchMissingAndDuplicateDeclarationsFailAtTranspilationStage() throws Exception {
        Path file = root.resolve("probe.c");
        var rt = runtime(rule("fuel", "string", "int"));
        for (String source : List.of("int fuel;", "string different;", "string fuel; string fuel;",
                "void f() { string fuel; }", "string fuel() { return \"text\"; }")) {
            Files.writeString(file, source);
            var result = rt.compile(file);
            assertFalse(result.succeeded(), source);
            assertEquals(CompilationStage.TRANSPILE, result.getProblems().get(0).getStage(), source);
            assertTrue(result.getProblems().get(0).getMessage().contains("probe.c:fuel"));
            assertNull(result.getBytecode());
        }
    }

    @Test void preservesSourceLocationsAndIgnoresMatchingNamesInOtherFiles() {
        TokenList input = new Scanner().scan("\nstring fuel;\nvoid f() { string fuel; }\n");
        var transformer = new FieldTypeTranspiler();
        var rules = List.of(rule("fuel", "string", "int"));
        var out = transformer.transpile(root.resolve("probe.c"), root, input, rules);
        assertEquals("int", out.get(0).lexeme());
        assertEquals(input.get(0).span(), out.get(0).span());
        assertEquals(2, out.get(0).line());
        assertSame(input, transformer.transpile(root.resolve("other.c"), root, input, rules));
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
        assertFalse(mud.bootResult().mudlibBoundary().transpileUntypedMethods());
        assertEquals(1, mud.bootResult().mudlibBoundary().fieldTypeOverrides().size());
        assertEquals(8, mud.<Object>administer(rt -> rt.invokeObject(rt.loadOrGetObject("probe"), "answer")));
        assertThrows(LPCRuntimeException.class, () -> mud.administer(rt -> rt.loadSource("untyped.c", "f() { return 1; }")));
    }

    @Test void rejectsMalformedUnknownDuplicateAndMissingSourceRules() throws Exception {
        Files.writeString(root.resolve("probe.c"), "string fuel;");
        Path json = root.resolve("rules.json");
        for (String value : List.of("{}", "[]", "{\"field_type_overrides\":{}}",
                json("probe.c", "fuel", "string", "void"), json("../probe.c", "fuel", "string", "int"),
                json("absent.c", "fuel", "string", "int"),
                json("probe.c", "fuel", "string", "int").replace("replacement_type", "replacement_typo"),
                json("probe.c", "fuel", "string", "int") + " {}",
                json("probe.c", "fuel", "string", "int").replace("\"file\":", "\"file\":\"probe.c\",\"file\":"))) {
            Files.writeString(json, value);
            Exception error = assertThrows(Exception.class, () -> TranspilationConfigReader.read(json, root), value);
            assertTrue(error instanceof IllegalArgumentException || error instanceof java.io.IOException, error.toString());
        }
        String value = json("probe.c", "fuel", "string", "int");
        String entry = value.substring(value.indexOf('[') + 1, value.lastIndexOf(']'));
        Files.writeString(json, "{\"field_type_overrides\":[" + entry + "," + entry + "]}");
        assertThrows(IllegalArgumentException.class, () -> TranspilationConfigReader.read(json, root));
    }

    @Test void overrideFileRequiresExplicitManifestSelectionAndWorksForGlobalHelpers() throws Exception {
        Files.createDirectories(root.resolve("jvmud"));
        Files.writeString(root.resolve("probe.c"), "string fuel; int helper() { fuel = 6; return fuel; }");
        Files.writeString(root.resolve("jvmud/transpilation.json"), json("probe.c", "fuel", "string", "int"));
        Path config = root.resolve("jvmud/test.config");
        Files.writeString(config, "mudlib_root = ..\nmfun_object = probe\ntranspilation.untyped_methods = true\n");
        var boundary = MudlibBoundaryConfigReader.read(root, "jvmud/test.config");
        assertTrue(boundary.fieldTypeOverrides().isEmpty());
        var rt = runtime();
        rt.registerMudlibBoundary(boundary);
        assertFalse(rt.compile(root.resolve("probe.c")).succeeded());
        Files.writeString(config, Files.readString(config) + "transpilation.overrides = transpilation.json\n");
        var enabled = runtime();
        enabled.registerMudlibBoundary(MudlibBoundaryConfigReader.read(root, "jvmud/test.config"));
        assertEquals(6, enabled.loadSource("caller.c", "int answer() { return helper(); }").invoke("answer"));
    }

    @Test void originalTorchCompilesAndUsesNumericFuelWithoutSourceChanges() throws Exception {
        Path mudlib = Path.of("mudlibs/lp245").toAbsolutePath();
        Path source = mudlib.resolve("obj/torch.c");
        byte[] before = Files.readAllBytes(source);
        var boundary = MudlibBoundaryConfigReader.read(mudlib, "jvmud/lp245.config");
        var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(mudlib).build());
        io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns.registerCore(rt, boundary.engineCapabilities());
        rt.registerMudlibBoundary(boundary);
        rt.setParserOptions(io.github.protasm.jvmud.compiler.parser.ParserOptions.features(boundary.languageFeatures()));
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
