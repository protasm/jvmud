package io.github.protasm.jvmud.engine;

import static org.junit.jupiter.api.Assertions.*;
import io.github.protasm.jvmud.instance.MudlibSpec;
import io.github.protasm.jvmud.support.AdminConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.*;

/** End-to-end engine supervision using actual sandboxed worker JVMs and authenticated network connections. */
class EngineArchitectureTest {
    private Path directory;
    @BeforeEach void setup() throws IOException { directory = Files.createTempDirectory(Path.of("/tmp"), "jvmud-it-"); }
    @AfterEach void cleanup() throws IOException {
        try (var paths = Files.walk(directory)) { for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path); }
    }
    private JVMud engine() { return new JVMud(new EngineConfiguration("127.0.0.1", 0, 0, directory.resolve("engine"), false)); }

    @Test void emptyEngineProvidesPublicMenuLocalBootstrapAndRemoteEngineConsole() throws Exception {
        try (JVMud engine = engine()) {
            engine.start();
            try (Socket player = player(engine.port())) {
                assertTrue(readUntil(player, "(or quit): ").contains("No mudlibs"));
                send(player, "quit\n"); assertEquals(-1, player.getInputStream().read());
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
                    readUntil(menu, "(or quit): "); send(menu, "first\n"); readUntil(menu, "ready>");
                    assertTrue(admin.command("call hub query_connections").contains("2"));
                    send(menu, "//quit\n"); assertFalse(readToEnd(menu).contains("Select a mudlib"));
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
