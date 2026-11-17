package io.github.protasm.jvmud.language.compiler.transpiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.language.compiler.LPCCompilationOptions;
import io.github.protasm.jvmud.language.compiler.LPCCompilerContext;
import io.github.protasm.jvmud.language.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.language.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.language.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.language.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.language.compiler.preprocessor.SearchPathIncludeResolver;
import io.github.protasm.jvmud.language.compiler.scanner.Scanner;
import io.github.protasm.jvmud.language.compiler.token.TokenList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checks token provenance and field override validation before semantic analysis. */
class FieldTypeTranspilerTest {
    @TempDir Path root;

    /** Compiles the selected source profile without constructing a hosted mudlib. */
    private CompilationResult compile(Path file) throws Exception {
        var environment = new LPCCompilerContext(new SearchPathIncludeResolver(root, List.of()));
        environment.setOptions(new LPCCompilationOptions(root, false, false, false,
                List.of(rule("fuel", "string", "int")), List.of(),
                List.of(), Map.of(), Map.of()));
        String sourceName = file.getFileName().toString().replaceFirst("\\.c$", "");
        return new CompilationPipeline("java/lang/Object", environment).run(
                file, Files.readString(file), sourceName,
                "/" + file.getFileName(), ParserOptions.defaults());
    }

    private FieldTypeOverride rule(String name, String expected, String replacement) {
        return new FieldTypeOverride("probe.c", name, expected, replacement);
    }

    @Test void mismatchMissingAndDuplicateDeclarationsFailAtTranspilationStage() throws Exception {
        Path file = root.resolve("probe.c");
        for (String source : List.of("int fuel;", "string different;", "string fuel; string fuel;",
                "void f() { string fuel; }", "string fuel() { return \"text\"; }")) {
            Files.writeString(file, source);
            var result = compile(file);
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

    private String json(String file, String field, String expected, String replacement) {
        return "{\"field_type_overrides\":[{\"file\":\"" + file + "\",\"field\":\"" + field
                + "\",\"expected_type\":\"" + expected + "\",\"replacement_type\":\"" + replacement + "\"}]}";
    }
}
