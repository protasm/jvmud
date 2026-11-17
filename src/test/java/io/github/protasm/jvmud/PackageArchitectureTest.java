package io.github.protasm.jvmud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/** Keeps the package directory an accurate map of production responsibilities. */
final class PackageArchitectureTest {
    private static final String ROOT_PACKAGE = "io.github.protasm.jvmud";
    private static final Path SOURCE_ROOT = Path.of("src/main/java");
    private static final Path PROJECT_PACKAGES = SOURCE_ROOT.resolve(ROOT_PACKAGE.replace('.', '/'));
    private static final Pattern PACKAGE = Pattern.compile("(?m)^package ([\\w.]+);");
    private static final Pattern PROJECT_REFERENCE = Pattern.compile(
            "\\bio\\.github\\.protasm\\.jvmud\\.[\\w.]+");

    @Test
    void topLevelPackagesNameTheFourMajorResponsibilities() throws IOException {
        try (var children = Files.list(PROJECT_PACKAGES)) {
            assertEquals(Set.of("language", "engine", "communication", "storage"),
                    children.filter(Files::isDirectory).map(path -> path.getFileName().toString())
                            .collect(java.util.stream.Collectors.toSet()));
        }
    }

    @Test
    void sourcePathsMatchDeclaredPackages() throws IOException {
        for (Path source : productionSources()) {
            var declaration = PACKAGE.matcher(Files.readString(source));
            assertTrue(declaration.find(), () -> "Missing package declaration: " + source);
            assertEquals(SOURCE_ROOT.resolve(declaration.group(1).replace('.', '/')), source.getParent(),
                    () -> "Package and source directory disagree: " + source);
        }
    }

    @Test
    void languageDoesNotDependOnHostingOrAdapters() throws IOException {
        assertDependencies("language", target -> target.startsWith(ROOT_PACKAGE + ".language."),
                "Language code may depend on language contracts, not a hosted world or its adapters.");
    }

    @Test
    void languageTestsExerciseLanguageWithoutBootingTheEngine() throws IOException {
        Path tests = Path.of("src/test/java").resolve(ROOT_PACKAGE.replace('.', '/')).resolve("language");
        assertDependencies(tests, javaSources(tests),
                target -> target.startsWith(ROOT_PACKAGE + ".language."),
                "Tests that assemble worlds or host services belong with engine integration tests.");
    }

    @Test
    void modelDoesNotDependOnConfigurationOrExecution() throws IOException {
        assertDependencies("engine/model", target -> target.startsWith(ROOT_PACKAGE + ".engine.model."),
                "The engine model owns invariants independently of manifests, LPC, and I/O.");
    }

    @Test
    void storageDoesNotDependOnTheRunningEngine() throws IOException {
        assertDependencies("storage", target -> target.startsWith(ROOT_PACKAGE + ".storage."),
                "Storage formats and database adapters must not depend on their engine callers.");
    }

    @Test
    void messagesAndFormattingDoNotDependOnTheirCallers() throws IOException {
        Predicate<String> communication = target -> target.startsWith(ROOT_PACKAGE + ".communication.");
        assertDependencies("communication/protocol", communication,
                "Message contracts are shared by transports and workers, not owned by either.");
        assertDependencies("communication/output", communication,
                "Text formatting must not depend on the manifest that selects its settings.");
    }

    private static void assertDependencies(String directory, Predicate<String> allowed, String explanation)
            throws IOException {
        assertDependencies(PROJECT_PACKAGES.resolve(directory), productionSources(), allowed, explanation);
    }

    private static void assertDependencies(Path inspected, List<Path> sources,
            Predicate<String> allowed, String explanation) throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path source : sources) {
            if (!source.startsWith(inspected)) continue;
            // Package documentation may link upward to explain consumers. Actual source references
            // (including fully qualified names) must follow the direction of ownership.
            String code = Files.readString(source).replaceAll("(?s)/\\*.*?\\*/", "")
                    .replaceAll("(?m)//.*$", "");
            code = PACKAGE.matcher(code).replaceAll("");
            var references = PROJECT_REFERENCE.matcher(code);
            while (references.find()) {
                String target = references.group();
                if (!allowed.test(target)) violations.add(source + " -> " + target);
            }
        }
        assertTrue(violations.isEmpty(), () -> explanation + "\n" + String.join("\n", violations));
    }

    private static List<Path> productionSources() throws IOException {
        return javaSources(PROJECT_PACKAGES);
    }

    private static List<Path> javaSources(Path root) throws IOException {
        try (var files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
    }
}
