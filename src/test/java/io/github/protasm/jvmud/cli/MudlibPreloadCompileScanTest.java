package io.github.protasm.jvmud.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MudlibPreloadCompileScanTest {
    @TempDir
    Path tempDir;

    @Test
    void scansAnyConfiguredMudlibAndUsesItsIdentityInTheReport() throws Exception {
        Path mudlibRoot = tempDir.resolve("sample");
        Path config = mudlibRoot.resolve("jvmud/sample.config");
        Path sourceRoot = mudlibRoot.resolve("source");
        Path report = tempDir.resolve("sample-report.md");
        Files.createDirectories(config.getParent());
        Files.createDirectories(sourceRoot.resolve("obj"));
        Files.writeString(config, """
                game_id = sample-world
                game_name = Sample World
                mudlib_root = ../source
                preload_file = preload.txt
                database.password_env = SAMPLE_WORLD_DATABASE_PASSWORD
                """);
        Files.writeString(sourceRoot.resolve("preload.txt"), "obj/example.c\n");
        Files.writeString(sourceRoot.resolve("obj/example.c"), "int answer() { return 42; }\n");

        MudlibPreloadCompileScan.main(new String[] {config.toString(), report.toString()});

        String contents = Files.readString(report);
        assertTrue(contents.contains("# Sample World Preload Compile Scan"), contents);
        assertTrue(contents.contains("- Total objects: 1"), contents);
        assertTrue(contents.contains("- Compiled: 1"), contents);
        assertTrue(contents.contains("- Failed: 0"), contents);
    }
}
