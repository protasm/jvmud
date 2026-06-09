package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTMethod;
import io.github.protasm.jvmud.compiler.parser.ast.ASTObject;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayLiteral;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprDynamicInvoke;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInvokeField;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprMappingLiteral;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSequence;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprTernary;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtFor;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.jvmud.compiler.parser.ast.stmt.ASTStmtWhile;
import io.github.protasm.jvmud.compiler.parser.ast.visitor.ASTVisitor;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.compiler.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.compiler.runtime.Truth;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibBoundaryConfigReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

final class MudlibCompatibilityScanTest {
    private static final Path REPO_ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final Path MUDLIB_ROOT = REPO_ROOT.resolve("mudlibs").resolve("lp245");
    private static final String CONFIG_PATH = "jvmud/config";
    private static final String PLAYER_SOURCE = "obj/player.c";
    private static final List<String> COMPATIBILITY_SET =
            List.of(
                    "obj/beer.c",
                    "obj/corpse.c",
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

        CompilationResult playerResult = results.get(PLAYER_SOURCE);
        assertTrue(playerResult != null, "`obj/player.c` is not part of the compatibility scan.");
        assertTrue(playerResult.getProblems().isEmpty(), () -> "`obj/player.c` must compile cleanly: "
                + playerResult.getProblems());
        assertTrue(playerResult.getBytecode() != null, "`obj/player.c` must compile through bytecode generation.");
        assertLoadsThroughJvmVerification(boundary);
        assertTrue(Files.exists(Path.of("target", "jvmud-mudlib-compatibility.md")));
    }

    private static void assertLoadsThroughJvmVerification(MudlibBoundary boundary) {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(MUDLIB_ROOT).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        LPCObjectHandle player = runtime.load(stripExtension(PLAYER_SOURCE));

        assertNotNull(player, "`obj/player.c` must define, verify, and instantiate through the JVM.");
    }

    @Test
    void vanillaForestMovementDispatchesWithoutRuntimeCastFailure() throws IOException {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(MUDLIB_ROOT).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        LPCObjectHandle player = runtime.load("obj/player");
        LPCObjectHandle wild = runtime.load("room/forest/wild1");
        Object forest = runtime.load("room/forest/forest1").instance();
        runtime.moveObject(player.instance(), wild.instance());

        runtime.refreshCommandActions(player.instance());

        assertEquals(1, runtime.dispatchCommand(player.instance(), "west"));
        assertEquals(forest, runtime.environment(player.instance()));
    }

    @Test
    void vanillaClearingMovementDispatchesWithoutRuntimeCastFailure() throws IOException {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(MUDLIB_ROOT).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        LPCObjectHandle player = runtime.load("obj/player");
        LPCObjectHandle clearing = runtime.load("room/forest/clearing");
        runtime.moveObject(player.instance(), clearing.instance());
        runtime.withCommandActor(player.instance(), () -> player.invoke("add_standard_commands"));

        runtime.refreshCommandActions(player.instance());

        assertEquals(1, runtime.dispatchCommand(player.instance(), "west"));
        assertEquals("room/forest/forest2", runtime.objectId(runtime.environment(player.instance())));

        runtime.refreshCommandActions(player.instance());
        assertEquals(1, runtime.dispatchCommand(player.instance(), "exa troll"));

        runtime.refreshCommandActions(player.instance());
        assertEquals(1, runtime.dispatchCommand(player.instance(), "kill troll"));

        runtime.refreshCommandActions(player.instance());
        assertEquals(1, runtime.dispatchCommand(player.instance(), "kill troll"));

        runtime.refreshCommandActions(player.instance());
        assertEquals(1, runtime.dispatchCommand(player.instance(), "east"));
        assertEquals("room/forest/clearing", runtime.objectId(runtime.environment(player.instance())));
    }

    @Test
    void vanillaSlopeMovementDispatchesMultiArgumentCallOther() throws IOException {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(MUDLIB_ROOT).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        LPCObjectHandle player = runtime.load("obj/player");
        LPCObjectHandle slope = runtime.load("room/mountain/slope");
        runtime.moveObject(player.instance(), slope.instance());

        runtime.refreshCommandActions(player.instance());

        assertEquals(1, runtime.dispatchCommand(player.instance(), "west"));
        assertEquals("room/orc/orc_vall", runtime.objectId(runtime.environment(player.instance())));

        runtime.clearOutputTranscript();
        runtime.invokeObject(runtime.environment(player.instance()), "long", 0);
        assertTrue(runtime.outputTranscript().contains("You are in the orc valley. This place is inhabited by orcs."));

        runtime.refreshCommandActions(player.instance());
        assertEquals(1, runtime.dispatchCommand(player.instance(), "east"));
        assertEquals("room/mountain/slope", runtime.objectId(runtime.environment(player.instance())));
    }

