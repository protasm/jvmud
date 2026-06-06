package io.github.protasm.jvmud.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LpcRuntime;
import io.github.protasm.jvmud.compiler.exec.LpcRuntimeConfig;
import io.github.protasm.jvmud.runtime.Capability;
import io.github.protasm.jvmud.runtime.Entity;
import io.github.protasm.jvmud.runtime.Link;
import io.github.protasm.jvmud.runtime.MudlibBoundary;
import io.github.protasm.jvmud.runtime.MudlibLifecycleEvent;
import io.github.protasm.jvmud.runtime.Place;
import io.github.protasm.jvmud.runtime.World;
import io.github.protasm.jvmud.runtime.WorldRuntime;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class AdminCliTest {
    @TempDir
    Path tempDir;

    @Test
    void telnetServerLaunchOptionsDefaultToLocalMudlibStart() {
        TelnetServer.LaunchOptions options = TelnetServer.parseLaunchOptions(new String[0]);

        assertEquals(Path.of("mudlib"), options.mudlibRoot());
        assertEquals(4000, options.port());
        assertEquals("localhost", options.bindAddress());
        assertEquals(MudlibBoot.DEFAULT_CONFIG_PATH, options.configObjectPath());
        assertFalse(options.help());
    }

    @Test
    void telnetServerLaunchOptionsAcceptNamedFlags() {
        TelnetServer.LaunchOptions options = TelnetServer.parseLaunchOptions(new String[] {
                "-mudlib-dir", "mudlib",
                "-port", "4303",
                "-host", "127.0.0.1",
                "-config", "jvmud/config"
        });

        assertEquals(Path.of("mudlib"), options.mudlibRoot());
        assertEquals(4303, options.port());
        assertEquals("127.0.0.1", options.bindAddress());
        assertEquals("jvmud/config", options.configObjectPath());
    }

    @Test
    void telnetServerLaunchOptionsStillAcceptLegacyPositionals() {
        TelnetServer.LaunchOptions options = TelnetServer.parseLaunchOptions(new String[] {
                "custom-mudlib", "4303", "0.0.0.0", "custom/config"
        });

        assertEquals(Path.of("custom-mudlib"), options.mudlibRoot());
        assertEquals(4303, options.port());
        assertEquals("0.0.0.0", options.bindAddress());
        assertEquals("custom/config", options.configObjectPath());
    }

    @Test
    void telnetServerLaunchOptionsRejectBadFlags() {
        assertThrows(IllegalArgumentException.class, () ->
                TelnetServer.parseLaunchOptions(new String[] {"-port"}));
        assertThrows(IllegalArgumentException.class, () ->
                TelnetServer.parseLaunchOptions(new String[] {"-port", "70000"}));
        assertThrows(IllegalArgumentException.class, () ->
                TelnetServer.parseLaunchOptions(new String[] {"-bogus", "value"}));
    }

    @Test
    void telnetSessionAcceptsPlayerCommandsOverSocket() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void long(mixed str) {
                    write("A test green.\\n");
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                String initial = readUntilPrompt(socket);
                assertTrue(initial.contains("JVMud telnet."));

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String look = readUntilPrompt(socket);
                assertTrue(look.contains("A test green."));

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
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
                void long(mixed str) {
                    write("A spaced-path green.\\n");
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, mudlibRoot, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                readUntilPrompt(socket);

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String look = readUntilPrompt(socket);
                assertTrue(look.contains("A spaced-path green."));

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
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

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket first = new Socket("127.0.0.1", server.port())) {
                first.setSoTimeout(5000);
                assertTrue(readUntilPrompt(first).contains("Attached player 1"));

                try (Socket second = new Socket("127.0.0.1", server.port())) {
                    second.setSoTimeout(5000);
                    assertTrue(readUntilPrompt(second).contains("Attached player 2"));

                    first.getOutputStream().write("touch\n".getBytes(StandardCharsets.UTF_8));
                    first.getOutputStream().flush();
                    assertTrue(readUntilPrompt(first).contains("touch 1"));

                    second.getOutputStream().write("touch\n".getBytes(StandardCharsets.UTF_8));
                    second.getOutputStream().flush();
                    assertTrue(readUntilPrompt(second).contains("touch 2"));

                    first.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                    second.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
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

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket first = new Socket("127.0.0.1", server.port());
                    Socket second = new Socket("127.0.0.1", server.port())) {
                first.setSoTimeout(5000);
                second.setSoTimeout(5000);
                assertTrue(readUntilPrompt(first).contains("Attached player 1"));
                assertTrue(readUntilPrompt(second).contains("Attached player 2"));

                first.getOutputStream().write("who\n".getBytes(StandardCharsets.UTF_8));
                first.getOutputStream().flush();
                String who = readUntilPrompt(first);
                assertTrue(who.contains("users=2 ip=127.0.0.1"));

                first.getOutputStream().write("poke\n".getBytes(StandardCharsets.UTF_8));
                first.getOutputStream().flush();
                assertTrue(readUntilPrompt(first).contains("sent"));
                assertTrue(readUntilContains(second, "poke from 127.0.0.1").contains("poke from 127.0.0.1"));

                first.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                second.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                first.getOutputStream().flush();
                second.getOutputStream().flush();
            }
        }
    }

    @Test
    void adminCanLoadCallCloneMoveInspectAndQuit() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("room.c"), """
                void long(mixed str) {
                    if (str)
                        write("A specific thing.\\n");
                    else
                        write("A readable room.\\n");
                }
                """);
        Files.writeString(tempDir.resolve("thing.c"), """
                string name = "a small thing";
                int value = 7;

                status id(mixed str) {
                    return str == "thing";
                }

                string short() {
                    return name;
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("boot " + tempDir);
        cli.execute("load room");
        cli.execute("clone thing");
        cli.execute("move thing room");
        cli.execute("where thing");
        cli.execute("inspect thing");
        cli.execute("look room");
        cli.execute("call thing short");
        cli.execute("objects");
        cli.execute("destruct thing");
        cli.execute("quit");

        String output = transcript.toString();
        assertTrue(output.contains("Booted runtime"));
        assertTrue(output.contains("Loaded room"));
        assertTrue(output.contains("Cloned thing as thing"));
        assertTrue(output.contains("thing is in room"));
        assertTrue(output.contains("runtime id: thing#clone"));
        assertTrue(output.contains("environment: room"));
        assertTrue(output.contains("string name = \"a small thing\""));
        assertTrue(output.contains("[thing]"));
        assertTrue(output.contains("int value = 7"));
        assertTrue(output.contains("status id(mixed)"));
        assertTrue(output.contains("string short()"));
        assertTrue(output.contains("A readable room."));
        assertTrue(output.contains("=> a small thing"));
        assertTrue(output.contains("Destructed thing"));
        assertFalse(cli.isRunning());
    }

    @Test
    void virtualFilesystemNavigatesWithinMudlibRoot() throws Exception {
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), "string short() { return \"start\"; }\n");
        Files.writeString(tempDir.resolve("README.txt"), "mudlib readme\nsecond line\n");

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("boot " + tempDir);
        cli.execute("pwd");
        cli.execute("ls");
        cli.execute("cd room");
        cli.execute("pwd");
        cli.execute("ls");
        cli.execute("cat /README.txt");
        cli.execute("load start");

        String output = transcript.toString();
        assertTrue(output.contains("/\n"));
        assertTrue(output.contains("room/"));
        assertTrue(output.contains("/room"));
        assertTrue(output.contains("start.c"));
        assertTrue(output.contains("   1  mudlib readme"));
        assertTrue(output.contains("   2  second line"));
        assertTrue(output.contains("Loaded room/start"));
    }

    @Test
    void virtualFilesystemRejectsPathsOutsideMudlibRoot() {
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("boot " + tempDir);
        cli.execute("cd ..");
        cli.execute("cat ../outside.txt");

        String output = transcript.toString();
        assertTrue(output.contains("Error: Path escapes mudlib root: .."));
        assertTrue(output.contains("Error: Path escapes mudlib root: ../outside.txt"));
    }

    @Test
    void localSessionMovementRegistersNativeWorldLinks() throws Exception {
        Files.createDirectories(tempDir.resolve("room"));
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void long(mixed str) {
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        WorldRuntime worldRuntime = new WorldRuntime(new World("test", "Test World"));
        Place church = worldRuntime.createPlace("room/village/church", "Village church");
        Entity actorEntity = worldRuntime.createEntity(
                "session/local", "local session", church, Capability.ACTOR, Capability.PERCEPTIVE);
        LocalSessionActor actor = new LocalSessionActor(runtime, worldRuntime, actorEntity, "tester");

        assertEquals(1, actor.move_player("south#room/village/vill_green"));

        Place green = worldRuntime.place("room/village/vill_green");
        Link south = worldRuntime.linkFrom(church, "south");
        assertEquals(green, south.destination());
        assertEquals(church, south.origin());
        assertEquals(green, worldRuntime.locationOf(actorEntity));

        Files.writeString(tempDir.resolve("room/bad.c"), "int broken() { return ; }\n");
        assertEquals(0, actor.move_player("east#room/bad"));
        assertEquals(green, worldRuntime.locationOf(actorEntity));
    }

    @Test
    void helpListsCommandsVerticallyWithDescriptions() {
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("help");

        String output = transcript.toString();
        assertTrue(output.contains("Admin commands:"));
        assertTrue(output.contains("l  load <path>"));
        assertTrue(output.contains("Compile, load, and register an LPC object."));
        assertTrue(output.contains("v  verbosity [quiet|normal|watch]"));
        assertTrue(output.contains("Show or change compiler/shell output detail."));
        assertTrue(output.indexOf("h  help") < output.indexOf("b  boot"));
        assertTrue(output.indexOf("b  boot") < output.indexOf("call"));
        assertTrue(output.indexOf("call") < output.indexOf("cat"));
        assertTrue(output.indexOf("cat") < output.indexOf("cd"));
        assertTrue(output.indexOf("cd") < output.indexOf("n  clone"));
        assertTrue(output.indexOf("n  clone") < output.indexOf("x  destruct"));
        assertTrue(output.indexOf("x  destruct") < output.indexOf("i  inspect"));
        assertTrue(output.indexOf("i  inspect") < output.indexOf("l  load"));
        assertTrue(output.indexOf("ls") < output.indexOf("m  move"));
        assertTrue(output.indexOf("pwd") < output.indexOf("r  reload"));
        assertTrue(output.indexOf("w  where") < output.indexOf("q  quit"));
    }

    @Test
    void verbosityControlsRoutineAndCompilerOutput() throws Exception {
        Files.writeString(tempDir.resolve("quiet.c"), """
                string short() {
                    return "quiet";
                }
                """);
        Files.writeString(tempDir.resolve("watch.c"), """
                string short() {
                    return "watch";
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("boot " + tempDir);
        cli.execute("verbosity quiet");
        cli.execute("load quiet");
        cli.execute("verbosity watch");
        cli.execute("load watch");

        String output = transcript.toString();
        assertTrue(output.contains("verbosity set to quiet"));
        assertTrue(!output.contains("Loaded quiet"));
        assertTrue(output.contains("verbosity set to watch"));
        assertTrue(output.contains("[compile] watch scan..."));
        assertTrue(output.contains("[compile] watch compile ok"));
        assertTrue(output.contains("Loaded watch"));
    }

    @Test
    void remainingSingleCharacterAliasesExecuteCommands() throws Exception {
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/item.c"), """
                string short() {
                    return "aliased item";
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("b " + tempDir);
        cli.execute("cd room");
        cli.execute("pwd");
        cli.execute("ls");
        cli.execute("l item");
        cli.execute("r item");
        cli.execute("i room/item");
        cli.execute("call room/item short");
        cli.execute("o");
        cli.execute("v quiet");
        cli.execute("q");

        String output = transcript.toString();
        assertTrue(output.contains("Booted runtime"));
        assertTrue(output.contains("/room"));
        assertTrue(output.contains("item.c"));
        assertTrue(output.contains("Loaded room/item"));
        assertTrue(output.contains("Reloaded room/item"));
        assertTrue(output.contains("runtime id: room/item"));
        assertTrue(output.contains("=> aliased item"));
        assertTrue(output.contains("room/item :"));
        assertFalse(output.contains("room.item"));
        assertTrue(output.contains("verbosity set to quiet"));
        assertFalse(cli.isRunning());
    }

    @Test
    void removedSingleCharacterAliasesAreNotCommands() {
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("a room/item short");
        cli.execute("t README.txt");
        cli.execute("d room");
        cli.execute("s");
        cli.execute("p");

        String output = transcript.toString();
        assertTrue(output.contains("Unknown command: a"));
        assertTrue(output.contains("Unknown command: t"));
        assertTrue(output.contains("Unknown command: d"));
        assertTrue(output.contains("Unknown command: s"));
        assertTrue(output.contains("Unknown command: p"));
        assertTrue(output.contains("Usage: help"));
    }

    @Test
    void commandErrorsIncludeUsage() {
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("call missingOnlyMethod");
        cli.execute("inspect");

        String output = transcript.toString();
        assertTrue(output.contains("Error: Missing argument 1 for call"));
        assertTrue(output.contains("Usage: call <handle> <method> [args...]"));
        assertTrue(output.contains("Error: Missing argument 0 for inspect"));
        assertTrue(output.contains("Usage: inspect <handle>"));
    }

    @Test
    void reloadReplacesLoadedObjectWithNewSource() throws Exception {
        Files.writeString(tempDir.resolve("thing.c"), """
                string short() {
                    return "old";
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("boot " + tempDir);
        cli.execute("load thing");
        cli.execute("call thing short");

        Files.writeString(tempDir.resolve("thing.c"), """
                string short() {
                    return "new";
                }
                """);
        cli.execute("reload thing");
        cli.execute("call thing short");

        String output = transcript.toString();
        assertTrue(output.contains("=> old"));
        assertTrue(output.contains("Reloaded thing"));
        assertTrue(output.contains("=> new"));
    }

    @Test
    void bootPreloadsInitFileAndRegistersStartingRoomWithoutPlayerHandle() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/init_file"), """
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

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("boot " + tempDir);
        cli.execute("objects");
        cli.execute("where local/player");

        String output = transcript.toString();
        assertTrue(output.contains("Preloaded 1 startup object(s)."));
        assertFalse(output.contains("Started local session"));
        assertTrue(output.contains("obj/preload : obj/preload"));
        assertTrue(output.contains("room/village/vill_green : room/village/vill_green"));
        assertFalse(output.contains("local/player : local/player"));
        assertTrue(output.contains("Error: Unknown object handle: local/player"));
    }

    @Test
    void bootDiscoversDedicatedMudlibBoundaryDeclarationObject() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/boundary.c"), """
                string mfun_object() {
                    return "/jvmud/functions.c";
                }

                mixed handled_lifecycle_events() {
                    return ({ "object-initialized", "scheduled tick" });
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .build());
        EngineEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, tempDir).boot();
        MudlibBoundary boundary = result.worldRuntime().mudlibBoundary();

        assertEquals("jvmud/boundary", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("jvmud/functions", boundary.mfunObjectPath().orElseThrow());
        assertTrue(boundary.handles(MudlibLifecycleEvent.OBJECT_LOADED));
        assertTrue(boundary.handles(MudlibLifecycleEvent.SCHEDULED_TICK));
        assertEquals(boundary, runtime.mudlibBoundary());
        assertTrue(result.preloadedObjects().contains("jvmud/boundary"));
    }

    @Test
    void bootReadsMudlibConfigObjectFromExplicitPath() throws Exception {
        Files.createDirectories(tempDir.resolve("config"));
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("place"));
        Files.writeString(tempDir.resolve("config/startup"), """
                game_id = strange-new-mudlib
                game_name = Strange New Mudlib
                mfun_object = config/mfuns
                initial_place = place/start
                initial_presence_id = presence/local
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
        Files.writeString(tempDir.resolve("place/start.c"), """
                string short() {
                    return "start";
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .build());
        EngineEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, tempDir, "config/startup").boot();
        MudlibBoundary boundary = result.worldRuntime().mudlibBoundary();

        assertEquals("strange-new-mudlib", boundary.gameId().orElseThrow());
        assertEquals("Strange New Mudlib", boundary.gameName().orElseThrow());
        assertEquals("config/startup", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("config/mfuns", boundary.mfunObjectPath().orElseThrow());
        assertEquals("place/start", boundary.initialPlacePath().orElseThrow());
        assertEquals("presence/local", boundary.initialPresenceId().orElseThrow());
        assertEquals("heartbeat", boundary.temporalTickMethod().orElseThrow());
        assertEquals(5, boundary.temporalTickIntervalSeconds());
        assertEquals("on_loaded", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED).orElseThrow());
        assertEquals("on_scope", boundary.lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED).orElseThrow());
        assertTrue(result.preloadedObjects().contains("obj/preload"));
        assertEquals("place/start", result.startingRoom());
        assertEquals("presence/local", result.actorHandle());
    }

    @Test
    void bootDeclaredMfunObjectHandlesUnresolvedCalls() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/boundary.c"), """
                string mfun_object() {
                    return "jvmud/functions";
                }
                """);
        Files.writeString(tempDir.resolve("jvmud/functions.c"), """
                mixed legacy_phrase() {
                    return "handled by mfun object";
                }
                """);
        Files.writeString(tempDir.resolve("caller.c"), """
                mixed phrase() {
                    return legacy_phrase();
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("boot " + tempDir);
        cli.execute("load caller");
        cli.execute("call caller phrase");

        String output = transcript.toString();
        assertTrue(output.contains("Loaded caller"));
        assertTrue(output.contains("=> handled by mfun object"));
    }

    private String readUntilPrompt(Socket socket) throws Exception {
        StringBuilder output = new StringBuilder();
        while (!output.toString().endsWith("> ")) {
            int value = socket.getInputStream().read();
            if (value == -1) {
                break;
            }
            output.append((char) value);
        }
        return output.toString();
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

    private void installMfunShim() throws Exception {
        installMfunShim(tempDir);
    }

    private void installMfunShim(Path mudlibRoot) throws Exception {
        Files.createDirectories(mudlibRoot.resolve("jvmud"));
        Files.writeString(mudlibRoot.resolve("jvmud/config"), """
                mfun_object = jvmud/mfuns
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                """);
        Files.writeString(mudlibRoot.resolve("jvmud/boundary.c"), """
                string mfun_object() {
                    return "jvmud/mfuns";
                }
                """);
        Files.writeString(mudlibRoot.resolve("jvmud/mfuns.c"), """
                void write(mixed value) {
                    jvmud_write(value);
                }

                void tell_object(object target, mixed value) {
                    jvmud_tell_object(target, value);
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

                void add_action(string method) {
                    jvmud_add_action(method);
                }

                void add_action(string method, string verb) {
                    jvmud_add_action(method, verb);
                }

                void add_verb(string verb) {
                    jvmud_add_verb(verb);
                }

                object this_player() {
                    return jvmud_current_actor();
                }

                mixed call_other(mixed target, string method) {
                    return jvmud_invoke_object(target, method);
                }

                mixed call_other(mixed target, string method, mixed arg) {
                    return jvmud_invoke_object(target, method, arg);
                }
                """);
    }
}
