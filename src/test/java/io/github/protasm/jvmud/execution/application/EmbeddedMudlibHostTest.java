package io.github.protasm.jvmud.execution.application;

import static org.junit.jupiter.api.Assertions.*;

import io.github.protasm.jvmud.execution.instance.MudlibSpec;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EmbeddedMudlibHostTest {
    @TempDir Path directory;

    @Test
    void directEndpointAttachesOnlyItsMudlib() throws Exception {
        try (EmbeddedMudlibHost engine = engine(spec("first", "0"), spec("second", "0"))) {
            engine.start();
            try (Socket socket = new Socket("127.0.0.1", engine.port(1))) {
                socket.setSoTimeout(3000);
                assertTrue(readUntil(socket, "ready>").contains("LOGIN second"));
                assertEquals(0, connections(engine, 0));
                assertEquals(1, connections(engine, 1));
                send(socket, "unrecognized-command\n");
                assertFalse(readUntil(socket, "ready>").contains("You can't do that."));
            }
        }
    }

    @Test
    void blockedWorldTickDoesNotDelayAnotherWorldAndIntervalsRemainIndependent() throws Exception {
        CountDownLatch blocked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch otherTick = new CountDownLatch(1);
        AtomicReference<String> firstThread = new AtomicReference<>();
        AtomicReference<String> secondThread = new AtomicReference<>();
        try (EmbeddedMudlibHost engine = engine(spec("slow", "0.01"), spec("fast", "0.02"), spec("manual", "0"))) {
            engine.start();
            var slow = engine.mudlibs().get(0);
            var fast = engine.mudlibs().get(1);
            assertEquals(java.time.Duration.ofMillis(10), slow.worldTickInterval());
            assertEquals(java.time.Duration.ofMillis(20), fast.worldTickInterval());
            slow.administer(runtime -> {
                slow.bootResult().worldRuntime().scheduler().scheduleAfter(1, () -> {
                    firstThread.set(Thread.currentThread().getName());
                    blocked.countDown();
                    try { release.await(5, TimeUnit.SECONDS); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                });
                return null;
            });
            try {
                assertTrue(blocked.await(2, TimeUnit.SECONDS));
                fast.administer(runtime -> {
                    fast.bootResult().worldRuntime().scheduler().scheduleAfter(1, () -> {
                        secondThread.set(Thread.currentThread().getName());
                        otherTick.countDown();
                    });
                    return null;
                });
                assertTrue(otherTick.await(2, TimeUnit.SECONDS), "Another mudlib must tick while the first is blocked");
                assertNotEquals(firstThread.get(), secondThread.get());
                assertEquals(0, engine.mudlibs().get(2).bootResult().worldRuntime().scheduler().currentTick());
            } finally { release.countDown(); }
            engine.close();
            long stoppedTick = fast.bootResult().worldRuntime().scheduler().currentTick();
            fast.advanceWorldTick();
            assertEquals(stoppedTick, fast.bootResult().worldRuntime().scheduler().currentTick());
        }
    }

    @Test
    void duplicateIdsFailStartupAndShutdownAlreadyBootedMudlibs() throws Exception {
        MudlibSpec first = spec("duplicate", "0");
        try (EmbeddedMudlibHost engine = engine(first, first)) {
            assertThrows(IllegalArgumentException.class, engine::start);
            assertEquals(2, engine.mudlibs().size());
            for (var mud : engine.mudlibs()) {
                assertEquals(1, (Integer) mud.administer(runtime -> runtime.invokeObject(
                        runtime.loadOrGetObject("hub"), "query_stopped")));
            }
        }
    }

    @Test
    void hostClosesDirectPlayerConnections() throws Exception {
        try (EmbeddedMudlibHost engine = engine(spec("only", "0"))) {
            engine.start();
            try (Socket socket = connect(engine)) {
                readUntil(socket, "ready>");
                engine.close();
                assertEquals(-1, socket.getInputStream().read());
            }
        }
    }

    private EmbeddedMudlibHost engine(MudlibSpec... specifications) {
        return new EmbeddedMudlibHost("127.0.0.1", 0, List.of(specifications));
    }

    private MudlibSpec spec(String id, String interval) throws IOException {
        Path root = directory.resolve(id);
        Files.createDirectories(root);
        Files.writeString(root.resolve("world.config"), """
                game_id = %s
                game_name = %s
                mudlib_object = hub
                initial_place = room
                player_object = player
                lifecycle.player_session_connected = logon
                lifecycle.server_shutdown = stopped
                player_prompt = ready>
                temporal_tick_interval = %s
                """.formatted(id, id, interval));
        Files.writeString(root.resolve("hub.c"), """
                int connections;
                int stops;
                void connected() { connections += 1; }
                int query_connections() { return connections; }
                void stopped(mixed reason) { stops += 1; }
                int query_stopped() { return stops; }
                """);
        Files.writeString(root.resolve("room.c"), "string short() { return \"room\"; }\n");
        Files.writeString(root.resolve("player.c"), """
                void logon() {
                    "hub"->connected();
                    jvmud_write("LOGIN %s\\n");
                }
                """.formatted(id));
        return new MudlibSpec(root, "world.config");
    }

    private int connections(EmbeddedMudlibHost engine, int index) {
        return (Integer) engine.mudlibs().get(index).administer(runtime ->
                runtime.invokeObject(runtime.loadOrGetObject("hub"), "query_connections"));
    }

    private Socket connect(EmbeddedMudlibHost engine) throws IOException {
        Socket socket = new Socket("127.0.0.1", engine.port());
        socket.setSoTimeout(3000);
        return socket;
    }

    private void send(Socket socket, String text) throws IOException {
        socket.getOutputStream().write(text.getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
    }

    private String readUntil(Socket socket, String marker) throws IOException {
        StringBuilder text = new StringBuilder();
        while (text.indexOf(marker) < 0) {
            int value = socket.getInputStream().read();
            if (value < 0) fail("Connection ended before " + marker + ": " + text);
            text.append((char) value);
        }
        return text.toString();
    }
}
