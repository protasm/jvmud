package io.github.protasm.jvmud.communication.console;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AdminConsoleTest {
    @Test void requiresExplicitEndpointAndTrust() {
        assertEquals("/tmp/engine.sock", AdminConsole.parseOptions(new String[]{"--socket", "/tmp/engine.sock"}).get("--socket"));
        assertEquals("4200", AdminConsole.parseOptions(new String[]{"--host", "remote", "--port", "4200", "--user", "alice", "--fingerprint", "a".repeat(64)}).get("--port"));
        for (String[] args : new String[][] { {}, {"--port", "4001"}, {"--socket", "x", "--port", "4100"},
                {"--port", "0", "--user", "x", "--fingerprint", "a".repeat(64)}, {"--socket"}, {"--socket", "x", "--socket", "y"}})
            assertThrows(IllegalArgumentException.class, () -> AdminConsole.parseOptions(args));
    }
}
