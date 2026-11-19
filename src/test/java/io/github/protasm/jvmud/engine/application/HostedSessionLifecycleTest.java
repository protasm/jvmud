package io.github.protasm.jvmud.engine.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.communication.support.AdminConnection;
import io.github.protasm.jvmud.engine.instance.MudlibSpec;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Exercises one connection across managed login, LPC attachment, rebind and disconnect. */
final class HostedSessionLifecycleTest {
    private static final String WILL_ECHO = "\u00ff\u00fb\u0001";
    private static final String WONT_ECHO = "\u00ff\u00fc\u0001";
    private static final String GMCP_START = "\u00ff\u00fa\u00c9";
    private static final String GMCP_END = "\u00ff\u00f0";
    private Path directory;
    private Path mudlibRoot;

    @BeforeEach
    void createDisposableMudlib() throws IOException {
        // macOS limits the length of the same-account administration socket path.
        directory = Files.createTempDirectory(Path.of("/tmp"), "jvmud-session-");
        mudlibRoot = directory.resolve("world");
        Files.createDirectories(mudlibRoot.resolve("jvmud"));
        Files.createDirectories(mudlibRoot.resolve("obj"));
        Files.createDirectories(mudlibRoot.resolve("room"));
        write("jvmud/session.config", """
                game_id = sessions
                game_name = Session Test
                session_policy = filesystem_accounts
                player_object = obj/controller
                initial_place = room/start
                preload_objects = obj/audit
                engine_capabilities = session_control
                player_prompt = "> "
                show_ruler = false
                lifecycle.player_persona_resolved = begin_body_selection
                lifecycle.player_session_post_rebind = connected_body
                lifecycle.player_session_disconnected = disconnected
                lifecycle.interaction_scope_started = offer_interactions
                """);
        write("room/start.c", "string short() { return \"Session laboratory\"; }");
        write("obj/audit.c", """
                object controller;
                object avatar;
                int disconnects;

                void remember_controller(object value) { controller = value; }
                void remember_avatar(object value) { avatar = value; }
                void disconnected() { disconnects += 1; }
                int users_count() { return jvmud_size(jvmud_users()); }
                int controller_interactive() { if (jvmud_interactive(controller)) return 1; return 0; }
                int avatar_interactive() { if (jvmud_interactive(avatar)) return 1; return 0; }
                int avatar_gmcp() { if (jvmud_gmcp_enabled(avatar)) return 1; return 0; }
                int disconnect_count() { return disconnects; }
                string avatar_location() { return jvmud_lpc_object_id(jvmud_entity_location(avatar)); }
                mixed avatar_address() { return jvmud_query_ip_number(avatar); }
                """);
        write("obj/controller.c", """
                void begin_body_selection(string user_id, string name, string gender, mapping attributes) {
                    jvmud_invoke_lpc_object("obj/audit", "remember_controller", jvmud_current_lpc_object());
                    jvmud_write("Choose body: ");
                    jvmud_capture_session_input("choose_body", 0);
                }

                void choose_body(string selection) {
                    object body;
                    body = jvmud_clone_lpc_object("obj/avatar");
                    jvmud_move_entity(body, jvmud_entity_location());
                    jvmud_invoke_lpc_object("obj/audit", "remember_avatar", body);
                    jvmud_rebind_session_lpc_object(body, jvmud_current_lpc_object());
                }
                """);
        write("obj/avatar.c", """
                void connected_body() {
                    if (jvmud_gmcp_enabled()) jvmud_write("Protocol retained.\\n");
                    jvmud_write("Secret phrase: ");
                    jvmud_capture_session_input("receive_phrase", 1, "selected body");
                }

                void offer_interactions() {
                    jvmud_enable_commands();
                    jvmud_add_action("ping", "ping");
                }

                int ping() { jvmud_write("avatar pong\\n"); return 1; }

                void receive_phrase(string phrase, string label) {
                    jvmud_write("Captured " + label + ":" + phrase + "\\n");
                    jvmud_send_gmcp("Test.Captured", ([ "phrase": phrase ]));
                }

                void receive_gmcp(string package_name, mixed payload) {
                    if (package_name == "Client.Probe") {
                        jvmud_write("GMCP reached the selected body.\\n");
                        jvmud_send_gmcp("Test.Reply", payload);
                    }
                }

                void disconnected() {
                    jvmud_invoke_lpc_object("obj/audit", "disconnected");
                }
                """);
    }