    @Test
    void vanillaAdventurersGuildCostCommandQuotesCosts() throws IOException {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(MUDLIB_ROOT).build());
        EngineEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        LPCObjectHandle player = runtime.load("obj/player");
        LPCObjectHandle guild = runtime.load("room/village/adv_guild");
        runtime.moveObject(player.instance(), guild.instance());

        runtime.refreshCommandActions(player.instance());
        runtime.clearOutputTranscript();

        assertTrue(Truth.isTruthy(runtime.dispatchCommand(player.instance(), "cost")));
        assertTrue(runtime.outputTranscript().contains("Str: "));
        assertTrue(runtime.outputTranscript().contains("gold coins to advance to level"));
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
        appendPlayerRuntimeSurface(report, results.get(PLAYER_SOURCE));

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

    private static void appendPlayerRuntimeSurface(StringBuilder report, CompilationResult playerResult) {
        report.append("\n## obj/player.c Runtime Surface\n\n");
        report.append("This checklist is extracted from the resolved `obj/player.c` AST. ");
        report.append("It is a runtime prioritization aid: compiling these calls does not mean each behavior is implemented.\n\n");

        if (playerResult == null || playerResult.getAstObject() == null) {
            report.append("Runtime surface is unavailable until `obj/player.c` parses far enough to produce an AST.\n");
            return;
        }

        RuntimeSurface surface = RuntimeSurfaceCollector.collect(playerResult.getAstObject());
        report.append("| Category | Calls |\n");
        report.append("| --- | --- |\n");
        appendSurfaceRow(report, "Global mfun/efun calls", surface.globalFunctions());
        appendSurfaceRow(report, "Mudlib method calls", surface.mudlibMethods());
        appendSurfaceRow(report, "Object method invocations", surface.objectInvocations());
        appendSurfaceRow(report, "Dynamic object invocations", surface.dynamicInvocations());

        appendPlayerRuntimeSupportMatrix(report, surface);
    }

    private static void appendPlayerRuntimeSupportMatrix(StringBuilder report, RuntimeSurface surface) {
        report.append("\n### Runtime Support Matrix\n\n");
        report.append("| Call | Status | Notes |\n");
        report.append("| --- | --- | --- |\n");
        for (String call : surface.globalFunctions()) {
            SupportEntry support = supportForGlobalCall(call);
            report.append("| `")
                    .append(escape(call))
                    .append("` | ")
                    .append(support.status())
                    .append(" | ")
                    .append(escape(support.notes()))
                    .append(" |\n");
        }
        for (String call : surface.dynamicInvocations()) {
            SupportEntry support = supportForDynamicCall(call);
            report.append("| `")
                    .append(escape(call))
                    .append("` | ")
                    .append(support.status())
                    .append(" | ")
                    .append(escape(support.notes()))
                    .append(" |\n");
        }
    }

