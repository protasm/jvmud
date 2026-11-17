package io.github.protasm.jvmud.language.compiler.transpiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

/** Checks method signature override validation without constructing game state. */
class MethodVarargsTranspilerTest {
    @TempDir Path root;

    /** Compiles the selected source profile without constructing a hosted mudlib. */
    private CompilationResult compile(Path file) throws Exception {
        var environment = new LPCCompilerContext(new SearchPathIncludeResolver(root, List.of()));
        environment.setOptions(new LPCCompilationOptions(root, false, false, false,
                List.of(), List.of(),
                List.of(new MethodVarargsOverride("base.c", "run", 0)), Map.of(), Map.of()));
        String sourceName = file.getFileName().toString().replaceFirst("\\.c$", "");
        return new CompilationPipeline("java/lang/Object", environment).run(
                file, Files.readString(file), sourceName,
                "/" + file.getFileName(), ParserOptions.defaults());
    }

    @Test void rejectsMissingAmbiguousAlreadyVarargsAndChangedSignatures() throws Exception {
        for (String source : new String[] {"void other() {}", "void run(int n) {}",
                "varargs void run() {}", "void run() {} void run(int n) {}", "void run();"}) {
            Files.writeString(root.resolve("base.c"), source);
            var result = compile(root.resolve("base.c"));
            assertFalse(result.succeeded(), source);
            assertEquals(CompilationStage.TRANSPILE, result.getProblems().get(0).getStage(), result.getProblems().toString());
        }
    }

    private String json() {
        return """
                {"method_varargs_overrides":[{"file":"base.c","method":"run","expected_parameter_count":0}]}
                """;
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
