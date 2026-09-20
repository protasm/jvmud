package io.github.protasm.jvmud.execution.application.worker;

import java.nio.file.*;

/** Standalone child probe: protected file contents must remain unavailable even through a mudlib symlink. */
public final class SandboxProbe {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]), secret = Path.of(args[1]);
        Files.writeString(root.resolve("allowed.txt"), "mudlib persistence");
        if (!Files.readString(root.resolve("allowed.txt")).equals("mudlib persistence")) throw new AssertionError();
        denied(() -> Files.readString(secret));
        denied(() -> Files.writeString(secret, "overwritten"));
        denied(() -> Files.readString(root.resolve("escape")));
        System.out.println("SANDBOX_OK");
    }
    private static void denied(Operation operation) throws Exception {
        try { operation.run(); } catch (java.io.IOException | SecurityException expected) { return; }
        throw new AssertionError("Sandbox allowed protected access");
    }
    @FunctionalInterface private interface Operation { void run() throws Exception; }
}