    private static SupportEntry supportForGlobalCall(String call) {
        return switch (call) {
        case "add_action" -> support("Partial", "Registers handler methods and explicit verbs, including the legacy third flag argument.");
        case "call_other" -> support("Partial", "Reflective invoke exists for zero/one argument calls; broader arity and error behavior remain compatibility work.");
        case "call_out" -> support("Implemented", "Schedules a one-shot method call on the current object.");
        case "capitalize" -> support("Implemented", "Mudlib mfun delegates to JVMud text capitalization.");
        case "cat" -> support("Partial", "Mudlib mfun reads and writes mudlib-rooted text; line-range paging remains compatibility work.");
        case "clone_object" -> support("Partial", "Delegates to the runtime object factory; behavior depends on loaded source/object lifecycle.");
        case "command" -> support("Partial", "Dispatches a command line against an entity's registered command actions.");
        case "crypt" -> support("Stubbed", "Mudlib mfun returns deterministic placeholder text for development login flow.");
        case "ctime" -> support("Implemented", "Mudlib mfun formats Unix timestamps through JVMud time formatting.");
        case "destruct" -> support("Partial", "Removes objects and inventory links from RuntimeContext.");
        case "enable_commands" -> support("Partial", "Marks the current entity as command-enabled for mudlib compatibility checks.");
        case "environment" -> support("Implemented", "RuntimeContext tracks object environments.");
        case "extract" -> support("Implemented", "Mudlib mfun delegates to JVMud inclusive text slicing.");
        case "file_name" -> support("Implemented", "Returns the runtime object id for loaded objects.");
        case "first_inventory" -> support("Implemented", "RuntimeContext can walk the first child in an inventory.");
        case "find_living" -> support("Partial", "Looks up objects registered through set_living_name.");
        case "find_player" -> support("Stubbed", "Mudlib mfun currently returns 0; real connected-player lookup remains future work.");
        case "input_to" -> support("Partial", "Captures the next line of bound session input for a persona; no-echo handling remains transport work.");
        case "next_inventory" -> support("Implemented", "RuntimeContext can walk sibling inventory links.");
        case "living" -> support("Partial", "Returns true for objects that have called enable_commands.");
        case "lower_case" -> support("Implemented", "Mudlib mfun delegates to JVMud text lowercasing.");
        case "log_file" -> support("Stubbed", "Mudlib mfun accepts the call and discards text until log policy exists.");
        case "move_object" -> support("Implemented", "RuntimeContext moves objects between inventories with cycle checks.");
        case "present" -> support("Partial", "RuntimeContext searches inventory by identity or id method.");
        case "query_idle" -> support("Implemented", "Reads idle time from the bound session/persona record.");
        case "query_ip_number" -> support("Implemented", "Reads the remote address from the bound session/persona record.");
        case "query_verb" -> support("Implemented", "Backed by the active command dispatch verb.");
        case "random" -> support("Implemented", "Mudlib mfun delegates to JVMud bounded integer randomness.");
        case "remove_call_out" -> support("Implemented", "Cancels a pending one-shot method call on the current object.");
        case "restore_object" -> support("Partial", "Mudlib mfun restores scalar fields from mudlib-rooted LPC object state.");
        case "save_object" -> support("Partial", "Mudlib mfun saves scalar fields to mudlib-rooted LPC object state.");
        case "say" -> support("Partial", "Writes to the shared output sink; no room/session broadcast routing yet.");
        case "set_heart_beat" -> support("Implemented", "Schedules or cancels a recurring temporal tick for the current object.");
        case "set_light" -> support("Implemented", "RuntimeContext tracks per-object light deltas.");
        case "set_living_name" -> support("Implemented", "Registers the current object in the mudlib living-name alias namespace.");
        case "sizeof" -> support("Implemented", "Typed and runtime-checked for arrays, mappings, strings, and dynamic mixed values.");
        case "strlen" -> support("Implemented", "Mudlib mfun delegates to JVMud size handling for strings.");
        case "tell_object" -> support("Partial", "Routes to a bound target session, with shared-output fallback for unbound objects.");
        case "this_object" -> support("Implemented", "Backed by RuntimeContext current-object stack.");
        case "this_player" -> support("Partial", "Uses command actor when present, otherwise current object; real telnet persona binding still pending.");
        case "time" -> support("Implemented", "Returns current Unix time in seconds.");
        case "transfer" -> support("Partial", "Moves an entity to a destination and returns LP-style success code 0; weight and failure policy remain future work.");
        case "users" -> support("Implemented", "Returns connected persona objects from the session/persona registry.");
        case "write" -> support("Partial", "Routes to the active persona session when bound, with shared-output fallback.");
        case "add_worth",
                "creator",
                "ed",
                "file_size",
                "filter_objects",
                "find_object",
                "intp",
                "localcmd",
                "ls",
                "mkdir",
                "move_player",
                "previous_object",
                "rm",
                "rmdir",
                "shout",
                "shutdown",
                "snoop",
                "sscanf",
                "tail",
                "wizlist" -> support("Missing", "Resolved through the mfun boundary but no JVMud mfun/efun implementation exists yet.");
        default -> support("Unknown", "No audit classification has been assigned yet.");
        };
    }

    private static SupportEntry supportForDynamicCall(String call) {
        return switch (call) {
        case "catch_shout" -> support("Missing", "Requires real broadcast/session routing for shout delivery.");
        case "id" -> support("Partial", "Can be invoked reflectively, but identifier matching depends on target mudlib objects being loaded.");
        case "query_age",
                "query_level",
                "query_name",
                "query_real_name",
                "query_value",
                "query_weight",
                "short" -> support("Partial", "Can be invoked reflectively once the target mudlib object is loaded and exposes the method.");
        default -> support("Unknown", "Dynamic object call requires runtime exercise to classify.");
        };
    }

    private static SupportEntry support(String status, String notes) {
        return new SupportEntry(status, notes);
    }

    private static void appendSurfaceRow(StringBuilder report, String category, Set<String> calls) {
        report.append("| ")
                .append(category)
                .append(" | ")
                .append(formatCalls(calls))
                .append(" |\n");
    }

