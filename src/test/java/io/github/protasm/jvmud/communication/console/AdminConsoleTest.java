package io.github.protasm.jvmud.communication.console;

import static org.junit.jupiter.api.Assertions.*;
import io.github.protasm.jvmud.communication.support.AdminConnection;
import io.github.protasm.jvmud.execution.application.EngineConfiguration;
import io.github.protasm.jvmud.execution.application.JVMud;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class AdminConsoleTest {
    @Test void ownerConsoleUsesDefaultStateWithoutCredentials() throws Exception {
        Path directory = Files.createTempDirectory(Path.of("/tmp"), "jvmud-console-");
        try (JVMud engine = new JVMud(new EngineConfiguration("127.0.0.1", 0, 0,
                directory.resolve(".jvmud/engine-4000"), false))) {
            engine.start();
            String output = runConsole(directory, List.of("--owner"), 0);
            assertTrue(output.contains("Connected to engine."));
            assertTrue(output.contains("Engine player="));
            assertFalse(output.contains("Access token:"));
            assertFalse(output.contains("fingerprint:"));
        } finally {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }

    @Test void positionalConsoleAuthenticatesOverTLSAndRejectsWrongPin() throws Exception {
        Path directory = Files.createTempDirectory(Path.of("/tmp"), "jvmud-console-");
        try (JVMud engine = new JVMud(new EngineConfiguration("127.0.0.1", 0, 0, directory.resolve("state"), false))) {
            engine.start();
            try (AdminConnection local = new AdminConnection(engine.localSocket())) {
                var issued = Pattern.compile("shown once\\): ([0-9a-f]{64})").matcher(local.command("admin-create tester"));
                assertTrue(issued.find());
                Files.writeString(directory.resolve("token"), issued.group(1));
                local.command("grant tester engine");
            }
            String pin = Files.readString(directory.resolve("state/admin-tls.sha256")).trim();
            List<String> arguments = List.of("127.0.0.1", Integer.toString(engine.adminPort()),
                    "--user", "tester", "--token-file", directory.resolve("token").toString(), "--fingerprint", pin);
            String success = runConsole(directory, arguments, 0);
            assertTrue(success.contains("Connected to engine."));
            assertTrue(success.contains("Engine player="));
            var invalidPin = new ArrayList<>(arguments);
            invalidPin.set(invalidPin.size() - 1, "0".repeat(64));
            String rejected = runConsole(directory, invalidPin, 1);
            assertTrue(rejected.contains("Error: Administration certificate fingerprint does not match."));
            assertFalse(rejected.contains("Connected to engine."));
            Files.writeString(directory.resolve("token"), "0".repeat(64));
            String rejectedToken = runConsole(directory, arguments, 1);
            assertTrue(rejectedToken.contains("Error: Authentication or authorization failed."));
            assertFalse(rejectedToken.contains("Connected to engine."));
            assertTrue(runConsole(directory, List.of("--owner"), 1).contains("Error: Cannot connect to the local engine at"));
            assertTrue(runConsole(directory, List.of("--fingerprint", "invalid"), 1).contains("Error: Invalid certificate fingerprint."));
            assertTrue(runConsole(directory, List.of(), 1).contains("Without an interactive terminal"));
        } finally {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }

    /** Runs the actual client with piped commands to exercise scripting without terminal prompts. */
    private static String runConsole(Path directory, List<String> arguments, int expectedExit) throws Exception {
        var command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Duser.home=" + directory,
                "-cp", System.getProperty("java.class.path"), AdminConsole.class.getName()));
        command.addAll(arguments);
        Path output = directory.resolve("console.log");
        Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            try (var input = process.getOutputStream()) { input.write("status\nquit\n".getBytes(StandardCharsets.UTF_8)); }
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "Console did not exit");
            String text = Files.readString(output);
            assertEquals(expectedExit, process.exitValue(), text);
            assertFalse(text.contains("Exception in thread"), text);
            assertFalse(text.contains("Caused by:"), text);
            assertFalse(text.contains("\tat "), text);
            return text;
        } finally { if (process.isAlive()) process.destroyForcibly().waitFor(); }
    }

    @Test void defaultsToLocalTLSAndAcceptsPositionalEndpoints() {
        assertEquals(java.util.Map.of("--host", "localhost", "--port", "4001"), AdminConsole.parseOptions(new String[]{}));
        assertEquals(java.util.Map.of("--host", "server.example", "--port", "4001"), AdminConsole.parseOptions(new String[]{"server.example"}));
        assertEquals(java.util.Map.of("--host", "server.example", "--port", "4401"), AdminConsole.parseOptions(new String[]{"server.example", "4401"}));
        assertEquals("alice", AdminConsole.parseOptions(new String[]{"server.example", "4401", "--user", "alice"}).get("--user"));
        assertEquals("localhost", AdminConsole.parseOptions(new String[]{"--port", "4401"}).get("--host"));
    }

    @Test void preservesExplicitSocketAndScriptOptions() {
        assertEquals(java.util.Map.of("--socket", Path.of(System.getProperty("user.home"), ".jvmud", "engine-4000", "engine.sock").toString()),
                AdminConsole.parseOptions(new String[]{"--owner"}));
        assertEquals("/tmp/engine.sock", AdminConsole.parseOptions(new String[]{"--socket", "/tmp/engine.sock"}).get("--socket"));
        assertEquals("4200", AdminConsole.parseOptions(new String[]{"--host", "remote", "--port", "4200", "--user", "alice", "--fingerprint", "a".repeat(64)}).get("--port"));
    }

    @Test void rejectsAmbiguousEndpointsAndInvalidValues() {
        for (String[] args : new String[][] { {"--socket", "x", "--port", "4100"},
                {"--owner", "--port", "4401"}, {"--owner", "--socket", "x"},
                {"--owner", "--user", "alice"}, {"--owner", "--owner"},
                {"remote", "--owner"}, {"--owner", "remote"},
                {"remote", "--socket", "x"}, {"remote", "--host", "other"},
                {"remote", "4401", "--port", "4402"}, {"remote", "4401", "extra"},
                {"remote", "0"}, {"remote", "65536"}, {"remote", "nonsense"},
                {""}, {"remote", ""}, {"--host", "--port", "4401"},
                {"--fingerprint", "invalid"}, {"--unknown", "x"}, {"--local"},
                {"--port", "0", "--user", "x", "--fingerprint", "a".repeat(64)}, {"--socket"}, {"--socket", "x", "--socket", "y"}})
            assertThrows(IllegalArgumentException.class, () -> AdminConsole.parseOptions(args));
    }
}
