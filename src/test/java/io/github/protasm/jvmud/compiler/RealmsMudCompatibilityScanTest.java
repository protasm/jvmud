package io.github.protasm.jvmud.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCLoadResult;
import io.github.protasm.jvmud.compiler.exec.LPCObjectHandle;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.compiler.parser.ParserOptions;
import io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline;
import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import io.github.protasm.jvmud.compiler.pipeline.CompilationResult;
import io.github.protasm.jvmud.compiler.pipeline.CompilationStage;
import io.github.protasm.jvmud.compiler.preproc.Preprocessor;
import io.github.protasm.jvmud.compiler.preproc.SearchPathIncludeResolver;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundaryConfigReader;
import io.github.protasm.jvmud.engine.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.engine.time.WorldScheduler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

final class RealmsMudCompatibilityScanTest {
    private static final Path REPO_ROOT = Path.of("").toAbsolutePath().normalize();
    private static final Path MUDLIB_ROOT = REPO_ROOT.resolve("mudlibs").resolve("realmsmud");
    private static final Path MUDLIB_SOURCE_ROOT = MUDLIB_ROOT.resolve("source");
    private static final String CONFIG_PATH = "jvmud/realmsmud.config";
    private static final Path REPORT_PATH = Path.of("target", "jvmud-realmsmud-compatibility.md");
    private static final List<String> COMPATIBILITY_SWEEP_ROOTS =
            List.of(
                    "lib/environment",
                    "lib/services",
                    "lib/modules/secure",
                    "secure/master",
                    "secure/simulated-efuns");
    private static final List<String> BOOT_AND_CORE_COMPATIBILITY_SET =
            List.of(
                    "secure/master.c",
                    "secure/simul_efun.c",
                    "secure/login.c",
                    "jvmud/compat.c",
                    "lib/core/baseSelector.c",
                    "lib/core/events.c",
                    "lib/core/messageParser.c",
                    "lib/core/organizations.c",
                    "lib/core/prerequisites.c",
                    "lib/core/stateMachine.c",
                    "lib/core/specification.c",
                    "lib/core/stateObject.c",
                    "lib/core/thing.c");
    private static final List<KnownIssue> KNOWN_ISSUES =
            List.of(
                    new KnownIssue(
                            "lib/modules/secure/dataAccess.c",
                            CompilationStage.ANALYZE,
                            87,
                            "Argument 2 type mismatch (expected LPCSTRING but found LPCINT)",
                            "RealmsMUD passes playerId to saveCompositeResearch, whose declared signature expects "
                                    + "playerName. This is tracked as a probable Realms source mismatch rather than "
                                    + "a JVMud language gap."));

    @Test
    void realmsEnvironmentServiceLoadsWithTheConfiguredBoundary() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        LPCLoadResult result = runtime.tryLoad("lib/services/environmentService");

