package io.github.protasm.jvmud;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/** Guards shared production code and launchers against bundled-mudlib coupling. */
final class FlavorNeutralityTest {
    private static final List<String> BUNDLED_MUDLIB_NAMES =
            List.of("lpmuseum", "lp245", "realmsmud", "avelorn");

    @Test
    void sharedProductionCodeAndScriptsDoNotNameBundledMudlibs() throws IOException {
        Path repositoryRoot = repositoryRoot();
        List<String> violations = new ArrayList<>();
        inspectTree(repositoryRoot.resolve("src/main/java"), repositoryRoot, violations);
        inspectTree(repositoryRoot.resolve("scripts"), repositoryRoot, violations);

        assertTrue(
                violations.isEmpty(),
                () -> "Bundled-mudlib references belong under mudlibs/<profile>, not shared code:\n"
                        + String.join("\n", violations));
    }

    private static void inspectTree(Path root, Path repositoryRoot, List<String> violations) throws IOException {
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                String relativePath = repositoryRoot.relativize(path).toString();
                String contents = new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1)
                        .toLowerCase(Locale.ROOT);
                String lowerPath = relativePath.toLowerCase(Locale.ROOT);
                for (String mudlibName : BUNDLED_MUDLIB_NAMES) {
                    if (lowerPath.contains(mudlibName) || contents.contains(mudlibName)) {
                        violations.add(relativePath + " contains " + mudlibName);
                    }
                }
            }
        }
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) && Files.isDirectory(current.resolve("src/main/java"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate JVMud repository root.");
    }
}
