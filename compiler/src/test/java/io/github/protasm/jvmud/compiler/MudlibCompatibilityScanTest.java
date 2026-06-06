package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.compiler.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibBoundaryConfigReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class MudlibCompatibilityScanTest {
    private static final Path REPO_ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final Path MUDLIB_ROOT = REPO_ROOT.resolve("mudlib");
    private static final String CONFIG_PATH = "jvmud/config";
    private static final List<String> COMPATIBILITY_SET =
            List.of(
                    "obj/beer.c",
                    "obj/money.c",
                    "obj/torch.c",
                    "room/hump.c",
                    "room/test.c",
                    "room/vill_green.c",
                    "room/vill_road1.c",
                    "room/vill_track.c",
                    "room/wild1.c");

    @Test
    void selectedMudlibFilesProduceCompatibilityReport() throws IOException {
        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(MUDLIB_ROOT, List.of()));
        EngineEfuns.registerCore(context);
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        context.setMfunObjectPath(boundary.mfunObjectPath().orElse(null));
        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object", context);
        Map<String, CompilationResult> results = new LinkedHashMap<>();

        // This is a radar test, not a gate. The report should make missing LPC/engine
        // features visible while the green build remains anchored to supported behavior.
        for (String sourceName : COMPATIBILITY_SET) {
            Path sourcePath = MUDLIB_ROOT.resolve(sourceName);
            String source = Files.readString(sourcePath);
            CompilationResult result =
                    pipeline.run(sourcePath, source, stripExtension(sourceName), "/" + sourceName, ParserOptions.defaults());
            results.put(sourceName, result);
        }

        writeReport(boundary, results);

        assertTrue(Files.exists(Path.of("target", "jvmud-mudlib-compatibility.md")));
    }

    private static void writeReport(MudlibBoundary boundary, Map<String, CompilationResult> results) throws IOException {
        Path reportPath = Path.of("target", "jvmud-mudlib-compatibility.md");
        Files.createDirectories(reportPath.getParent());

        StringBuilder report = new StringBuilder();
        report.append("# JVMud Mudlib Compatibility Scan\n\n");
        report.append("This report is informational. It captures the current compiler/runtime gaps without failing the build.\n\n");
        report.append("Configured mfun object: `")
                .append(boundary.mfunObjectPath().orElse(""))
                .append("`\n\n");
        report.append("| Source | Status | First Problem Stage | First Problem |\n");
        report.append("| --- | --- | --- | --- |\n");

        for (Map.Entry<String, CompilationResult> entry : results.entrySet()) {
            List<CompilationProblem> problems = entry.getValue().getProblems();
            if (problems.isEmpty()) {
                report.append("| ").append(entry.getKey()).append(" | supported |  |  |\n");
                continue;
            }

            CompilationProblem first = problems.get(0);
            report.append("| ")
                    .append(entry.getKey())
                    .append(" | unsupported | ")
                    .append(first.getStage())
                    .append(" | ")
                    .append(escape(first.getMessage()))
                    .append(" |\n");
        }

        Files.writeString(reportPath, report.toString());
    }

    private static String stripExtension(String sourceName) {
        int dot = sourceName.lastIndexOf('.');
        return dot == -1 ? sourceName : sourceName.substring(0, dot);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("|", "\\|").replace("\n", " ");
    }
}
