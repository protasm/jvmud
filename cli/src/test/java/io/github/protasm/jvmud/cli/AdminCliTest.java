package io.github.protasm.jvmud.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class AdminCliTest {
    @TempDir
    Path tempDir;

    @Test
    void adminCanLoadCallCloneMoveInspectAndQuit() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("room.c"), """
                void long() {
                    write("A readable room.\\n");
                }
                """);
        Files.writeString(tempDir.resolve("thing.c"), """
                string name = "a small thing";
                int value = 7;

                status id(str) {
                    return str == "thing";
                }

                string short() {
                    return name;
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("/boot " + tempDir);
        cli.execute("/load room");
        cli.execute("/clone thing");
        cli.execute("/move thing room");
        cli.execute("/where thing");
        cli.execute("/inspect thing");
        cli.execute("/look room");
        cli.execute("/call thing short");
        cli.execute("/objects");
        cli.execute("/destruct thing");
        cli.execute("/quit");

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

        cli.execute("/boot " + tempDir);
        cli.execute("/pwd");
        cli.execute("/ls");
        cli.execute("/cd room");
        cli.execute("/pwd");
        cli.execute("/ls");
        cli.execute("/cat /README.txt");
        cli.execute("/load start");

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

        cli.execute("/boot " + tempDir);
        cli.execute("/cd ..");
        cli.execute("/cat ../outside.txt");

        String output = transcript.toString();
        assertTrue(output.contains("Error: Path escapes mudlib root: .."));
        assertTrue(output.contains("Error: Path escapes mudlib root: ../outside.txt"));
    }

    @Test
    void localSessionMovementRegistersNativeWorldLinks() throws Exception {
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/vill_green.c"), """
                void long(str) {
                }
                """);

        LpcRuntime runtime = new LpcRuntime(LpcRuntimeConfig.builder().baseIncludePath(tempDir).build());
        EngineEfuns.registerCore(runtime);
        WorldRuntime worldRuntime = new WorldRuntime(new World("test", "Test World"));
        Place church = worldRuntime.createPlace("room/church", "Village church");
        Entity actorEntity = worldRuntime.createEntity(
                "session/local", "local session", church, Capability.ACTOR, Capability.PERCEPTIVE);
        LocalSessionActor actor = new LocalSessionActor(runtime, worldRuntime, actorEntity, "tester");

        assertEquals(1, actor.move_player("south#room/vill_green"));

        Place green = worldRuntime.place("room/vill_green");
        Link south = worldRuntime.linkFrom(church, "south");
        assertEquals(green, south.destination());
        assertEquals(church, south.origin());
        assertEquals(green, worldRuntime.locationOf(actorEntity));
    }

    @Test
    void helpListsCommandsVerticallyWithDescriptions() {
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("/help");

        String output = transcript.toString();
        assertTrue(output.contains("Slash commands:"));
        assertTrue(output.contains("l  /load <path>"));
        assertTrue(output.contains("Compile, load, and register an LPC object."));
        assertTrue(output.contains("v  /verbosity [quiet|normal|watch]"));
        assertTrue(output.contains("Show or change compiler/shell output detail."));
        assertTrue(output.indexOf("h  /help") < output.indexOf("b  /boot"));
        assertTrue(output.indexOf("/actor") < output.indexOf("b  /boot"));
        assertTrue(output.indexOf("b  /boot") < output.indexOf("/call"));
        assertTrue(output.indexOf("/call") < output.indexOf("/cat"));
        assertTrue(output.indexOf("/cat") < output.indexOf("/cd"));
        assertTrue(output.indexOf("/cd") < output.indexOf("n  /clone"));
        assertTrue(output.indexOf("n  /clone") < output.indexOf("x  /destruct"));
        assertTrue(output.indexOf("x  /destruct") < output.indexOf("/dispatch <command"));
        assertTrue(output.indexOf("x  /destruct") < output.indexOf("i  /inspect"));
        assertTrue(output.indexOf("i  /inspect") < output.indexOf("l  /load"));
        assertTrue(output.indexOf("/ls") < output.indexOf("m  /move"));
        assertTrue(output.indexOf("/pwd") < output.indexOf("r  /reload"));
        assertTrue(output.indexOf("w  /where") < output.indexOf("q  /quit"));
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

        cli.execute("/boot " + tempDir);
        cli.execute("/verbosity quiet");
        cli.execute("/load quiet");
        cli.execute("/verbosity watch");
        cli.execute("/load watch");

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

        cli.execute("/b " + tempDir);
        cli.execute("/cd room");
        cli.execute("/pwd");
        cli.execute("/ls");
        cli.execute("/l item");
        cli.execute("/r item");
        cli.execute("/i room/item");
        cli.execute("/call room/item short");
        cli.execute("/o");
        cli.execute("/v quiet");
        cli.execute("/q");

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

        cli.execute("/a room/item short");
        cli.execute("/t README.txt");
        cli.execute("/d room");
        cli.execute("/s");
        cli.execute("/p");

        String output = transcript.toString();
        assertTrue(output.contains("Unknown slash command: /a"));
        assertTrue(output.contains("Unknown slash command: /t"));
        assertTrue(output.contains("Unknown slash command: /d"));
        assertTrue(output.contains("Unknown slash command: /s"));
        assertTrue(output.contains("Unknown slash command: /p"));
        assertTrue(output.contains("Usage: /help"));
    }

    @Test
    void commandErrorsIncludeUsage() {
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("/call missingOnlyMethod");
        cli.execute("/inspect");

        String output = transcript.toString();
        assertTrue(output.contains("Error: Missing argument 1 for call"));
        assertTrue(output.contains("Usage: /call <handle> <method> [args...]"));
        assertTrue(output.contains("Error: Missing argument 0 for inspect"));
        assertTrue(output.contains("Usage: /inspect <handle>"));
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

        cli.execute("/boot " + tempDir);
        cli.execute("/load thing");
        cli.execute("/call thing short");

        Files.writeString(tempDir.resolve("thing.c"), """
                string short() {
                    return "new";
                }
                """);
        cli.execute("/reload thing");
        cli.execute("/call thing short");

        String output = transcript.toString();
        assertTrue(output.contains("=> old"));
        assertTrue(output.contains("Reloaded thing"));
        assertTrue(output.contains("=> new"));
    }

    @Test
    void dispatchRunsObjectDefinedVerbForSelectedActor() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("actor.c"), """
                string short() {
                    return "actor";
                }
                """);
        Files.writeString(tempDir.resolve("tool.c"), """
                void init() {
                    add_action("wave");
                    add_verb("wave");
                }

                int wave(str) {
                    write("waved " + str + "\\n");
                    return 1;
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("/boot " + tempDir);
        cli.execute("/load actor");
        cli.execute("/clone tool");
        cli.execute("/move tool actor");
        cli.execute("/actor actor");
        cli.execute("/dispatch wave hello");
        cli.execute("/dispatch dance");

        String output = transcript.toString();
        assertTrue(output.contains("Command actor is actor"));
        assertTrue(output.contains("waved hello"));
        assertTrue(output.contains("=> 1"));
        assertTrue(output.contains("actor does not understand: dance"));
    }

    @Test
    void plainInputDispatchesAsPlayerCommandsAndSlashInputRunsAdminCommands() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("actor.c"), """
                string short() {
                    return "actor";
                }
                """);
        Files.writeString(tempDir.resolve("tool.c"), """
                void init() {
                    add_action("wave");
                    add_verb("wave");
                }

                int wave(str) {
                    write("waved " + str + "\\n");
                    return 1;
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("/boot " + tempDir);
        cli.execute("/load actor");
        cli.execute("/clone tool");
        cli.execute("/move tool actor");
        cli.execute("/actor actor");
        cli.execute("wave hello");
        cli.execute("dance");
        cli.execute("/objects");
        cli.execute("dance");
        cli.execute("/help");
        cli.execute("/objects");

        String output = transcript.toString();
        assertTrue(output.contains("waved hello"));
        assertFalse(output.contains("=> 1"));
        assertTrue(output.contains("You can't do that."));
        assertTrue(output.contains("actor : actor"));
        assertTrue(output.contains("Slash commands:"));
    }

    @Test
    void plainInputRequiresSelectedActor() {
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("/boot " + tempDir);
        cli.execute("look");

        String output = transcript.toString();
        assertTrue(output.contains("No player is active. Use /actor <handle> or /help."));
    }

    @Test
    void plainHelpIsAMudlibCommandNotAShellFallback() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("actor.c"), """
                string short() {
                    return "actor";
                }
                """);
        Files.writeString(tempDir.resolve("guide.c"), """
                void init() {
                    add_action("help");
                    add_verb("help");
                }

                int help(str) {
                    write("Mudlib help.\\n");
                    return 1;
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("/boot " + tempDir);
        cli.execute("/load actor");
        cli.execute("/actor actor");
        cli.execute("help");
        cli.execute("/clone guide");
        cli.execute("/move guide actor");
        cli.execute("help");
        cli.execute("/help");

        String output = transcript.toString();
        assertTrue(output.contains("You can't do that."));
        assertTrue(output.contains("Mudlib help."));
        assertTrue(output.contains("Slash commands:"));
    }

    @Test
    void bootPreloadsInitFileAndStartsLocalActorInStartingRoom() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/init_file"), """
                # preload one simple object
                obj/preload.c
                """);
        Files.writeString(tempDir.resolve("obj/preload.c"), """
                string short() {
                    return "preloaded object";
                }
                """);
        Files.writeString(tempDir.resolve("room/vill_green.c"), """
                void init() {
                    add_action("north");
                    add_verb("north");
                }

                void long(str) {
                    write("You are on the green.\\n");
                }

                int north(str) {
                    call_other(this_player(), "move_player", "north#room/church");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("room/church.c"), """
                void long(str) {
                    write("You are in the church.\\n");
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("/boot " + tempDir);
        cli.execute("/where local/player");
        cli.execute("look");
        cli.execute("north");
        cli.execute("/where local/player");
        cli.execute("/objects");

        String output = transcript.toString();
        assertTrue(output.contains("Preloaded 2 startup object(s)."));
        assertTrue(output.contains("Started local session in room/vill_green"));
        assertTrue(output.contains("local/player is in room/vill_green"));
        assertTrue(output.contains("You are on the green."));
        assertTrue(output.contains("You are in the church."));
        assertTrue(output.contains("local/player is in room/church"));
        assertTrue(output.contains("obj/preload : obj/preload"));
        assertTrue(output.contains("room/vill_green : room/vill_green"));
        assertTrue(output.contains("local/player : local/player"));
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
        assertTrue(boundary.handles(MudlibLifecycleEvent.OBJECT_INITIALIZED));
        assertTrue(boundary.handles(MudlibLifecycleEvent.SCHEDULED_TICK));
        assertEquals(boundary, runtime.mudlibBoundary());
        assertTrue(result.preloadedObjects().contains("jvmud/boundary"));
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

        cli.execute("/boot " + tempDir);
        cli.execute("/load caller");
        cli.execute("/call caller phrase");

        String output = transcript.toString();
        assertTrue(output.contains("Loaded caller"));
        assertTrue(output.contains("=> handled by mfun object"));
    }

    @Test
    void vanillaMudlibBootStartsInVillageGreenAndMovesNorthToChurch() {
        Path mudlibRoot = Path.of("..", "mudlib").toAbsolutePath().normalize();
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("/boot " + mudlibRoot);
        cli.execute("/where local/player");
        cli.execute("north");
        cli.execute("/where local/player");
        cli.execute("look");

        String output = transcript.toString();
        assertTrue(output.contains("Started local session in room/vill_green"));
        assertTrue(output.contains("local/player is in room/vill_green"));
        assertTrue(output.contains("You are in the local village church."));
        assertTrue(output.contains("local/player is in room/church"));
    }

    private void installMfunShim() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/boundary.c"), """
                string mfun_object() {
                    return "jvmud/mfuns";
                }
                """);
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void write(mixed value) {
                    jvmud_write(value);
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
