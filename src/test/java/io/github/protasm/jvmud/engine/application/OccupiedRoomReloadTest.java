package io.github.protasm.jvmud.engine.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.communication.support.AdminConnection;
import io.github.protasm.jvmud.engine.instance.MudlibSpec;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Live room edits through authenticated administration while two players remain connected. */
final class OccupiedRoomReloadTest {
    private Path directory;
    private Path mudlibRoot;

    @BeforeEach
    void copySmallMercies() throws IOException {
        // The engine's Unix bootstrap socket needs a short path on macOS.
        directory = Files.createTempDirectory(Path.of("/tmp"), "jvmud-reload-");
        mudlibRoot = directory.resolve("smallmercies");
        Path original = Path.of("mudlibs/smallmercies");
        try (var sources = Files.walk(original)) {
            for (Path source : sources.toList()) {
                Path destination = mudlibRoot.resolve(original.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(source, destination);
                }
            }
        }
    }

    @AfterEach
    void removeDisposableWorld() throws IOException {
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    @Test
    void behaviorReloadPreservesBothPlayersAndExistingResidents() throws Exception {
        try (JVMud engine = engine()) {
            engine.start();
            MudlibStatus world = engine.startMudlib(new MudlibSpec(mudlibRoot, "jvmud/smallmercies.config"), 0, 0);
            assertNotEquals(ProcessHandle.current().pid(), world.pid());
            try (AdminConnection admin = administration(engine, world);
                    Guest alice = login(world.playerPort(), "Alice");
                    Guest bob = login(world.playerPort(), "Bob")) {
                assertOccupiedSquare(alice, "Bob", "Village Square");
                assertOccupiedSquare(bob, "Alice", "Village Square");
                // Change executable prose, rather than fields set by create(): reload preserves live state.
                Path room = mudlibRoot.resolve("source/lib/room.c");
                Files.writeString(room, Files.readString(room).replace("Exits: ", "Freshly painted exits: "));

                String result = admin.command("reload room/square");

                assertTrue(result.contains("Reloaded room/square"), result);
                assertOccupiedSquare(alice, "Bob", "Freshly painted exits: ");
                assertOccupiedSquare(bob, "Alice", "Freshly painted exits: ");
                assertCanVisitGardenAndReturn(alice, "Freshly painted exits: ");
                assertCanVisitGardenAndReturn(bob, "Freshly painted exits: ");
            }
        }
    }

    @Test
    void failedReloadsKeepTheWorkingRoomAvailableToPlayersAndAdministration() throws Exception {
        try (JVMud engine = engine()) {
            engine.start();
            MudlibStatus world = engine.startMudlib(new MudlibSpec(mudlibRoot, "jvmud/smallmercies.config"), 0, 0);
            try (AdminConnection admin = administration(engine, world);
                    Guest alice = login(world.playerPort(), "Alice");
                    Guest bob = login(world.playerPort(), "Bob")) {
                assertOccupiedSquare(alice, "Bob", "Village Square");
                assertOccupiedSquare(bob, "Alice", "Village Square");
                Path square = mudlibRoot.resolve("source/room/square.c");
                String workingSource = Files.readString(square);
                Files.writeString(square, "void create( {\n");

                String result = admin.command("reload room/square");

                assertTrue(result.contains("Error:"), result);
                assertTrue(admin.command("call room/square short").contains("Village Square"));
                assertOccupiedSquare(alice, "Bob", "Village Square");
                assertOccupiedSquare(bob, "Alice", "Village Square");
                assertCanVisitGardenAndReturn(alice, "Village Square");
                assertCanVisitGardenAndReturn(bob, "Village Square");

                // This source compiles, but a new field cannot replace the live object's layout.
                Files.writeString(square, "int renovation_budget;\n" + workingSource);
                String incompatible = admin.command("reload room/square");
                assertTrue(incompatible.contains("Error:"), incompatible);
                assertTrue(admin.command("call room/square short").contains("Village Square"));
                assertOccupiedSquare(alice, "Bob", "Village Square");
                assertOccupiedSquare(bob, "Alice", "Village Square");
                assertCanVisitGardenAndReturn(alice, "Village Square");
                assertCanVisitGardenAndReturn(bob, "Village Square");
            }
        }
    }

    private JVMud engine() {
        return new JVMud(new EngineConfiguration("127.0.0.1", 0, 0, directory.resolve("engine"), false));
    }

    /** Creates a mudlib-scoped token locally, then authenticates on the worker's TLS endpoint. */
    private AdminConnection administration(JVMud engine, MudlibStatus world) throws Exception {
        try (AdminConnection local = new AdminConnection(engine.localSocket())) {
            String issued = local.command("admin-create editor");
            String token = issued.substring(issued.indexOf(": ") + 2).trim();
            assertEquals("Grant updated.\n", local.command("grant editor mudlib:smallmercies"));
            String fingerprint = Files.readString(directory.resolve("engine/admin-tls.sha256")).trim();
            AdminConnection admin = new AdminConnection(world.adminPort(), fingerprint, "editor", token);
            assertEquals("mudlib:smallmercies", admin.greeting);
            return admin;
        }
    }

    private static Guest login(int port, String name) throws IOException {
        Guest guest = new Guest(new Socket("127.0.0.1", port));
        try {
            guest.socket().setSoTimeout(8000);
            guest.readUntil("Name (2-16 letters): ");
            guest.send(name);
            guest.readUntil("Gender (male/female): ");
            guest.send("female");
            guest.readUntil("Class (warrior/mage): ");
            assertTrue(guest.command("warrior", "Welcome, ").contains("Village Square"));
            return guest;
        } catch (IOException | RuntimeException | Error failure) {
            guest.close();
            throw failure;
        }
    }

    private static void assertOccupiedSquare(Guest player, String otherPlayer, String expectedRoomText) throws IOException {
        String view = player.command("look", "Village Square");
        assertTrue(view.contains(expectedRoomText), view);
        assertTrue(view.contains(otherPlayer + " is here."), view);
        assertEquals(1, view.split("mayor is here\\.", -1).length - 1, view);
    }

    private static void assertCanVisitGardenAndReturn(Guest player, String expectedRoomText) throws IOException {
        String garden = player.command("north", "Municipal Herb Garden");
        assertTrue(garden.contains("Municipal Herb Garden"), garden);
        String square = player.command("south", "Village Square");
        assertTrue(square.contains(expectedRoomText), square);
    }

    /** Uses complete command prompts as boundaries, without timing-dependent quiet periods. */
    private record Guest(Socket socket) implements AutoCloseable {
        void send(String line) throws IOException {
            socket.getOutputStream().write((line + "\n").getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
        }

        String command(String line, String responseMarker) throws IOException {
            send(line);
            // Other players can cause output and a prompt while this client is idle.
            // First wait for this command's response, then consume its complete prompt.
            return readUntil(responseMarker) + readUntil("> ");
        }

        String readUntil(String marker) throws IOException {
            StringBuilder text = new StringBuilder();
            while (text.indexOf(marker) < 0) {
                int next = socket.getInputStream().read();
                if (next == -1) {
                    throw new EOFException(text.toString());
                }
                text.append((char) next);
            }
            return text.toString();
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
