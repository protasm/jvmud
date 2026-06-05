package io.github.protasm.jvmud.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void helpListsCommandsVerticallyWithDescriptions() {
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("help");

        String output = transcript.toString();
        assertTrue(output.contains("Commands:"));
        assertTrue(output.contains("l  load <path>"));
        assertTrue(output.contains("Compile, load, and register an LPC object."));
        assertTrue(output.contains("v  verbosity [quiet|normal|watch]"));
        assertTrue(output.contains("Show or change compiler/admin output detail."));
        assertTrue(output.indexOf("h  help") < output.indexOf("b  boot"));
        assertTrue(output.indexOf("b  boot") < output.indexOf("call"));
        assertTrue(output.indexOf("call") < output.indexOf("cat"));
        assertTrue(output.indexOf("cat") < output.indexOf("cd"));
        assertTrue(output.indexOf("cd") < output.indexOf("n  clone"));
        assertTrue(output.indexOf("n  clone") < output.indexOf("x  destruct"));
        assertTrue(output.indexOf("x  destruct") < output.indexOf("i  inspect"));
        assertTrue(output.indexOf("i  inspect") < output.indexOf("l  load"));
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
}
