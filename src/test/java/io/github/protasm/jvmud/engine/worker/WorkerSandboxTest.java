package io.github.protasm.jvmud.engine.worker;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WorkerSandboxTest {
    @TempDir Path directory;
    @Test void confinesFilesAndRejectsSymlinkEscape() throws Exception {
        Path root = Files.createDirectory(directory.resolve("mudlib"));
        Path scratch = Files.createDirectory(directory.resolve("scratch"));
        Path state = Files.createDirectory(directory.resolve("engine"));
        Path secret = state.resolve("administrators.json"); Files.writeString(secret, "protected");
        Files.createSymbolicLink(root.resolve("escape"), secret);
        Path classes = Path.of("target/test-classes").toRealPath();
        List<String> java = List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-XX:-UsePerfData",
                "-Djava.io.tmpdir=" + scratch, "-Duser.home=" + scratch, "-cp", classes.toString(), SandboxProbe.class.getName(), root.toString(), secret.toString());
        Process process = new ProcessBuilder(WorkerSandbox.command(root, scratch, state, java, List.of(classes)))
                .directory(root.toFile()).redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(10, TimeUnit.SECONDS));
            String result = new String(process.getInputStream().readAllBytes());
            assertEquals(0, process.exitValue(), result); assertTrue(result.contains("SANDBOX_OK"), result);
            assertEquals("protected", Files.readString(secret));
        } finally { process.destroyForcibly(); }
    }
}
