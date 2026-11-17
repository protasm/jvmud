package io.github.protasm.jvmud.language.compiler.transpiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.protasm.jvmud.language.compiler.LPCCompilationOptions;
import io.github.protasm.jvmud.language.compiler.LPCCompilerContext;
import io.github.protasm.jvmud.language.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.language.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.language.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.language.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.language.compiler.preprocessor.SearchPathIncludeResolver;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checks local override selection and JSON validation without a hosted runtime. */
class LocalTypeTranspilerTest {
    @TempDir Path root;

    /** Compiles the selected source profile without constructing a hosted mudlib. */
    private CompilationResult compile(Path file) throws Exception {
        var environment = new LPCCompilerContext(new SearchPathIncludeResolver(root, List.of()));
        environment.setOptions(new LPCCompilationOptions(root, false, false, false,
                List.of(), List.of(new LocalTypeOverride("base.c", "run", "list", "object", "object*")),
                List.of(), Map.of(), Map.of()));
        String sourceName = file.getFileName().toString().replaceFirst("\\.c$", "");
        return new CompilationPipeline("java/lang/Object", environment).run(
                file, Files.readString(file), sourceName,
                "/" + file.getFileName(), ParserOptions.defaults());
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
            var result = compile(root.resolve("base.c"));
            assertFalse(result.succeeded(), source);
            assertEquals(CompilationStage.TRANSPILE, result.getProblems().get(0).getStage(), result.getProblems().toString());
            assertNull(result.getBytecode());
        }
    }

    private String json() {
        return """
                {"local_type_overrides":[{"file":"base.c","method":"run","local":"list",
                "expected_type":"object","replacement_type":"object*"}]}
                """;
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
