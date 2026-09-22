package io.github.protasm.jvmud.execution.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.language.exec.LPCObjectLoadObserver;
import io.github.protasm.jvmud.language.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.language.exec.LPCRuntime;
import io.github.protasm.jvmud.language.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.execution.model.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.execution.model.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.execution.model.mudlib.MudlibProjectionRole;
import io.github.protasm.jvmud.execution.instance.CombinedPlayerPersonaAdapter;
import io.github.protasm.jvmud.execution.instance.MudInstance;
import io.github.protasm.jvmud.execution.instance.MudlibBoot;
import io.github.protasm.jvmud.execution.instance.MudlibBootProgress;
import io.github.protasm.jvmud.execution.instance.MudlibBootResult;
import io.github.protasm.jvmud.execution.instance.InstancePersona;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class EngineTest {
    /** Connects to the sole mudlib directly for login tests. */
    private static Socket connectToOnlyMudlib(EmbeddedMudlibHost engine) throws IOException {
        Socket socket = new Socket("127.0.0.1", engine.port());
        return socket;
    }

    private static final String DEFAULT_CONFIG_PATH = "jvmud/test.config";
    private static final String LP245_CONFIG_PATH = "jvmud/lp245.config";

    @Test
    void telnetServerLaunchOptionsAcceptStartupLoadTraceFlag() {
        EngineLauncher.LaunchOptions options = EngineLauncher.parseLaunchOptions(new String[] {
                "--trace-startup-loads", "mudlibs/smallmercies/jvmud/smallmercies.config"
        });

        assertEquals(repositoryRoot().resolve("mudlibs/smallmercies"), options.mudlibs().getFirst().root());
        assertEquals("jvmud/smallmercies.config", options.mudlibs().getFirst().configPath());
        assertTrue(options.traceStartupLoads());
    }

    @Test
    void telnetServerLaunchOptionsRejectBadFlags() {
        assertThrows(IllegalArgumentException.class, () ->
                EngineLauncher.parseLaunchOptions(new String[] {"-port"}));
        assertThrows(IllegalArgumentException.class, () ->
                EngineLauncher.parseLaunchOptions(new String[] {"mudlibs/smallmercies/jvmud/smallmercies.config", "extra"}));
        assertThrows(IllegalArgumentException.class, () ->
                EngineLauncher.parseLaunchOptions(new String[] {"-bogus", "value"}));
        assertThrows(IllegalArgumentException.class, () ->
                EngineLauncher.parseLaunchOptions(new String[] {"--trace-startup-loads", "one", "two"}));
    }

    @TempDir
    Path tempDir;

    @Test
    void telnetServerLaunchOptionsAcceptMudlibName() {
        var options = EngineLauncher.parseLaunchOptions(new String[] {"--port", "4567", "smallmercies"});
        assertEquals(repositoryRoot().resolve("mudlibs/smallmercies"), options.mudlibs().getFirst().root());
        assertEquals("jvmud/smallmercies.config", options.mudlibs().getFirst().configPath());
        assertEquals(4567, options.port());
    }

    @Test
    void launchConfigResolutionPrefersDirectFileThenFallsBack() throws IOException {
        Path direct = tempDir.resolve("example");
        Path fallback = tempDir.resolve("mudlibs/example/jvmud/example.config");
        Files.createDirectories(fallback.getParent());
        Files.writeString(fallback, "");
        Files.writeString(direct, "");
        assertEquals(direct, EngineLauncher.resolveLaunchConfigFile(Path.of("example"), tempDir));
        assertEquals(direct, EngineLauncher.resolveLaunchConfigFile(direct, tempDir));
        Files.delete(direct);
        assertEquals(fallback, EngineLauncher.resolveLaunchConfigFile(Path.of("example"), tempDir));
        Files.createDirectory(direct);
        assertEquals(fallback, EngineLauncher.resolveLaunchConfigFile(Path.of("example"), tempDir));
    }

    @Test
    void launchConfigResolutionReportsBothMissingPaths() {
        var error = assertThrows(IllegalArgumentException.class, () ->
                EngineLauncher.resolveLaunchConfigFile(Path.of("missing"), tempDir));
        assertTrue(error.getMessage().contains(tempDir.resolve("missing").toString()));
        assertTrue(error.getMessage().contains(tempDir.resolve("mudlibs/missing/jvmud/missing.config").toString()));
    }

    @Test
    void bootRequiresAnExplicitExistingManifest() {
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        assertThrows(
                IllegalStateException.class,
                () -> new MudlibBoot(runtime, tempDir, "jvmud/missing.config").boot());
    }

    @Test
    void bootDeliversExplicitServerStartedLifecycleHook() throws IOException {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/test.config"), """
                game_id = lifecycle-test
                mudlib_object = jvmud/mudlib
                lifecycle.server_started = server_started
                """);
        Files.writeString(tempDir.resolve("jvmud/mudlib.c"), """
                int started;
                void server_started() { started = 1; }
                int query_started() { return started; }
                """);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());

        new MudlibBoot(runtime, tempDir, "jvmud/test.config").boot();

        assertEquals(1, runtime.invokeObject(runtime.loadOrGetObject("jvmud/mudlib"), "query_started"));
    }

    @Test
    void engineCanLaunchWithoutMudlibs() {
        assertTrue(EngineLauncher.parseLaunchOptions(new String[0]).mudlibs().isEmpty());
    }

    @Test
    void telnetServerLaunchOptionsAcceptSingleConfigFileArgument() {
        EngineLauncher.LaunchOptions options = EngineLauncher.parseLaunchOptions(new String[] {
                "mudlibs/lp245/jvmud/lp245.config"
        });

        assertEquals(repositoryRoot().resolve("mudlibs/lp245"), options.mudlibs().getFirst().root());
        assertEquals(4000, options.port());
        assertEquals("localhost", options.bindAddress());
        assertEquals("jvmud/lp245.config", options.mudlibs().getFirst().configPath());
        assertFalse(options.traceStartupLoads());
    }

    @Test
    void telnetServerLaunchOptionsAcceptNetworkSettings() {
        EngineLauncher.LaunchOptions options = EngineLauncher.parseLaunchOptions(new String[] {
                "--bind", "0.0.0.0", "mudlibs/smallmercies/jvmud/smallmercies.config",
                "--port", "4567", "--trace-startup-loads"
        });
        assertEquals("0.0.0.0", options.bindAddress());
        assertEquals(4567, options.port());
        assertEquals("jvmud/smallmercies.config", options.mudlibs().getFirst().configPath());
        assertTrue(options.traceStartupLoads());
    }

    @Test
    void telnetServerLaunchOptionsRejectInvalidNetworkSettings() {
        for (String[] args : new String[][] {
                {"--bind"}, {"--bind", ""}, {"--bind", "--port", "4000"},
                {"--port"}, {"--port", "abc"}, {"--port", "0"},
                {"--port", "-1"}, {"--port", "65536"}, {"--port", "999999999999"}
        }) {
            assertThrows(IllegalArgumentException.class, () -> EngineLauncher.parseLaunchOptions(args));
        }
    }

    @Test
    void engineAdministrationIsAlwaysSeparate() {
        var defaults = EngineLauncher.parseLaunchOptions(new String[0]);
        assertEquals(4001, defaults.adminPort());
        assertEquals(null, defaults.publicHost());
        assertEquals("play.jvmud.org", EngineLauncher.parseLaunchOptions(new String[]{"--public-host", "play.jvmud.org"}).publicHost());
        assertThrows(IllegalArgumentException.class, () -> EngineLauncher.parseLaunchOptions(new String[]{"--public-host"}));
        var options = EngineLauncher.parseLaunchOptions(new String[]{"--port", "4500", "--admin-port", "4600", "--state-dir", "target/engine"});
        assertEquals(4600, options.adminPort());
        for (String[] args : new String[][] {{"--admin-port", "4000"}, {"--admin-port", "0"}, {"--admin-port", "bad"}, {"--admin-port"}})
            assertThrows(IllegalArgumentException.class, () -> EngineLauncher.parseLaunchOptions(args));
    }

    @Test
    void configuredMudlibDirectoryResolvesNamesRegardlessOfOptionOrder(@TempDir Path directory) throws IOException {
        Path manifest = directory.resolve("sample/jvmud/sample.config");
        Files.createDirectories(manifest.getParent());
        Files.writeString(manifest, "game_id = sample\n");
        for (String[] args : new String[][] {{"--mudlib-dir", directory.toString(), "sample"},
                {"sample", "--mudlib-dir", directory.toString()}}) {
            var options = EngineLauncher.parseLaunchOptions(args);
            assertEquals(directory, options.mudlibDirectory());
            assertEquals(directory.resolve("sample"), options.mudlibs().getFirst().root());
        }
        assertThrows(IllegalArgumentException.class, () -> EngineLauncher.parseLaunchOptions(new String[]{"--mudlib-dir"}));
    }

    @Test
    void telnetServerLaunchOptionsAcceptHelp() {
        EngineLauncher.LaunchOptions options = EngineLauncher.parseLaunchOptions(new String[] {"--help"});

        assertTrue(options.help());
        assertFalse(options.traceStartupLoads());
    }

    @Test
    void lp245GoPuzzleRespondsToSpokenMoveOverTelnet() throws Exception {
        Path lp245 = lp245TestRoot();

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, lp245, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "What is your name: ")
                        .contains("What is your name: "));

                socket.getOutputStream().write("gotest\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: ").contains("Password: "));

                socket.getOutputStream().write("secret1\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: (again) ")
                        .contains("Password: (again) "));

                socket.getOutputStream().write("secret1\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your email address")
                        .contains("Please enter your email address"));

                socket.getOutputStream().write("none\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Are you, male, female or other")
                        .contains("Are you, male, female or other"));

                socket.getOutputStream().write("o\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "> ").contains("Welcome, Creature!"));

                socket.getOutputStream().write("south\neast\neast\nnorth\neast\nlook at board\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String board = readUntilQuietAfterContains(socket, "5|.......");
                assertTrue(board.contains("It is black"), board);
                socket.getOutputStream().write("say play b1\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String reply = readUntilQuietAfterContains(socket, "You feel that you have gained some experience.");
                assertTrue(reply.contains("Right !"), reply);
                assertFalse(reply.contains("wrongness"), reply);
                socket.getOutputStream().write("look at board\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String next = readUntilQuietAfterContains(socket, "7|.......");
                assertTrue(next.contains("6|......."), next);
                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                readUntilSocketClosed(socket);
            }
        }
    }

    @Test
    void lp245TrollHuntKeepsHeartbeatAfterExaminingMonster() throws Exception {
        Path lp245 = lp245TestRoot();

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, lp245, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "What is your name: ")
                        .contains("What is your name: "));

                socket.getOutputStream().write("hbtest\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: ").contains("Password: "));

                socket.getOutputStream().write("secret1\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: (again) ")
                        .contains("Password: (again) "));

                socket.getOutputStream().write("secret1\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your email address")
                        .contains("Please enter your email address"));

                socket.getOutputStream().write("none\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Are you, male, female or other")
                        .contains("Are you, male, female or other"));

                socket.getOutputStream().write("o\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "> ").contains("Welcome, Creature!"));

                socket.getOutputStream().write("south\nwest\nwest\nwest\nwest\nwest\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String trollRoom = readUntilQuietAfterContains(socket, "A troll.");
                assertTrue(trollRoom.contains("You are in a big forest."), trollRoom);

                socket.getOutputStream().write("exa troll\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String troll = readUntilQuietAfterContains(socket, "Troll is carrying:");
                assertTrue(troll.contains("It is a nasty troll that look very aggressive."), troll);

                String firstAttack = readUntilQuietAfterContains(socket, "Troll ");
                assertFalse(firstAttack.contains("Your sensitive mind notices a wrongness"), firstAttack);
                assertFalse(firstAttack.contains("You have no heart beat"), firstAttack);

                socket.getOutputStream().write("east\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String fled = readUntilQuietAfterContains(socket, "You are now hunted by Troll.");
                assertTrue(fled.contains("A small clearing."), fled);
                assertFalse(fled.contains("Your sensitive mind notices a wrongness"), fled);
                assertFalse(fled.contains("You have no heart beat"), fled);

                socket.getOutputStream().write("west\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String returned = readUntilQuietAfterContains(socket, "A troll.");
                assertTrue(returned.contains("You are in a big forest."), returned);
                assertFalse(returned.contains("Your sensitive mind notices a wrongness"), returned);
                assertFalse(returned.contains("You have no heart beat"), returned);

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                readUntilSocketClosed(socket);
            }
        }
    }

    @Test
    void lp245WizardHallMissingSouthActionFallsThroughCleanly() throws Exception {
        Path lp245 = lp245TestRoot();
        Path player = lp245.resolve("obj/player.c");
        // Only this disposable fixture is patched; the archive copy retains read-only modes.
        assertTrue(player.toFile().setWritable(true, true));
        Files.writeString(player, Files.readString(player)
                .replace("move_object(myself, \"room/church\");", "move_object(myself, \"room/wiz_hall\");"));

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, lp245, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "What is your name: ")
                        .contains("What is your name: "));

                socket.getOutputStream().write("wizbug\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: ").contains("Password: "));

                socket.getOutputStream().write("secret1\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: (again) ")
                        .contains("Password: (again) "));

                socket.getOutputStream().write("secret1\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your email address")
                        .contains("Please enter your email address"));

                socket.getOutputStream().write("none\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Are you, male, female or other")
                        .contains("Are you, male, female or other"));

                socket.getOutputStream().write("o\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String greeting = readUntilQuietAfterContains(socket, "> ");
                assertTrue(greeting.contains("Welcome, Creature!"), greeting);
                assertTrue(greeting.contains("Leo the Archwizard."), greeting);

                socket.getOutputStream().write("south\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String south = readUntilQuietAfterContains(socket, "> ");
                assertFalse(south.contains("Your sensitive mind notices a wrongness"), south);
                assertFalse(south.contains("You can't do that."), south);

                socket.getOutputStream().write("north\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String north = readUntilQuietAfterContains(socket, "A strong magic force stops you.");
                assertFalse(north.contains("Your sensitive mind notices a wrongness"), north);

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                readUntilSocketClosed(socket);
            }
        }
    }

    @Test
    void telnetNegotiatesGmcpAndBridgesJsonMessagesToMudlibCode() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("jvmud/test.config"), """
                engine_capabilities = session_control
                player_object = obj/player
                initial_place = room/start
                lifecycle.object_loaded = initialize
                lifecycle.interaction_scope_started = offer_interactions
                """);
        Files.writeString(tempDir.resolve("obj/player.c"), """
                void initialize(mixed first_load) {}
                void offer_interactions() {
                  jvmud_enable_commands();
                  jvmud_add_action("noop", "noop");
                }
                int noop(mixed ignored) { return 1; }
                void client_protocol_changed(string protocol, int enabled) {
                  if (protocol == "GMCP" && enabled) {
                    jvmud_send_gmcp("Test.Hello", ([ "name": "JVMud", "version": 1 ]));
                  }
                }
                void receive_gmcp(string package_name, mixed payload) {
                  if (package_name == "Core.Ping") {
                    jvmud_send_gmcp("Core.Ping");
                  } else if (package_name == "Client.Test") {
                    jvmud_write("gmcp client=" + payload["client"] + "\n");
                  }
                }
                """);
        Files.writeString(tempDir.resolve("room/start.c"), """
                void initialize(mixed first_load) {}
                void offer_interactions() {}
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost("127.0.0.1", 0, tempDir, "jvmud/test.config")) {
            server.start();
            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                String initial = readUntilQuietAfterContains(socket, "Attached player 1");
                assertTrue(containsTelnetCommand(initial, 251, 201), printable(initial));

                socket.getOutputStream().write(new byte[] {(byte) 255, (byte) 253, (byte) 201});
                socket.getOutputStream().flush();
                String hello = readUntilQuietAfterContains(socket, "Test.Hello");
                assertTrue(hello.contains("{\"name\":\"JVMud\",\"version\":1}"), printable(hello));
                assertTrue(containsGmcpFrame(hello, "Test.Hello"), printable(hello));

                writeGmcpFrame(socket, "Client.Test {\"client\":\"Mudlet\"}");
                assertTrue(readUntilQuietAfterContains(socket, "gmcp client=Mudlet")
                        .contains("gmcp client=Mudlet"));

                writeGmcpFrame(socket, "Core.Ping");
                String ping = readUntilQuietAfterContains(socket, "Core.Ping");
                assertTrue(containsGmcpFrame(ping, "Core.Ping"), printable(ping));
            }
        }
    }

    @Test
    void telnetServerReportsExplicitPreloadManifestSummary() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve(LP245_CONFIG_PATH), """
                mfun_object = jvmud/mfuns
                initial_place = room/village/vill_green
                preload_file = init_file
                """);
        Files.writeString(tempDir.resolve("init_file"), """
                obj/preload
                obj/broken
                """);
        Files.writeString(tempDir.resolve("obj/preload.c"), """
                string short() {
                    return "preload";
                }
                """);
        Files.writeString(tempDir.resolve("obj/broken.c"), "int broken( { return 1; }\n");
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                string short() {
                    return "green";
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            assertEquals(
                    "preload manifest init_file: compiled 1 object(s), skipped 1 object(s). Skipped: obj/broken",
                    EmbeddedMudlibHost.preloadSummary(server.mudlibs().getFirst().bootResult()));
        }
    }

    @Test
    void legacyPlayerObjectAdapterMarksConfiguredPlayerAsCombinedProjection() {
        Object playerObject = new Object();

        var projection = new CombinedPlayerPersonaAdapter("obj/player").combinedProjection(playerObject);

        assertEquals("obj/player", projection.sourcePath());
        assertEquals(playerObject, projection.object());
        assertTrue(projection.hasRole(MudlibProjectionRole.PLAYER_PROFILE));
        assertTrue(projection.hasRole(MudlibProjectionRole.PERSONA_BEHAVIOR));
        assertTrue(projection.hasRole(MudlibProjectionRole.COMBINED_PLAYER_PERSONA));
    }

    @Test
    void telnetSessionAcceptsPlayerCommandsOverSocket() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void init() {
                    add_action("look");
                    add_verb("look");
                }

                void long(mixed str) {
                    write("A test green.\\n");
                }

                int look(mixed str) {
                    long(str);
                    return 1;
                }
                """);
        installMinimalMudlibPlayer(tempDir, "room/village/vill_green");

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                String initial = readUntilContains(socket, "Attached player 1");
                assertTrue(initial.contains("JVMud telnet."));

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String look = readUntilContains(socket, "A test green.");
                assertTrue(look.contains("A test green."));

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetSessionPassesPlayerCommandsWithoutMudlibAliasKnowledge() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void init() {
                    add_action("short_w", "w");
                    add_action("long_west", "west");
                }

                int short_w(mixed str) {
                    write("raw w command\\n");
                    return 1;
                }

                int long_west(mixed str) {
                    write("west command\\n");
                    return 1;
                }
                """);
        installMinimalMudlibPlayer(tempDir, "room/village/vill_green");

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                readUntilContains(socket, "Attached player 1");

                socket.getOutputStream().write("w\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String shortCommand = readUntilContains(socket, "raw w command");
                assertTrue(shortCommand.contains("raw w command"), shortCommand);
                assertFalse(shortCommand.contains("west command"), shortCommand);

                socket.getOutputStream().write("west\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String longCommand = readUntilContains(socket, "west command");
                assertTrue(longCommand.contains("west command"), longCommand);

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetSessionBootsMudlibRootWithSpaces() throws Exception {
        Path mudlibRoot = tempDir.resolve("mud lib");
        installMfunShim(mudlibRoot);
        Files.createDirectories(mudlibRoot.resolve("room/village"));
        Files.writeString(mudlibRoot.resolve("room/village/vill_green.c"), """
                void init() {
                    add_action("look");
                    add_verb("look");
                }

                void long(mixed str) {
                    write("A spaced-path green.\\n");
                }

                int look(mixed str) {
                    long(str);
                    return 1;
                }
                """);
        installMinimalMudlibPlayer(mudlibRoot, "room/village/vill_green");

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, mudlibRoot, DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                readUntilContains(socket, "Attached player 1");

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String look = readUntilContains(socket, "A spaced-path green.");
                assertTrue(look.contains("A spaced-path green."));

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void hostedMudlibWorldAdvancesConfiguredTemporalTicks() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mfun_object = jvmud/mfuns
                player_object = obj/player
                initial_place = room/village/vill_green
                lifecycle.player_session_connected = logon
                temporal_tick_method = heart_beat
                temporal_tick_interval = 0.01
                """);
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void write(mixed value) {
                    jvmud_write(value);
                }

                void set_heart_beat(int enabled) {
                    jvmud_schedule_recurring_tick(enabled, 0);
                }
                """);
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                string short() {
                    return "green";
                }
                """);
        Files.writeString(tempDir.resolve("obj/player.c"), """
                void logon() {
                    set_heart_beat(1);
                }

                void heart_beat() {
                    write("world tick delivered\\n");
                    set_heart_beat(0);
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);

                String tick = readUntilContains(socket, "world tick delivered");

                assertTrue(tick.contains("JVMud telnet."));
                assertTrue(tick.contains("world tick delivered"));
            }
        }
    }

    @Test
    void telnetSessionCanAttachConfiguredMudlibPlayerObject() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                initial_place = room/village/vill_green
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                string name;

                void reset(mixed arg) {
                    name = "mudlib player";
                }

                string query_name() {
                    return name;
                }

                string query_real_name() {
                    return name;
                }

                int query_level() {
                    return 0;
                }

                int query_invis() {
                    return 0;
                }

                int remove_ghost() {
                    return 1;
                }

                int id(mixed str) {
                    return str == "player";
                }

                void init() {
                    add_action("look");
                    add_verb("look");
                }

                int look(mixed str) {
                    call_other(environment(this_object()), "long", 0);
                    return 1;
                }

                int move_player(mixed dir_dest) {
                    if (dir_dest != "north#room/village/church")
                        return 0;

                    say(query_name() + " leaves north.\\n");
                    move_object(this_object(), "room/village/church");
                    say(query_name() + " arrives.\\n");
                    call_other(environment(this_object()), "long", 0);
                    return 1;
                }
                """);
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void init() {
                    add_action("north");
                    add_verb("north");
                }

                void long(mixed str) {
                    write("You are on the green.\\n");
                }

                int north(mixed str) {
                    call_other(this_player(), "move_player", "north#room/village/church");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("room/village/church.c"), """
                void long(mixed str) {
                    write("You are in the church.\\n");
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilContains(socket, "Attached player 1 as obj/test_player#clone1")
                        .contains("Attached player 1 as obj/test_player#clone1"));

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "You are on the green.").contains("You are on the green."));

                socket.getOutputStream().write("north\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String north = readUntilContains(socket, "You are in the church.");
                assertTrue(north.contains("You are in the church."), north);
                assertFalse(north.contains("mudlib player leaves north."), north);
                assertFalse(north.contains("mudlib player arrives."), north);

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void runtimeErrorsCanBeHandledByMudlibBoundaryObject() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mudlib_object = jvmud/mudlib
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                player_prompt = "> "
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                lifecycle.runtime_error = runtime_error
                """);
        Files.writeString(tempDir.resolve("jvmud/mudlib.c"), """
                void runtime_error(mixed actor, mixed context, mixed operation, mixed detail) {
                    jvmud_write_to_lpc_object(actor, "A velvet curtain falls over the machinery.\\n");
                    jvmud_append_mudlib_text("/log/RUNTIME", context + ":" + operation + ":" + detail + "\\n");
                }
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                void init() {
                    add_action("boom", "boom");
                }

                int boom(mixed arg) {
                    int divisor;

                    return 1 / divisor;
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilContains(socket, "Attached player 1").contains("Attached player 1"));

                socket.getOutputStream().write("boom\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String output = readUntilQuietAfterContains(socket, "A velvet curtain falls over the machinery.");
                assertTrue(output.contains("A velvet curtain falls over the machinery."), output);
                assertFalse(output.contains("Error:"), output);
                assertFalse(output.contains("/ by zero"), output);
            }
        }

        String log = Files.readString(tempDir.resolve("log/RUNTIME"));
        assertTrue(log.contains("command:boom"), log);
        assertTrue(log.contains("/ by zero"), log);
    }

    @Test
    void compileErrorsCanBeLoggedByMudlibBoundaryObject() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mudlib_object = jvmud/mudlib
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                player_prompt = "> "
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                lifecycle.log_error = log_error
                lifecycle.runtime_error = runtime_error
                """);
        Files.writeString(tempDir.resolve("jvmud/mudlib.c"), """
                void log_error(mixed file, mixed err) {
                    jvmud_append_mudlib_text("/log/COMPILER", file + "\\n");
                    jvmud_append_mudlib_text("/log/COMPILER", err + "\\n");
                }

                void runtime_error(mixed actor, mixed context, mixed operation, mixed detail) {
                    jvmud_write_to_lpc_object(actor, "Your sensitive mind notices a wrongness in the fabric of space.\\n");
                    jvmud_append_mudlib_text("/log/RUNTIME", context + ":" + operation + ":" + detail + "\\n");
                }
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                void init() {
                    add_action("loadbroken", "loadbroken");
                }

                int loadbroken(mixed arg) {
                    jvmud_clone_lpc_object("obj/broken");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("obj/broken.c"), """
                int broken() {
                    return 1
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilContains(socket, "Attached player 1").contains("Attached player 1"));

                socket.getOutputStream().write("loadbroken\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String output = readUntilQuietAfterContains(socket, "Your sensitive mind notices a wrongness");
                assertTrue(output.contains("Your sensitive mind notices a wrongness in the fabric of space."), output);
                assertFalse(output.contains("Compilation failed:"), output);
                assertFalse(output.contains("Expect ';' after return statement"), output);
            }
        }

        String compilerLog = Files.readString(tempDir.resolve("log/COMPILER"));
        assertTrue(compilerLog.contains("/obj/broken"), compilerLog);
        assertTrue(compilerLog.contains("Compilation failed:"), compilerLog);
        assertTrue(compilerLog.contains("Expect ';' after return statement"), compilerLog);
        String runtimeLog = Files.readString(tempDir.resolve("log/RUNTIME"));
        assertTrue(runtimeLog.contains("command:loadbroken"), runtimeLog);
    }

    @Test
    void serverShutdownCanNotifyMudlibBoundaryObject() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mudlib_object = jvmud/mudlib
                mfun_object = jvmud/mfuns
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.server_shutdown = notify_shutdown
                """);
        Files.writeString(tempDir.resolve("jvmud/mudlib.c"), """
                void notify_shutdown(mixed reason) {
                    if (reason) {
                        jvmud_append_mudlib_text("/log/SHUTDOWN", "PANIC! " + reason + "\\n");
                    } else {
                        jvmud_append_mudlib_text("/log/SHUTDOWN", "LPmud shutting down immediately.\\n");
                    }
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        EmbeddedMudlibHost server = new EmbeddedMudlibHost("127.0.0.1", 0, tempDir, LP245_CONFIG_PATH);
        server.start();
        server.close();
        server.close();

        assertEquals("LPmud shutting down immediately.\n", Files.readString(tempDir.resolve("log/SHUTDOWN")));
    }

    @Test
    void telnetSessionRoutesCapturedInputThroughMfunInputTo() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                player_prompt = "> "
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                string name;

                void reset(mixed arg) {
                    name = "unnamed";
                }

                string query_name() {
                    return name;
                }

                string query_real_name() {
                    return name;
                }

                int query_level() {
                    return 0;
                }

                int query_invis() {
                    return 0;
                }

                int remove_ghost() {
                    return 1;
                }

                int id(mixed str) {
                    return str == "player";
                }

                void init() {
                    add_action("ask_name", "name");
                }

                int ask_name(mixed str) {
                    write("Name: ");
                    input_to("set_name");
                    return 1;
                }

                void set_name(mixed str) {
                    name = str;
                    write("Hello " + name + "\\n");
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                readUntilContains(socket, "Attached player 1");

                socket.getOutputStream().write("name\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String namePrompt = readUntilQuietAfterContains(socket, "Name: ");
                assertTrue(namePrompt.contains("Name: "));
                assertFalse(namePrompt.contains("Name: > "), namePrompt);

                socket.getOutputStream().write("Alice\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String greeting = readUntilContains(socket, "Hello Alice\r\n> ");
                assertTrue(greeting.contains("Hello Alice"));

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetSessionInvokesConfiguredPlayerConnectionLifecycleHook() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("WELCOME"), "Welcome login.\\n");
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.player_session_connected = logon
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                string name;

                void reset(mixed arg) {
                    name = "logon";
                }

                int logon() {
                    cat("/WELCOME");
                    write("What is your name: ");
                    input_to("logon2");
                    return 1;
                }

                void logon2(mixed str) {
                    name = lower_case(str);
                    write("Hello " + capitalize(name) + "\\n");
                }

                string query_name() {
                    return name;
                }

                string query_real_name() {
                    return name;
                }

                int query_level() {
                    return 0;
                }

                int query_invis() {
                    return 0;
                }

                int remove_ghost() {
                    return 1;
                }

                int id(mixed str) {
                    return str == "player";
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                String greeting = readUntilQuietAfterContains(socket, "What is your name: ");
                assertTrue(greeting.contains("Welcome login."), greeting);
                assertTrue(greeting.contains("What is your name: "), greeting);
                assertFalse(greeting.contains("What is your name: > "), greeting);
                assertTrue(greeting.indexOf("JVMud telnet.") < greeting.indexOf("What is your name: "), greeting);

                socket.getOutputStream().write("Alice\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Hello Alice").contains("Hello Alice"));

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetLoginWhoDoesNotExposePendingLoginControllerOrDeadPrompt() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mfun_object = jvmud/mfuns
                player_object = obj/login
                initial_place = room/start
                lifecycle.player_session_connected = logon
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                string short() {
                    return "start";
                }
                """);
        Files.writeString(tempDir.resolve("obj/login.c"), """
                void logon() {
                    write("Please enter your login name: ");
                    input_to("get_login");
                }

                void get_login(string name) {
                    if (name == "who") {
                        write("users=" + sizeof(users()) + "\\n");
                        destruct(this_object());
                    }
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                String greeting = readUntilQuietAfterContains(socket, "Please enter your login name: ");
                assertTrue(greeting.contains("JVMud telnet."), greeting);
                assertFalse(greeting.contains("Attached player"), greeting);

                socket.getOutputStream().write("who\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String output = readUntilSocketClosed(socket);

                assertTrue(output.contains("users=0"), output);
                assertFalse(output.contains(">"), output);
                assertFalse(output.contains("You can't do that."), output);
            }
        }
    }

    @Test
    void telnetSessionInvokesConfiguredPlayerPostRebindLifecycleHook() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mfun_object = jvmud/mfuns
                player_object = obj/login
                initial_place = room/start
                lifecycle.player_session_connected = logon
                lifecycle.player_session_post_rebind = wire_commands
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                string short() {
                    return "start";
                }
                """);
        Files.writeString(tempDir.resolve("obj/login.c"), """
                void logon() {
                    write("Name: ");
                    input_to("finish_login");
                }

                void finish_login(string name) {
                    object player = jvmud_clone_lpc_object("/obj/player.c");
                    jvmud_rebind_session_lpc_object(player, this_object());
                    jvmud_destroy_lpc_object(this_object());
                    write("Welcome " + name + "\\n");
                }
                """);
        Files.writeString(tempDir.resolve("obj/player.c"), """
                void wire_commands() {
                    jvmud_add_action("executeCommand", "", 2);
                }

                int executeCommand(string command) {
                    write("rebound handled " + command + "\\n");
                    return 1;
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Name: ").contains("Name: "));

                socket.getOutputStream().write("Alice\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String handled = readUntilQuietAfterContains(socket, "rebound handled look");
                assertTrue(handled.contains("rebound handled look"), handled);
                assertFalse(handled.contains("You can't do that."), handled);

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                String nextLogin = readUntilQuietAfterContains(socket, "Name: ");
                assertTrue(nextLogin.contains("Name: "), nextLogin);
                assertFalse(nextLogin.contains("Could not attach player"), nextLogin);
                assertFalse(nextLogin.contains("Attached player 2 in"), nextLogin);

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetSessionDoesNotFallbackToHostPersonaWhenConfiguredMudlibPlayerAttachFails() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mfun_object = jvmud/mfuns
                player_object = obj/login
                initial_place = room/start
                lifecycle.player_session_connected = logon
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                string short() {
                    return "start";
                }
                """);
        Files.writeString(tempDir.resolve("obj/login.c"), """
                void logon() {
                    raise_error("login attach failed");
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                String output = readUntilSocketClosed(socket);
                assertTrue(output.contains("Could not attach player:"), output);
                assertFalse(output.contains("Attached player 1 in"), output);
                assertFalse(output.contains("You can't do that."), output);
            }
        }
    }

    @Test
    void telnetSessionDoesNotCreateHostPersonaWhenMudlibPlayerObjectIsMissing() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mfun_object = jvmud/mfuns
                initial_place = room/start
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                string short() {
                    return "start";
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                String output = readUntilSocketClosed(socket);
                assertTrue(output.contains("Mudlib config must define player_object"), output);
                assertFalse(output.contains("Attached player 1 in"), output);
                assertFalse(output.contains("You can't do that."), output);
            }
        }
    }

    @Test
    void telnetInputCaptureDeliversExtraCompatibilityArguments() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                initial_place = room/start
                lifecycle.player_session_connected = logon
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                int logon() {
                    write("Code: ");
                    input_to("capture", 0, "left", 7);
                    return 1;
                }

                int capture(string line, string label, int count) {
                    write(label + ":" + line + ":" + count + "\\n");
                    return 0;
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                String prompt = readUntilQuietAfterContains(socket, "Code: ");
                assertTrue(prompt.contains("Code: "), prompt);

                socket.getOutputStream().write("blue\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String captured = readUntilQuietAfterContains(socket, "left:blue:7");
                assertTrue(captured.contains("left:blue:7"), captured);
                assertFalse(captured.contains("You can't do that."), captured);

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetLoginRestoresSavedPlayerInsteadOfCreatingAgain() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.player_session_connected = logon
                lifecycle.player_session_disconnected = quit
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                string name;
                string password;
                string mailaddr;
                int gender;
                int level;
                int password_attempts;

                void reset(mixed arg) {
                    gender = -1;
                    level = -1;
                }

                int logon() {
                    write("Name: ");
                    input_to("logon2");
                    return 1;
                }

                void logon2(mixed str) {
                    name = lower_case(str);
                    if (!restore_object("players/" + name))
                        write("New character.\\n");

                    if (level != -1)
                        input_to("check_password", 1);
                    else
                        input_to("new_password", 1);

                    write("Password: ");
                }

                void new_password(mixed str) {
                    if (!password) {
                        password = str;
                        input_to("new_password", 1);
                        write("Password: (again) ");
                        return;
                    }

                    password = str;
                    level = 1;
                    write("Email: ");
                    input_to("getmailaddr");
                }

                void getmailaddr(mixed str) {
                    mailaddr = str;
                    write("Gender: ");
                    input_to("getgender");
                }

                void getgender(mixed str) {
                    gender = 1;
                    enable_commands();
                    write("Welcome new " + capitalize(name) + "\\n");
                }

                void check_password(mixed str) {
                    if (str == password) {
                        password_attempts = 0;
                        write("Welcome back " + capitalize(name) + "\\n");
                    } else {
                        write("Wrong password!\\n");
                        password_attempts = password_attempts + 1;
                        if (password_attempts < 3) {
                            input_to("check_password", 1);
                            write("Password: ");
                            return;
                        }
                        destruct(this_object());
                    }
                }

                int quit() {
                    save_object("players/" + name);
                    write("Saving " + capitalize(name) + ".\\n");
                    return 1;
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                String firstNamePrompt = readUntilQuietAfterContains(socket, "Name: ");
                assertTrue(firstNamePrompt.contains("Name: "));
                assertFalse(firstNamePrompt.contains("Name: > "), firstNamePrompt);

                socket.getOutputStream().write("alice\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String firstPassword = readUntilQuietAfterContains(socket, "Password: ");
                assertTrue(firstPassword.contains("New character."), firstPassword);
                assertTrue(firstPassword.contains("Password: "), firstPassword);
                assertFalse(firstPassword.contains("Password: > "), firstPassword);

                socket.getOutputStream().write("secret\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Password: (again) ").contains("Password: (again) "));

                socket.getOutputStream().write("secret\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Email: ").contains("Email: "));

                socket.getOutputStream().write("alice@example.test\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Gender: ").contains("Gender: "));

                socket.getOutputStream().write("female\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Welcome new Alice").contains("Welcome new Alice"));

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                socket.shutdownOutput();
                assertSavedPlayerJsonFile(tempDir.resolve("players/alice.o"));
            }
            String savedPlayer = Files.readString(tempDir.resolve("players/alice.o"));
            assertTrue(savedPlayer.contains("\"format\""), savedPlayer);
            assertTrue(savedPlayer.contains("\"jvmud.lpc-object-state\""), savedPlayer);
            assertTrue(savedPlayer.contains("\"obj.test_player.name\""), savedPlayer);
            assertTrue(savedPlayer.contains("\"value\""), savedPlayer);
            assertTrue(savedPlayer.contains("\"alice\""), savedPlayer);

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilContains(socket, "Name: ").contains("Name: "));

                socket.getOutputStream().write("alice\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String secondPassword = readUntilContains(socket, "Password: ");
                assertFalse(secondPassword.contains("New character."), secondPassword);
                assertTrue(secondPassword.contains("Password: "), secondPassword);

                socket.getOutputStream().write("secret\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String welcomeBack = readUntilContains(socket, "Welcome back Alice");
                assertTrue(welcomeBack.contains("Welcome back Alice"), welcomeBack);
                assertFalse(welcomeBack.contains("Email: "), welcomeBack);
                assertFalse(welcomeBack.contains("Gender: "), welcomeBack);

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilContains(socket, "Name: ").contains("Name: "));

                socket.getOutputStream().write("alice\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Password: ").contains("Password: "));

                socket.getOutputStream().write("wrong\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String firstBadPassword = readUntilContains(socket, "Password: ");
                assertTrue(firstBadPassword.contains("Wrong password!"), firstBadPassword);
                assertTrue(firstBadPassword.contains("Password: "), firstBadPassword);
                assertFalse(firstBadPassword.contains("> "), firstBadPassword);

                socket.getOutputStream().write("wrong\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String secondBadPassword = readUntilContains(socket, "Password: ");
                assertTrue(secondBadPassword.contains("Wrong password!"), secondBadPassword);
                assertTrue(secondBadPassword.contains("Password: "), secondBadPassword);
                assertFalse(secondBadPassword.contains("> "), secondBadPassword);

                socket.getOutputStream().write("wrong\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String thirdBadPassword = readUntilContains(socket, "Wrong password!");
                assertTrue(thirdBadPassword.contains("Wrong password!"), thirdBadPassword);
                assertFalse(thirdBadPassword.contains("> "), thirdBadPassword);
                String closeTail = readUntilSocketClosed(socket);
                assertFalse(closeTail.contains("> "), closeTail);
                assertFalse(closeTail.contains("You can't do that."), closeTail);
            }

            try (Socket socket = connectToOnlyMudlib(server)) {
                socket.setSoTimeout(5000);
                String reattached = readUntilContains(socket, "Name: ");
                assertFalse(reattached.contains("Attached player"), reattached);
                assertTrue(reattached.contains("Name: "), reattached);

                socket.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetConnectionsShareOneBootedMudRuntime() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                int touches = 0;

                void init() {
                    add_action("touch");
                    add_verb("touch");
                }

                int touch(mixed str) {
                    touches = touches + 1;
                    write("touch " + touches + "\\n");
                    return 1;
                }
                """);
        installMinimalMudlibPlayer(tempDir, "room/village/vill_green");

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket first = connectToOnlyMudlib(server)) {
                first.setSoTimeout(5000);
                assertTrue(readUntilContains(first, "Attached player 1").contains("Attached player 1"));

                try (Socket second = connectToOnlyMudlib(server)) {
                    second.setSoTimeout(5000);
                    assertTrue(readUntilContains(second, "Attached player 2").contains("Attached player 2"));

                    first.getOutputStream().write("touch\n".getBytes(StandardCharsets.UTF_8));
                    first.getOutputStream().flush();
                    assertTrue(readUntilContains(first, "touch 1").contains("touch 1"));

                    second.getOutputStream().write("touch\n".getBytes(StandardCharsets.UTF_8));
                    second.getOutputStream().flush();
                    assertTrue(readUntilContains(second, "touch 2").contains("touch 2"));

                    first.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                    second.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                    first.getOutputStream().flush();
                    second.getOutputStream().flush();
                }
            }
        }
    }

    @Test
    void telnetConnectionsBindRuntimeSessionsAndRouteTargetedOutput() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void init() {
                    add_action("who");
                    add_verb("who");
                    add_action("poke");
                    add_verb("poke");
                }

                int who(mixed str) {
                    write("users=" + sizeof(users()) + " ip=" + query_ip_number(this_player()) + "\\n");
                    return 1;
                }

                int poke(mixed str) {
                    object *list;

                    list = users();
                    tell_object(list[1], "poke from " + query_ip_number(this_player()) + "\\n");
                    write("sent\\n");
                    return 1;
                }
                """);
        installMinimalMudlibPlayer(tempDir, "room/village/vill_green");

        try (EmbeddedMudlibHost server = new EmbeddedMudlibHost(
                "127.0.0.1", 0, tempDir, DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket first = connectToOnlyMudlib(server)) {
                first.setSoTimeout(5000);
                assertTrue(readUntilContains(first, "Attached player 1").contains("Attached player 1"));
                // Complete the first login before starting the second; relay scheduling need not preserve accept order.
                try (Socket second = connectToOnlyMudlib(server)) {
                    second.setSoTimeout(5000);
                    assertTrue(readUntilContains(second, "Attached player 2").contains("Attached player 2"));

                    first.getOutputStream().write("who\n".getBytes(StandardCharsets.UTF_8));
                    first.getOutputStream().flush();
                    String who = readUntilContains(first, "users=2 ip=127.0.0.1");
                    assertTrue(who.contains("users=2 ip=127.0.0.1"));

                    first.getOutputStream().write("poke\n".getBytes(StandardCharsets.UTF_8));
                    first.getOutputStream().flush();
                    assertTrue(readUntilContains(first, "sent").contains("sent"));
                    assertTrue(readUntilContains(second, "poke from 127.0.0.1").contains("poke from 127.0.0.1"));

                    first.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                    second.getOutputStream().write("//quit\n".getBytes(StandardCharsets.UTF_8));
                    first.getOutputStream().flush();
                    second.getOutputStream().flush();
                }
            }
        }
    }

    @Test
    void bootPreloadsExplicitManifestAndRegistersStartingPlaceWithoutPlayerHandle() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve(DEFAULT_CONFIG_PATH), """
                mfun_object = jvmud/mfuns
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                preload_file = init_file
                initial_place = room/village/vill_green
                """);
        Files.writeString(tempDir.resolve("init_file"), """
                # preload one simple object
                obj/preload.c
                """);
        Files.writeString(tempDir.resolve("obj/preload.c"), """
                string short() {
                    return "preloaded object";
                }
                """);
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void init() {
                    add_action("north");
                    add_verb("north");
                }

                void long(mixed str) {
                    write("You are on the green.\\n");
                }

                int north(mixed str) {
                    call_other(this_player(), "move_player", "north#room/village/church");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("room/village/church.c"), """
                void init() {
                    add_action("south");
                    add_verb("south");
                }

                void long(mixed str) {
                    write("You are in the church.\\n");
                }

                int south(mixed str) {
                    call_other(this_player(), "move_player", "south#room/village/vill_green");
                    return 1;
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .build());
        CoreEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, tempDir, DEFAULT_CONFIG_PATH, true).boot();

        assertEquals(1, result.preloadedObjects().size());
        assertTrue(result.preloadedObjects().contains("obj/preload"));
        assertEquals("room/village/vill_green", result.initialPlacePath());
        assertTrue(result.skippedPreloads().isEmpty());
        assertEquals(List.of("obj/preload"), result.preloadManifestPreloadedObjects());
        assertTrue(result.preloadManifestSkippedPreloads().isEmpty());
    }

    @Test
    void bootReportsProgressAroundConfiguredAndManifestPreloads() throws Exception {
        Files.createDirectories(tempDir.resolve("config"));
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("config/startup"), """
                preload_objects = obj/configured
                preload_file = init_file
                """);
        Files.writeString(tempDir.resolve("init_file"), """
                obj/preload
                obj/broken
                """);
        Files.writeString(tempDir.resolve("obj/configured.c"), """
                string short() {
                    return "configured";
                }
                """);
        Files.writeString(tempDir.resolve("obj/preload.c"), """
                string short() {
                    return "preload";
                }
                """);
        Files.writeString(tempDir.resolve("obj/broken.c"), "int broken( { return 1; }\n");

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .build());
        CoreEfuns.registerCore(runtime);
        List<String> progressEvents = new java.util.ArrayList<>();

        MudlibBootResult result = new MudlibBoot(
                        runtime,
                        tempDir,
                        "config/startup",
                        false,
                        new MudlibBootProgress() {
                            @Override
                            public void preloadStarted(PreloadKind kind, String sourcePath) {
                                progressEvents.add("start " + kind + " " + sourcePath);
                            }

                            @Override
                            public void preloadFinished(PreloadKind kind, String sourcePath, boolean loaded) {
                                progressEvents.add("finish " + kind + " " + sourcePath + " " + loaded);
                            }

                            @Override
                            public void preloadFailed(PreloadKind kind, String sourcePath, Throwable error) {
                                progressEvents.add("failed " + kind + " " + sourcePath + " "
                                        + error.getClass().getSimpleName());
                            }
                        })
                .boot();

        assertTrue(result.preloadedObjects().contains("obj/configured"));
        assertTrue(result.preloadManifestPreloadedObjects().contains("obj/preload"));
        assertTrue(result.preloadManifestSkippedPreloads().contains("obj/broken"));
        assertEquals(List.of(
                "start CONFIGURED_OBJECT obj/configured",
                "finish CONFIGURED_OBJECT obj/configured true",
                "start MANIFEST_OBJECT obj/preload",
                "finish MANIFEST_OBJECT obj/preload true",
                "start MANIFEST_OBJECT obj/broken",
                "failed MANIFEST_OBJECT obj/broken LPCRuntimeException",
                "finish MANIFEST_OBJECT obj/broken false"), progressEvents);
    }

    @Test
    void objectLoadObserverReportsIndirectStartupLoads() throws Exception {
        Files.createDirectories(tempDir.resolve("config"));
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("config/startup"), """
                lifecycle.object_loaded = reset
                preload_objects = obj/preload
                """);
        Files.writeString(tempDir.resolve("obj/preload.c"), """
                void reset() {
                    jvmud_load_lpc_object("/obj/dependency");
                }
                """);
        Files.writeString(tempDir.resolve("obj/dependency.c"), """
                string short() {
                    return "dependency";
                }
                """);

        List<String> objectLoadEvents = new java.util.ArrayList<>();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .objectLoadObserver(new LPCObjectLoadObserver() {
                    @Override
                    public void objectLoadStarted(String objectId, Path sourcePath, int depth) {
                        objectLoadEvents.add("start " + depth + " " + objectId);
                    }

                    @Override
                    public void objectLoadFinished(
                            String objectId, Path sourcePath, int depth, boolean loaded, long elapsedNanos) {
                        objectLoadEvents.add("finish " + depth + " " + objectId + " " + loaded);
                    }

                    @Override
                    public void objectCompileStarted(String objectId, Path sourcePath) {
                        objectLoadEvents.add("compile-start " + objectId);
                    }

                    @Override
                    public void objectCompileFinished(
                            String objectId, Path sourcePath, boolean compiled, long elapsedNanos) {
                        objectLoadEvents.add("compile-finish " + objectId + " " + compiled);
                    }
                })
                .build());
        CoreEfuns.registerCore(runtime);

        new MudlibBoot(runtime, tempDir, "config/startup", false).boot();

        assertEquals(List.of(
                "start 0 obj/preload",
                "compile-start obj/preload",
                "compile-finish obj/preload true",
                "start 1 obj/dependency",
                "compile-start obj/dependency",
                "compile-finish obj/dependency true",
                "finish 1 obj/dependency true",
                "finish 0 obj/preload true"), objectLoadEvents);
    }

    @Test
    void objectLoadObserverReportsRuntimeFailureCause() throws Exception {
        Files.writeString(tempDir.resolve("broken.c"), """
                void reset() {
                    jvmud_raise_error("missing setup");
                }
                """);

        List<String> objectLoadEvents = new java.util.ArrayList<>();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .objectLoadObserver(new LPCObjectLoadObserver() {
                    @Override
                    public void objectLoadFailed(String objectId, Path sourcePath, int depth, Throwable failure) {
                        objectLoadEvents.add("failed " + depth + " " + objectId + " " + failure.getMessage());
                    }

                    @Override
                    public void objectLoadFinished(
                            String objectId, Path sourcePath, int depth, boolean loaded, long elapsedNanos) {
                        objectLoadEvents.add("finish " + depth + " " + objectId + " " + loaded);
                    }
                })
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(MudlibBoundary.builder()
                .lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED, "reset")
                .build());

        assertThrows(RuntimeException.class, () -> runtime.load("broken"));

        assertEquals(2, objectLoadEvents.size());
        assertTrue(objectLoadEvents.get(0).startsWith("failed 0 broken "), objectLoadEvents.toString());
        assertTrue(objectLoadEvents.get(0).contains("missing setup"), objectLoadEvents.toString());
        assertEquals("finish 0 broken false", objectLoadEvents.get(1));
    }

    @Test
    void startupObjectLoadTraceSummarizesUniqueObjectsAndAttempts() {
        io.github.protasm.jvmud.execution.instance.StartupObjectLoadTrace trace = new io.github.protasm.jvmud.execution.instance.StartupObjectLoadTrace(true);
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
            trace.objectLoadFinished("obj/one", tempDir.resolve("obj/one.c"), 0, true, 1_000_000);
            trace.objectLoadFinished("obj/one", tempDir.resolve("obj/one.c"), 0, true, 2_000_000);
            trace.objectLoadFinished("obj/two", tempDir.resolve("obj/two.c"), 0, true, 3_000_000);
            trace.objectLoadFinished("obj/broken", tempDir.resolve("obj/broken.c"), 0, false, 4_000_000);
            trace.objectCompileFinished("obj/one", tempDir.resolve("obj/one.c"), true, 5_000_000);
            trace.objectCompileFinished("obj/one", tempDir.resolve("obj/one.c"), true, 6_000_000);
            trace.objectCompileFinished("obj/two", tempDir.resolve("obj/two.c"), true, 7_000_000);
            trace.objectCompileFinished("obj/broken", tempDir.resolve("obj/broken.c"), false, 8_000_000);
        } finally {
            System.setOut(originalOut);
        }

        assertEquals(
                "startup object load summary: loaded 2 unique object(s) across 3 load attempt(s), "
                        + "failed 1 unique object(s) across 1 load attempt(s).\n"
                        + "startup compile summary: compiled 2 unique object(s) across 3 compile attempt(s), "
                        + "failed 1 unique object(s) across 1 compile attempt(s).",
                trace.summary());

        trace.finishStartup();
        trace.objectLoadFinished("obj/later", tempDir.resolve("obj/later.c"), 0, true, 9_000_000);
        trace.objectCompileFinished("obj/later", tempDir.resolve("obj/later.c"), true, 10_000_000);

        assertEquals(
                "startup object load summary: loaded 2 unique object(s) across 3 load attempt(s), "
                        + "failed 1 unique object(s) across 1 load attempt(s).\n"
                        + "startup compile summary: compiled 2 unique object(s) across 3 compile attempt(s), "
                        + "failed 1 unique object(s) across 1 compile attempt(s).",
                trace.summary());
    }

    @Test
    void startupObjectLoadTracePrintsFailureCause() {
        io.github.protasm.jvmud.execution.instance.StartupObjectLoadTrace trace = new io.github.protasm.jvmud.execution.instance.StartupObjectLoadTrace(true);
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            trace.objectLoadFailed(
                    "obj/broken",
                    tempDir.resolve("obj/broken.c"),
                    1,
                    new IllegalStateException("database not installed"));
            trace.objectCompileFailed(
                    "obj/uncompiled",
                    tempDir.resolve("obj/uncompiled.c"),
                    new IllegalArgumentException("bad syntax"));
        } finally {
            System.setOut(originalOut);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains(
                "  startup object /obj/broken: failed because IllegalStateException: database not installed"), text);
        assertTrue(text.contains(
                "startup compile /obj/uncompiled: failed because IllegalArgumentException: bad syntax"), text);
    }

    @Test
    void bootDiscoversDedicatedMudlibDeclarationObject() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mudlib.c"), """
                string mfun_object() {
                    return "/jvmud/functions.c";
                }

                string player_prompt() {
                    return "% ";
                }

                mixed handled_lifecycle_events() {
                    return ({ "object-initialized", "scheduled tick" });
                }
                """);
        Files.writeString(tempDir.resolve("jvmud/test.config"), """
                mudlib_object = jvmud/mudlib
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .build());
        CoreEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, tempDir, "jvmud/test.config").boot();
        MudlibBoundary boundary = result.worldRuntime().mudlibBoundary();

        assertEquals("jvmud/mudlib", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("jvmud/functions", boundary.mfunObjectPath().orElseThrow());
        assertEquals("% ", boundary.playerPrompt().orElseThrow());
        assertTrue(boundary.handles(MudlibLifecycleEvent.OBJECT_LOADED));
        assertTrue(boundary.handles(MudlibLifecycleEvent.SCHEDULED_TICK));
        assertEquals(boundary, runtime.mudlibBoundary());
        assertTrue(result.preloadedObjects().contains("jvmud/mudlib"));
    }

    @Test
    void bootReadsMudlibConfigObjectFromExplicitPath() throws Exception {
        Files.createDirectories(tempDir.resolve("config"));
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("place"));
        Files.writeString(tempDir.resolve("config/startup"), """
                game_id = strange-new-mudlib
                game_name = Strange New Mudlib
                mudlib_object = config/mudlib
                mfun_object = config/mfuns
                player_prompt = "$ "
                initial_place = place/start
                preload_objects = obj/preload
                lifecycle.object_loaded = on_loaded
                lifecycle.interaction_scope_started = on_scope
                temporal_tick_method = heartbeat
                temporal_tick_interval = 5
                """);
        Files.writeString(tempDir.resolve("obj/preload.c"), """
                string short() {
                    return "preload";
                }
                """);
        Files.writeString(tempDir.resolve("config/mudlib.c"), """
                string player_prompt() {
                    return "% ";
                }
                """);
        Files.writeString(tempDir.resolve("place/start.c"), """
                string short() {
                    return "start";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .build());
        CoreEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, tempDir, "config/startup").boot();
        MudlibBoundary boundary = result.worldRuntime().mudlibBoundary();

        assertEquals("strange-new-mudlib", boundary.gameId().orElseThrow());
        assertEquals("Strange New Mudlib", boundary.gameName().orElseThrow());
        assertEquals("config/mudlib", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("config/mfuns", boundary.mfunObjectPath().orElseThrow());
        assertEquals("$ ", boundary.playerPrompt().orElseThrow());
        assertEquals("place/start", boundary.initialPlacePath().orElseThrow());
        assertEquals("heartbeat", boundary.temporalTickMethod().orElseThrow());
        assertEquals(5, boundary.temporalTickIntervalSeconds());
        assertEquals("on_loaded", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED).orElseThrow());
        assertEquals("on_scope", boundary.lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED).orElseThrow());
        assertTrue(result.preloadedObjects().contains("config/mudlib"));
        assertTrue(result.preloadedObjects().contains("obj/preload"));
        assertEquals("place/start", result.initialPlacePath());
    }

    @Test
    void bootPreservesConfigCompatibilityDataWhenBoundaryObjectIsMerged() throws Exception {
        Files.createDirectories(tempDir.resolve("config"));
        Files.writeString(tempDir.resolve("config/startup"), """
                mudlib_object = config/mudlib
                database.url = jdbc:test://localhost/mud
                database.user = muduser
                database.password = mudpass
                engine_function.jvmud_size = sizeof
                compatibility.predefine.__VERSION_MAJOR__ = 3
                compatibility.predefine.__VERSION_MINOR__ = 6
                compatibility.function_predefine.__EFUN_DEFINED__.text_width = 0
                """);
        Files.writeString(tempDir.resolve("config/mudlib.c"), """
                string player_prompt() {
                    return "% ";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .build());
        CoreEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, tempDir, "config/startup", false).boot();
        MudlibBoundary boundary = result.worldRuntime().mudlibBoundary();

        assertEquals("config/mudlib", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("% ", boundary.playerPrompt().orElseThrow());
        assertEquals("jdbc:test://localhost/mud", boundary.databaseJdbcUrl().orElseThrow());
        assertEquals("muduser", boundary.databaseUser().orElseThrow());
        assertEquals("mudpass", boundary.databasePassword().orElseThrow());
        assertEquals("jvmud_size", boundary.engineFunctionAliases().get("sizeof"));
        assertEquals("3", boundary.compatibilityPredefines().get("__VERSION_MAJOR__"));
        assertEquals("6", boundary.compatibilityPredefines().get("__VERSION_MINOR__"));
        assertEquals(
                Map.of("text_width", "0"),
                boundary.compatibilityFunctionPredefines().get("__EFUN_DEFINED__"));
        assertEquals(boundary, runtime.mudlibBoundary());
    }

    private String uniqueAccountId(String prefix) {
        String suffix = Long.toString(System.nanoTime(), 36);
        return (prefix + suffix).toLowerCase();
    }

    private Path lp245TestRoot() throws IOException {
        Path source = repositoryRoot().resolve("mudlibs/lp245");
        Path target = tempDir.resolve("lp245-" + Long.toString(System.nanoTime(), 36));
        copyMudlibTreeWithoutSavedAccounts(source, target);
        return target;
    }

    private void copyMudlibTreeWithoutSavedAccounts(Path source, Path target) throws IOException {
        try (var paths = Files.walk(source)) {
            paths.forEach(path -> copyMudlibPathWithoutSavedAccounts(source, target, path));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private void copyMudlibPathWithoutSavedAccounts(Path source, Path target, Path path) {
        Path relative = source.relativize(path);
        if (isSavedAccountFile(relative)) {
            return;
        }

        Path destination = target.resolve(relative.toString());
        try {
            if (Files.isDirectory(path)) {
                Files.createDirectories(destination);
            } else {
                Files.copy(path, destination);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isSavedAccountFile(Path relative) {
        return relative.getNameCount() == 2
                && "accounts".equals(relative.getName(0).toString())
                && relative.getFileName().toString().endsWith(".o");
    }

    private void assertPasswordRejected(Socket socket, String password, String expected) throws Exception {
        socket.getOutputStream().write((password + "\n").getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        String rejection = readUntilQuietAfterContains(socket, "Password: ");
        assertTrue(rejection.contains(expected), rejection);
        assertFalse(rejection.contains("Password: > "), rejection);
    }

    private boolean containsTelnetCommand(String text, int command, int option) {
        return text.indexOf("" + (char) 255 + (char) command + (char) option) >= 0;
    }

    private boolean containsGmcpFrame(String text, String messagePrefix) {
        return text.contains("" + (char) 255 + (char) 250 + (char) 201 + messagePrefix)
                && text.contains("" + (char) 255 + (char) 240);
    }

    private void writeGmcpFrame(Socket socket, String message) throws IOException {
        ByteArrayOutputStream frame = new ByteArrayOutputStream();
        frame.write(new byte[] {(byte) 255, (byte) 250, (byte) 201});
        for (byte value : message.getBytes(StandardCharsets.UTF_8)) {
            frame.write(value);
            if (Byte.toUnsignedInt(value) == 255) {
                frame.write(value);
            }
        }
        frame.write(new byte[] {(byte) 255, (byte) 240});
        socket.getOutputStream().write(frame.toByteArray());
        socket.getOutputStream().flush();
    }

    private String printable(String text) {
        return text.replace("\r", "\\r").replace("\n", "\\n");
    }

    private void assertNoBareLineFeeds(String text) {
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == '\n') {
                assertTrue(index > 0 && text.charAt(index - 1) == '\r', printable(text));
            }
        }
    }

    private String readUntilContains(Socket socket, String expected) throws Exception {
        StringBuilder output = new StringBuilder();
        while (!output.toString().contains(expected)) {
            int value = socket.getInputStream().read();
            if (value == -1) {
                break;
            }
            output.append((char) value);
        }
        return output.toString();
    }

    private Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))
                    && Files.isDirectory(current.resolve("mudlibs"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate JVMud repository root.");
    }

    private String readUntilQuietAfterContains(Socket socket, String expected) throws Exception {
        StringBuilder output = new StringBuilder(readUntilContains(socket, expected));
        long deadline = System.nanoTime() + 50_000_000L;
        while (System.nanoTime() < deadline) {
            while (socket.getInputStream().available() > 0) {
                int value = socket.getInputStream().read();
                if (value == -1) {
                    return output.toString();
                }
                output.append((char) value);
            }
            Thread.sleep(5);
        }
        return output.toString();
    }

    private String readUntilSocketClosed(Socket socket) throws Exception {
        StringBuilder output = new StringBuilder();
        while (true) {
            int value = socket.getInputStream().read();
            if (value == -1) {
                return output.toString();
            }
            output.append((char) value);
        }
    }

    private void assertSavedPlayerFile(Path path) throws Exception {
        long deadline = System.nanoTime() + 1_000_000_000L;
        while (System.nanoTime() < deadline) {
            if (Files.isRegularFile(path)) {
                return;
            }
            Thread.sleep(10);
        }
        assertTrue(Files.isRegularFile(path));
    }

    private void assertSavedPlayerJsonFile(Path path) throws Exception {
        long deadline = System.nanoTime() + 1_000_000_000L;
        while (System.nanoTime() < deadline) {
            if (Files.isRegularFile(path) && Files.readString(path).contains("\"format\"")) {
                return;
            }
            Thread.sleep(10);
        }
        assertTrue(Files.isRegularFile(path));
        assertTrue(Files.readString(path).contains("\"format\""));
    }

    private void installMfunShim() throws Exception {
        installMfunShim(tempDir);
    }

    private void installMfunShim(Path mudlibRoot) throws Exception {
        Files.createDirectories(mudlibRoot.resolve("jvmud"));
        String config = """
                mfun_object = jvmud/mfuns
                language_features = protected_evaluation, typed_function_literals, inline_callables, multi_value_mappings, varargs
                engine_capabilities = mudlib_files, database, session_control, host_control
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                """;
        Files.writeString(mudlibRoot.resolve(DEFAULT_CONFIG_PATH), config);
        Files.writeString(mudlibRoot.resolve(LP245_CONFIG_PATH), config);
        Files.writeString(mudlibRoot.resolve("jvmud/mudlib.c"), """
                string mfun_object() {
                    return "jvmud/mfuns";
                }
                """);
        Files.writeString(mudlibRoot.resolve("jvmud/mfuns.c"), """
                void write(mixed value) {
                    jvmud_write(value);
                }

                void tell_object(object target, mixed value) {
                    jvmud_write_to_lpc_object(target, value);
                }

                void say(mixed value) {
                    jvmud_emit_perceivable(jvmud_current_actor(), value);
                }

                void say(mixed value, object excluded) {
                    jvmud_emit_perceivable_except(jvmud_current_actor(), value, excluded);
                }

                int sizeof(mixed value) {
                    return jvmud_size(value);
                }

                object *users() {
                    return jvmud_users();
                }

                int query_idle(mixed player) {
                    return jvmud_query_idle(player);
                }

                mixed query_ip_number(mixed player) {
                    return jvmud_query_ip_number(player);
                }

                int save_object(string path) {
                    return jvmud_save_lpc_object_state(path);
                }

                int restore_object(string path) {
                    return jvmud_restore_lpc_object_state(path);
                }

                string ctime(int timestamp) {
                    return jvmud_format_time(timestamp);
                }

                void enable_commands() {
                    jvmud_enable_commands();
                }

                void add_action(string method) {
                    jvmud_add_action(method);
                }

                void add_action(string method, string verb) {
                    jvmud_add_action(method, verb);
                }

                void add_verb(string verb) {
                    jvmud_add_verb(verb);
                }

                void input_to(string method) {
                    jvmud_capture_session_input(method, 0);
                }

                void input_to(string method, int noecho) {
                    jvmud_capture_session_input(method, noecho);
                }

                void input_to(string method, int noecho, mixed arg1) {
                    jvmud_capture_session_input(method, noecho, arg1);
                }

                void input_to(string method, int noecho, mixed arg1, mixed arg2) {
                    jvmud_capture_session_input(method, noecho, arg1, arg2);
                }

                object this_player() {
                    return jvmud_current_actor();
                }


                mixed call_other(mixed target, string method) {
                    return jvmud_invoke_lpc_object(target, method);
                }

                mixed call_other(mixed target, string method, mixed arg) {
                    return jvmud_invoke_lpc_object(target, method, arg);
                }

                int cat(string path) {
                    mixed text;

                    text = jvmud_read_mudlib_text(path);
                    if (!stringp(text))
                        return 0;

                    write(text);
                    return 1;
                }

                string capitalize(mixed value) {
                    return jvmud_capitalize_text(value);
                }

                mixed creator(mixed ob) {
                    return 0;
                }

                object environment() {
                    return jvmud_entity_location();
                }

                object environment(mixed ob) {
                    return jvmud_entity_location(ob);
                }

                string extract(mixed value, int from) {
                    return jvmud_extract_text(value, from);
                }

                string extract(mixed value, int from, int to) {
                    return jvmud_extract_text(value, from, to);
                }

                void move_object(mixed ob, mixed destination) {
                    jvmud_move_entity(ob, destination);
                }

                string lower_case(mixed value) {
                    return jvmud_lowercase_text(value);
                }

                status stringp(mixed value) {
                    return jvmud_is_string(value);
                }

                object this_object() {
                    return jvmud_current_lpc_object();
                }

                void destruct(object ob) {
                    jvmud_destroy_lpc_object(ob);
                }
                """);
    }

    private void installMinimalMudlibPlayer(Path mudlibRoot, String initialPlace) throws Exception {
        Files.createDirectories(mudlibRoot.resolve("obj"));
        Files.writeString(mudlibRoot.resolve("obj/player.c"), """
                string query_name() {
                    return "mudlib player";
                }

                string query_real_name() {
                    return "mudlib player";
                }

                int query_level() {
                    return 0;
                }

                int query_invis() {
                    return 0;
                }

                int remove_ghost() {
                    return 1;
                }

                int id(mixed value) {
                    return value == "player" || value == "me";
                }
                """);
        String playerConfig = "player_object = obj/player\ninitial_place = " + initialPlace + "\n";
        Files.writeString(
                mudlibRoot.resolve(DEFAULT_CONFIG_PATH),
                playerConfig,
                StandardOpenOption.APPEND);
        Files.writeString(
                mudlibRoot.resolve(LP245_CONFIG_PATH),
                playerConfig,
                StandardOpenOption.APPEND);
    }
}
