package io.github.protasm.jvmud.execution.application;

import static org.junit.jupiter.api.Assertions.*;
import io.github.protasm.jvmud.execution.instance.MudlibSpec;
import io.github.protasm.jvmud.communication.support.AdminConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.*;

/** End-to-end engine supervision using actual separate worker JVMs and authenticated network connections. */
class EngineArchitectureTest {
    private Path directory;
    @BeforeEach void setup() throws IOException { directory = Files.createTempDirectory(Path.of("/tmp"), "jvmud-it-"); }
    @AfterEach void cleanup() throws IOException {
        try (var paths = Files.walk(directory)) { for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path); }
    }
    private JVMud engine() { return new JVMud(new EngineConfiguration("127.0.0.1", 0, 0, directory.resolve("engine"), false)); }

    @Test void directoryShowsDetailsAndBlurbWithoutCreatingPlayerSessions() throws Exception {
        try (JVMud engine = new JVMud(new EngineConfiguration("127.0.0.1", 0, 0,
                directory.resolve("engine"), false, directory, "play.jvmud.org"))) {
            engine.start();
            var mudlib = engine.startMudlib(spec("sample"), 0, 0);
            try (Socket player = player(engine.port())) {
                String menu = readUntil(player, "Q to quit: ");
                assertTrue(menu.contains("1. sample [sample]"));
                assertFalse(menu.contains("Telnet:"));
                Path description = directory.resolve("engine/descriptions/sample.txt");
                Files.createDirectories(description.getParent());
                Files.writeString(description, "A friendly world.\nCome explore!");
                send(player, "1\n");
                String details = readUntil(player, "Q to quit: ");
                assertTrue(details.contains("Telnet: play.jvmud.org:" + mudlib.playerPort()));
                assertTrue(details.contains("A friendly world.\r\nCome explore!"));
                assertFalse(details.contains("play.jvmud.org:" + mudlib.adminPort()));
                assertFalse(details.contains("LOGIN"));
                Files.writeString(description, "Updated description.");
                send(player, "1\n");
                assertTrue(readUntil(player, "Q to quit: ").contains("Updated description."));
                for (String command : List.of("l", "L")) {
                    send(player, command + "\n");
                    assertTrue(readUntil(player, "Q to quit: ").contains("1. sample [sample]"));
                }
                send(player, "999\n");
                assertTrue(readUntil(player, "Q to quit: ").contains("Please enter"));
                engine.stopMudlib("sample");
                send(player, "1\n");
                assertTrue(readUntil(player, "Q to quit: ").contains("no longer available"));
                send(player, "L\n");
                assertTrue(readUntil(player, "Q to quit: ").contains("No mudlibs"));
                send(player, "q\n");
                assertTrue(readToEnd(player).contains("Goodbye!"));
            }
        }
    }

    @Test void administrationStartsCatalogNamesAndConfinesLocalAndRemoteRequests() throws Exception {
        MudlibSpec original = spec("sample");
        Path catalog = directory.resolve("installed");
        Files.createDirectories(catalog);
        Path root = catalog.resolve("sample");
        Files.move(original.root(), root);
        Files.createDirectories(root.resolve("jvmud"));
        Path manifest = root.resolve("jvmud/sample.config");
        Files.move(root.resolve("mudlib.config"), manifest);
        try (JVMud engine = new JVMud(new EngineConfiguration("127.0.0.1", 0, 0, directory.resolve("engine"), false, catalog))) {
            engine.start();
            try (AdminConnection local = new AdminConnection(engine.localSocket())) {
                String token = token(local.command("admin-create operator"));
                local.command("grant operator engine");
                try (AdminConnection remote = new AdminConnection(engine.adminPort(), pin(), "operator", token)) {
                    for (AdminConnection connection : List.of(local, remote)) {
                        assertEquals("sample\n", connection.command("available"));
                        assertTrue(connection.command("start " + manifest).startsWith("Error:"));
                        assertTrue(connection.command("start ../sample").startsWith("Error:"));
                        assertTrue(connection.command("start missing").startsWith("Error:"));
                    }
                    String defaultStart = remote.command("start sample");
                    assertEquals(4100, engine.mudlibs().getFirst().playerPort());
                    assertEquals(4101, engine.mudlibs().getFirst().adminPort());
                    assertTrue(defaultStart.contains("state=RUNNING") || defaultStart.startsWith("Error: Mudlib ports 4100/4101 could not be bound;"), defaultStart);
                    local.command("stop sample");
                    // Both occupied endpoint positions must fail without taking down the engine.
                    for (String ports : List.of(engine.port() + " 0", "0 " + engine.adminPort())) {
                        String rejected = remote.command("start sample " + ports);
                        assertTrue(rejected.startsWith("Error: Mudlib ports "), rejected);
                        assertTrue(rejected.contains("Supply different player and admin ports."));
                        assertEquals(-1, engine.mudlibs().getFirst().pid());
                        assertTrue(local.command("status").contains("Engine player="));
                    }
                    assertTrue(remote.command("start sample 0 0").contains("state=RUNNING"));
                    var running = engine.mudlibs().getFirst();
                    try (Socket player = player(running.playerPort())) { assertTrue(readUntil(player, "ready>").contains("LOGIN sample")); }
                    assertTrue(local.command("restart sample").contains("state=RUNNING"));
                    Path outside = directory.resolve("outside.config");
                    Files.copy(manifest, outside);
                    Files.delete(manifest);
                    Files.createSymbolicLink(manifest, outside);
                    for (AdminConnection connection : List.of(local, remote)) {
                        assertTrue(connection.command("restart sample").startsWith("Error:"));
                        assertEquals("No mudlibs are available to start.\n", connection.command("available"));
                    }
                    assertEquals(MudlibStatus.State.RUNNING, engine.mudlibs().getFirst().state());
                    local.command("stop sample");
                    assertTrue(remote.command("start sample").startsWith("Error:"));
                }
            }
        }
    }

    @Test void emptyEngineProvidesDirectoryLocalBootstrapAndRemoteEngineConsole() throws Exception {
        try (JVMud engine = engine()) {
            engine.start();
            try (Socket player = player(engine.port())) {
                assertTrue(readUntil(player, "Q to quit: ").contains("No mudlibs"));
                send(player, "Q\n"); assertTrue(readToEnd(player).contains("Goodbye!"));
            }
            try (AdminConnection local = new AdminConnection(engine.localSocket())) {
                assertEquals("engine", local.greeting);
                String token = token(local.command("admin-create operator"));
                assertEquals("Grant updated.\n", local.command("grant operator engine"));
                try (AdminConnection remote = new AdminConnection(engine.adminPort(), pin(), "operator", token)) {
                    assertEquals("engine", remote.greeting); assertTrue(remote.command("status").contains("Engine player="));
                }
            }
            assertTrue(engine.mudlibs().isEmpty());
        }
    }

    @Test void workerPortsShareLiveStateAndCrashLeavesEngineAndOtherMudlibAlive() throws Exception {
        try (JVMud engine = engine()) {
            engine.start();
            var first = engine.startMudlib(spec("first"), 0, 0);
            var second = engine.startMudlib(spec("second"), 0, 0);
            assertNotEquals(ProcessHandle.current().pid(), first.pid()); assertNotEquals(first.pid(), second.pid());
            assertNotEquals(first.playerPort(), second.playerPort()); assertNotEquals(first.playerPort(), first.adminPort());
            try (AdminConnection local = new AdminConnection(engine.localSocket())) {
                String token = token(local.command("admin-create helper")); local.command("grant helper mudlib:first");
                try (AdminConnection admin = new AdminConnection(first.adminPort(), pin(), "helper", token);
                     Socket direct = player(first.playerPort()); Socket menu = player(engine.port())) {
                    assertEquals("mudlib:first", admin.greeting);
                    String login = readUntil(direct, "ready>"); assertTrue(login.contains("LOGIN first")); assertFalse(login.contains("Select a mudlib"));
                    String choices = readUntil(menu, "Q to quit: ");
                    assertTrue(choices.contains("1. first [first]"));
                    assertTrue(choices.contains("2. second [second]"));
                    send(menu, "1\n");
                    String details = readUntil(menu, "Q to quit: ");
                    assertTrue(details.contains("Telnet: same host, port " + first.playerPort()));
                    assertFalse(details.contains("LOGIN"));
                    assertTrue(admin.command("call hub query_connections").contains("1"));
                    send(menu, "Q\n"); assertTrue(readToEnd(menu).contains("Goodbye!"));
                    try (var denied = new AdminConnection(engine.adminPort(), pin(), "helper", token)) { assertTrue(denied.greeting.startsWith("ERROR:")); }
                    ProcessHandle.of(first.pid()).orElseThrow().destroyForcibly();
                    awaitState(engine, "first", MudlibStatus.State.FAILED);
                    assertEquals(-1, direct.getInputStream().read());
                    assertTrue(local.command("status").contains("FAILED"));
                    try (Socket survivor = player(second.playerPort())) { assertTrue(readUntil(survivor, "ready>").contains("LOGIN second")); }
                    var restarted = engine.restartMudlib("first"); assertEquals(first.playerPort(), restarted.playerPort()); assertNotEquals(first.pid(), restarted.pid());
                    engine.stopMudlib("first"); assertEquals(MudlibStatus.State.STOPPED, engine.mudlibs().getFirst().state());
                }
            }
        }
    }

    @Test void failedBootAndPortCollisionLeaveEngineUsableAndReleaseWorkers() throws Exception {
        try (JVMud engine = engine()) {
            engine.start();
            MudlibSpec broken = spec("broken"); Files.writeString(broken.root().resolve("room.c"), "invalid LPC");
            assertThrows(IOException.class, () -> engine.startMudlib(broken, 0, 0));
            assertEquals(MudlibStatus.State.FAILED, engine.mudlibs().getFirst().state());
            try (ServerSocket occupied = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))) {
                assertThrows(IOException.class, () -> engine.startMudlib(spec("collision"), 0, occupied.getLocalPort()));
                assertEquals(-1, engine.mudlibs().getLast().pid());
            }
            assertEquals(MudlibStatus.State.RUNNING, engine.startMudlib(spec("good"), 0, 0).state());
            try (var local = new AdminConnection(engine.localSocket())) { assertTrue(local.command("mudlibs").contains("good")); }
        }
    }

    @Test void registryAndTLSIdentitySurviveEngineRestart() throws Exception {
        String token;
        String fingerprint;
        try (JVMud first = engine()) {
            first.start(); fingerprint = pin();
            try (var local = new AdminConnection(first.localSocket())) {
                token = token(local.command("admin-create persistent")); local.command("grant persistent engine");
            }
        }
        try (JVMud second = engine()) {
            second.start(); assertEquals(fingerprint, pin());
            try (var remote = new AdminConnection(second.adminPort(), pin(), "persistent", token)) {
                assertEquals("engine", remote.greeting); assertTrue(remote.command("status").contains("Engine player="));
            }
        }
    }

    @Test void outOfMemoryTerminatesOnlyTheOffendingWorker() throws Exception {
        try (JVMud engine = engine()) {
            engine.start();
            MudlibSpec hungry = spec("hungry");
            Files.writeString(hungry.root().resolve("hub.c"), "\nvoid exhaust() { string value = \"0123456789\"; while (1) { value = value + value; } }\n", StandardOpenOption.APPEND);
            var status = engine.startMudlib(hungry, 0, 0);
            var healthy = engine.startMudlib(spec("healthy"), 0, 0);
            try (var local = new AdminConnection(engine.localSocket())) {
                String token = token(local.command("admin-create tester")); local.command("grant tester mudlib:hungry");
                try (var admin = new AdminConnection(status.adminPort(), pin(), "tester", token)) {
                    assertThrows(IOException.class, () -> admin.command("call hub exhaust"));
                }
                awaitState(engine, "hungry", MudlibStatus.State.FAILED);
                assertTrue(local.command("status").contains("healthy"));
                try (Socket player = player(healthy.playerPort())) { assertTrue(readUntil(player, "ready>").contains("LOGIN healthy")); }
            }
        }
    }

    private MudlibSpec spec(String id) throws IOException {
        Path root = directory.resolve(id); Files.createDirectories(root);
        Files.writeString(root.resolve("mudlib.config"), """
                game_id = %s
                game_name = %s
                mudlib_object = hub
                initial_place = room
                player_object = player
                lifecycle.player_session_connected = logon
                player_prompt = ready>
                temporal_tick_interval = 0.1
                """.formatted(id, id));
        Files.writeString(root.resolve("hub.c"), "int connections; void connected() { connections += 1; } int query_connections() { return connections; }\n");
        Files.writeString(root.resolve("room.c"), "string short() { return \"room\"; }\n");
        Files.writeString(root.resolve("player.c"), "void logon() { \"hub\"->connected(); jvmud_write(\"LOGIN " + id + "\\n\"); }\n");
        return new MudlibSpec(root, "mudlib.config");
    }
    private String pin() throws IOException { return Files.readString(directory.resolve("engine/admin-tls.sha256")).trim(); }
    private String token(String result) { return result.substring(result.indexOf(": ") + 2).trim(); }
    private static Socket player(int port) throws IOException { Socket s = new Socket("127.0.0.1", port); s.setSoTimeout(5000); return s; }
    private static void send(Socket socket, String line) throws IOException { socket.getOutputStream().write(line.getBytes(StandardCharsets.UTF_8)); }
    private static String readUntil(Socket socket, String marker) throws IOException {
        StringBuilder result = new StringBuilder();
        while (result.indexOf(marker) < 0) { int c = socket.getInputStream().read(); if (c < 0) throw new EOFException(result.toString()); result.append((char)c); }
        return result.toString();
    }
    private static String readToEnd(Socket socket) throws IOException { return new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8); }
    private static void awaitState(JVMud engine, String id, MudlibStatus.State state) {
        assertTimeoutPreemptively(Duration.ofSeconds(8), () -> {
            while (engine.mudlibs().stream().noneMatch(m -> m.id().equals(id) && m.state() == state)) Thread.sleep(20);
        });
    }
}