    private static String formatCalls(Set<String> calls) {
        if (calls.isEmpty())
            return "";

        return calls.stream()
                .map(call -> "`" + escape(call) + "`")
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
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

    private record RuntimeSurface(
            Set<String> globalFunctions,
            Set<String> mudlibMethods,
            Set<String> objectInvocations,
            Set<String> dynamicInvocations) {}

    private record SupportEntry(String status, String notes) {}

    private static final class RuntimeSurfaceCollector implements ASTVisitor {
        private final Set<String> globalFunctions = new TreeSet<>();
        private final Set<String> mudlibMethods = new TreeSet<>();
        private final Set<String> objectInvocations = new TreeSet<>();
        private final Set<String> dynamicInvocations = new TreeSet<>();

        static RuntimeSurface collect(ASTObject object) {
            RuntimeSurfaceCollector collector = new RuntimeSurfaceCollector();
            object.accept(collector);
            return new RuntimeSurface(
                    sortedCopy(collector.globalFunctions),
                    sortedCopy(collector.mudlibMethods),
                    sortedCopy(collector.objectInvocations),
                    sortedCopy(collector.dynamicInvocations));
        }

        private static Set<String> sortedCopy(Set<String> calls) {
            return Collections.unmodifiableSet(new TreeSet<>(calls));
        }

        @Override
        public void visitObject(ASTObject object) {
            object.fields().accept(this);
            object.methods().accept(this);
        }

        @Override
        public void visitMethod(ASTMethod method) {
            if (method.body() != null)
                method.body().accept(this);
        }

        @Override
        public void visitStmtBlock(ASTStmtBlock stmt) {
            stmt.statements().forEach(statement -> statement.accept(this));
        }

        @Override
        public void visitStmtExpression(ASTStmtExpression stmt) {
            stmt.expression().accept(this);
        }

        @Override
        public void visitStmtFor(ASTStmtFor stmt) {
            visitIfPresent(stmt.initializer());
            visitIfPresent(stmt.condition());
            visitIfPresent(stmt.update());
            stmt.body().accept(this);
        }

        @Override
        public void visitStmtIfThenElse(ASTStmtIfThenElse stmt) {
            stmt.condition().accept(this);
            stmt.thenBranch().accept(this);
            if (stmt.elseBranch() != null)
                stmt.elseBranch().accept(this);
        }

        @Override
        public void visitStmtReturn(ASTStmtReturn stmt) {
            visitIfPresent(stmt.returnValue());
        }

        @Override
        public void visitStmtWhile(ASTStmtWhile stmt) {
            stmt.condition().accept(this);
            stmt.body().accept(this);
        }

        @Override
        public void visitExpression(ASTExpression expression) {
            if (expression instanceof ASTExprArrayAccess access) {
                access.target().accept(this);
                access.index().accept(this);
            } else if (expression instanceof ASTExprArrayLiteral literal) {
                literal.elements().forEach(element -> element.accept(this));
            } else if (expression instanceof ASTExprArrayStore store) {
                store.target().accept(this);
                store.index().accept(this);
                store.value().accept(this);
            } else if (expression instanceof ASTExprMappingLiteral literal) {
                literal.entries().forEach(entry -> {
                    entry.key().accept(this);
                    entry.value().accept(this);
                });
            } else if (expression instanceof ASTExprSequence sequence) {
                sequence.expressions().forEach(expr -> expr.accept(this));
            } else if (expression instanceof ASTExprTernary ternary) {
                ternary.condition().accept(this);
                ternary.thenBranch().accept(this);
                ternary.elseBranch().accept(this);
            }
        }

        @Override
        public void visitExprFieldStore(ASTExprFieldStore expr) {
            expr.value().accept(this);
        }

        @Override
        public void visitExprLocalStore(ASTExprLocalStore expr) {
            expr.value().accept(this);
        }

        @Override
        public void visitExprOpBinary(ASTExprOpBinary expr) {
            expr.left().accept(this);
            expr.right().accept(this);
        }

        @Override
        public void visitExprOpUnary(ASTExprOpUnary expr) {
            expr.right().accept(this);
        }

        @Override
        public void visitExprCallEfun(ASTExprCallEfun expr) {
            globalFunctions.add(expr.signature().name());
            expr.arguments().accept(this);
        }

        @Override
        public void visitExprCallMethod(ASTExprCallMethod expr) {
            mudlibMethods.add(expr.method().symbol().name());
            expr.arguments().accept(this);
        }

        @Override
        public void visitExprDynamicInvoke(ASTExprDynamicInvoke expr) {
            dynamicInvocations.add(expr.methodName());
            expr.target().accept(this);
            expr.arguments().accept(this);
        }

        @Override
        public void visitExprInvokeField(ASTExprInvokeField expr) {
            objectInvocations.add(expr.methodName());
            expr.arguments().accept(this);
        }

        @Override
        public void visitExprInvokeLocal(ASTExprInvokeLocal expr) {
            objectInvocations.add(expr.methodName());
            expr.arguments().accept(this);
        }

        private void visitIfPresent(ASTExpression expression) {
            if (expression != null)
                expression.accept(this);
        }
    }
}