    @AfterEach
    void removeDisposableMudlib() throws IOException {
        if (directory == null) return;
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    @Test
    void oneConnectionKeepsItsInputAndProtocolStateAcrossAttachmentAndRebind() throws Exception {
        try (JVMud engine = new JVMud(new EngineConfiguration(
                "127.0.0.1", 0, 0, directory.resolve("engine"), false))) {
            engine.start();
            MudlibStatus world = engine.startMudlib(new MudlibSpec(mudlibRoot, "jvmud/session.config"), 0, 0);
            assertNotEquals(ProcessHandle.current().pid(), world.pid());
            try (AdminConnection admin = administration(engine, world)) {
                try (Client client = new Client(world.playerPort())) {
                    String greeting = client.until("Please enter your user ID: ");
                    assertTrue(greeting.contains("\u00ff\u00fb\u00c9"), greeting);
                    assertValue(admin, "users_count", 0);

                    // Negotiate before a Persona exists: this state belongs to the connection.
                    client.negotiateGMCP();
                    client.command("sessiontest", "Create it? (yes/no) ");
                    client.command("yes", WILL_ECHO);
                    client.command("Passw0rd!", "Password again: ");
                    client.command("Passw0rd!", WONT_ECHO);
                    client.command("", "Persona name: ");
                    client.command("Ada", "Gender (female/male/neutral/none/other): ");
                    client.command("other", "Choose body: ");
                    assertValue(admin, "controller_interactive", 1);
                    assertValue(admin, "users_count", 0);

                    String selected = client.command("enter", WILL_ECHO);
                    assertTrue(selected.contains("Protocol retained."), selected);
                    assertTrue(selected.contains("Secret phrase: "), selected);
                    assertValue(admin, "controller_interactive", 0);
                    assertValue(admin, "avatar_interactive", 1);
                    assertValue(admin, "avatar_gmcp", 1);
                    assertTrue(admin.command("call obj/audit avatar_location").contains("=> room/start"));
                    assertTrue(admin.command("call obj/audit avatar_address").contains("=> 127.0.0.1"));

                    String captured = client.command("violet", WONT_ECHO);
                    assertTrue(captured.contains("Captured selected body:violet"), captured);
                    assertTrue(captured.contains(GMCP_START + "Test.Captured {\"phrase\":\"violet\"}" + GMCP_END), captured);
                    assertValue(admin, "users_count", 1);
                    assertTrue(client.command("ping", "> ").contains("avatar pong"));

                    client.sendGMCP("Client.Probe {\"connection\":\"retained\"}");
                    String protocolReply = client.until(GMCP_END);
                    assertTrue(protocolReply.contains("GMCP reached the selected body."), protocolReply);
                    assertTrue(protocolReply.contains(
                            GMCP_START + "Test.Reply {\"connection\":\"retained\"}" + GMCP_END), protocolReply);

                    client.send("//quit");
                    assertFalse(client.toEnd().contains("Something goes wrong."));
                }
                awaitValue(admin, "disconnect_count", 1);
                assertValue(admin, "users_count", 0);
                assertValue(admin, "avatar_interactive", 0);
                assertValue(admin, "avatar_gmcp", 0);

                // Closing a connection during managed login creates no extra Persona disconnect.
                try (Client next = new Client(world.playerPort())) {
                    next.until("Please enter your user ID: ");
                    assertValue(admin, "users_count", 0);
                }
                assertValue(admin, "disconnect_count", 1);
            }
        }
    }

    private void write(String relativePath, String source) throws IOException {
        Files.writeString(mudlibRoot.resolve(relativePath), source);
    }

    private AdminConnection administration(JVMud engine, MudlibStatus world) throws Exception {
        try (AdminConnection local = new AdminConnection(engine.localSocket())) {
            String issued = local.command("admin-create lifecycle");
            String token = issued.substring(issued.indexOf(": ") + 2).trim();
            assertEquals("Grant updated.\n", local.command("grant lifecycle mudlib:sessions"));
            String fingerprint = Files.readString(directory.resolve("engine/admin-tls.sha256")).trim();
            AdminConnection admin = new AdminConnection(world.adminPort(), fingerprint, "lifecycle", token);
            assertEquals("mudlib:sessions", admin.greeting);
            return admin;
        }
    }

    private static void assertValue(AdminConnection admin, String method, int expected) throws IOException {
        String result = admin.command("call obj/audit " + method);
        assertTrue(result.contains("=> " + expected + "\n"), result);
    }

    private static void awaitValue(AdminConnection admin, String method, int expected) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        String result;
        do {
            result = admin.command("call obj/audit " + method);
            if (result.contains("=> " + expected + "\n")) return;
            Thread.sleep(10);
        } while (System.nanoTime() < deadline);
        assertTrue(result.contains("=> " + expected + "\n"), result);
    }

    /** Keeps Telnet bytes visible so input-echo and GMCP boundaries are explicit assertions. */
    private static final class Client implements AutoCloseable {
        private final Socket socket;

        private Client(int port) throws IOException {
            socket = new Socket("127.0.0.1", port);
            socket.setSoTimeout(8000);
        }

        private void send(String line) throws IOException {
            socket.getOutputStream().write((line + "\n").getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
        }

        private String command(String line, String marker) throws IOException {
            send(line);
            return until(marker);
        }

        private void negotiateGMCP() throws IOException {
            socket.getOutputStream().write(new byte[] {(byte) 255, (byte) 253, (byte) 201});
            socket.getOutputStream().flush();
        }

        private void sendGMCP(String message) throws IOException {
            socket.getOutputStream().write(new byte[] {(byte) 255, (byte) 250, (byte) 201});
            socket.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().write(new byte[] {(byte) 255, (byte) 240});
            socket.getOutputStream().flush();
        }

        private String until(String marker) throws IOException {
            StringBuilder text = new StringBuilder();
            while (text.indexOf(marker) < 0) {
                int next = socket.getInputStream().read();
                if (next == -1) throw new EOFException(text.toString());
                text.append((char) next);
            }
            return text.toString();
        }

        private String toEnd() throws IOException {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            socket.getInputStream().transferTo(bytes);
            return bytes.toString(StandardCharsets.ISO_8859_1);
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
