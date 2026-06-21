package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.compiler.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundaryConfigReader;
import io.github.protasm.jvmud.engine.mudlib.MudlibLifecycleEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

final class RealmsMudCompatibilityScanTest {
    private static final Path REPO_ROOT = Path.of("").toAbsolutePath().normalize();
    private static final Path MUDLIB_ROOT = REPO_ROOT.resolve("mudlibs").resolve("realmsmud");
    private static final Path MUDLIB_SOURCE_ROOT = MUDLIB_ROOT.resolve("source");
    private static final String CONFIG_PATH = "jvmud/realmsmud.config";
    private static final Path REPORT_PATH = Path.of("target", "jvmud-realmsmud-compatibility.md");
    private static final List<String> COMPATIBILITY_SWEEP_ROOTS =
            List.of(
                    "lib/environment",
                    "lib/services",
                    "lib/modules/secure",
                    "secure/master",
                    "secure/simulated-efuns");
    private static final List<String> BOOT_AND_CORE_COMPATIBILITY_SET =
            List.of(
                    "secure/master.c",
                    "secure/simul_efun.c",
                    "secure/login.c",
                    "jvmud/jvmud.c",
                    "lib/core/baseSelector.c",
                    "lib/core/events.c",
                    "lib/core/messageParser.c",
                    "lib/core/organizations.c",
                    "lib/core/prerequisites.c",
                    "lib/core/stateMachine.c",
                    "lib/core/specification.c",
                    "lib/core/stateObject.c",
                    "lib/core/thing.c");
    private static final List<KnownIssue> KNOWN_ISSUES =
            List.of(
                    new KnownIssue(
                            "lib/modules/secure/dataAccess.c",
                            CompilationStage.ANALYZE,
                            87,
                            "Argument 2 type mismatch (expected LPCSTRING but found LPCINT)",
                            "RealmsMUD passes playerId to saveCompositeResearch, whose declared signature expects "
                                    + "playerName. This is tracked as a probable Realms source mismatch rather than "
                                    + "a JVMud language gap."));

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
        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object", context);
        Map<String, CompilationResult> results = new LinkedHashMap<>();

        // This is deliberately a radar, not a readiness gate. It should keep exposing
        // the first RealmsMUD blockers while the rest of the build stays green.
        for (String sourceName : compatibilitySet(sourceRoot)) {
            Path sourcePath = compatibilitySourcePath(sourceRoot, sourceName);
            assertTrue(Files.isRegularFile(sourcePath), sourceName + " should exist in the RealmsMUD mudlib tree.");
            String source = Files.readString(sourcePath);
            CompilationResult result =
                    pipeline.run(sourcePath, source, stripExtension(sourceName), "/" + sourceName, ParserOptions.defaults());
            results.put(sourceName, result);
        }

        writeReport(boundary, results);

