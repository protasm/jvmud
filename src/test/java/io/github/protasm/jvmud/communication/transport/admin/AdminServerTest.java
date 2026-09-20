package io.github.protasm.jvmud.communication.transport.admin;

import static org.junit.jupiter.api.Assertions.*;
import io.github.protasm.jvmud.communication.admin.AdminSession;
import io.github.protasm.jvmud.storage.admin.AdminRegistry;
import io.github.protasm.jvmud.communication.support.AdminConnection;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Real TLS authentication, scoped grants, certificate trust and idle-session revocation. */
final class AdminServerTest {
    @TempDir Path temp;
    @Test void authenticatesScopesAndRevokesAnIdleSession() throws Exception {
        var registry = new AdminRegistry(temp);
        String token = registry.create("alice"); registry.grant("alice", "mudlib:first", true);
        var tls = AdminTLS.server(temp);
        String fingerprint = Files.readString(temp.resolve("admin-tls.sha256")).trim();
        try (AdminServer first = new AdminServer("127.0.0.1", 0, tls, registry, "mudlib:first", () -> session("mudlib:first"));
             AdminServer second = new AdminServer("127.0.0.1", 0, tls, registry, "engine", () -> session("engine"))) {
            first.start(); second.start();
            try (var allowed = new AdminConnection(first.port(), fingerprint, "alice", token);
                 var denied = new AdminConnection(second.port(), fingerprint, "alice", token);
                 var wrong = new AdminConnection(first.port(), fingerprint, "alice", "wrong")) {
                assertEquals("mudlib:first", allowed.greeting);
                assertTrue(denied.greeting.startsWith("ERROR:")); assertTrue(wrong.greeting.startsWith("ERROR:"));
                assertEquals("ok: hello", allowed.command("hello"));
                registry.grant("alice", "mudlib:first", false);
                assertEquals(-1, allowed.input.read(), "Revocation must disconnect an idle authenticated session");
            }
            assertThrows(Exception.class, () -> new AdminConnection(first.port(), "0".repeat(64), "alice", token));
            assertFalse(Files.readString(temp.resolve("administrators.json")).contains(token));
            var reloaded = new AdminRegistry(temp);
            assertTrue(reloaded.authenticate("alice", token)); assertFalse(reloaded.allows("alice", token, "mudlib:first"));
            String replacement = registry.rotate("alice");
            assertFalse(registry.authenticate("alice", token)); assertTrue(registry.authenticate("alice", replacement));
        }
    }
    private static AdminSession session(String scope) {
        return new AdminSession() {
            public String scope() { return scope; }
            public Reply execute(String command) { return new Reply("ok: " + command, !command.equals("quit")); }
        };
    }
}
