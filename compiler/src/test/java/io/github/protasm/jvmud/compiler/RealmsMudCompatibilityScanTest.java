package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.compiler.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundaryConfigReader;
import io.github.protasm.jvmud.engine.mudlib.MudlibLifecycleEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class RealmsMudCompatibilityScanTest {
    private static final Path REPO_ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final Path MUDLIB_ROOT = REPO_ROOT.resolve("mudlibs").resolve("realmsmud");
    private static final Path MUDLIB_SOURCE_ROOT = MUDLIB_ROOT.resolve("source");
    private static final String CONFIG_PATH = "jvmud/realmsmud.config";
    private static final Path REPORT_PATH = Path.of("target", "jvmud-realmsmud-compatibility.md");
    private static final List<String> COMPATIBILITY_SET =
            List.of(
                    "secure/master.c",
                    "secure/simul_efun.c",
                    "secure/login.c",
                    "lib/core/events.c",
                    "lib/core/stateMachine.c",
                    "lib/core/specification.c",
                    "lib/environment/environment.c",
                    "lib/environment/region.c",
                    "lib/environment/walkableRegion.c");

    @Test
    void selectedRealmsMudFilesProduceCompatibilityRadarReport() throws IOException {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        Path sourceRoot = boundary.mudlibRootPath().orElse(MUDLIB_SOURCE_ROOT);
        assertTrue(
                Files.isSameFile(MUDLIB_SOURCE_ROOT, sourceRoot),
                "RealmsMUD mudlib_root should resolve to the upstream source tree.");
        assertConfiguredBoundary(boundary);

        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(sourceRoot, List.of()));
        CoreEfuns.registerCore(context);
        context.setMudlibBoundary(boundary);
        context.setMfunObjectPath(boundary.mfunObjectPath().orElse(null));
        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object", context);
        Map<String, CompilationResult> results = new LinkedHashMap<>();

        // This is deliberately a radar, not a readiness gate. It should keep exposing
        // the first RealmsMUD blockers while the rest of the build stays green.
        for (String sourceName : COMPATIBILITY_SET) {
            Path sourcePath = sourceRoot.resolve(sourceName);
            assertTrue(Files.isRegularFile(sourcePath), sourceName + " should exist in the RealmsMUD source tree.");
            String source = Files.readString(sourcePath);
            CompilationResult result =
                    pipeline.run(sourcePath, source, stripExtension(sourceName), "/" + sourceName, ParserOptions.defaults());
            results.put(sourceName, result);
        }

        writeReport(boundary, results);

        assertFalse(results.isEmpty(), "RealmsMUD compatibility radar should scan at least one file.");
        assertTrue(Files.exists(REPORT_PATH), "RealmsMUD compatibility report should be written.");
    }

    private static void assertConfiguredBoundary(MudlibBoundary boundary) {
        assertEquals("realmsmud", boundary.gameId().orElseThrow());
        assertEquals("RealmsMUD", boundary.gameName().orElseThrow());
        assertEquals("jvmud/mudlib", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("jvmud/mfuns", boundary.mfunObjectPath().orElseThrow());
        assertEquals("secure/login", boundary.playerObjectPath().orElseThrow());
        assertEquals(">", boundary.playerPrompt().orElseThrow());
        assertEquals(78, boundary.maxLineLength());
        assertFalse(boundary.showRuler());
        assertEquals("areas/eledhel/southern-city/12x2", boundary.initialPlacePath().orElseThrow());
        assertEquals("init/init_file", boundary.preloadFilePath().orElseThrow());
        assertTrue(boundary.preloadObjectPaths().contains("secure/master"));
        assertTrue(boundary.preloadObjectPaths().contains("secure/simul_efun"));
        assertEquals("create", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED).orElseThrow());
        assertEquals("reset", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_ACTIVATED).orElseThrow());
        assertEquals(
                "prepare_destruct",
                boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_DESTRUCTION_REQUESTED).orElseThrow());
        assertEquals("logon", boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_CONNECTED).orElseThrow());
        assertEquals("log_error", boundary.lifecycleMethod(MudlibLifecycleEvent.LOG_ERROR).orElseThrow());
        assertEquals("runtime_error", boundary.lifecycleMethod(MudlibLifecycleEvent.RUNTIME_ERROR).orElseThrow());
        assertEquals("heart_beat", boundary.temporalTickMethod().orElseThrow());
        assertEquals(1, boundary.temporalTickIntervalSeconds());
    }

    private static void writeReport(MudlibBoundary boundary, Map<String, CompilationResult> results)
            throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());

        long supported = results.values().stream().filter(result -> result.getProblems().isEmpty()).count();
        long unsupported = results.size() - supported;

        StringBuilder report = new StringBuilder();
        report.append("# JVMud RealmsMUD Compatibility Scan\n\n");
        report.append("This report is informational. It captures the current compiler/runtime gaps ");
        report.append("without making RealmsMUD readiness a build gate.\n\n");
        report.append("- Mudlib root: `").append(escape(MUDLIB_ROOT.toString())).append("`\n");
        report.append("- Configured source root: `")
                .append(escape(boundary.mudlibRootPath().map(Path::toString).orElse("")))
                .append("`\n");
        report.append("- Configured mfun object: `")
                .append(escape(boundary.mfunObjectPath().orElse("")))
                .append("`\n");
        report.append("- Scanned files: ").append(results.size()).append("\n");
        report.append("- Supported now: ").append(supported).append("\n");
        report.append("- Current blockers: ").append(unsupported).append("\n\n");
        report.append("## Selected Boot And Gameplay Spine\n\n");
        report.append("| Source | Status | First Problem Stage | First Problem Line | First Problem |\n");
        report.append("| --- | --- | --- | ---: | --- |\n");

        for (Map.Entry<String, CompilationResult> entry : results.entrySet()) {
            List<CompilationProblem> problems = entry.getValue().getProblems();
            if (problems.isEmpty()) {
                report.append("| `").append(entry.getKey()).append("` | supported |  |  |  |\n");
                continue;
            }

            CompilationProblem first = problems.get(0);
            report.append("| `")
                    .append(entry.getKey())
                    .append("` | unsupported | ")
                    .append(escape(String.valueOf(first.getStage())))
                    .append(" | ")
                    .append(first.getLine() == null ? "" : first.getLine())
                    .append(" | ")
                    .append(escape(problemSummary(first)))
                    .append(" |\n");
        }

        report.append("\n## Why These Files\n\n");
        report.append("- `secure/master.c` is RealmsMUD's LDMud master object and driver-hook entry point.\n");
        report.append("- `secure/simul_efun.c` exposes the mudlib's global compatibility surface.\n");
        report.append("- `secure/login.c` is the player connection and account flow.\n");
        report.append("- `/lib/core` and `/lib/environment` files are the smallest early slice toward ");
        report.append("events, state, descriptions, regions, and a playable place.\n");

        Files.writeString(REPORT_PATH, report.toString());
    }

    private static String problemSummary(CompilationProblem problem) {
        String message = problem.getMessage();
        Throwable throwable = problem.getThrowable();
        if (throwable != null && throwable.getMessage() != null && !throwable.getMessage().isBlank()) {
            message = message + " - " + throwable.getMessage();
        }
        return firstLine(message);
    }

    private static String firstLine(String value) {
        if (value == null) {
            return "";
        }
        return value.split("\\R", 2)[0];
    }

    private static String stripExtension(String sourceName) {
        int dot = sourceName.lastIndexOf('.');
        return dot == -1 ? sourceName : sourceName.substring(0, dot);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "\\|").replace("\n", " ").replace("\r", " ");
    }
}