        assertFalse(results.isEmpty(), "RealmsMUD compatibility radar should scan at least one file.");
        assertTrue(Files.exists(REPORT_PATH), "RealmsMUD compatibility report should be written.");
    }

    @Test
    void synchronousRealmsEventsUsePassedEventNameForDirectHandlers() throws IOException {
        String eventsSource = Files.readString(MUDLIB_SOURCE_ROOT.resolve("lib/core/events.c"));
        assertTrue(
                eventsSource.contains("function_exists($2, $1)"),
                "notifySynchronous should test direct event handlers with the closure event argument.");
        assertFalse(
                eventsSource.contains("function_exists(event, $1)"),
                "notifySynchronous should not rely on lexical event lookup inside the filter closure.");
    }

    @Test
    void realmsSettingsRestoreConvertsPersistedSafetyTeleportToInt() throws IOException {
        String settingsSource = Files.readString(MUDLIB_SOURCE_ROOT.resolve("lib/modules/secure/settings.h"));
        assertTrue(
                settingsSource.contains("lastSafetyTeleport = to_int(persistence->extractSaveData("),
                "RealmsMUD stores settings values as text, so safety teleport restore needs numeric conversion.");
    }

    @Test
    void realmsHelpPassesUnicodeFlagToBaseCommandDisplayMethods() throws IOException {
        String helpSource = Files.readString(MUDLIB_SOURCE_ROOT.resolve("lib/commands/player/help.c"));
        assertTrue(
                helpSource.contains("int useUnicode = charset == \"unicode\";"),
                "RealmsMUD base command display helpers expect a numeric unicode flag, not a charset string.");
        assertTrue(
                helpSource.contains("displaySynopsis(command, colorConfiguration, useUnicode)"),
                "Help detail rendering should pass the numeric unicode flag through display helpers.");
    }

    @Test
    void realmsGeneratedEquipmentBonusesUseItemBonusKeys() throws IOException {
        String enchantmentsSource = Files.readString(
                MUDLIB_SOURCE_ROOT.resolve("lib/services/materials/components/enchantments.c"));
        assertTrue(
                enchantmentsSource.contains("string itemSkill = getBlueprintDetails(item, \"skill to use\")"),
                "Generated equipment enchantments should prefer blueprint skill metadata for item bonus keys.");
        assertTrue(
                enchantmentsSource.contains("bonusesService->isValidBonus(typedBonus)"),
                "Generated equipment enchantments should only add item-specific bonus keys Realms accepts.");
    }

    @Test
    void realmsBonusesServiceInitializesEmptyRuntimeArrays() throws IOException {
        String bonusesSource = Files.readString(MUDLIB_SOURCE_ROOT.resolve("lib/services/bonusesService.c"));
        assertTrue(
                bonusesSource.contains("if(!bonuses || !sizeof(bonuses))"),
                "Bonus list lazy initialization should tolerate runtimes that materialize zero arrays as empty.");
    }

    private static List<String> compatibilitySet(Path sourceRoot) throws IOException {
        Set<String> sourceNames = new LinkedHashSet<>(BOOT_AND_CORE_COMPATIBILITY_SET);
        for (String sweepRoot : COMPATIBILITY_SWEEP_ROOTS) {
            Path root = sourceRoot.resolve(sweepRoot);
            try (Stream<Path> paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".c"))
                        .map(sourceRoot::relativize)
                        .map(Path::toString)
                        .map(path -> path.replace('\\', '/'))
                        .sorted()
                        .forEach(sourceNames::add);
            }
        }
        return List.copyOf(sourceNames);
    }

    private static Path compatibilitySourcePath(Path sourceRoot, String sourceName) {
        if (sourceName.startsWith("jvmud/"))
            return MUDLIB_ROOT.resolve(sourceName);
        return sourceRoot.resolve(sourceName);
    }

    private static void assertConfiguredBoundary(MudlibBoundary boundary) {
        assertEquals("realmsmud", boundary.gameId().orElseThrow());
        assertEquals("RealmsMUD", boundary.gameName().orElseThrow());
        assertEquals("jvmud/mudlib", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("secure/simul_efun", boundary.mudlibGlobalObjectPath().orElseThrow());
        assertEquals("jvmud/jvmud", boundary.compatibilityGlobalObjectPath().orElseThrow());
        assertEquals(
                MUDLIB_ROOT.resolve("jvmud/jvmud.c").toAbsolutePath().normalize(),
                boundary.compatibilityGlobalObjectSourcePath().orElseThrow());
        assertEquals("secure/login", boundary.playerObjectPath().orElseThrow());
        assertEquals(">", boundary.playerPrompt().orElseThrow());
        assertEquals(78, boundary.maxLineLength());
        assertFalse(boundary.showRuler());
        assertEquals("areas/eledhel/southern-city/12x2", boundary.initialPlacePath().orElseThrow());
        assertEquals("init/init_file", boundary.preloadFilePath().orElseThrow());
        assertTrue(boundary.preloadObjectPaths().contains("secure/master"));
        assertTrue(boundary.preloadObjectPaths().contains("secure/simul_efun"));
        assertTrue(boundary.preloadObjectPaths().contains("lib/services/environmentService"));
        assertEquals("jdbc:mysql://localhost:3306/RealmsLib", boundary.databaseJdbcUrl().orElseThrow());
        assertEquals("realmslib", boundary.databaseUser().orElseThrow());
        assertFalse(boundary.databasePassword().orElseThrow().isBlank());
        assertEquals("jvmud_allocate", boundary.directEfunAlias("allocate").orElseThrow());
        assertEquals("jvmud_format_text", boundary.directEfunAlias("sprintf").orElseThrow());
        assertEquals("jvmud_db_exec", boundary.directEfunAlias("db_exec").orElseThrow());
        assertEquals("jvmud_db_fetch", boundary.directEfunAlias("db_fetch").orElseThrow());
        assertEquals("jvmud_write_to_lpc_object", boundary.directEfunAlias("tell_object").orElseThrow());
        assertTrue(boundary.directEfunAlias("exec").isEmpty());
        assertEquals("jvmud_capture_session_input", boundary.directEfunAlias("input_to").orElseThrow());
        assertEquals("jvmud_current_lpc_object", boundary.directEfunAlias("this_object").orElseThrow());
        assertEquals("jvmud_query_ip_number", boundary.directEfunAlias("query_ip_number").orElseThrow());
        assertEquals("jvmud_query_ip_name", boundary.directEfunAlias("query_ip_name").orElseThrow());
        assertEquals("jvmud_lpc_object_info", boundary.directEfunAlias("object_info").orElseThrow());
        assertEquals("jvmud_configure_lpc_object", boundary.directEfunAlias("configure_object").orElseThrow());
        assertEquals("jvmud_find_lpc_object", boundary.directEfunAlias("find_object").orElseThrow());
        assertEquals("jvmud_find_lpc_object", boundary.directEfunAlias("blueprint").orElseThrow());
        assertEquals("jvmud_to_lpc_object", boundary.directEfunAlias("to_object").orElseThrow());
        assertEquals("jvmud_list_mudlib_paths", boundary.directEfunAlias("get_dir").orElseThrow());
        assertEquals("jvmud_load_lpc_object", boundary.directEfunAlias("load_object").orElseThrow());
        assertEquals("jvmud_clone_lpc_object", boundary.directEfunAlias("clone_object").orElseThrow());
        assertEquals("jvmud_method_exists", boundary.directEfunAlias("function_exists").orElseThrow());
        assertEquals("jvmud_find_entity", boundary.directEfunAlias("present").orElseThrow());
        assertEquals("jvmud_set_entity_location", boundary.directEfunAlias("set_environment").orElseThrow());
        assertEquals("jvmud_entity_location", boundary.directEfunAlias("environment").orElseThrow());
        assertEquals("jvmud_remove_mudlib_text", boundary.directEfunAlias("rm").orElseThrow());
        assertEquals("jvmud_copy_mudlib_text", boundary.directEfunAlias("copy_file").orElseThrow());
        assertEquals("jvmud_rename_mudlib_text", boundary.directEfunAlias("rename").orElseThrow());
        assertEquals("jvmud_create_mudlib_directory", boundary.directEfunAlias("mkdir").orElseThrow());
        assertEquals("jvmud_remove_mudlib_directory", boundary.directEfunAlias("rmdir").orElseThrow());
        assertEquals("jvmud_regex_match", boundary.directEfunAlias("regexp").orElseThrow());
        assertEquals("jvmud_driver_info", boundary.directEfunAlias("driver_info").orElseThrow());
        assertEquals("jvmud_to_int", boundary.directEfunAlias("to_int").orElseThrow());
        assertEquals("jvmud_to_string", boundary.directEfunAlias("to_string").orElseThrow());
        assertEquals("jvmud_sqrt", boundary.directEfunAlias("sqrt").orElseThrow());
        assertEquals("jvmud_member", boundary.directEfunAlias("member").orElseThrow());
        assertEquals("jvmud_mapping_keys", boundary.directEfunAlias("m_indices").orElseThrow());
        assertEquals("jvmud_mapping_values", boundary.directEfunAlias("m_values").orElseThrow());
        assertEquals("jvmud_mapping_from_keys", boundary.directEfunAlias("mkmapping").orElseThrow());
        assertEquals("jvmud_mapping_delete", boundary.directEfunAlias("m_delete").orElseThrow());
        assertEquals("jvmud_inherited_programs", boundary.directEfunAlias("inherit_list").orElseThrow());
        assertEquals("jvmud_apply_callable", boundary.directEfunAlias("apply").orElseThrow());
        assertEquals("jvmud_random", boundary.directEfunAlias("random").orElseThrow());
        assertEquals("jvmud_capitalize_text", boundary.directEfunAlias("capitalize").orElseThrow());
        assertEquals("jvmud_is_mapping", boundary.directEfunAlias("mappingp").orElseThrow());
        assertEquals("jvmud_is_object", boundary.directEfunAlias("objectp").orElseThrow());
        assertEquals("jvmud_is_array", boundary.directEfunAlias("pointerp").orElseThrow());
        assertEquals("jvmud_is_string", boundary.directEfunAlias("stringp").orElseThrow());
        assertEquals(
                "\"JVMud RealmsMUD LDMud compatibility\"",
                boundary.compatibilityPredefines().get("__VERSION__"));
        assertEquals("3", boundary.compatibilityPredefines().get("__VERSION_MAJOR__"));
        assertEquals("6", boundary.compatibilityPredefines().get("__VERSION_MINOR__"));
        assertEquals("3", boundary.compatibilityPredefines().get("__VERSION_MICRO__"));
        assertEquals("create", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED).orElseThrow());
        assertEquals("reset", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_ACTIVATED).orElseThrow());
        assertEquals(
                "prepare_destruct",
                boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_DESTRUCTION_REQUESTED).orElseThrow());
        assertEquals("logon", boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_CONNECTED).orElseThrow());
        assertEquals("addCommands", boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_POST_REBIND).orElseThrow());
        assertEquals("init", boundary.lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED).orElseThrow());
        assertEquals("log_error", boundary.lifecycleMethod(MudlibLifecycleEvent.LOG_ERROR).orElseThrow());
        assertEquals("runtime_error", boundary.lifecycleMethod(MudlibLifecycleEvent.RUNTIME_ERROR).orElseThrow());
        assertEquals("heart_beat", boundary.temporalTickMethod().orElseThrow());
        assertEquals(1, boundary.temporalTickIntervalSeconds());
    }

    private static void writeReport(MudlibBoundary boundary, Map<String, CompilationResult> results)
            throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());

        long supported = results.values().stream().filter(result -> result.getProblems().isEmpty()).count();
        long knownIssues = results.entrySet().stream().filter(RealmsMudCompatibilityScanTest::isKnownIssue).count();
        long unsupported = results.size() - supported - knownIssues;

        StringBuilder report = new StringBuilder();
        report.append("# JVMud RealmsMUD Compatibility Scan\n\n");
        report.append("This report is informational. It captures the current compiler/runtime gaps ");
        report.append("without making RealmsMUD readiness a build gate.\n\n");
        report.append("- Mudlib root: `").append(escape(MUDLIB_ROOT.toString())).append("`\n");
        report.append("- Configured source root: `")
                .append(escape(boundary.mudlibRootPath().map(Path::toString).orElse("")))
                .append("`\n");
        report.append("- Mudlib global object: `")
                .append(escape(boundary.mudlibGlobalObjectPath().orElse("")))
                .append("`\n");
        report.append("- JVMud compatibility global object: `")
                .append(escape(boundary.compatibilityGlobalObjectPath().orElse("")))
                .append("`\n");
        report.append("- Scanned files: ").append(results.size()).append("\n");
        report.append("- Supported now: ").append(supported).append("\n");
        report.append("- Ignored known RealmsMUD source issues: ").append(knownIssues).append("\n");
        report.append("- Current blockers: ").append(unsupported).append("\n\n");
        report.append("## Boot, Core, And Environment Sweep\n\n");
        report.append("| Source | Status | First Problem Stage | First Problem Line | First Problem |\n");
        report.append("| --- | --- | --- | ---: | --- |\n");

        for (Map.Entry<String, CompilationResult> entry : results.entrySet()) {
            List<CompilationProblem> problems = entry.getValue().getProblems();
            if (problems.isEmpty()) {
                report.append("| `").append(entry.getKey()).append("` | supported |  |  |  |\n");
                continue;
            }

            CompilationProblem first = problems.get(0);
            KnownIssue knownIssue = matchingKnownIssue(entry.getKey(), first);
            if (knownIssue != null) {
                report.append("| `")
                        .append(entry.getKey())
                        .append("` | known Realms source issue | ")
                        .append(escape(String.valueOf(first.getStage())))
                        .append(" | ")
                        .append(first.getLine() == null ? "" : first.getLine())
                        .append(" | ")
                        .append(escape(problemSummary(first)))
                        .append(" |\n");
                continue;
            }

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
        report.append("- `/lib/core` files cover the reusable event, state, selector, organization, ");
        report.append("prerequisite, message, and thing surfaces used by gameplay objects.\n");
        report.append("- The `/lib/environment/**/*.c` sweep covers environmental elements, generated ");
        report.append("rooms, regions, buildings, features, terrain, inventories, doors, interiors, ");
        report.append("and item definitions without hand-maintaining a giant source list.\n");
        report.append("- The `/lib/services/**/*.c`, `/lib/modules/secure/**/*.c`, ");
        report.append("`/secure/master/**/*.c`, and `/secure/simulated-efuns/**/*.c` sweeps cover ");
        report.append("RealmsMUD's service objects, security/data-service modules, master support ");
        report.append("objects, and simulated global efun surface.\n");
        appendKnownIssueNotes(report, results);

        Files.writeString(REPORT_PATH, report.toString());
    }

    private static void appendKnownIssueNotes(StringBuilder report, Map<String, CompilationResult> results) {
        report.append("\n## Ignored Known RealmsMUD Source Issues\n\n");
        report.append("These are deliberately excluded from the current blocker count so the radar can ");
        report.append("keep exposing new JVMud compatibility gaps. They are not treated as JVMud language ");
        report.append("features to implement.\n\n");
        report.append("| Source | Line | Stage | Status | Note |\n");
        report.append("| --- | ---: | --- | --- | --- |\n");
        for (KnownIssue issue : KNOWN_ISSUES) {
            CompilationResult result = results.get(issue.sourceName());
            String status = "not scanned";
            if (result != null && result.getProblems().isEmpty()) {
                status = "resolved or no longer first problem";
            } else if (result != null && matchingKnownIssue(issue.sourceName(), result.getProblems().get(0)) != null) {
                status = "ignored";
            } else if (result != null) {
                status = "different first problem";
            }
            report.append("| `")
                    .append(issue.sourceName())
                    .append("` | ")
                    .append(issue.line())
                    .append(" | ")
                    .append(issue.stage())
                    .append(" | ")
                    .append(status)
                    .append(" | ")
                    .append(escape(issue.note()))
                    .append(" |\n");
        }
        report.append("\nKnown issues only classify the current first compiler problem for a file. ");
        report.append("Later problems in that same file may appear after the known issue is fixed or ");
        report.append("the compiler gains broader diagnostic recovery.\n");
    }

    private static boolean isKnownIssue(Map.Entry<String, CompilationResult> entry) {
        List<CompilationProblem> problems = entry.getValue().getProblems();
        return !problems.isEmpty() && matchingKnownIssue(entry.getKey(), problems.get(0)) != null;
    }

    private static KnownIssue matchingKnownIssue(String sourceName, CompilationProblem problem) {
        return KNOWN_ISSUES.stream()
                .filter(issue -> issue.matches(sourceName, problem))
                .findFirst()
                .orElse(null);
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

    /**
     * Documents one RealmsMUD source-level mismatch that should stay visible in the radar while
     * being excluded from the current JVMud blocker count.
     */
    private record KnownIssue(
            String sourceName, CompilationStage stage, int line, String messageFragment, String note) {
        private boolean matches(String candidateSourceName, CompilationProblem problem) {
            if (!sourceName.equals(candidateSourceName)) {
                return false;
            }
            if (stage != problem.getStage()) {
                return false;
            }
            if (problem.getLine() == null || problem.getLine() != line) {
                return false;
            }
            return problemSummary(problem).contains(messageFragment);
        }
    }
}
