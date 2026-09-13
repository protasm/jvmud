package io.github.protasm.jvmud.transport.telnet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;

/** Keeps command-line server diagnostics with the selected mudlib, while retaining console output. */
final class MudlibServerLog {
    private MudlibServerLog() {}
    static void install(Path mudlibRoot, int port) throws IOException {
        Path directory = mudlibRoot.resolve("jvmud/log");
        Files.createDirectories(directory);
        Path file = directory.resolve("server-" + port + ".log");
        if (!Files.exists(file)) Files.createFile(file, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
        PrintStream log = new PrintStream(Files.newOutputStream(file, StandardOpenOption.APPEND), true, StandardCharsets.UTF_8);
        System.setOut(tee(System.out, log));
        System.setErr(tee(System.err, log));
        System.out.println("Server log: " + file.toAbsolutePath());
    }
    private static PrintStream tee(PrintStream console, PrintStream log) {
        return new PrintStream(new OutputStream() {
            @Override public void write(int value) { console.write(value); log.write(value); }
            @Override public void write(byte[] bytes, int offset, int length) { console.write(bytes, offset, length); log.write(bytes, offset, length); }
            @Override public void flush() { console.flush(); log.flush(); }
        }, true, StandardCharsets.UTF_8);
    }
}
