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

    /** Scan the preserved archive, including objects outside the eight-entry preload file. */
    @Test
    void originalArchiveCompilesAndInitializesOutsideDocumentedHistoricalExceptions() throws Exception {
        var rt = isolatedArchiveRuntime();
        Set<String> compileExceptions = Set.of("obj/master.c", "obj/team.c", "room/def_castle.c");
        Set<String> initializationExceptions = Set.of("obj/explore_xp.c", "players/lars/test.c");
        var hashes = new ObjectMapper().readTree(Path.of("src/test/resources/lp245-lysator/upstream-sha256.json").toFile());
        List<String> failures = new ArrayList<>();
        int compiled = 0, loaded = 0;
        List<String> sourceNames = new ArrayList<>();
        hashes.fieldNames().forEachRemaining(sourceNames::add);
        Collections.sort(sourceNames);
        for (String name : sourceNames) {
            if (!name.endsWith(".c") || compileExceptions.contains(name)) continue;
            var result = rt.compile(temp.resolve(name));
            if (!result.succeeded()) {
                failures.add(name + ": " + result.getProblems().stream().map(p -> p.getMessage()).toList());
                continue;
            }
            compiled++;
            if (initializationExceptions.contains(name)) continue;
            try {
                rt.load(name.substring(0, name.length() - 2));
                loaded++;
            } catch (RuntimeException | LinkageError e) {
                failures.add(name + ": " + e);
            }
        }
        assertEquals(List.of(), failures);
        assertEquals(283, compiled);
        assertEquals(281, loaded);
    }

    @Test
    void originalTracerStoresMixedResultsAndSelectsInventoryByNumber() throws Exception {
        var rt = isolatedArchiveRuntime();
        var tracer = rt.load("obj/trace");
        var room = rt.loadSource("trace_room.c", "string short() { return \"Probe room\"; }");
        var first = rt.loadSource("first.c", "string short() { return \"First\"; }");
        rt.moveObject(first.instance(), room.instance());
        var actor = rt.loadSource("trace_actor.c", "string query_name() { return \"Visitor\"; }");
        rt.moveObject(actor.instance(), room.instance());
        tracer.invoke("assign", "number", 42);
        assertEquals(42, rt.withCommandActor(actor.instance(), () -> tracer.invoke("parse_list", "$number")));
        tracer.invoke("assign", "array", List.of(1, 2));
        assertEquals(List.of(1, 2), rt.withCommandActor(actor.instance(), () -> tracer.invoke("parse_list", "$array")));
        tracer.invoke("assign", "room", room.instance());
        assertSame(first.instance(), rt.withCommandActor(actor.instance(), () -> tracer.invoke("parse_list", "$room:#1")));
    }

    @Test
    void originalDeathSequenceReturnsGhostsToChurchAndClearsItsQueue() throws Exception {
        var rt = isolatedArchiveRuntime();
        var scheduler = new io.github.protasm.jvmud.engine.time.WorldScheduler();
        rt.setScheduler(scheduler);
        var room = rt.load("room/death/death_room");
        var ghost = rt.loadSource("death_probe.c", """
                inherit "/obj/player";
                void prepare() { ghost = 1; enable_commands(); }
                """);
        ghost.invoke("prepare");
        rt.bindSession("death-probe", ghost.instance(), "127.0.0.1", text -> {});
        rt.moveObject(ghost.instance(), room.instance());
        Object second = rt.cloneObject("death_probe");
        rt.invokeObject(second, "prepare");
        rt.bindSession("second-death-probe", second, "127.0.0.1", text -> {});
        rt.moveObject(second, room.instance());
        scheduler.advanceBy(69);
        assertSame(room.instance(), rt.environment(ghost.instance()));
        scheduler.advanceBy(1);
        assertSame(rt.loadOrGetObject("room/church"), rt.environment(ghost.instance()));
        assertSame(rt.loadOrGetObject("room/church"), rt.environment(second));
        assertFalse(rt.outputTranscript().contains("You have no heart beat"), rt.outputTranscript());
        assertFalse(Files.exists(temp.resolve("jvmud/log/HEART_BEAT")));
        rt.clearOutputTranscript();
        // A completed queue must not emit the scene again or recurse on another tick.
        room.invoke("heart_beat");
        assertEquals("", rt.outputTranscript());
    }

    @Test
    void originalDeathRoomTracksMultipleGhostsAndRemovesOne() throws Exception {
        var rt = isolatedArchiveRuntime();
        var room = rt.load("room/death/death_room");
        Object first = rt.cloneObject("obj/player");
        Object second = rt.cloneObject("obj/player");
        StringBuilder firstText = new StringBuilder(), secondText = new StringBuilder();
        rt.bindSession("first-ghost", first, "127.0.0.1", firstText::append);
        rt.bindSession("second-ghost", second, "127.0.0.1", secondText::append);
        room.invoke("add_player", first);
        room.invoke("add_player", second);
        for (int i = 0; i < 5; i++) room.invoke("heart_beat");
        assertTrue(firstText.toString().contains("IT IS TIME"));
        assertTrue(secondText.toString().contains("IT IS TIME"));
        firstText.setLength(0);
        secondText.setLength(0);
        room.invoke("remove_player", first);
        for (int i = 0; i < 5; i++) room.invoke("heart_beat");
        assertEquals("", firstText.toString());
        assertTrue(secondText.toString().contains("NO GLANDS"));
        room.invoke("remove_player", second);
        assertDoesNotThrow(() -> room.invoke("heart_beat"));
    }

    @Test
    void originalForestJacketCanBePickedUpWornDroppedAndSold() throws Exception {
        var rt = isolatedArchiveRuntime();
        var forest = rt.load("room/forest1");
        Object jacket = rt.present("jacket", forest.instance());
        assertNotNull(jacket);
        Object player = rt.cloneObject("obj/player");
        rt.withCommandActor(player, () -> rt.invokeObject(player, "logon2", "jacketprobe"));
        rt.withCommandActor(player, () -> rt.invokeObject(player, "move_player_to_start3", "room/forest1"));
        rt.moveObject(player, forest.instance());
        rt.refreshCommandActions(player);
        assertEquals(1, rt.dispatchCommand(player, "get jacket"));
        assertSame(player, rt.environment(jacket));
        assertEquals(2, rt.invokeObject(jacket, "query_weight"));
        assertEquals(50, rt.invokeObject(jacket, "query_value"));
        assertEquals(1, rt.dispatchCommand(player, "wear jacket"));
        assertEquals(1, rt.invokeObject(jacket, "query_worn"));
        assertEquals(1, rt.dispatchCommand(player, "drop jacket"));
        assertEquals(0, rt.invokeObject(jacket, "query_worn"));
        assertSame(forest.instance(), rt.environment(jacket));
        assertEquals(1, rt.dispatchCommand(player, "get jacket"));
        Object shop = rt.loadOrGetObject("room/shop");
        rt.moveObject(player, shop);
        rt.refreshCommandActions(player);
        assertEquals(1, rt.dispatchCommand(player, "sell jacket"));
        assertEquals(50, rt.invokeObject(player, "query_money"));
        assertSame(rt.loadOrGetObject("room/store"), rt.environment(jacket));
    }

    @Test
    void originalShopValuesFrogCrownInInventoryAndOnFloorBeforeSelling() throws Exception {
        var rt = isolatedArchiveRuntime();
        var plain = rt.load("room/plane9");
        Object frog = rt.present("frog", plain.instance());
        Object crown = rt.present("crown", frog);
        assertNotNull(crown);
        var shop = rt.load("room/shop");
        var player = rt.load("obj/player");
        rt.moveObject(player.instance(), shop.instance());
        rt.refreshCommandActions(player.instance());
        for (Object location : List.of(player.instance(), shop.instance())) {
            rt.moveObject(crown, location);
            rt.clearOutputTranscript();
            assertEquals(1, rt.dispatchCommand(player.instance(), "value crown"));
            assertTrue(rt.outputTranscript().contains("You would get 30 gold coins."), rt.outputTranscript());
            assertSame(location, rt.environment(crown));
            assertEquals(0, player.invoke("query_money"));
        }
        rt.moveObject(crown, player.instance());
        assertEquals(1, rt.dispatchCommand(player.instance(), "sell crown"));
        assertEquals(30, player.invoke("query_money"));
        assertSame(rt.loadOrGetObject("room/store"), rt.environment(crown));
    }

    @Test
    void originalShopPaysForSoldItemAndMovesItIntoStock() throws Exception {
        var rt = isolatedArchiveRuntime();
        var shop = rt.load("room/shop");
        var player = rt.load("obj/player");
        rt.moveObject(player.instance(), shop.instance());
        var item = rt.loadSource("sale_item.c", """
                string short() { return "A sale probe"; }
                int id(string name) { return name == "probe"; }
                int query_value() { return 25; }
                int query_weight() { return 1; }
                int drop() { return 0; }
                """);
        rt.moveObject(item.instance(), player.instance());
        assertEquals(1, rt.withCommandActor(player.instance(), () -> shop.invoke("sell", "probe")));
        assertEquals(25, player.invoke("query_money"));
        assertSame(rt.loadOrGetObject("room/store"), rt.environment(item.instance()));
    }

    @Test
    void originalGoBoardStartsPatchesAndScoresGrid() throws Exception {
        var rt = isolatedArchiveRuntime();
        var room = rt.loadSource("go_room.c", "string short() { return \"Go room\"; }");
        var actor = rt.loadSource("black_player.c", "string query_name() { return \"Black\"; }");
        var opponent = rt.loadSource("white_player.c", """
                string query_name() { return "White"; }
                int id(string name) { return name == "white"; }
                """);
        var board = rt.load("players/lars/board");
        rt.moveObject(board.instance(), room.instance());
        rt.moveObject(actor.instance(), room.instance());
        rt.moveObject(opponent.instance(), room.instance());
        rt.withCommandActor(actor.instance(), () -> board.invoke("start", "white"));
        assertTrue(rt.outputTranscript().contains("Board initialized."), rt.outputTranscript());
        assertEquals(1, board.invoke("patch", "4 4 @"));
        rt.clearOutputTranscript();
        assertEquals(1, board.invoke("score"));
        assertTrue(rt.outputTranscript().contains("points to black"), rt.outputTranscript());
        assertDoesNotThrow(() -> board.invoke("fill"));
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

    /** Storage's init(arg) must run instead of its inherited room init() hook. */
    @Test
    void enteringOriginalStorageCreatesQuicktyperWithoutDuplicates() throws Exception {
        var rt = isolatedArchiveRuntime();
        var actor = rt.loadSource("storage_visitor.c", """
                void activate() { enable_commands(); }
                string query_name() { return "Visitor"; }
                int query_level() { return 1; }
                void move_player(string route) {
                    string direction;
                    string destination;
                    sscanf(route, "%s#%s", direction, destination);
                    move_object(this_object(), destination);
                }
                """);
        actor.invoke("activate");
        rt.bindSession("storage-visitor", actor.instance(), "127.0.0.1", text -> {});
        var storage = rt.load("room/storage");
        rt.moveObject(actor.instance(), storage.instance());
        Object quicktyper = rt.present("tech_quicktyper", storage.instance());
        assertNotNull(quicktyper);
        rt.refreshCommandActions(actor.instance());
        assertSame(quicktyper, rt.present("tech_quicktyper", storage.instance()));
        assertEquals(1, rt.dispatchCommand(actor.instance(), "east"));
        assertEquals("room/shop", rt.objectId(rt.environment(actor.instance())));
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
        assertEquals(0, room.invoke("parents")); // Dynamic LPC helpers return the mixed zero sentinel.
        assertEquals(1, room.invoke("no_parent"));
        assertEquals(List.of(box.instance(), bag.instance(), coin.instance()), room.invoke("descendants"));
        assertEquals(List.of(box.instance(), bag.instance()), room.invoke("level", 1));
        assertEquals(List.of(coin.instance()), room.invoke("level", -2));
        assertEquals(List.of(), room.invoke("level", -3));
        assertEquals(List.of(), coin.invoke("descendants"));
    }
}
