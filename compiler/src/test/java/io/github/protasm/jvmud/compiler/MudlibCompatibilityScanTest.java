package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.compiler.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibBoundaryConfigReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

final class MudlibCompatibilityScanTest {
    private static final Path REPO_ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final Path MUDLIB_ROOT = REPO_ROOT.resolve("mudlib");
    private static final String CONFIG_PATH = "jvmud/config";
    private static final String PLAYER_SOURCE = "obj/player.c";
    private static final List<String> COMPATIBILITY_SET =
            List.of(
                    "obj/beer.c",
                    "obj/money.c",
                    PLAYER_SOURCE,
                    "obj/torch.c",
                    "room/mountain/hump.c",
                    "room/test.c",
                    "room/village/vill_green.c",
                    "room/village/vill_road1.c",
                    "room/village/vill_road2.c",
                    "room/village/vill_track.c",
                    "room/forest/forest1.c",
                    "room/forest/wild1.c");

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
        report.append("| Source | Status | First Problem Stage | First Problem Line | First Problem |\n");
        report.append("| --- | --- | --- | --- | --- |\n");

        for (Map.Entry<String, CompilationResult> entry : results.entrySet()) {
            List<CompilationProblem> problems = entry.getValue().getProblems();
            if (problems.isEmpty()) {
                report.append("| ").append(entry.getKey()).append(" | supported |  |  |  |\n");
                continue;
            }

            CompilationProblem first = problems.get(0);
            report.append("| ")
                    .append(entry.getKey())
                    .append(" | unsupported | ")
                    .append(first.getStage())
                    .append(" | ")
                    .append(first.getLine() == null ? "" : first.getLine())
                    .append(" | ")
                    .append(escape(first.getMessage()))
                    .append(" |\n");
        }

        appendPlayerTracker(report, results.get(PLAYER_SOURCE));

        Files.writeString(reportPath, report.toString());
    }

    private static void appendPlayerTracker(StringBuilder report, CompilationResult playerResult) throws IOException {
        report.append("\n## obj/player.c Compatibility Tracker\n\n");
        report.append("This section tracks the staged blockers for the vanilla player object. ");
        report.append("Later stages remain pending until earlier gates produce enough structure to inspect them.\n\n");

        if (playerResult == null) {
            report.append("`obj/player.c` is not part of the compatibility scan.\n");
            return;
        }

        List<CompilationProblem> problems = playerResult.getProblems();
        if (problems.isEmpty()) {
            report.append("`obj/player.c` currently compiles through bytecode generation.\n");
            return;
        }

        CompilationProblem first = problems.get(0);
        report.append("Current first blocker: `")
                .append(first.getStage())
                .append("`");
        if (first.getLine() != null) {
            report.append(" line ").append(first.getLine());
        }
        report.append(" - ")
                .append(escape(first.getMessage()))
                .append("\n\n");

        Optional<SourceExcerpt> sourceExcerpt = sourceExcerpt(PLAYER_SOURCE, first);
        if (sourceExcerpt.isPresent()) {
            SourceExcerpt excerpt = sourceExcerpt.get();
            if (!excerpt.line().isBlank()) {
                report.append("Current source location: `")
                        .append(excerpt.sourceName())
                        .append(":")
                        .append(excerpt.lineNumber())
                        .append("`\n\n");
                report.append("```lpc\n");
                report.append(excerpt.line()).append("\n");
                report.append("```\n\n");
            }
        }

        report.append("| Gate | Status | Blocker |\n");
        report.append("| --- | --- | --- |\n");
        appendGate(report, "Parser", first, CompilationStage.PARSE);
        appendGate(report, "Semantic analysis", first, CompilationStage.ANALYZE);
        appendGate(report, "Efun/runtime surface", first, CompilationStage.LOWER, CompilationStage.COMPILE);
    }

    private static void appendGate(
            StringBuilder report,
            String gate,
            CompilationProblem first,
            CompilationStage... stages) {
        for (CompilationStage stage : stages) {
            if (first.getStage() == stage) {
                report.append("| ")
                        .append(gate)
                        .append(" | blocked | ")
                        .append(problemSummary(first))
                        .append(" |\n");
                return;
            }
        }

        if (comesBefore(first.getStage(), stages[0])) {
            report.append("| ")
                    .append(gate)
                    .append(" | pending | Waiting for `")
                    .append(first.getStage())
                    .append("` blocker to clear. |\n");
            return;
        }

        report.append("| ")
                .append(gate)
                .append(" | clear so far | No blocker reached before `")
                .append(first.getStage())
                .append("`. |\n");
    }

    private static boolean comesBefore(CompilationStage current, CompilationStage target) {
        return current.ordinal() < target.ordinal();
    }

    private static String problemSummary(CompilationProblem problem) {
        StringBuilder summary = new StringBuilder();
        if (problem.getLine() != null) {
            summary.append("line ").append(problem.getLine()).append(": ");
        }
        summary.append(escape(problem.getMessage()));
        return summary.toString();
    }

    private static Optional<SourceExcerpt> sourceExcerpt(String sourceName, CompilationProblem problem)
            throws IOException {
        String methodName = missingMethodReturnTypeName(problem);
        if (methodName != null) {
            Optional<SourceExcerpt> declaration = findUntypedMethodDeclaration(methodName);
            if (declaration.isPresent())
                return declaration;
        }

        if (problem.getLine() == null)
            return Optional.empty();

        String line = sourceLine(sourceName, problem.getLine());
        if (line.isBlank())
            return Optional.empty();

        return Optional.of(new SourceExcerpt(sourceName, problem.getLine(), line));
    }

    private static String missingMethodReturnTypeName(CompilationProblem problem) {
        Matcher matcher = Pattern.compile("Method '([^']+)' must declare a return type").matcher(problem.getMessage());
        return matcher.matches() ? matcher.group(1) : null;
    }

    private static Optional<SourceExcerpt> findUntypedMethodDeclaration(String methodName) throws IOException {
        Pattern declarationPattern =
                Pattern.compile("^\\s*(?:static\\s+)?" + Pattern.quote(methodName) + "\\s*\\(");

        try (var files = Files.walk(MUDLIB_ROOT)) {
            List<Path> candidates = files.filter(Files::isRegularFile)
                    .filter(MudlibCompatibilityScanTest::isMudlibSource)
                    .sorted(Comparator.comparing(MudlibCompatibilityScanTest::sourceName))
                    .toList();

            for (Path candidate : candidates) {
                List<String> lines = Files.readAllLines(candidate);
                for (int i = 0; i < lines.size(); i++) {
                    if (declarationPattern.matcher(lines.get(i)).find())
                        return Optional.of(new SourceExcerpt(sourceName(candidate), i + 1, lines.get(i)));
                }
            }
        }

        return Optional.empty();
    }

    private static boolean isMudlibSource(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".c") || name.endsWith(".h");
    }

    private static String sourceName(Path path) {
        return MUDLIB_ROOT.relativize(path).toString();
    }

    private static String sourceLine(String sourceName, int lineNumber) throws IOException {
        List<String> lines = Files.readAllLines(MUDLIB_ROOT.resolve(sourceName));
        if (lineNumber < 1 || lineNumber > lines.size()) {
            return "";
        }
        return lines.get(lineNumber - 1);
    }

    private static String stripExtension(String sourceName) {
        int dot = sourceName.lastIndexOf('.');
        return dot == -1 ? sourceName : sourceName.substring(0, dot);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("|", "\\|").replace("\n", " ");
    }

    private record SourceExcerpt(String sourceName, int lineNumber, String line) {}
}
