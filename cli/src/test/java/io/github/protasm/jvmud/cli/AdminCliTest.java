package io.github.protasm.jvmud.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.engine.EngineEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
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
    void adminCanLoadCallCloneMoveInspectAndQuit() throws Exception {
        Files.writeString(tempDir.resolve("room.c"), """
                string long(mixed str) {
                    if (str)
                        return "A specific thing.";
                    else
                        return "A readable room.";
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

        cli.execute("boot " + configFile());
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

        cli.execute("boot " + configFile());
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
    void virtualFilesystemRejectsPathsOutsideMudlibRoot() throws Exception {
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("boot " + configFile());
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
        assertTrue(output.contains("Admin commands:"));
        assertTrue(output.contains("l  load <path>"));
        assertTrue(output.contains("Compile, load, and register an LPC object."));
        assertTrue(output.contains("Start a fresh mudlib sandbox without a player session."));
        assertTrue(output.contains("v  verbosity [quiet|normal|watch]"));
        assertTrue(output.contains("Show or change compiler/shell output detail."));
        assertTrue(output.contains("c  compile <path>"));
        assertTrue(output.contains("ir ir <path>"));
        assertTrue(output.contains("p  parse <path>"));
        assertTrue(output.contains("pp preprocess <path>"));
        assertTrue(output.contains("s  scan <path>"));
        assertTrue(output.contains("compile <path>"));
        assertTrue(output.indexOf("h  help") < output.indexOf("b  boot"));
        assertTrue(output.indexOf("b  boot") < output.indexOf("call"));
        assertTrue(output.indexOf("call") < output.indexOf("cat"));
        assertTrue(output.indexOf("cat") < output.indexOf("cd"));
        assertTrue(output.indexOf("cd") < output.indexOf("clone <path>"));
        assertTrue(output.indexOf("clone <path>") < output.indexOf("c  compile"));
        assertTrue(output.indexOf("c  compile") < output.indexOf("x  destruct"));
        assertTrue(output.indexOf("x  destruct") < output.indexOf("i  inspect"));
        assertTrue(output.indexOf("i  inspect") < output.indexOf("ir ir"));
        assertTrue(output.indexOf("ir ir") < output.indexOf("l  load"));
        assertTrue(output.indexOf("l  load") < output.indexOf("k  look"));
        assertTrue(output.indexOf("ls") < output.indexOf("m  move"));
        assertTrue(output.indexOf("m  move") < output.indexOf("o  objects"));
        assertTrue(output.indexOf("o  objects") < output.indexOf("p  parse"));
        assertTrue(output.indexOf("p  parse") < output.indexOf("pp preprocess"));
        assertTrue(output.indexOf("pp preprocess") < output.indexOf("pwd"));
        assertTrue(output.indexOf("pwd") < output.indexOf("r  reload"));
        assertTrue(output.indexOf("r  reload") < output.indexOf("s  scan"));
        assertTrue(output.indexOf("s  scan") < output.indexOf("v  verbosity"));
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

        cli.execute("boot " + configFile());
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

        cli.execute("b " + configFile());
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
    void unassignedSingleCharacterAliasesAreNotCommands() {
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("a room/item short");
        cli.execute("t README.txt");
        cli.execute("d room");
        cli.execute("n room/item");

        String output = transcript.toString();
        assertTrue(output.contains("Unknown command: a"));
        assertTrue(output.contains("Unknown command: t"));
        assertTrue(output.contains("Unknown command: d"));
        assertTrue(output.contains("Unknown command: n"));
        assertTrue(output.contains("Usage: help"));
    }

    @Test
    void commandErrorsIncludeUsage() {
        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("call missingOnlyMethod");
        cli.execute("inspect");
        cli.execute("pp");
        cli.execute("parse");
        cli.execute("compile");

        String output = transcript.toString();
        assertTrue(output.contains("Error: Missing argument 1 for call"));
        assertTrue(output.contains("Usage: call <handle> <method> [args...]"));
        assertTrue(output.contains("Error: Missing argument 0 for inspect"));
        assertTrue(output.contains("Usage: inspect <handle>"));
        assertTrue(output.contains("Error: Missing argument 0 for pp"));
        assertTrue(output.contains("Usage: preprocess <path>"));
        assertTrue(output.contains("Error: Missing argument 0 for parse"));
        assertTrue(output.contains("Usage: parse <path>"));
        assertTrue(output.contains("Error: Missing argument 0 for compile"));
        assertTrue(output.contains("Usage: compile <path>"));
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

        cli.execute("boot " + configFile());
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
    void compilerInspectionCommandsDoNotLoadObjects() throws Exception {
        Files.createDirectories(tempDir.resolve("include"));
        Files.writeString(tempDir.resolve("include/value.h"), "#define VALUE 7\n");
        Files.writeString(tempDir.resolve("tool.c"), """
                #include "include/value.h"

                int value() {
                    return VALUE;
                }

                string short() {
                    return "tool";
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("boot " + configFile());
        cli.execute("pp tool");
        cli.execute("s tool");
        cli.execute("p tool");
        cli.execute("ir tool");
        cli.execute("c tool");
        cli.execute("objects");

        String output = transcript.toString();
        assertTrue(output.contains("return  7;"));
        assertTrue(output.contains("T_STRING_LITERAL"));
        assertTrue(output.contains("ASTObject(tool)"));
        assertTrue(output.contains("ASTMethod[null value]"));
        assertFalse(output.contains("TypedIR"));
        assertTrue(output.contains("object tool extends java/lang/Object"));
        assertTrue(output.contains("method int value()"));
        assertTrue(output.contains("entry entry_0"));
        assertTrue(output.contains("entry_0:"));
        assertTrue(output.contains("return const 7:int"));
        assertTrue(output.contains("value"));
        assertTrue(output.contains("Compiled tool as tool"));
        assertTrue(output.contains("(no objects)"));
    }

    @Test
    void bootStartsMudlibSandboxWithoutInitialWorldObject() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                mfun_object = jvmud/mfuns
                initial_place = room/village/vill_green
                """);
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                string status() {
                    return "sandbox";
                }
                """);
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                string short() {
                    return "green";
                }
                """);

        StringWriter transcript = new StringWriter();
        AdminCli cli = new AdminCli(new PrintWriter(transcript, true));

        cli.execute("boot " + tempDir.resolve("jvmud/lp245.config"));
        cli.execute("objects");

        String output = transcript.toString();
        assertTrue(output.contains("Booted runtime"));
        assertTrue(output.contains("(no objects)"));
        assertFalse(output.contains("room/village/vill_green : room/village/vill_green"));
        assertFalse(output.contains("local/player"));
    }

    private Path configFile() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Path configFile = tempDir.resolve("jvmud/test.config");
        if (!Files.exists(configFile)) {
            Files.writeString(configFile, "");
        }
        return configFile;
    }

}
