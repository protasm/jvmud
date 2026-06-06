package io.github.protasm.jvmud.compiler.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class LPCFormatterCliTest {
    @TempDir
    Path tempDir;

    @Test
    void formatsFilesInPlace() throws Exception {
        Path source = tempDir.resolve("object.c");
        Files.writeString(source, """
                zed()
                {
                \treturn 2;
                }
                alpha()
                {
                \treturn 1;
                }
                """);

        CommandResult result = run(source.toString());

        assertEquals(0, result.exitCode());
        assertTrue(result.out().contains("formatted " + source));
        assertEquals("""
                alpha() {
                  return 1;
                }

                zed() {
                  return 2;
                }
                """, Files.readString(source));
    }

    @Test
    void printsUsageWhenNoFilesAreProvided() {
        CommandResult result = run();

        assertEquals(2, result.exitCode());
        assertEquals("Usage: ./jvmud-format <file> [file...]\n", result.err());
    }

    @Test
    void reportsMissingFiles() {
        Path source = tempDir.resolve("missing.c");

        CommandResult result = run(source.toString());

        assertEquals(1, result.exitCode());
        assertEquals("Not a file: " + source + "\n", result.err());
    }

    private CommandResult run(String... args) {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(outBytes, true);
        PrintWriter err = new PrintWriter(errBytes, true);

        int exitCode = new LPCFormatterCli(out, err, new LPCFormatter()).run(args);

        return new CommandResult(exitCode, outBytes.toString(), errBytes.toString());
    }

    private record CommandResult(int exitCode, String out, String err) {
    }
}
