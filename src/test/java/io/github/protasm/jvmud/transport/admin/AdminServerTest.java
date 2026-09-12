package io.github.protasm.jvmud.transport.admin;

import static org.junit.jupiter.api.Assertions.*;

import io.github.protasm.jvmud.instance.MudInstance;
import io.github.protasm.jvmud.instance.InstancePersona;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Exercises real socket authentication, shared live state, and per-server isolation. */
final class AdminServerTest {
    @TempDir Path temp;

    private Path world() throws IOException {
        Path root = temp.resolve("world");
        try (var paths = Files.walk(Path.of("mudlibs/smallmercies"))) {
            for (Path source : paths.toList()) {
                Path destination = root.resolve(Path.of("mudlibs/smallmercies").relativize(source));
                if (Files.isDirectory(source)) Files.createDirectories(destination);
                else Files.copy(source, destination);
            }
        }
        Files.writeString(root.resolve("source/room/square.c"),
                "\nvoid rename_square(string value) { title = value; }\n", StandardOpenOption.APPEND);
        return root;
    }

    @Test
    void commandsChangeThePlayersWorldAndStayIsolatedByPort() throws Exception {
        Path root = world();
        MudInstance first = MudInstance.boot(root, "jvmud/smallmercies.config");
        MudInstance second = MudInstance.boot(root, "jvmud/smallmercies.config");
        Path key1 = temp.resolve("first.token");
        Path key2 = temp.resolve("second.token");
        try (AdminServer a = new AdminServer(first, 0, key1);
                AdminServer b = new AdminServer(second, 0, key2);
                Client client = new Client(a.port(), Files.readString(key1));
                Client other = new Client(b.port(), Files.readString(key2))) {
            assertNotEquals(a.port(), b.port());
            assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(key1));
            StringWriter playerText = new StringWriter();
            PrintWriter playerOut = new PrintWriter(playerText, true);
            InstancePersona player = first.attachPersona(playerOut, "127.0.0.1");
            first.dispatch(player, playerOut, "Alice");
            first.dispatch(player, playerOut, "female");
            first.dispatch(player, playerOut, "warrior");
            assertTrue(client.command("objects").contains("player/adventurer#"));
            assertFalse(client.command("call room/square rename_square Changed").contains("Error:"));
            playerText.getBuffer().setLength(0);
            first.dispatch(player, playerOut, "look");
            assertTrue(playerText.toString().contains("Changed"), playerText.toString());
            assertTrue(other.command("call room/square short").contains("Village Square"));
            assertTrue(client.command("boot anything.config").contains("cannot boot"));
            assertTrue(client.command("call room/square short").contains("Changed"));
            client.command("quit");
            assertFalse(client.running);
            try (Client reconnected = new Client(a.port(), Files.readString(key1))) {
                assertTrue(reconnected.command("call room/square short").contains("Changed"));
                // Reload an unoccupied service/template; all admin sessions refresh registry ids.
                assertTrue(reconnected.command("reload npc/villager").contains("Reloaded"));
                assertTrue(reconnected.command("inspect npc/villager").contains("runtime id:"));
            }
            playerText.getBuffer().setLength(0);
            first.dispatch(player, playerOut, "look");
            assertTrue(playerText.toString().contains("Changed"));
            first.detachPersona(player);
        } finally {
            first.shutdown(0);
            second.shutdown(0);
        }
        assertFalse(Files.exists(key1));
        assertFalse(Files.exists(key2));
    }

    @Test
    void rejectsWrongCredentialsAndMalformedFramesWithoutStoppingListener() throws Exception {
        MudInstance mud = MudInstance.boot(world(), "jvmud/smallmercies.config");
        Path key = temp.resolve("admin.token");
        try (AdminServer server = new AdminServer(mud, 0, key)) {
            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(3000);
                var out = new DataOutputStream(socket.getOutputStream());
                var in = new DataInputStream(socket.getInputStream());
                AdminWire.write(out, AdminWire.VERSION, 128);
                AdminWire.write(out, "wrong", 256);
                assertTrue(AdminWire.read(in, 1024).startsWith("ERROR:"));
                assertEquals(-1, in.read());
            }
            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(3000);
                var out = new DataOutputStream(socket.getOutputStream());
                out.writeInt(Integer.MAX_VALUE);
                out.flush();
                assertEquals(-1, socket.getInputStream().read());
            }
            try (Client client = new Client(server.port(), Files.readString(key))) {
                assertTrue(client.command("objects").contains("room/square"));
            }
        } finally { mud.shutdown(0); }
    }

    @Test
    void credentialCollisionIsNotOverwrittenAndCloseDisconnectsIdleClients() throws Exception {
        MudInstance mud = MudInstance.boot(world(), "jvmud/smallmercies.config");
        Path key = temp.resolve("admin.token");
        AdminServer server = new AdminServer(mud, 0, key);
        try {
            String token = Files.readString(key);
            assertThrows(FileAlreadyExistsException.class, () -> new AdminServer(mud, 0, key));
            assertEquals(token, Files.readString(key));
            try (Client client = new Client(server.port(), token)) {
                server.close();
                assertEquals(-1, client.in.read());
            }
            assertFalse(Files.exists(key));
        } finally { server.close(); mud.shutdown(0); }
    }

    private static final class Client implements AutoCloseable {
        final Socket socket;
        final DataInputStream in;
        final DataOutputStream out;
        boolean running = true;
        Client(int port, String token) throws IOException {
            socket = new Socket("127.0.0.1", port);
            socket.setSoTimeout(5000);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            AdminWire.write(out, AdminWire.VERSION, 128);
            AdminWire.write(out, token, 256);
            assertTrue(AdminWire.read(in, AdminWire.MAX_RESPONSE_BYTES).contains("Connected to live JVMud"));
        }
        String command(String command) throws IOException {
            AdminWire.write(out, command, AdminWire.MAX_COMMAND_BYTES);
            String result = AdminWire.read(in, AdminWire.MAX_RESPONSE_BYTES);
            running = in.readBoolean();
            return result;
        }
        public void close() throws IOException { socket.close(); }
    }
}
