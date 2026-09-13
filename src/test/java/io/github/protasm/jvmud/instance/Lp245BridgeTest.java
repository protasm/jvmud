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

    private com.fasterxml.jackson.databind.JsonNode copyOriginalArchive() throws Exception {
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
        return hashes;
    }

    private LPCRuntime isolatedArchiveRuntime() throws Exception {
        copyOriginalArchive();
        var boundary = MudlibBoundaryConfigReader.read(temp, "jvmud/lp245.config");
        var rt = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(temp).build());
        CoreEfuns.registerCore(rt, boundary.engineCapabilities());
        rt.registerMudlibBoundary(boundary);
        rt.setParserOptions(ParserOptions.features(boundary.languageFeatures()));
        return rt;
    }

    /** Exercises original guild initialization, title tables and actual player advancement. */
    @Test
    void originalGuildInitializesAndAdvancesPlayer() throws Exception {
        var rt = isolatedArchiveRuntime();
        var guild = rt.load("room/adv_guild");
        guild.invoke("reset", 0);
        assertEquals(676, guild.invoke("get_next_exp", 0));
        assertEquals(1000000, guild.invoke("get_next_exp", 19));
        var player = rt.load("obj/player");
        player.invoke("reset", 0);
        String[] expected = {"the shadow", "the experienced fighter", "the charming siren"};
        for (int gender = 0; gender < 3; gender++) {
            player.invoke("set_gender", gender);
            rt.withCommandActor(player.instance(), () -> guild.invoke("query_cost", 5));
            assertEquals(expected[gender], guild.invoke("get_new_title", 5));
            assertEquals("the apprentice Wizard", guild.invoke("get_new_title", 19));
        }
        player.invoke("set_gender", 1);
        assertEquals(1, rt.withCommandActor(player.instance(), () -> guild.invoke("advance", 0)));
        assertEquals(1, player.invoke("query_level"));
        assertEquals("the utter novice", player.invoke("query_title"));
        assertEquals(0, player.invoke("query_money"));
        rt.withCommandActor(player.instance(), () -> guild.invoke("advance", "level"));
        assertEquals(1, player.invoke("query_level")); // Insufficient funds do not advance.
        player.invoke("add_money", 2000);
        rt.withCommandActor(player.instance(), () -> guild.invoke("advance", "level"));
        assertEquals(2, player.invoke("query_level"));
        assertEquals("the simple wanderer", player.invoke("query_title"));
        assertEquals(1014, player.invoke("query_exp"));
        assertEquals(310, player.invoke("query_money"));
    }

    /** Uses the original monster's configuration, conversation matching and heartbeat paths. */
    @Test
    void originalMonsterUsesChatArraysAndGuildExperience() throws Exception {
        var rt = isolatedArchiveRuntime();
        var room = rt.loadSource("arena.c", "string short() { return \"Arena\"; }");
        var actor = rt.loadSource("actor.c", """
                inherit "/obj/player";
                void activate() { enable_commands(); hit_point = 1000; max_hp = 1000; }
                """);
        actor.invoke("reset", 0);
        actor.invoke("activate");
        rt.bindSession("monster-actor", actor.instance(), "127.0.0.1", text -> {});
        rt.moveObject(actor.instance(), room.instance());
        var monster = rt.load("obj/monster");
        monster.invoke("reset", 0);
        monster.invoke("set_name", "probe");
        monster.invoke("set_level", 3);
        assertEquals(3, monster.invoke("query_level"));
        assertEquals(1522, monster.invoke("query_exp"));
        assertEquals(66, monster.invoke("query_hp"));
        rt.moveObject(monster.instance(), room.instance());
        var listener = rt.loadSource("listener.c", """
                string message;
                int matched(string text) { message = text; return 73; }
                string received() { return message; }
                """);
        monster.invoke("set_match", listener.instance(), List.of("matched"), List.of("says: "), List.of("hello"));
        assertEquals(73, monster.invoke("test_match", "Visitor says: hello\n"));
        assertEquals("Visitor says: hello\n", listener.invoke("received"));
        monster.invoke("load_chat", 100, List.of("Idle chat marker\n"));
        monster.invoke("heart_beat");
        assertTrue(rt.outputTranscript().contains("Idle chat marker"), rt.outputTranscript());
        monster.invoke("load_a_chat", 100, List.of("Combat chat marker\n"));
        monster.invoke("attack_object", actor.instance());
        monster.invoke("heart_beat");
        assertTrue(rt.outputTranscript().contains("Combat chat marker"), rt.outputTranscript());
    }

    /** Original rooms pass array configuration into the monster and expose their NPCs. */
    @Test
    void originalRoomsConfigureMonstersWithChatArrays() throws Exception {
        var rt = isolatedArchiveRuntime();
        String[][] cases = {{"room/vill_road2", "harry"}, {"room/orc_vall", "orc"},
                {"room/fortress", "orc"}, {"room/pub2", "player"}, {"room/yard", "beggar"}};
        for (String[] entry : cases) {
            var room = rt.load(entry[0]);
            assertNotNull(rt.present(entry[1], room.instance()), entry[0]);
        }
        Object harry = rt.present("harry", rt.loadOrGetObject("room/vill_road2"));
        rt.clearOutputTranscript();
        rt.invokeObject(harry, "test_match", "Alice says: hello\n");
        assertTrue(rt.outputTranscript().contains("Harry says: Pleased to meet you!"), rt.outputTranscript());
    }

    /** Exercises commands registered by the original carried Quicktyper, including storage refresh. */
    @Test
    void originalQuicktyperAliasesHistoryQueueRefreshAndAutoload() throws Exception {
        var rt = isolatedArchiveRuntime();
        var scheduler = new io.github.protasm.jvmud.engine.time.WorldScheduler();
        rt.setScheduler(scheduler);
        var actor = rt.loadSource("quicktyper_actor.c", """
                string seen = "";
                void activate() { enable_commands(); }
                string query_name() { return "Visitor"; }
                int query_level() { return 1; }
                void init() { add_action("record", "mark"); }
                int record(string text) { seen += text + ";"; return 1; }
                string recorded() { return seen; }
                """);
        actor.invoke("activate");
        rt.bindSession("quicktyper-actor", actor.instance(), "127.0.0.1", text -> {});
        rt.moveObject(actor.instance(), rt.load("room/church").instance());
        var quicktyper = rt.load("obj/quicktyper");
        rt.moveObject(quicktyper.instance(), actor.instance());
        rt.refreshCommandActions(actor.instance());
        assertEquals(1, rt.dispatchCommand(actor.instance(), "alias m mark"));
        assertEquals(1, rt.dispatchCommand(actor.instance(), "m hello"));
        assertEquals("hello;", actor.invoke("recorded"));
        assertEquals(1, rt.dispatchCommand(actor.instance(), "%%"));
        assertEquals("hello;hello;", actor.invoke("recorded"));
        assertEquals(1, rt.dispatchCommand(actor.instance(), "%2"));
        assertEquals("hello;hello;hello;", actor.invoke("recorded"));
        rt.clearOutputTranscript();
        assertEquals(1, rt.dispatchCommand(actor.instance(), "history"));
        assertTrue(rt.outputTranscript().contains("mark hello"), rt.outputTranscript());
        assertEquals(1, rt.dispatchCommand(actor.instance(), "do mark first,mark second,mark third"));
        assertEquals(1, rt.dispatchCommand(actor.instance(), "do"));
        scheduler.advanceBy(2);
        assertEquals("hello;hello;hello;first;", actor.invoke("recorded"));
        assertEquals(1, rt.dispatchCommand(actor.instance(), "resume"));
        scheduler.advanceBy(1);
        assertEquals("hello;hello;hello;first;second;third;", actor.invoke("recorded"));
        assertEquals(1, rt.dispatchCommand(actor.instance(), "refresh"));
        assertSame(actor.instance(), rt.environment(quicktyper.instance()));
        scheduler.advanceBy(30);
        assertSame(actor.instance(), rt.environment(quicktyper.instance()));
        assertEquals(1, rt.dispatchCommand(actor.instance(), "m refreshed"));
        String saved = (String) quicktyper.invoke("query_auto_load");
        assertEquals("obj/quicktyper:1;m mark;.X.Z;", saved);
        Object restored = rt.cloneObject("obj/quicktyper");
        rt.invokeObject(restored, "init_arg", saved.substring(saved.indexOf(':') + 1));
        assertEquals(saved, rt.invokeObject(restored, "query_auto_load"));
        assertEquals(1, rt.dispatchCommand(actor.instance(), "alias m mark replaced"));
        assertEquals(1, rt.dispatchCommand(actor.instance(), "m"));
        assertTrue(((String) actor.invoke("recorded")).endsWith("refreshed;replaced;"));
        assertEquals(1, rt.dispatchCommand(actor.instance(), "alias m"));
        assertEquals(0, rt.dispatchCommand(actor.instance(), "m"));
    }

    /** New player saves live beside preserved sources and restore in an independent runtime. */
    @Test
    void originalPlayerSaveRestoresAcrossRuntimeRestart() throws Exception {
        var hashes = copyOriginalArchive();
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
