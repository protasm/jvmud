package io.github.protasm.jvmud.instance;

import static org.junit.jupiter.api.Assertions.*;
import io.github.protasm.jvmud.compiler.exec.*;
import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundaryConfigReader;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies upstream preservation and real LPC calls through the new immutable-source bridge. */
final class Lp245BridgeTest {
    @TempDir Path temp;
    private static final Path UPSTREAM = Path.of("mudlibs/lp245");

    private LPCRuntime runtime() throws Exception {
        Path bridge = Files.createDirectories(temp.resolve("jvmud"));
        for (String name : List.of("mfuns.c", "mudlib.c", "lp245.config")) {
            Files.copy(UPSTREAM.resolve("jvmud").resolve(name), bridge.resolve(name));
        }
        Path config = bridge.resolve("lp245.config");
        Files.writeString(config, Files.readString(config).replace("transpilation.overrides = transpilation.json", ""));
        var boundary = MudlibBoundaryConfigReader.read(temp, "jvmud/lp245.config");
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(temp).build());
        CoreEfuns.registerCore(runtime, boundary.engineCapabilities());
        runtime.registerMudlibBoundary(boundary);
        runtime.setParserOptions(ParserOptions.features(boundary.languageFeatures()));
        return runtime;
    }

    /** New player saves live beside preserved sources and restore in an independent runtime. */
    @Test
    void originalPlayerSaveRestoresAcrossRuntimeRestart() throws Exception {
        var hashes = new ObjectMapper().readTree(Path.of("src/test/resources/lp245-lysator/upstream-sha256.json").toFile());
        var names = hashes.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            Path destination = temp.resolve(name);
            Files.createDirectories(destination.getParent());
            Files.copy(UPSTREAM.resolve(name), destination);
        }
        Files.createDirectories(temp.resolve("jvmud"));
        for (String name : List.of("lp245.config", "mfuns.c", "mudlib.c", "transpilation.json"))
            Files.copy(UPSTREAM.resolve("jvmud").resolve(name), temp.resolve("jvmud").resolve(name));
        String source = """
                inherit "/obj/player";
                void identify(string value) { name = value; }
                int restore_saved(string path) { return restore_object(path); }
                """;
        for (int restart = 0; restart < 2; restart++) {
            var boundary = MudlibBoundaryConfigReader.read(temp, "jvmud/lp245.config");
            var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(temp).build());
            CoreEfuns.registerCore(rt, boundary.engineCapabilities());
            rt.registerMudlibBoundary(boundary);
            rt.setParserOptions(ParserOptions.features(boundary.languageFeatures()));
            var player = rt.loadSource("persistence_probe.c", source);
            player.invoke("reset", 0);
            if (restart == 0) {
                player.invoke("identify", "persistenceprobe");
                player.invoke("add_money", 37);
                player.invoke("save_me", 0);
                assertTrue(Files.isRegularFile(temp.resolve("players/persistenceprobe.o")));
            } else {
                assertEquals(1, player.invoke("restore_saved", "players/persistenceprobe"));
                assertEquals("persistenceprobe", player.invoke("query_real_name"));
                assertEquals(37, player.invoke("query_money"));
                // Subsequent saves replace the character's own file, preserving updated state.
                player.invoke("add_money", 5);
                player.invoke("save_me", 0);
                player.invoke("add_money", 100);
                assertEquals(1, player.invoke("restore_saved", "players/persistenceprobe"));
                assertEquals(42, player.invoke("query_money"));
            }
        }
        var entries = hashes.fields();
        while (entries.hasNext()) {
            var entry = entries.next();
            assertEquals(entry.getValue().asText(), HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(Files.readAllBytes(temp.resolve(entry.getKey())))), entry.getKey());
        }
    }

    /** Loads the original player and exercises the two adapted methods and initial login prompt. */
    @Test
    void originalPlayerUsesLocalArraysAndStartsLogon() throws Exception {
        Path mudlib = UPSTREAM.toAbsolutePath();
        var boundary = MudlibBoundaryConfigReader.read(mudlib, "jvmud/lp245.config");
        var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(mudlib).build());
        CoreEfuns.registerCore(rt, boundary.engineCapabilities());
        rt.registerMudlibBoundary(boundary);
        rt.setParserOptions(ParserOptions.features(boundary.languageFeatures()));
        var player = rt.load("obj/player");
        player.invoke("reset", 0);
        rt.bindSession("local-override-player", player.instance(), "127.0.0.1", text -> {});
        player.invoke("who");
        player.invoke("list_peoples");
        assertTrue(rt.outputTranscript().contains("There are now 1 players"), rt.outputTranscript());
        assertEquals(1, player.invoke("logon"));
        assertTrue(rt.outputTranscript().contains("What is your name: "), rt.outputTranscript());
        assertTrue(rt.hasCapturedSessionInput(player.instance()));
    }

    /** Runs the unchanged living base against a concrete implementation of its implicit hook. */
    @Test
    void originalLivingStatsDispatchToConcreteShort() throws Exception {
        Path mudlib = UPSTREAM.toAbsolutePath();
        var boundary = MudlibBoundaryConfigReader.read(mudlib, "jvmud/lp245.config");
        var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(mudlib).build());
        CoreEfuns.registerCore(rt, boundary.engineCapabilities());
        rt.registerMudlibBoundary(boundary);
        rt.setParserOptions(ParserOptions.features(boundary.languageFeatures()));
        rt.load("obj/living");
        var concrete = rt.loadSource("living_probe.c", """
                inherit "/obj/living";
                string short() { return "Concrete living probe"; }
                """);
        concrete.invoke("show_stats");
        assertTrue(rt.outputTranscript().contains("Concrete living probe"), rt.outputTranscript());
        // The original descendant retains the same mixed contract as the living base.
        var monster = rt.load("obj/monster.talk");
        assertEquals(0, monster.invoke("can_put_and_get", 0));
        assertEquals(1, monster.invoke("can_put_and_get", "bag"));
    }

    /** Exercises inherited field writes and reads across separately generated room classes. */
    @Test
    void originalRoomBaseSupportsInheritedExitsItemsAndProperties() throws Exception {
        Path mudlib = UPSTREAM.toAbsolutePath();
        var boundary = MudlibBoundaryConfigReader.read(mudlib, "jvmud/lp245.config");
        var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(mudlib).build());
        CoreEfuns.registerCore(rt, boundary.engineCapabilities());
        rt.registerMudlibBoundary(boundary);
        rt.setParserOptions(ParserOptions.features(boundary.languageFeatures()));
        // Load the parent first so a child cannot conceal an incompatible parent field descriptor.
        var base = rt.load("room/room");
        var green = rt.load("room/vill_green");
        green.invoke("reset", 0);
        assertEquals(List.of("room/church", "north", "room/hump", "west", "room/vill_track", "east"),
                green.invoke("query_dest_dir"));
        assertEquals("Village green", green.invoke("short"));
        assertEquals("three", green.invoke("convert_number", 3));
        assertEquals("nine", base.invoke("convert_number", 9));
        var probe = rt.loadSource("room_probe.c", """
                inherit "/room/room";
                void configure() {
                    items = ({"table", "A wooden table", "window", "An open window"});
                }
                void set_property(mixed value) { property = value; }
                """);
        probe.invoke("configure");
        assertEquals(1, probe.invoke("id", "window"));
        assertEquals(0, probe.invoke("id", "door"));
        probe.invoke("set_property", List.of("no_fight", "no_steal"));
        assertEquals(1, probe.invoke("query_property", "no_steal"));
        assertEquals(0, probe.invoke("query_property", "no_magic"));
        probe.invoke("set_property", "no_magic");
        assertEquals(1, probe.invoke("query_property", "no_magic"));
        assertEquals("no_magic", probe.invoke("query_property", 0));
    }

    @Test
    void originalSourcesMatchArchiveBaseline() throws Exception {
        var hashes = new ObjectMapper().readTree(Path.of("src/test/resources/lp245-lysator/upstream-sha256.json").toFile());
        var entries = hashes.fields();
        int count = 0;
        while (entries.hasNext()) {
            var entry = entries.next();
            String name = entry.getKey();
            // Saves and logs may eventually change; original LPC and includes may not.
            if (!name.endsWith(".c") && !name.endsWith(".h")) continue;
            String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(Files.readAllBytes(UPSTREAM.resolve(name))));
            assertEquals(entry.getValue().asText(), actual, name);
            count++;
        }
        assertTrue(count >= 286);
    }

    @Test
    void mappedStringOperationsAndSevenScanfCapturesPreserveValues() throws Exception {
        LPCRuntime runtime = runtime();
        var probe = runtime.loadSource("probe.c", """
                mixed run() {
                  string a, b, c, d;
                  int e, f, g, count;
                  count = sscanf("one two three four 5 6 7", "%s %s %s %s %d %d %d", a, b, c, d, e, f, g);
                  return ({count, a, b, c, d, e, f, g});
                }
                mixed split_empty() { return explode("", ""); }
                mixed split_fields() { return explode(" a b ", " "); }
                mixed split_chars() { return explode("abc", ""); }
                string join_values() { return implode(({"a", 2, "c", ""}), "b"); }
                string join_empty() { return implode(({}), ","); }
                """);
        assertEquals(List.of(7, "one", "two", "three", "four", 5, 6, 7), probe.invoke("run"));
        assertEquals(List.of(""), probe.invoke("split_empty"));
        assertEquals(List.of("", "a", "b", ""), probe.invoke("split_fields"));
        assertEquals(List.of("a", "b", "c"), probe.invoke("split_chars"));
        assertEquals("abcb", probe.invoke("join_values"));
        assertEquals("", probe.invoke("join_empty"));
    }

    @Test
    void mappedWriteFileAppendsAndReportsFailureInDisposableStorage() throws Exception {
        LPCRuntime runtime = runtime();
        var probe = runtime.loadSource("writer.c", """
                int append(string path, string text) { return write_file(path, text); }
                """);
        assertEquals(1, probe.invoke("append", "notes.txt", "first"));
        assertEquals(1, probe.invoke("append", "notes.txt", "second"));
        assertEquals("firstsecond", Files.readString(temp.resolve("notes.txt")));
        assertEquals(0, probe.invoke("append", "..", "outside"));
    }

    @Test
    void traversalMatchesAncestorAndBreadthFirstDepthContracts() throws Exception {
        LPCRuntime runtime = runtime();
        String source = """
                mixed parents() { return all_environment(); }
                int no_parent() { return !all_environment(); }
                mixed descendants() { return deep_inventory(); }
                mixed level(int depth) { return deep_inventory(this_object(), depth); }
                """;
        var room = runtime.loadSource("room.c", source);
        var box = runtime.loadSource("box.c", source);
        var bag = runtime.loadSource("bag.c", source);
        var coin = runtime.loadSource("coin.c", source);
        runtime.moveObject(box.instance(), room.instance());
        runtime.moveObject(bag.instance(), room.instance());
        runtime.moveObject(coin.instance(), box.instance());
        assertEquals(List.of(box.instance(), room.instance()), coin.invoke("parents"));
        assertNull(room.invoke("parents"));
        assertEquals(1, room.invoke("no_parent"));
        assertEquals(List.of(box.instance(), bag.instance(), coin.instance()), room.invoke("descendants"));
        assertEquals(List.of(box.instance(), bag.instance()), room.invoke("level", 1));
        assertEquals(List.of(coin.instance()), room.invoke("level", -2));
        assertEquals(List.of(), room.invoke("level", -3));
        assertEquals(List.of(), coin.invoke("descendants"));
    }
}
