package io.github.protasm.jvmud.language.formatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LPCFormatterCLI {
    private final PrintWriter out;
    private final PrintWriter err;
    private final LPCFormatter formatter;

    public LPCFormatterCLI(PrintWriter out, PrintWriter err, LPCFormatter formatter) {
        this.out = out;
        this.err = err;
        this.formatter = formatter;
    }

    public static void main(String[] args) {
        int exitCode = new LPCFormatterCLI(
                new PrintWriter(System.out, true),
                new PrintWriter(System.err, true),
                new LPCFormatter())
                .run(args);

        if (exitCode != 0)
            System.exit(exitCode);
    }

    int run(String[] args) {
        if (args.length == 0 || isHelp(args[0])) {
            usage(args.length == 0 ? err : out);
            return args.length == 0 ? 2 : 0;
        }

        boolean failed = false;
        for (String arg : args) {
            Path target = Path.of(arg);
            if (!Files.isRegularFile(target)) {
                err.println("Not a file: " + arg);
                failed = true;
                continue;
            }

            try {
                formatter.formatFile(target);
                out.println("formatted " + target);
            } catch (IOException e) {
                err.println("Could not format " + arg + ": " + e.getMessage());
                failed = true;
            }
        }

        return failed ? 1 : 0;
    }

    private boolean isHelp(String arg) {
        return "-h".equals(arg) || "--help".equals(arg) || "help".equals(arg);
    }

    private void usage(PrintWriter writer) {
        writer.println("Usage: scripts/jvmud-format <file> [file...]");
    }
}