        assertTrue(result.succeeded(), () -> result.error()
                .map(Throwable::toString)
                .orElse("Environment service load failed without an error."));
    }

    @Test
    void selectedRealmsMudFilesProduceCompatibilityRadarReport() throws IOException {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        Path sourceRoot = boundary.mudlibRootPath().orElse(MUDLIB_SOURCE_ROOT);
        assertTrue(
                Files.isSameFile(MUDLIB_SOURCE_ROOT, sourceRoot),
                "RealmsMUD mudlib_root should resolve to the upstream source tree.");
        assertConfiguredBoundary(boundary);

        RuntimeContext context = new RuntimeContext(new SearchPathIncludeResolver(sourceRoot, List.of()));
        CoreEfuns.registerCore(context);
        context.setMudlibBoundary(boundary);
        CompilationPipeline pipeline = new CompilationPipeline("java/lang/Object", context);
        Map<String, CompilationResult> results = new LinkedHashMap<>();

        // This is deliberately a radar, not a readiness gate. It should keep exposing
        // the first RealmsMUD blockers while the rest of the build stays green.
        for (String sourceName : compatibilitySet(sourceRoot)) {
            Path sourcePath = compatibilitySourcePath(sourceRoot, sourceName);
            assertTrue(Files.isRegularFile(sourcePath), sourceName + " should exist in the RealmsMUD mudlib tree.");
            String source = Files.readString(sourcePath);
            CompilationResult result =
                    pipeline.run(sourcePath, source, stripExtension(sourceName), "/" + sourceName, ParserOptions.defaults());
            results.put(sourceName, result);
        }

        writeReport(boundary, results);

        assertFalse(results.isEmpty(), "RealmsMUD compatibility radar should scan at least one file.");
        assertTrue(Files.exists(REPORT_PATH), "RealmsMUD compatibility report should be written.");
    }

    @Test
    void synchronousRealmsEventsUsePassedEventNameForDirectHandlers() throws IOException {
        String eventsSource = Files.readString(MUDLIB_SOURCE_ROOT.resolve("lib/core/events.c"));
        assertTrue(
                eventsSource.contains("function_exists($2, $1)"),
                "notifySynchronous should test direct event handlers with the closure event argument.");
        assertFalse(
                eventsSource.contains("function_exists(event, $1)"),
                "notifySynchronous should not rely on lexical event lookup inside the filter closure.");
    }

    @Test
    void realmsSettingsRestoreConvertsPersistedSafetyTeleportToInt() throws IOException {
        String settingsSource = Files.readString(MUDLIB_SOURCE_ROOT.resolve("lib/modules/secure/settings.h"));
        assertTrue(
                settingsSource.contains("lastSafetyTeleport = to_int(persistence->extractSaveData("),
                "RealmsMUD stores settings values as text, so safety teleport restore needs numeric conversion.");
    }

    @Test
    void realmsHelpPassesUnicodeFlagToBaseCommandDisplayMethods() throws IOException {
        String helpSource = Files.readString(MUDLIB_SOURCE_ROOT.resolve("lib/commands/player/help.c"));
        assertTrue(
                helpSource.contains("int useUnicode = charset == \"unicode\";"),
                "RealmsMUD base command display helpers expect a numeric unicode flag, not a charset string.");
        assertTrue(
                helpSource.contains("displaySynopsis(command, colorConfiguration, useUnicode)"),
                "Help detail rendering should pass the numeric unicode flag through display helpers.");
    }

    @Test
    void realmsProgramNameReportsBlueprintPathForClones() throws IOException {
        String jvmudShimSource = Files.readString(MUDLIB_ROOT.resolve("jvmud/compat.c"));
        assertTrue(
                jvmudShimSource.contains("ret = regreplace(ret, \"(#[^.]*)\", \"\", 1);"),
                "RealmsMUD security checks compare program_name() to blueprint paths, so clone ids must be stripped.");
    }

    @Test
    void realmsObjectNamesTranslateCloneIdentityAtTheCompatibilityBoundary() throws IOException {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object potion = runtime.cloneObject("lib/instances/items/potions/healing.c");
        LPCObjectHandle compatibility = runtime.load(MUDLIB_ROOT.resolve("jvmud/compat.c"));

        assertEquals(
                "lib/instances/items/potions/healing#1",
                compatibility.invoke("object_name", potion));
        assertTrue(compatibility.invoke(
                        "clone_object", "lib/instances/items/potions/healing#clone1")
                instanceof Object);
    }

    @Test
    void realmsOnlineRoleListsExcludeDisconnectedCachedIdentities() throws IOException {
        String jvmudShimSource = Files.readString(MUDLIB_ROOT.resolve("jvmud/compat.c"));
        assertTrue(
                jvmudShimSource.contains("if (jvmud_interactive(player))"),
                "RealmsMUD's cached player identities should not be reported as active sessions after disconnect.");
        assertTrue(
                jvmudShimSource.contains("if (jvmud_interactive(wizard))"),
                "RealmsMUD's cached wizard identities should not be reported as active sessions after disconnect.");
    }

    @Test
    void realmsPlayerNameCanBeInitializedBeforePersistence() throws IOException {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        LPCObjectHandle materialAttributes =
                runtime.load(MUDLIB_SOURCE_ROOT.resolve("lib/modules/materialAttributes.c"));
        materialAttributes.invoke("Name", "material");
        assertEquals("material", materialAttributes.invoke("RealName"));

        LPCObjectHandle player = runtime.load(MUDLIB_SOURCE_ROOT.resolve("lib/realizations/player.c"));
        List<String> methodNames = Stream.of(player.instance().getClass().getMethods())
                .map(java.lang.reflect.Method::getName)
                .sorted()
                .toList();
        assertFalse(methodNames.contains("isPlayer"), methodNames.toString());
        player.invoke("Name", "codexcheck");

        assertEquals("codexcheck", player.invoke("RealName"));
    }

    @Test
    void realmsClonedEnvironmentsRemainIsolatedByBlueprint() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player.c");
        runtime.invokeObject(player, "Name", "instance-test-player");
        Object first = runtime.loadOrGetObject(
                "areas/tol-dhurath/temple-interior/pedestal-2x1.c");

        runtime.invokeObject(first, "enterEnvironment", player, 0);
        assertEquals(
                "areas/tol-dhurath/temple-interior/pedestal-2x1#clone1",
                runtime.objectId(runtime.environment(player)));
        Object firstClone = runtime.environment(player);
        Object stateMachine = runtime.invokeObject(firstClone, "stateMachine");
        runtime.invokeObject(stateMachine, "syncState", "sixth test");
        runtime.bindSession("test/instance-player", player, "127.0.0.1", ignored -> {});
        runtime.refreshCommandActions(player);

        assertEquals(1, runtime.dispatchCommand(player, "east"));
        assertEquals(
                "areas/tol-dhurath/temple-interior/pedestal-2x2#clone1",
                runtime.objectId(runtime.environment(player)));

    }

    @Test
    void realmsAlternateCharacterCreationAssignsTheOwningAccountBeforePersistence() throws IOException {
        String menuInteractions = Files.readString(
                MUDLIB_SOURCE_ROOT.resolve("secure/login/menu-interactions.c"));

        int playerLookup = menuInteractions.indexOf("loginModule->getPlayerObject(characterName)");
        int accountAssignment = menuInteractions.indexOf("player->setUserName(userName)", playerLookup);
        int sessionTransfer = menuInteractions.indexOf("exec(player, this_object())", playerLookup);

        assertTrue(playerLookup >= 0, menuInteractions);
        assertTrue(accountAssignment > playerLookup, menuInteractions);
        assertTrue(sessionTransfer > accountAssignment, menuInteractions);
    }

    @Test
    void realmsLoginCanAssignAnAccountNameThroughTheCompatibilityObjectIdentity() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player");
        LPCObjectHandle loginCaller = runtime.loadSource("secure/login/account-assignment.c", """
                string assign(object player, string account) {
                    player->setUserName(account);
                    return player->UserName();
                }
                """);

        assertEquals("codex-account", loginCaller.invoke("assign", player, "codex-account"));
    }

    @Test
    void realmsLivingRegistersItsInheritedGameplayHeartbeats() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        LPCObjectHandle player = runtime.load(MUDLIB_SOURCE_ROOT.resolve("lib/realizations/player.c"));

        assertEquals(
                List.of(
                        "combatHeartBeat",
                        "healingHeartBeat",
                        "materialAttributesHeartBeat",
                        "researchHeartBeat",
                        "traitsHeartBeat",
                        "biologicalHeartBeat"),
                inheritedFieldValue(player.instance(), "heartBeatMethods"));
    }

    @Test
    void realmsScheduledLivingHeartbeatDispatchesCombatAgainstARegisteredFoe() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.setScheduler(scheduler);
        runtime.registerMudlibBoundary(boundary);

        Path objectInfoPath = MUDLIB_SOURCE_ROOT.resolve("secure/simulated-efuns/object-info.c");
        String preprocessedObjectInfo = new Preprocessor(
                        new SearchPathIncludeResolver(MUDLIB_SOURCE_ROOT, List.of()),
                        boundary.compatibilityPredefines(),
                        boundary.compatibilityFunctionPredefines())
                .preprocess(objectInfoPath, Files.readString(objectInfoPath));
        assertFalse(preprocessedObjectInfo.contains("int set_heart_beat"), preprocessedObjectInfo);

        Object player = runtime.cloneObject("lib/realizations/player.c");
        Object marta = runtime.cloneObject(
                "areas/argalen-keep/surrounding/characters/marta/marta.c");
        LPCObjectHandle arena = runtime.loadSource("test/arena.c", "void create() {}\n");
        runtime.moveObject(player, arena.instance());
        runtime.moveObject(marta, arena.instance());
        runtime.invokeObject(player, "Name", "heartbeat-test-player");
        runtime.invokeObject(player, "hitPoints", 60);

        runtime.invokeObject(marta, "attack", player);
        Object simulatedEfuns = runtime.loadOrGetObject("secure/simul_efun");
        assertFalse(Stream.of(simulatedEfuns.getClass().getMethods())
                        .anyMatch(method -> method.getName().equals("set_heart_beat")),
                "The Realms fallback must be omitted when the compatibility boundary supplies recurring ticks.");
        @SuppressWarnings("unchecked")
        Map<Object, ?> recurringTicks =
                (Map<Object, ?>) inheritedFieldValue(
                        inheritedFieldValue(runtime, "runtimeContext"), "recurringTickTasks");
        assertTrue(recurringTicks.containsKey(marta),
                "Marta should register herself for recurring ticks when combat starts; registered="
                        + recurringTicks.keySet().stream().map(runtime::objectId).toList());
        int initialHitPoints = ((Number) runtime.invokeObject(player, "hitPoints")).intValue();
        scheduler.advanceBy(20);

        int remainingHitPoints = ((Number) runtime.invokeObject(player, "hitPoints")).intValue();
        assertTrue(remainingHitPoints < initialHitPoints,
                "Marta should attack a registered foe during living heartbeats.");
    }

    @Test
    void realmsDeferredCallbacksPreserveAllArguments() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.setScheduler(scheduler);
        runtime.registerMudlibBoundary(boundary);

        LPCObjectHandle callback = runtime.loadSource("test/realms-callback.c", """
                mixed *received = ({});

                void start() {
                    call_out("finish", 2, this_object(), "regainConsciousness", this_object());
                }

                void finish(object actor, string eventName, object initiator, int optionalFlag) {
                    received = ({ actor, eventName, initiator, optionalFlag });
                }

                mixed *receivedArguments() {
                    return received;
                }
                """);

        callback.invoke("start");
        scheduler.advanceTo(2);

        assertEquals(
                List.of(callback.instance(), "regainConsciousness", callback.instance(), 0),
                callback.invoke("receivedArguments"));
    }

    @Test
    void realmsTutorialAdvancesAfterItsTimedIntroduction() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.setScheduler(scheduler);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player");
        Object battleScene = runtime.loadOrGetObject("tutorial/rooms/battleScene");
        StringBuilder output = new StringBuilder();
        runtime.bindSession("test/tutorial-player", player, "127.0.0.1", output::append);
        runtime.invokeObject(player, "Name", "tutorial-test-player");
        runtime.invokeObject(battleScene, "enterEnvironment", player, 0);
        Object playerBattleScene = runtime.environment(player);
        runtime.refreshCommandActions(player);

        assertEquals(1, runtime.dispatchCommand(player, "resetEverything"));
        Object stateMachine = runtime.invokeObject(playerBattleScene, "stateMachine");
        assertEquals("initiate story", runtime.invokeObject(stateMachine, "getCurrentState", player));
        @SuppressWarnings("unchecked")
        Map<String, Object> actors = (Map<String, Object>) inheritedFieldValue(stateMachine, "actors");
        Object galadhel = actors.get("galadhel");
        assertNotNull(galadhel);
        @SuppressWarnings("unchecked")
        Map<Object, List<String>> eventList =
                (Map<Object, List<String>>) inheritedFieldValue(stateMachine, "eventList");
        assertTrue(eventList.containsKey(galadhel), eventList.toString());
        assertTrue(eventList.get(galadhel).contains("onTriggerConversation"), eventList.toString());

        scheduler.advanceBy(21);

        assertEquals(
                "berenar interjects",
                runtime.invokeObject(stateMachine, "getCurrentState", player),
                output.toString());
        assertTrue(output.toString().contains("Get up, damn it!"), output.toString());
    }

    @Test
    void realmsCombatRechecksTheTargetBetweenStrikes() throws Exception {
        String combat = Files.readString(MUDLIB_SOURCE_ROOT.resolve("lib/modules/combat.c"));

        int attackLoop = combat.indexOf("foreach(mapping attack in getAttacks())");
        int attackGuard = combat.indexOf("if (combatTargetIsGone(foe))", attackLoop);
        int weaponDispatch = combat.indexOf("if(attackObject()->isWeaponAttack(attack))", attackLoop);
        assertTrue(attackLoop >= 0 && attackGuard > attackLoop && attackGuard < weaponDispatch, combat);

        int extraDamageLoop = combat.indexOf("foreach(string damageType in extraDmg)");
        int extraDamageGuard = combat.indexOf("if (combatTargetIsGone(foe))", extraDamageLoop);
        int extraDamageCalculation = combat.indexOf("damage = calculateDamage(weapon, damageType)", extraDamageLoop);
        assertTrue(extraDamageLoop >= 0 && extraDamageGuard > extraDamageLoop
                && extraDamageGuard < extraDamageCalculation, combat);

        int primaryDamage = combat.indexOf("calculateDamage(weapon, primaryDamageType)");
        int primaryDamageGuard = combat.indexOf("if(!combatTargetIsGone(foe))", primaryDamage);
        int primaryHit = combat.indexOf("foe->hit(damage, primaryDamageType", primaryDamage);
        assertTrue(primaryDamage >= 0 && primaryDamageGuard > primaryDamage
                && primaryDamageGuard < primaryHit, combat);

        int postHitGuard = combat.indexOf("if(!combatTargetIsGone(foe))", primaryHit);
        int ordinaryHitMessage = combat.indexOf(
                "attackObject()->displayMessage(this_object(), foe, damageInflicted", primaryHit);
        assertTrue(postHitGuard > primaryHit && postHitGuard < ordinaryHitMessage, combat);
    }

    @Test
    void realmsSwordShopBuildsItsPurchasableItemCatalog() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object shopRoom = runtime.loadOrGetObject("areas/argalen-keep/surrounding/blacksmith/0x0.c");
        Object store = runtime.invokeObject(shopRoom, "getShop");
        Object player = runtime.cloneObject("lib/realizations/player.c");
        Object shopService = runtime.loadOrGetObject("lib/services/shopService.c");

        Object details = runtime.invokeObject(
                shopService, "getBuyItemDetailsForType", player, store, "weapon", "sword");

        assertTrue(details instanceof Map<?, ?> itemDetails && !itemDetails.isEmpty());
    }

    @Test
    void realmsShopSellsOneConsumableWithoutCopyingItsStockCount() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);
        LPCObjectHandle purchaseScenario = runtime.loadSource(
                "test/shop-purchase.c",
                "mixed stockQuantity() {\n"
                        + "    object store = clone_object(\"/lib/environment/shopInventories/baseShop.c\");\n"
                        + "    object potion = clone_object(\"/lib/instances/items/potions/healing.c\");\n"
                        + "    potion->set(\"quantity\", 12);\n"
                        + "    store->storeItem(potion);\n"
                        + "    mapping inventory = store->storeInventory();\n"
                        + "    string stockKey = m_indices(inventory)[0];\n"
                        + "    return inventory[stockKey][\"quantity\"];\n"
                        + "}\n"
                        + "mixed *purchaseOneConsumable() {\n"
                        + "    object player = clone_object(\"/lib/realizations/player.c\");\n"
                        + "    object store = clone_object(\"/lib/environment/shopInventories/baseShop.c\");\n"
                        + "    object potion = clone_object(\"/lib/instances/items/potions/healing.c\");\n"
                        + "    object shop = load_object(\"/lib/services/shopService.c\");\n"
                        + "    player->Money(1000);\n"
                        + "    potion->set(\"quantity\", 12);\n"
                        + "    store->storeItem(potion);\n"
                        + "    mapping details = shop->getBuyItemDetailsForType(player, store, \"potion\", \"all\");\n"
                        + "    string menuKey = m_indices(details)[0];\n"
                        + "    mixed error = catch(shop->buyItem(player, store, details[menuKey]));\n"
                        + "    object purchased = present(\"healing potion\", player);\n"
                        + "    mapping inventory = store->storeInventory();\n"
                        + "    string stockKey = m_indices(inventory)[0];\n"
                        + "    return ({ error, inventory[stockKey][\"quantity\"],\n"
                        + "        purchased ? purchased->query(\"quantity\") : -1 });\n"
                        + "}\n");

        assertEquals(12, runtime.invokeObject(purchaseScenario.instance(), "stockQuantity"));
        assertEquals(List.of(0, 11, 1),
                runtime.invokeObject(purchaseScenario.instance(), "purchaseOneConsumable"));
    }

    @Test
    void unfinishedEledhelUniversityRoomsAreNotAdvertisedAsExits() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        for (String roomPath : List.of(
                "areas/eledhel/southern-city/3x5.c",
                "areas/eledhel/southern-city/4x5.c")) {
            Object room = runtime.loadOrGetObject(roomPath);
            assertFalse(((List<?>) runtime.invokeObject(room, "exits")).contains("north"), roomPath);
        }
        for (String roomPath : List.of(
                "areas/eledhel/southern-city/3x8.c",
                "areas/eledhel/southern-city/4x8.c")) {
            Object room = runtime.loadOrGetObject(roomPath);
            assertFalse(((List<?>) runtime.invokeObject(room, "exits")).contains("south"), roomPath);
        }
    }

    @Test
    void tolDhurathEntryNorthExitNamesItsExistingNeighbor() throws Exception {
        Path source = MUDLIB_SOURCE_ROOT.resolve("areas/tol-dhurath/entry/17x0.c");
        Path destination = MUDLIB_SOURCE_ROOT.resolve("areas/tol-dhurath/entry/17x1.c");

        assertTrue(Files.isRegularFile(destination));
        assertTrue(Files.readString(source)
                .contains("/areas/tol-dhurath/entry/17x1.c"));
    }

    @Test
    void realmsAdvertisedAreaDestinationsExist() throws Exception {
        Pattern destinationPattern = Pattern.compile(
                "add(?:Building|Exit|ExitWithDoor)\\s*\\([^;]*?\"(/areas/[^\"]+\\.c)\"",
                Pattern.DOTALL);
        Path areasRoot = MUDLIB_SOURCE_ROOT.resolve("areas");
        List<Path> areaSources;
        try (Stream<Path> paths = Files.walk(areasRoot)) {
            areaSources = paths.filter(path -> path.toString().endsWith(".c")).toList();
        }

        for (Path source : areaSources) {
            Matcher matcher = destinationPattern.matcher(Files.readString(source));
            while (matcher.find()) {
                String destination = matcher.group(1);
                Path destinationSource = MUDLIB_SOURCE_ROOT.resolve(destination.substring(1));
                assertTrue(Files.isRegularFile(destinationSource),
                        () -> source + " advertises missing destination " + destination);
            }
        }
    }

    @Test
    void realmsArgalenQuestCanBeginAndAdvanceThroughItsActualStateMachine() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player.c");
        Object quest = runtime.loadOrGetObject(
                "areas/argalen-keep/state-machine/argalen-keep-quest.c");
        String questPath = "/areas/argalen-keep/state-machine/argalen-keep-quest.c";
        runtime.invokeObject(player, "Name", "quest-test-player");

        assertEquals(1, runtime.invokeObject(player, "beginQuest", questPath));
        assertEquals(List.of(questPath), runtime.invokeObject(player, "activeQuests"));
        assertEquals("peaceful", runtime.invokeObject(player, "questState", questPath));
        assertTrue(((String) runtime.invokeObject(player, "questStory", questPath))
                .contains("Argalen Keep stands as it always has"));

        assertEquals(1, runtime.invokeObject(quest, "receiveEvent", player, "startPatrol"));
        assertEquals("on patrol", runtime.invokeObject(player, "questState", questPath));
        assertTrue(((String) runtime.invokeObject(player, "questStory", questPath))
                .contains("departed Argalen Keep on patrol"));
    }

    @Test
    void realmsPlayerCanJoinTheActualFighterGuild() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player.c");
        Object guildsService = runtime.loadOrGetObject("lib/services/guildsService.c");
        Object researchService = runtime.loadOrGetObject("lib/services/researchService.c");
        runtime.loadOrGetObject("guilds/fighter/fighter.c");
        List<String> trees = List.of(
                "blade-attacks", "axe-attacks", "polearm-attacks", "shields",
                "bludgeon-attacks", "combat-techniques");
        for (String tree : trees) {
            runtime.loadOrGetObject("guilds/fighter/" + tree + ".c");
            assertNotNull(runtime.invokeObject(
                    researchService, "researchTree", "/guilds/fighter/" + tree + ".c"));
        }
        StringBuilder output = new StringBuilder();
        runtime.bindSession("test/fighter-player", player, "127.0.0.1", output::append);
        runtime.invokeObject(player, "Name", "fighter-test-player");
        assertEquals(1, runtime.invokeObject(guildsService, "isValidGuild", "fighter"));

        assertEquals(1, runtime.invokeObject(player, "joinGuild", "fighter"));
        assertEquals(1, runtime.invokeObject(player, "memberOfGuild", "fighter"));
        assertEquals(1, runtime.invokeObject(player, "guildLevel", "fighter"));
        assertTrue(((Number) runtime.invokeObject(player, "AvailableSkillPoints")).intValue() > 0);
        assertTrue(((Number) runtime.invokeObject(player, "researchPoints")).intValue() > 0);
        assertFalse(output.isEmpty(), "Joining the guild should initiate its level-up selector.");
    }

    @Test
    void realmsMarketPriceCategoryDisplaysItsNextMenuOnce() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player.c");
        StringBuilder output = new StringBuilder();
        runtime.bindSession("test/trading-player", player, "127.0.0.1", output::append);
        runtime.invokeObject(player, "Name", "trading-test-player");
        runtime.refreshCommandActions(player);

        assertEquals(1, runtime.dispatchCommand(player, "trading"));
        assertTrue(((Number) runtime.dispatchCommand(player, "2")).intValue() != 0);
        output.setLength(0);
        assertTrue(((Number) runtime.dispatchCommand(player, "4")).intValue() != 0);

        String instruction = "You must select a number from 1 to 68.";
        assertEquals(1, countOccurrences(output.toString(), instruction), () -> "output=" + output);
    }

    @Test
    void realmsTravelCommandPlansItsRequestedRouteFromTheActualPort() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player.c");
        Object port = runtime.loadOrGetObject("lib/modules/domains/trading/ports/eledhel.c");
        Object vehicleService = runtime.loadOrGetObject("lib/services/vehicleService.c");
        StringBuilder output = new StringBuilder();
        runtime.bindSession("test/travel-player", player, "127.0.0.1", output::append);
        runtime.invokeObject(player, "Name", "travel-test-player");
        runtime.invokeObject(player, "initializeTrader");
        runtime.invokeObject(player, "addCash", 5000);
        int cashBeforeTravel = ((Number) runtime.invokeObject(player, "getCash")).intValue();
        runtime.invokeObject(player, "setCurrentLocation", "Eledhel");
        Object wagon = runtime.invokeObject(player, "addVehicle", "wagon", "Eledhel");
        Object blueprint = runtime.invokeObject(vehicleService, "queryVehicleBlueprint", "wagon");
        runtime.invokeObject(wagon, "initializeVehicle", blueprint);
        runtime.moveObject(player, port);
        runtime.refreshCommandActions(player);

        assertEquals(1, runtime.dispatchCommand(player, "travel Hillgarath"));
        assertTrue(output.toString().contains("Hillgarath (overland route)"), () -> "output=" + output);
        assertFalse(output.toString().contains("Orothysse (maritime route)"), () -> "output=" + output);

        output.setLength(0);
        assertTrue(((Number) runtime.dispatchCommand(player, "1")).intValue() != 0);
        assertTrue(output.toString().contains("Travel Confirmation"), () -> "output=" + output);
        assertTrue(output.toString().contains("Confirm Travel to Hillgarath"), () -> "output=" + output);

        output.setLength(0);
        assertTrue(((Number) runtime.dispatchCommand(player, "1")).intValue() != 0);
        assertTrue(((Number) runtime.invokeObject(player, "getCash")).intValue() < cashBeforeTravel);
        assertEquals("Hillgarath", runtime.invokeObject(player, "getCurrentLocation"));
        assertEquals("Hillgarath", runtime.invokeObject(wagon, "getLocation"));
    }

    @Test
    void realmsEnvironmentalDescriptionsSeparateUnspacedDetailFragments() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object environmentService = runtime.loadOrGetObject("lib/services/environmentService.c");
        Object appleStand = runtime.loadOrGetObject("lib/environment/features/trees/apple-stand.c");
        runtime.invokeObject(environmentService, "season", "summer");
        runtime.invokeObject(environmentService, "timeOfDay", "noon");
        Object ambientLight = runtime.invokeObject(environmentService, "ambientLight");

        String description = (String) runtime.invokeObject(
                appleStand, "description", "default", ambientLight);

        assertFalse(description.contains("fruitcasting"), description);
        assertFalse(description.contains("leavescasting"), description);
        assertFalse(description.contains("fruitclearly"), description);
        assertFalse(description.contains("leavesclearly"), description);
    }

    @Test
    void realmsRegexReplacementWrapperPreservesCallableReplacements() throws IOException {
        String source = Files.readString(
                MUDLIB_SOURCE_ROOT.resolve("secure/simulated-efuns/strings.c"));

        assertTrue(
                source.contains("public nomask varargs string regreplace(string inputString, string search,\n"
                        + "    mixed replace, int flags)"),
                "Realms' regex helper must accept both text and callable replacement values");
    }

    @Test
    void realmsSayCompatibilityExcludesTheCurrentPlayer() throws IOException {
        String source = Files.readString(MUDLIB_ROOT.resolve("jvmud/compat.c"));

        assertTrue(
                source.contains(
                        "jvmud_emit_perceivable_except(this_object(), message, this_player());"),
                "Realms' say adapter must not echo the observer message back to its actor");
    }

    @Test
    void realmsFullPreloadManifestExcludesMudlibTestFixtures() throws IOException {
        String generator = Files.readString(REPO_ROOT.resolve("mudlibs/realmsmud/jvmud/generate-full-init"));

        assertTrue(generator.contains("! -path \"$SOURCE_DIR/lib/tests/*\""), generator);
    }

    @Test
    void realmsQuestPersistenceAcceptsTheFullStateHistoryColumnWidth() throws IOException {
        String initialSchema = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "secure/simulated-efuns/database/initial/generateDB.sql"));
        String migration = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "secure/simulated-efuns/database/migrations/0011_expand_quest_state_history.sql"));

        assertTrue(initialSchema.contains("p_statesCompleted varchar(256)"), initialSchema);
        assertTrue(migration.contains("p_statesCompleted varchar(256)"), migration);
    }

    @Test
    void realmsObedienceQuestRebuildsTransientStateAfterReload() throws IOException {
        String room = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/temple-interior/pedestal-1x1.c"));
        String quest = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/state-machine/obedience-quest.c"));

        assertTrue(room.contains("(state != RestoredState) && (state == \"first test\")"), room);
        assertTrue(room.contains("(state != RestoredState) && (state == \"second test\")"), room);
        assertTrue(room.contains("startSecondTest(0, this_player());"), room);
        assertTrue(room.contains("startFinalPassage(0, this_player());"), room);
        assertTrue(quest.contains("private object ensureUhrdalen()"), quest);
        assertTrue(quest.contains("ensureUhrdalen();\n\n    if (!objectp(player)"), quest);
    }

    @Test
    void realmsEchoPoolAwardsItsDelayedCompletionOnlyOnce() throws IOException {
        String source = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/objects/echo-pool.c"));

        assertTrue(source.contains("public void finishOrder()\n{\n"
                + "    // Several order commands can queue callbacks before the first one runs.\n"
                + "    // Once one callback completes the puzzle, the remaining callbacks must not\n"
                + "    // grant the reward or advance the quest again.\n"
                + "    if (puzzleSolved)\n"
                + "    {\n"
                + "        return;\n"
                + "    }"), source);
    }

    @Test
    void realmsEchoPoolRestoresCompletionForTheThirdTest() throws IOException {
        String pool = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/objects/echo-pool.c"));
        String room = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/temple-interior/pedestal-pilon.c"));

        assertTrue(pool.contains("public void restoreSolvedState()\n{\n"
                + "    puzzleSolved = 1;\n"
                + "}"), pool);
        assertTrue(room.contains("if (present(this_player()) && (currentState() == \"third test\") &&\n"
                + "        echoPool())\n"
                + "    {\n"
                + "        echoPool()->restoreSolvedState();\n"
                + "    }"), room);
    }

    @Test
    void realmsCrucibleAcceptsItsDocumentedCommandsAndVisibleName() throws IOException {
        String source = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/objects/crucible.c"));

        assertTrue(source.contains("return member(({ \"crucible-hidden\", \"braziers\", \"brazier\","),
                source);
        assertTrue(source.contains("element = regreplace(element, \" ?brazier$\", \"\");"), source);
        assertFalse(source.contains("element = regreplace(element, \" ?(brazier|flame|fire)$\", \"\");"),
                source);
    }

    @Test
    void realmsShadowMirrorCanBeExaminedByItsVisibleNames() throws IOException {
        String source = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/objects/shadow-mirror.c"));

        assertTrue(source.contains("return member(({ \"mirror-hidden\", \"mirror\", \"shadow\", \"reflection\","),
                source);
    }

    @Test
    void realmsDreamPoolCanBeExaminedByItsVisibleNames() throws IOException {
        String source = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/objects/dream-pool.c"));

        assertTrue(source.contains("return member(({ \"dream-hidden\", \"pool\", \"dream pool\", \"liquid\","),
                source);
    }

    @Test
    void realmsDreamTracksEachPlayersVisionIndependently() throws IOException {
        String pool = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/objects/dream-pool.c"));
        String entry = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/temple-interior/dream/dream-entry.c"));

        assertTrue(pool.contains("dreamEntry->setReturnPool(this_player(), this_object());"), pool);
        assertTrue(entry.contains("private mapping returnPools = ([]);"), entry);
        assertTrue(entry.contains("private mapping choicesMade = ([]);"), entry);
        assertTrue(entry.contains("choicesMade[player]++;"), entry);
        assertTrue(entry.contains("object pool = getReturnPool(player);"), entry);
        assertTrue(entry.contains("m_delete(returnPools, player);"), entry);
    }

    @Test
    void realmsDreamScenesDoNotLeakConversationStyleTokens() throws IOException {
        for (String scene : List.of("dream-temptation.c", "dream-bargain.c", "dream-fall.c")) {
            String source = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                    "areas/tol-dhurath/temple-interior/dream/" + scene));
            assertFalse(source.contains("@S@"), scene + ": " + source);
        }
    }

    @Test
    void realmsGauntletCanBeExaminedAndRestoresQuestCompletion() throws IOException {
        String source = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/objects/gauntlet.c"));

        assertTrue(source.contains("return member(({ \"gauntlet-hidden\", \"gauntlet\", \"glyph\","),
                source);
        assertTrue(source.contains("private void restoreCompletedGauntlet()"), source);
        assertTrue(source.contains("({ \"seventh test\", \"poem complete\", \"quest complete\" })"),
                source);
        assertTrue(source.contains("restoreCompletedGauntlet();\n    add_action"), source);
    }

    @Test
    void realmsRuneWallPersistsPlacedRunesAndRepairsResistanceProgress() throws IOException {
        String source = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/objects/rune-wall.c"));

        assertTrue(source.contains("return \"test of obedience rune wall\";"), source);
        assertTrue(source.contains("private mapping RuneBits = (["), source);
        assertTrue(source.contains("private string encodeRunes(string *runes)"), source);
        assertTrue(source.contains("public int hasRecordedRune(object player, string rune)"), source);
        assertTrue(source.contains("return sprintf(\"placed:%d\", placed);"), source);
        assertTrue(source.contains("player->characterState(this_object(), encodeRunes(placed));"), source);
        assertTrue(source.contains("ret = explode(encoded, \",\");"), source);
        assertTrue(source.contains("private void restorePlacedRunes(object player)"), source);
        assertTrue(source.contains("placed += ({ \"resistance\" });"), source);
        assertTrue(source.contains("wall[rune, 0] = verses[rune];"), source);
        assertTrue(source.contains("if ((questState == \"seventh test\") && allRunesPlaced())"), source);
        assertTrue(source.contains("stateMachine->receiveEvent(player, \"allRunesPlaced\");"), source);
        assertTrue(source.contains("restorePlacedRunes(this_player());\n    add_action"), source);
    }

    @Test
    void realmsFinalUhrdalenConversationDoesNotRepeatAfterAwardingEnvy() throws IOException {
        String source = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/state-machine/obedience-quest.c"));

        assertTrue(source.contains("present(\"rune of envy\", player) ||\n"
                + "        runeWall->hasRecordedRune(player, \"envy\")"), source);
        assertTrue(source.contains("object runeWall = load_object(\n"
                + "        \"/areas/tol-dhurath/objects/rune-wall.c\");"), source);
        assertTrue(source.contains("move_object(Uhrdalen, load_object(HoldingRoom));\n"
                + "        return;"), source);
    }

    @Test
    void realmsSolvedFourthPedestalAllowsThePlayerIntoItsMaze() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.setScheduler(scheduler);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player.c");
        runtime.bindSession("test/fourth-pedestal-player", player, "127.0.0.1", ignored -> {});
        runtime.invokeObject(player, "Name", "fourth-pedestal-player");
        Object blueprint = runtime.loadOrGetObject(
                "areas/tol-dhurath/temple-interior/pedestal-1x1.c");
        runtime.invokeObject(blueprint, "enterEnvironment", player, 0);
        Object room = runtime.environment(player);
        Object stateMachine = runtime.invokeObject(room, "stateMachine");
        runtime.invokeObject(stateMachine, "syncState", "fourth test");
        runtime.invokeObject(room, "currentState", "fourth test");
        runtime.invokeObject(room, "startFourthTest", 0, player);
        runtime.refreshCommandActions(player);

        for (int repetition = 0; repetition < 2; repetition++) {
            for (String plate : List.of("envy", "fear", "sorrow", "wrath")) {
                assertEquals(1, runtime.dispatchCommand(player, "press " + plate));
                scheduler.advanceBy(2);
            }
        }

        assertEquals(1, runtime.dispatchCommand(player, "east"));
        assertTrue(runtime.objectId(runtime.environment(player)).contains("pedestal-1x2"),
                runtime.objectId(runtime.environment(player)));
    }

    @Test
    void realmsSolvedFifthPedestalAllowsThePlayerIntoItsMaze() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        WorldScheduler scheduler = new WorldScheduler();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.setScheduler(scheduler);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player.c");
        runtime.bindSession("test/fifth-pedestal-player", player, "127.0.0.1", ignored -> {});
        runtime.invokeObject(player, "Name", "fifth-pedestal-player");
        Object blueprint = runtime.loadOrGetObject(
                "areas/tol-dhurath/temple-interior/pedestal-1x1.c");
        runtime.invokeObject(blueprint, "enterEnvironment", player, 0);
        Object room = runtime.environment(player);
        Object stateMachine = runtime.invokeObject(room, "stateMachine");
        runtime.invokeObject(stateMachine, "syncState", "fifth test");
        runtime.invokeObject(room, "currentState", "fifth test");
        runtime.invokeObject(room, "startFifthTest", 0, player);
        runtime.refreshCommandActions(player);

        for (String plate : List.of("sorrow", "wrath")) {
            assertEquals(1, runtime.dispatchCommand(player, "press " + plate));
            scheduler.advanceBy(2);
        }

        assertEquals(1, runtime.dispatchCommand(player, "east"));
        assertTrue(runtime.objectId(runtime.environment(player)).contains("pedestal-1x2"),
                runtime.objectId(runtime.environment(player)));
    }

    @Test
    void realmsPedestalPersistsAndRestoresAnOpenedPassage() throws IOException {
        String pedestal = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/objects/pedestal.c"));
        String room = Files.readString(MUDLIB_SOURCE_ROOT.resolve(
                "areas/tol-dhurath/temple-interior/pedestal-1x1.c"));

        assertTrue(pedestal.contains("player->characterState(this_object(), passageState);"), pedestal);
        assertTrue(pedestal.contains("public int restoreOpenedPassage(object player, string test)"), pedestal);
        assertTrue(pedestal.contains("orbs = validSequences[test] + ([]);"), pedestal);
        assertTrue(room.contains("pedestal()->restoreOpenedPassage(this_player(), PassageTests[state])"), room);
        assertTrue(room.contains("if (!passageRestored && StateMachine"), room);
    }

    @Test
    void realmsPedestalRoomEvaluatesItsDynamicDescriptionCallback() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object room = runtime.cloneObject(
                "areas/tol-dhurath/temple-interior/pedestal-1x1.c");
        String description = String.valueOf(runtime.invokeObject(room, "long", 0, 0));

        assertFalse(description.contains("#<function"), description);
        assertFalse(description.contains("##DESC COLOR##"), description);
        assertTrue(Pattern.compile("Beside\\s+each of the pedestals").matcher(description).find(), description);
    }

    @Test
    void realmsActiveQuestDataCanBeCollectedForPersistence() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        LPCObjectHandle player = runtime.loadSource("jvmud-tests/quest-saving.c", """
                inherit "/lib/realizations/player.c";

                mapping questSnapshot() {
                    return sendQuests();
                }
                """);
        String questPath = "/areas/tol-dhurath/state-machine/obedience-quest.c";

        assertEquals(1, player.invoke("beginQuest", questPath));
        Object snapshot = player.invoke("questSnapshot");
        assertTrue(snapshot instanceof Map<?, ?>);
        Object quests = ((Map<?, ?>) snapshot).get("quests");
        assertTrue(quests instanceof Map<?, ?>);
        assertTrue(((Map<?, ?>) quests).containsKey(questPath), String.valueOf(snapshot));
    }

    @Test
    void realmsRoadsideSignPostCanBeExamined() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object signPost = runtime.loadOrGetObject("lib/environment/items/objects/signPost");

        String description = runtime.invokeObject(signPost, "long", 0).toString();
        assertTrue(description.startsWith("You see the signpost is a weathered wooden post"), description);
        assertTrue(description.stripTrailing().endsWith("exposure to the weather."), description);
    }

    @Test
    void realmsAppleUsesItsFoodBlueprintInsteadOfTheNamesakeWoodMaterial() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object apple = runtime.cloneObject("lib/instances/items/food/plants/fruit/apple");
        String description = runtime.invokeObject(apple, "long", 1).toString();
        Object appleWood = runtime.cloneObject("lib/instances/items/materials/wood/apple");
        String woodDescription = runtime.invokeObject(appleWood, "long", 1).toString();

        assertTrue(description.contains("Round fruits with crisp flesh"), description);
        assertFalse(description.contains("plank of apple wood"), description);
        assertTrue(woodDescription.contains("plank of apple wood"), woodDescription);
    }

    @Test
    void realmsActualPlayerCraftsAWeaponThroughTheProductionSelectors() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player.c");
        LPCObjectHandle workshop = runtime.loadSource("test/crafting-workshop.c", "void create() {}\n");
        StringBuilder output = new StringBuilder();
        runtime.bindSession("test/crafting-player", player, "127.0.0.1", output::append);
        runtime.invokeObject(player, "Name", "crafting-test-player");
        runtime.invokeObject(player, "Wis", 50);
        runtime.invokeObject(player, "Str", 50);
        runtime.invokeObject(player, "Int", 50);
        runtime.invokeObject(player, "addSkillPoints", 500);
        for (Map.Entry<String, Integer> skill : Map.of(
                        "blacksmithing", 20,
                        "metal crafting", 10,
                        "weapon smithing", 10,
                        "chemistry", 10,
                        "physics", 10,
                        "leatherworking", 20,
                        "carpentry", 10,
                        "wood crafting", 10,
                        "gem crafting", 20,
                        "sculpture", 15)
                .entrySet()) {
            assertTrue(((Number) runtime.invokeObject(
                    player, "advanceSkill", skill.getKey(), skill.getValue())).intValue() != 0);
        }
        runtime.invokeObject(player, "addResearchPoints", 20);
        for (String research : List.of(
                "crafting/materials/craftCommonMetal",
                "crafting/materials/craftGems",
                "crafting/materials/craftRareGems",
                "crafting/materials/craftUncommonMetal",
                "crafting/materials/craftAlloy",
                "crafting/materials/craftRareMetal",
                "crafting/materials/craftPreciousMetal",
                "crafting/materials/craftMythicMetal",
                "crafting/materials/craftLeather",
                "crafting/materials/craftExoticLeather",
                "crafting/materials/craftCommonWood",
                "crafting/materials/craftUncommonWood",
                "crafting/materials/craftRareWood",
                "crafting/weapons/craftWeapons",
                "crafting/weapons/common/annealing",
                "crafting/weapons/swords/craftLongSwords")) {
            assertEquals(1, runtime.invokeObject(player, "initiateResearch",
                    "/lib/instances/research/" + research + ".c"), research);
            if (research.equals("crafting/weapons/craftWeapons")) {
                assertEquals(1, runtime.invokeObject(player, "addResearchTree",
                        "/lib/instances/research/crafting/weapons/swords/swordsmithing.c"));
            }
        }

        for (Map.Entry<String, Integer> material : List.of(
                Map.entry("metal/admantite", 5),
                Map.entry("metal/admantite", 6),
                Map.entry("metal/steel", 10),
                Map.entry("metal/iron", 3),
                Map.entry("metal/iron", 5),
                Map.entry("wood/koa", 5),
                Map.entry("leather/pegasus-leather", 5),
                Map.entry("metal/gold", 3),
                Map.entry("metal/platinum", 3),
                Map.entry("metal/galvorn", 3),
                Map.entry("crystal/ruby", 5))) {
            Object item = runtime.cloneObject(
                    "lib/instances/items/materials/" + material.getKey() + ".c");
            runtime.invokeObject(item, "set", "quantity", material.getValue());
            runtime.moveObject(item, player);
        }

        Object selector = runtime.cloneObject("lib/modules/crafting/selectMaterialsSelector.c");
        runtime.invokeObject(selector, "setItem", "long sword");
        runtime.invokeObject(selector, "setType", "weapons");
        runtime.invokeObject(selector, "setSubType", "swords");
        runtime.moveObject(player, workshop.instance());
        runtime.moveObject(selector, player);
        runtime.invokeObject(selector, "initiateSelector", player);

        for (String selection : List.of(
                "1", "1", "1", "6", "25",
                "2", "11", "1", "26", "2", "15", "25",
                "3", "9", "1", "13", "2", "16", "3", "38", "11",
                "4", "13", "2", "35", "1", "26", "27")) {
            assertTrue(((Number) runtime.dispatchCommand(player, selection)).intValue() != 0,
                    () -> "selection=" + selection + ", output=" + output);
        }

        output.setLength(0);
        assertTrue(((Number) runtime.dispatchCommand(player, "8")).intValue() != 0);
        assertTrue(output.toString().contains("successfully crafted long sword"),
                () -> "output=" + output);
        List<String> inventoryNames = new java.util.ArrayList<>();
        for (Object item = runtime.firstInventory(player); item != null;
                item = runtime.nextInventory(item)) {
            String itemId = runtime.objectId(item);
            if (itemId != null && itemId.contains("/items/")) {
                inventoryNames.add(String.valueOf(runtime.invokeObject(item, "query", "name")));
            }
        }
        assertTrue(inventoryNames.stream().anyMatch(name -> name.equalsIgnoreCase("long sword")),
                inventoryNames.toString());
        assertNotNull(runtime.present("long sword", player), inventoryNames.toString());
    }

    @Test
    void realmsPotionConsumptionReportsTheActionToItsPlayer() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player.c");
        Object potion = runtime.cloneObject("lib/instances/items/potions/healing.c");
        LPCObjectHandle arena = runtime.loadSource("test/potion-arena.c", "void create() {}\n");
        StringBuilder output = new StringBuilder();
        runtime.bindSession("test/potion-player", player, "127.0.0.1", output::append);
        runtime.moveObject(player, arena.instance());
        runtime.moveObject(potion, player);
        runtime.invokeObject(player, "Name", "potion-test-player");
        int hitPointsBefore = ((Number) runtime.invokeObject(player, "hitPoints")).intValue();

        runtime.clearOutputTranscript();
        output.setLength(0);
        assertEquals(1, runtime.dispatchCommand(player, "drink healing potion"));

        assertTrue(output.toString().contains("You drink Healing Potion."), () -> "output=" + output);
        assertTrue(((Number) runtime.invokeObject(player, "hitPoints")).intValue() > hitPointsBefore);
        assertNull(runtime.present("healing potion", player));
    }

    @Test
    void realmsWeaponActionsReportEquipmentChangesToTheirPlayer() throws Exception {
        MudlibBoundary boundary = MudlibBoundaryConfigReader.read(MUDLIB_ROOT, CONFIG_PATH);
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(MUDLIB_ROOT)
                .build());
        CoreEfuns.registerCore(runtime);
        runtime.registerMudlibBoundary(boundary);

        Object player = runtime.cloneObject("lib/realizations/player.c");
        Object claymore = runtime.cloneObject("lib/instances/items/weapons/swords/claymore.c");
        LPCObjectHandle arena = runtime.loadSource("test/equipment-arena.c", "void create() {}\n");
        StringBuilder output = new StringBuilder();
        runtime.bindSession("test/equipment-player", player, "127.0.0.1", output::append);
        runtime.moveObject(player, arena.instance());
        runtime.moveObject(claymore, player);
        runtime.invokeObject(player, "Name", "equipment-test-player");

        runtime.refreshCommandActions(player);
        assertEquals(1, runtime.dispatchCommand(player, "wield claymore"));
        assertTrue(output.toString().contains("You equip Claymore."), () -> "output=" + output);

        output.setLength(0);
        runtime.refreshCommandActions(player);
        assertEquals(1, runtime.dispatchCommand(player, "unwield claymore"));
        assertTrue(output.toString().contains("You unequip Claymore."), () -> "output=" + output);
    }

    private static Object inheritedFieldValue(Object target, String fieldName) throws Exception {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                java.lang.reflect.Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                // Continue through the generated LPC inheritance chain.
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static int countOccurrences(String value, String target) {
        int count = 0;
        for (int start = 0; (start = value.indexOf(target, start)) >= 0; start += target.length()) {
            count++;
        }
        return count;
    }

    private static List<String> compatibilitySet(Path sourceRoot) throws IOException {
        Set<String> sourceNames = new LinkedHashSet<>(BOOT_AND_CORE_COMPATIBILITY_SET);
        for (String sweepRoot : COMPATIBILITY_SWEEP_ROOTS) {
            Path root = sourceRoot.resolve(sweepRoot);
            try (Stream<Path> paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".c"))
                        .map(sourceRoot::relativize)
                        .map(Path::toString)
                        .map(path -> path.replace('\\', '/'))
                        .sorted()
                        .forEach(sourceNames::add);
            }
        }
        return List.copyOf(sourceNames);
    }

    private static Path compatibilitySourcePath(Path sourceRoot, String sourceName) {
        if (sourceName.startsWith("jvmud/"))
            return MUDLIB_ROOT.resolve(sourceName);
        return sourceRoot.resolve(sourceName);
    }

    private static void assertConfiguredBoundary(MudlibBoundary boundary) {
        assertEquals("realmsmud", boundary.gameId().orElseThrow());
        assertEquals("RealmsMUD", boundary.gameName().orElseThrow());
        assertEquals("jvmud/mudlib", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("secure/simul_efun", boundary.mudlibGlobalObjectPath().orElseThrow());
        assertEquals("jvmud/compat", boundary.compatibilityGlobalObjectPath().orElseThrow());
        assertEquals(Set.of("players", "wizards"), boundary.compatibilityGlobalOverrides());
        assertEquals(
                MUDLIB_ROOT.resolve("jvmud/compat.c").toAbsolutePath().normalize(),
                boundary.compatibilityGlobalObjectSourcePath().orElseThrow());
        assertEquals("secure/login", boundary.playerObjectPath().orElseThrow());
        assertEquals(">", boundary.playerPrompt().orElseThrow());
        assertEquals(78, boundary.maxLineLength());
        assertFalse(boundary.showRuler());
        assertEquals("areas/eledhel/southern-city/12x2", boundary.initialPlacePath().orElseThrow());
        assertEquals("init/init_file", boundary.preloadFilePath().orElseThrow());
        assertTrue(boundary.preloadObjectPaths().contains("secure/master"));
        assertTrue(boundary.preloadObjectPaths().contains("secure/simul_efun"));
        assertTrue(boundary.preloadObjectPaths().contains("lib/services/environmentService"));
        assertEquals("jdbc:mysql://127.0.0.1:3306/RealmsLib", boundary.databaseJdbcUrl().orElseThrow());
        assertEquals("realmslib", boundary.databaseUser().orElseThrow());
        assertFalse(boundary.databasePassword().orElseThrow().isBlank());
        assertEquals("jvmud_allocate", boundary.engineFunction("allocate").orElseThrow());
        assertEquals("jvmud_format_text", boundary.engineFunction("sprintf").orElseThrow());
        assertEquals("jvmud_db_exec", boundary.engineFunction("db_exec").orElseThrow());
        assertEquals("jvmud_db_fetch", boundary.engineFunction("db_fetch").orElseThrow());
        assertEquals("jvmud_find_player", boundary.engineFunction("findPlayer").orElseThrow());
        assertEquals("jvmud_add_action", boundary.engineFunction("add_action").orElseThrow());
        assertEquals("jvmud_enable_commands", boundary.engineFunction("enable_commands").orElseThrow());
        assertEquals("jvmud_rebind_session_lpc_object", boundary.engineFunction("exec").orElseThrow());
        assertEquals("jvmud_invoke_lpc_object", boundary.engineFunction("call_other").orElseThrow());
        assertEquals("jvmud_write_to_lpc_object", boundary.engineFunction("tell_object").orElseThrow());
        assertEquals("jvmud_capture_session_input", boundary.engineFunction("input_to").orElseThrow());
        assertEquals("jvmud_current_lpc_object", boundary.engineFunction("this_object").orElseThrow());
        assertEquals("jvmud_current_verb", boundary.engineFunction("query_verb").orElseThrow());
        assertEquals("jvmud_current_verb", boundary.engineFunction("query_command").orElseThrow());
        assertEquals("jvmud_query_ip_number", boundary.engineFunction("query_ip_number").orElseThrow());
        assertEquals("jvmud_query_ip_name", boundary.engineFunction("query_ip_name").orElseThrow());
        assertEquals("jvmud_query_idle", boundary.engineFunction("query_idle").orElseThrow());
        assertEquals("jvmud_lpc_object_info", boundary.engineFunction("object_info").orElseThrow());
        assertEquals("jvmud_lpc_object_id", boundary.engineFunction("object_name").orElseThrow());
        assertEquals("jvmud_configure_lpc_object", boundary.engineFunction("configure_object").orElseThrow());
        assertEquals("jvmud_find_lpc_object", boundary.engineFunction("find_object").orElseThrow());
        assertEquals("jvmud_find_lpc_object", boundary.engineFunction("blueprint").orElseThrow());
        assertEquals("jvmud_to_lpc_object", boundary.engineFunction("to_object").orElseThrow());
        assertEquals("jvmud_list_mudlib_paths", boundary.engineFunction("get_dir").orElseThrow());
        assertEquals("jvmud_load_lpc_object", boundary.engineFunction("load_object").orElseThrow());
        assertEquals("jvmud_clone_lpc_object", boundary.engineFunction("clone_object").orElseThrow());
        assertEquals("jvmud_method_exists", boundary.engineFunction("function_exists").orElseThrow());
        assertEquals("jvmud_find_entity", boundary.engineFunction("present").orElseThrow());
        assertEquals("jvmud_move_entity", boundary.engineFunction("move_object").orElseThrow());
        assertEquals("jvmud_set_entity_location", boundary.engineFunction("set_environment").orElseThrow());
        assertEquals("jvmud_entity_location", boundary.engineFunction("environment").orElseThrow());
        assertEquals("jvmud_remove_mudlib_text", boundary.engineFunction("rm").orElseThrow());
        assertEquals("jvmud_copy_mudlib_text", boundary.engineFunction("copy_file").orElseThrow());
        assertEquals("jvmud_rename_mudlib_text", boundary.engineFunction("rename").orElseThrow());
        assertTrue(boundary.engineFunction("mkdir").isEmpty());
        assertEquals("jvmud_remove_mudlib_directory", boundary.engineFunction("rmdir").orElseThrow());
        assertEquals("jvmud_regex_match", boundary.engineFunction("regexp").orElseThrow());
        assertEquals("jvmud_regex_replace", boundary.engineFunction("regreplace").orElseThrow());
        assertEquals("jvmud_regex_explode", boundary.engineFunction("regexplode").orElseThrow());
        assertEquals("jvmud_remove_action", boundary.engineFunction("remove_action").orElseThrow());
        assertTrue(boundary.engineFunction("driver_info").isEmpty());
        assertEquals("jvmud_notify_fail", boundary.engineFunction("notify_fail").orElseThrow());
        assertEquals("jvmud_to_int", boundary.engineFunction("to_int").orElseThrow());
        assertEquals("jvmud_to_string", boundary.engineFunction("to_string").orElseThrow());
        assertEquals("jvmud_time", boundary.engineFunction("time").orElseThrow());
        assertEquals("jvmud_sqrt", boundary.engineFunction("sqrt").orElseThrow());
        assertEquals("jvmud_write", boundary.engineFunction("write").orElseThrow());
        assertEquals("jvmud_emit_perceivable_at", boundary.engineFunction("tell_room").orElseThrow());
        assertEquals("jvmud_member", boundary.engineFunction("member").orElseThrow());
        assertEquals("jvmud_mapping_keys", boundary.engineFunction("m_indices").orElseThrow());
        assertEquals("jvmud_mapping_values", boundary.engineFunction("m_values").orElseThrow());
        assertEquals("jvmud_mapping_from_keys", boundary.engineFunction("mkmapping").orElseThrow());
        assertEquals("jvmud_mapping_delete", boundary.engineFunction("m_delete").orElseThrow());
        assertEquals("jvmud_inherited_programs", boundary.engineFunction("inherit_list").orElseThrow());
        assertEquals("jvmud_serialize_lpc_value", boundary.engineFunction("save_value").orElseThrow());
        assertEquals("jvmud_deserialize_lpc_value", boundary.engineFunction("restore_value").orElseThrow());
        assertEquals("jvmud_schedule_deferred_callback", boundary.engineFunction("call_out").orElseThrow());
        assertEquals("jvmud_cancel_deferred_callback", boundary.engineFunction("remove_call_out").orElseThrow());
        assertEquals("jvmud_wrap_text", boundary.engineFunction("format").orElseThrow());
        assertEquals("jvmud_apply_callable", boundary.engineFunction("apply").orElseThrow());
        assertEquals("jvmud_filter", boundary.engineFunction("filter").orElseThrow());
        assertEquals("jvmud_filter_indices", boundary.engineFunction("filter_indices").orElseThrow());
        assertEquals("jvmud_map", boundary.engineFunction("map").orElseThrow());
        assertEquals("jvmud_sort_array", boundary.engineFunction("sort_array").orElseThrow());
        assertEquals("jvmud_sscanf", boundary.engineFunction("sscanf").orElseThrow());
        assertEquals("jvmud_random", boundary.engineFunction("random").orElseThrow());
        assertEquals("jvmud_capitalize_text", boundary.engineFunction("capitalize").orElseThrow());
        assertEquals("jvmud_is_mapping", boundary.engineFunction("mappingp").orElseThrow());
        assertEquals("jvmud_is_object", boundary.engineFunction("objectp").orElseThrow());
        assertEquals("jvmud_is_array", boundary.engineFunction("pointerp").orElseThrow());
        assertEquals("jvmud_is_string", boundary.engineFunction("stringp").orElseThrow());
        assertEquals("jvmud_is_int", boundary.engineFunction("intp").orElseThrow());
        assertEquals(
                "\"JVMud RealmsMUD LDMud compatibility\"",
                boundary.compatibilityPredefines().get("__VERSION__"));
        assertEquals("3", boundary.compatibilityPredefines().get("__VERSION_MAJOR__"));
        assertEquals("6", boundary.compatibilityPredefines().get("__VERSION_MINOR__"));
        assertEquals("3", boundary.compatibilityPredefines().get("__VERSION_MICRO__"));
        assertEquals(
                "1",
                boundary.compatibilityFunctionPredefines().get("__EFUN_DEFINED__").get("set_heart_beat"));
        assertEquals("create", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED).orElseThrow());
        assertEquals("reset", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_ACTIVATED).orElseThrow());
        assertEquals(
                "prepare_destruct",
                boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_DESTRUCTION_REQUESTED).orElseThrow());
        assertEquals("logon", boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_CONNECTED).orElseThrow());
        assertEquals("addCommands", boundary.lifecycleMethod(MudlibLifecycleEvent.PLAYER_SESSION_POST_REBIND).orElseThrow());
        assertEquals("init", boundary.lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED).orElseThrow());
        assertEquals("log_error", boundary.lifecycleMethod(MudlibLifecycleEvent.LOG_ERROR).orElseThrow());
        assertEquals("runtime_error", boundary.lifecycleMethod(MudlibLifecycleEvent.RUNTIME_ERROR).orElseThrow());
        assertEquals("heart_beat", boundary.temporalTickMethod().orElseThrow());
        assertEquals(1, boundary.temporalTickIntervalSeconds());
    }

    private static void writeReport(MudlibBoundary boundary, Map<String, CompilationResult> results)
            throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());

        long supported = results.values().stream().filter(result -> result.getProblems().isEmpty()).count();
        long knownIssues = results.entrySet().stream().filter(RealmsMudCompatibilityScanTest::isKnownIssue).count();
        long unsupported = results.size() - supported - knownIssues;

        StringBuilder report = new StringBuilder();
        report.append("# JVMud RealmsMUD Compatibility Scan\n\n");
        report.append("This report is informational. It captures the current compiler/runtime gaps ");
        report.append("without making RealmsMUD readiness a build gate.\n\n");
        report.append("- Mudlib root: `").append(escape(MUDLIB_ROOT.toString())).append("`\n");
        report.append("- Configured source root: `")
                .append(escape(boundary.mudlibRootPath().map(Path::toString).orElse("")))
                .append("`\n");
        report.append("- Mudlib global object: `")
                .append(escape(boundary.mudlibGlobalObjectPath().orElse("")))
                .append("`\n");
        report.append("- JVMud compatibility global object: `")
                .append(escape(boundary.compatibilityGlobalObjectPath().orElse("")))
                .append("`\n");
        report.append("- Scanned files: ").append(results.size()).append("\n");
        report.append("- Supported now: ").append(supported).append("\n");
        report.append("- Ignored known RealmsMUD source issues: ").append(knownIssues).append("\n");
        report.append("- Current blockers: ").append(unsupported).append("\n\n");
        report.append("## Boot, Core, And Environment Sweep\n\n");
        report.append("| Source | Status | First Problem Stage | First Problem Line | First Problem |\n");
        report.append("| --- | --- | --- | ---: | --- |\n");

        for (Map.Entry<String, CompilationResult> entry : results.entrySet()) {
            List<CompilationProblem> problems = entry.getValue().getProblems();
            if (problems.isEmpty()) {
                report.append("| `").append(entry.getKey()).append("` | supported |  |  |  |\n");
                continue;
            }

            CompilationProblem first = problems.get(0);
            KnownIssue knownIssue = matchingKnownIssue(entry.getKey(), first);
            if (knownIssue != null) {
                report.append("| `")
                        .append(entry.getKey())
                        .append("` | known Realms source issue | ")
                        .append(escape(String.valueOf(first.getStage())))
                        .append(" | ")
                        .append(first.getLine() == null ? "" : first.getLine())
                        .append(" | ")
                        .append(escape(problemSummary(first)))
                        .append(" |\n");
                continue;
            }

            report.append("| `")
                    .append(entry.getKey())
                    .append("` | unsupported | ")
                    .append(escape(String.valueOf(first.getStage())))
                    .append(" | ")
                    .append(first.getLine() == null ? "" : first.getLine())
                    .append(" | ")
                    .append(escape(problemSummary(first)))
                    .append(" |\n");
        }

        report.append("\n## Why These Files\n\n");
        report.append("- `secure/master.c` is RealmsMUD's LDMud master object and driver-hook entry point.\n");
        report.append("- `secure/simul_efun.c` exposes the mudlib's global compatibility surface.\n");
        report.append("- `secure/login.c` is the player connection and account flow.\n");
        report.append("- `/lib/core` files cover the reusable event, state, selector, organization, ");
        report.append("prerequisite, message, and thing surfaces used by gameplay objects.\n");
        report.append("- The `/lib/environment/**/*.c` sweep covers environmental elements, generated ");
        report.append("rooms, regions, buildings, features, terrain, inventories, doors, interiors, ");
        report.append("and item definitions without hand-maintaining a giant source list.\n");
        report.append("- The `/lib/services/**/*.c`, `/lib/modules/secure/**/*.c`, ");
        report.append("`/secure/master/**/*.c`, and `/secure/simulated-efuns/**/*.c` sweeps cover ");
        report.append("RealmsMUD's service objects, security/data-service modules, master support ");
        report.append("objects, and simulated global efun surface.\n");
        appendKnownIssueNotes(report, results);

        Files.writeString(REPORT_PATH, report.toString());
    }

    private static void appendKnownIssueNotes(StringBuilder report, Map<String, CompilationResult> results) {
        report.append("\n## Ignored Known RealmsMUD Source Issues\n\n");
        report.append("These are deliberately excluded from the current blocker count so the radar can ");
        report.append("keep exposing new JVMud compatibility gaps. They are not treated as JVMud language ");
        report.append("features to implement.\n\n");
        report.append("| Source | Line | Stage | Status | Note |\n");
        report.append("| --- | ---: | --- | --- | --- |\n");
        for (KnownIssue issue : KNOWN_ISSUES) {
            CompilationResult result = results.get(issue.sourceName());
            String status = "not scanned";
            if (result != null && result.getProblems().isEmpty()) {
                status = "resolved or no longer first problem";
            } else if (result != null && matchingKnownIssue(issue.sourceName(), result.getProblems().get(0)) != null) {
                status = "ignored";
            } else if (result != null) {
                status = "different first problem";
            }
            report.append("| `")
                    .append(issue.sourceName())
                    .append("` | ")
                    .append(issue.line())
                    .append(" | ")
                    .append(issue.stage())
                    .append(" | ")
                    .append(status)
                    .append(" | ")
                    .append(escape(issue.note()))
                    .append(" |\n");
        }
        report.append("\nKnown issues only classify the current first compiler problem for a file. ");
        report.append("Later problems in that same file may appear after the known issue is fixed or ");
        report.append("the compiler gains broader diagnostic recovery.\n");
    }

    private static boolean isKnownIssue(Map.Entry<String, CompilationResult> entry) {
        List<CompilationProblem> problems = entry.getValue().getProblems();
        return !problems.isEmpty() && matchingKnownIssue(entry.getKey(), problems.get(0)) != null;
    }

    private static KnownIssue matchingKnownIssue(String sourceName, CompilationProblem problem) {
        return KNOWN_ISSUES.stream()
                .filter(issue -> issue.matches(sourceName, problem))
                .findFirst()
                .orElse(null);
    }

    private static String problemSummary(CompilationProblem problem) {
        String message = problem.getMessage();
        Throwable throwable = problem.getThrowable();
        if (throwable != null && throwable.getMessage() != null && !throwable.getMessage().isBlank()) {
            message = message + " - " + throwable.getMessage();
        }
        return firstLine(message);
    }

    private static String firstLine(String value) {
        if (value == null) {
            return "";
        }
        return value.split("\\R", 2)[0];
    }

    private static String stripExtension(String sourceName) {
        int dot = sourceName.lastIndexOf('.');
        return dot == -1 ? sourceName : sourceName.substring(0, dot);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "\\|").replace("\n", " ").replace("\r", " ");
    }

    /**
     * Documents one RealmsMUD source-level mismatch that should stay visible in the radar while
     * being excluded from the current JVMud blocker count.
     */
    private record KnownIssue(
            String sourceName, CompilationStage stage, int line, String messageFragment, String note) {
        private boolean matches(String candidateSourceName, CompilationProblem problem) {
            if (!sourceName.equals(candidateSourceName)) {
                return false;
            }
            if (stage != problem.getStage()) {
                return false;
            }
            if (problem.getLine() == null || problem.getLine() != line) {
                return false;
            }
            return problemSummary(problem).contains(messageFragment);
        }
    }
}
