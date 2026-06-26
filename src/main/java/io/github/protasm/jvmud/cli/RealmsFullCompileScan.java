package io.github.protasm.jvmud.cli;

import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundaryConfigReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Compiles every object listed in the RealmsMUD full preload manifest without loading instances.
 *
 * <p>This is a compatibility radar for compiler/analyzer/codegen failures. It deliberately stops
 * before object construction so database-backed {@code create()} methods do not drown out compile
 * errors.</p>
 */
public final class RealmsFullCompileScan {
    private static final Path DEFAULT_CONFIG = Path.of("mudlibs/realmsmud/jvmud/realmsmud.full.config");
    private static final Path DEFAULT_REPORT = Path.of("target/realms-full-compile.md");

    private RealmsFullCompileScan() {}

    public static void main(String[] args) throws IOException {
        Path configFile = resolveRepoPath(args.length > 0 ? Path.of(args[0]) : DEFAULT_CONFIG);
        Path reportFile = resolveRepoPath(args.length > 1 ? Path.of(args[1]) : DEFAULT_REPORT);
        if (args.length > 2) {
            throw new IllegalArgumentException("Usage: scripts/scan-realms-full-compile.sh [config-file] [report-file]");
        }

        Path mudlibRoot = mudlibRootForConfigFile(configFile);
        String configObjectPath = mudlibRoot.relativize(configFile).toString().replace('\\', '/');
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(mudlibRoot, configObjectPath);
        Path activeRoot = boundary.mudlibRootPath().orElse(mudlibRoot).toAbsolutePath().normalize();
        Path manifest = activeRoot.resolve(boundary.preloadFilePath()
                .orElseThrow(() -> new IllegalArgumentException("Config does not declare preload_file")));

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(mudlibRoot.toAbsolutePath().normalize())
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        List<String> entries = manifestEntries(manifest);
        List<Failure> failures = new ArrayList<>();
        int compiled = 0;

        for (String entry : entries) {
            Path sourcePath = activeRoot.resolve(stripLeadingSlash(entry)).normalize();
            try {
                CompilationResult result = runtime.compile(sourcePath);
                if (result.succeeded()) {
                    compiled++;
                } else {
                    failures.add(Failure.fromProblems(entry, result.getProblems()));
                }
            } catch (RuntimeException | Error e) {
                failures.add(Failure.fromThrowable(entry, e));
            }

            int attempted = compiled + failures.size();
            if (attempted % 100 == 0 || attempted == entries.size()) {
                System.out.printf(
                        "compile scan: %d/%d attempted, %d failed%n",
                        attempted,
                        entries.size(),
                        failures.size());
            }
        }

        writeReport(reportFile, configFile, manifest, entries.size(), compiled, failures);
        System.out.printf(
                "Realms full compile scan: %d compiled, %d failed. Report: %s%n",
                compiled,
                failures.size(),
                reportFile);

        if (!failures.isEmpty()) {
            System.exit(1);
        }
    }

    private static List<String> manifestEntries(Path manifest) throws IOException {
        List<String> entries = new ArrayList<>();
        for (String line : Files.readAllLines(manifest, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                entries.add(trimmed);
            }
        }
        return entries;
    }

    private static void writeReport(
            Path reportFile,
            Path configFile,
            Path manifest,
            int total,
            int compiled,
            List<Failure> failures) throws IOException {
        Path parent = reportFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(reportFile, StandardCharsets.UTF_8))) {
            out.println("# RealmsMUD Full Compile Scan");
            out.println();
            out.println("- Config: `" + configFile + "`");
            out.println("- Manifest: `" + manifest + "`");
            out.println("- Total objects: " + total);
            out.println("- Compiled: " + compiled);
            out.println("- Failed: " + failures.size());
            out.println();

            if (failures.isEmpty()) {
                out.println("No compile failures.");
                return;
            }

            out.println("## Failures");
            out.println();
            for (Failure failure : failures) {
                out.println("### `" + failure.objectPath() + "`");
                out.println();
                for (String detail : failure.details()) {
                    out.println("- " + detail);
                }
                out.println();
            }
        }
    }

    private static Path mudlibRootForConfigFile(Path configFile) {
        Path configDir = configFile.getParent();
        if (configDir == null) {
            throw new IllegalArgumentException("Config file must have a parent directory: " + configFile);
        }
        if ("jvmud".equals(configDir.getFileName().toString())) {
            Path root = configDir.getParent();
            if (root == null) {
                throw new IllegalArgumentException("Config file must live inside a mudlib root: " + configFile);
            }
            return root;
        }
        return configDir;
    }

    private static Path resolveRepoPath(Path path) {
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return launchRoot().resolve(path).normalize();
    }

    private static Path launchRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) && Files.isDirectory(current.resolve("mudlibs"))) {
                return current;
            }
            current = current.getParent();
        }
        return Path.of("").toAbsolutePath().normalize();
    }

    private static String stripLeadingSlash(String value) {
        String stripped = value;
        while (stripped.startsWith("/")) {
            stripped = stripped.substring(1);
        }
        return stripped;
    }

    private static String problemDetail(CompilationProblem problem) {
        StringBuilder detail = new StringBuilder(problem.getStage().name()).append(": ");
        detail.append(problem.getMessage());
        if (problem.getLine() != null) {
            detail.append(" (line ").append(problem.getLine()).append(")");
        }
        if (problem.getThrowable() != null) {
            detail.append(" [").append(problem.getThrowable().getClass().getSimpleName()).append(": ")
                    .append(problem.getThrowable().getMessage()).append("]");
        }
        return detail.toString();
    }

    private record Failure(String objectPath, List<String> details) {
        static Failure fromProblems(String objectPath, List<CompilationProblem> problems) {
            return new Failure(objectPath, problems.stream()
                    .map(RealmsFullCompileScan::problemDetail)
                    .toList());
        }

        static Failure fromThrowable(String objectPath, Throwable throwable) {
            String message = throwable.getMessage();
            return new Failure(
                    objectPath,
                    List.of(throwable.getClass().getSimpleName() + (message == null ? "" : ": " + message)));
        }
    }
}
