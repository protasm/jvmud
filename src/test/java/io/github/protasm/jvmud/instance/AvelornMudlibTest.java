package io.github.protasm.jvmud.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Native boundary and first character-flow coverage for the Avelorn exemplar mudlib. */
class AvelornMudlibTest {
    private static final Pattern PLACE_DESTINATION =
            Pattern.compile("\\\"(place/[a-z_/]+)\\\"");

    @TempDir
    Path tempDir;

    @Test
    void createsPersistsAndRestoresANonBinaryRanger() throws Exception {
        Path mudlib = copyAvelornFixture();
        MudInstance mud = MudInstance.boot(mudlib, "jvmud/avelorn.config");
        StringWriter firstOutput = new StringWriter();
        PrintWriter firstWriter = new PrintWriter(firstOutput, true);
        InstancePersona first = mud.attachPersona(firstWriter, "127.0.0.1");

        assertEquals("avelorn", mud.gameId());
        assertEquals("place/brindleford/village_green", mud.startingPlacePath());
        assertTrue(firstOutput.toString().contains("Account ID:"), firstOutput.toString());

        dispatch(mud, first, firstWriter, "lantern_test");
        dispatch(mud, first, firstWriter, "yes");
        dispatch(mud, first, firstWriter, "Avelorn1!");
        dispatch(mud, first, firstWriter, "Avelorn1!");
        dispatch(mud, first, firstWriter, "Arden Vale");
        dispatch(mud, first, firstWriter, "non-binary");
        dispatch(mud, first, firstWriter, "ranger");
        dispatch(mud, first, firstWriter, "pronouns");
        dispatch(mud, first, firstWriter, "score");
        dispatch(mud, first, firstWriter, "look oren");
        dispatch(mud, first, firstWriter, "look elara");
        dispatch(mud, first, firstWriter, "look rowan");
        dispatch(mud, first, firstWriter, "inventory");
        dispatch(mud, first, firstWriter, "equipment");
        dispatch(mud, first, firstWriter, "north");
        dispatch(mud, first, firstWriter, "train");
        dispatch(mud, first, firstWriter, "improve dexterity");
        dispatch(mud, first, firstWriter, "score");
        dispatch(mud, first, firstWriter, "south");
        dispatch(mud, first, firstWriter, "east");
        dispatch(mud, first, firstWriter, "list");
        dispatch(mud, first, firstWriter, "buy bread");
        dispatch(mud, first, firstWriter, "inventory");
        dispatch(mud, first, firstWriter, "sell bread");
        dispatch(mud, first, firstWriter, "money");
        dispatch(mud, first, firstWriter, "west");
        dispatch(mud, first, firstWriter, "save");

        String created = firstOutput.toString();
        assertTrue(created.contains("Welcome to Avelorn, Arden vale."), created);
        assertTrue(created.contains("uses they/them pronouns"), created);
        assertTrue(created.contains("They are a sworn novice, and they have begun their service"), created);
        assertTrue(created.contains("level 1 ranger"), created);
        assertTrue(created.contains("He is plainly dressed"), created);
        assertTrue(created.contains("She is plainly dressed"), created);
        assertTrue(created.contains("They are plainly dressed"), created);
        assertTrue(created.contains("Brindleford Lantern House"), created);
        assertTrue(created.contains("ashwood shortbow (equipped)"), created);
        assertTrue(created.contains("body: blue wool travel cloak (equipped)"), created);
        assertTrue(created.contains("You advance to level 2!"), created);
        assertTrue(created.contains("Your dexterity improves to 15."), created);
        assertTrue(created.contains("Brindleford Market"), created);
        assertTrue(created.contains("You buy loaf of field bread for 1 silver, 2 copper."), created);
        assertTrue(created.contains("You sell loaf of field bread for 6 copper."), created);
        assertTrue(created.contains("You carry 1 gold, 1 silver, 4 copper."), created);
        assertTrue(Files.isRegularFile(mudlib.resolve("accounts/lantern_test.o")));
        String snapshot = Files.readString(mudlib.resolve("accounts/lantern_test.o"));
        assertTrue(snapshot.contains("inventory_state"), snapshot);
        assertTrue(!snapshot.contains("Avelorn1!"), snapshot);

        mud.detachPersona(first);

        StringWriter secondOutput = new StringWriter();
        PrintWriter secondWriter = new PrintWriter(secondOutput, true);
        InstancePersona second = mud.attachPersona(secondWriter, "127.0.0.2");
        dispatch(mud, second, secondWriter, "lantern_test");
        dispatch(mud, second, secondWriter, "Avelorn1!");
        dispatch(mud, second, secondWriter, "pronouns");
        dispatch(mud, second, secondWriter, "inventory");
        dispatch(mud, second, secondWriter, "equipment");
        dispatch(mud, second, secondWriter, "score");
        dispatch(mud, second, secondWriter, "money");

        String restored = secondOutput.toString();
        assertTrue(restored.contains("Welcome back, Arden vale."), restored);
        assertTrue(restored.contains("uses they/them pronouns"), restored);
        assertTrue(restored.contains("ashwood shortbow (equipped)"), restored);
        assertTrue(restored.contains("level 2 ranger"), restored);
        assertTrue(restored.contains("Dexterity 15"), restored);
        assertTrue(restored.contains("1 gold, 1 silver, 4 copper"), restored);
    }

    @ParameterizedTest
    @CsvSource({
        "male, he, him, is, has, his",
        "female, she, her, is, has, her",
        "non-binary, they, them, are, have, their"
    })
    void rendersTheThreeGenderGrammars(
            String gender,
            String subject,
            String object,
            String be,
            String have,
            String possessive) throws Exception {
        Path mudlib = copyAvelornFixture();
        MudInstance mud = MudInstance.boot(mudlib, "jvmud/avelorn.config");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output, true);
        InstancePersona persona = mud.attachPersona(writer, "127.0.0.1");

        dispatch(mud, persona, writer, "grammar_" + gender.replace("-", "_"));
        dispatch(mud, persona, writer, "yes");
        dispatch(mud, persona, writer, "Avelorn1!");
        dispatch(mud, persona, writer, "Avelorn1!");
        dispatch(mud, persona, writer, "Morgan Vale");
        dispatch(mud, persona, writer, gender);
        dispatch(mud, persona, writer, "cleric");
        dispatch(mud, persona, writer, "pronouns");

        String transcript = output.toString();
        assertTrue(transcript.contains("uses " + subject + "/" + object + " pronouns"), transcript);
        assertTrue(transcript.contains(
                capitalize(subject) + " " + be + " a sworn novice, and " + subject + " " + have
                        + " begun " + possessive + " service"), transcript);
    }

    @ParameterizedTest
    @CsvSource({
        "fighter, Crown arming sword, Strength 14, Stamina",
        "ranger, ashwood shortbow, Dexterity 14, Stamina",
        "mage, oak focus staff, Intelligence 14, Mana",
        "cleric, temple mace, Wisdom 14, Faith"
    })
    void givesEachClassItsOwnLevelOneProgressionAndStarterKit(
            String characterClass,
            String weapon,
            String primaryAttribute,
            String resourceName) throws Exception {
        Path mudlib = copyAvelornFixture();
        MudInstance mud = MudInstance.boot(mudlib, "jvmud/avelorn.config");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output, true);
        InstancePersona persona = mud.attachPersona(writer, "127.0.0.1");

        dispatch(mud, persona, writer, "class_" + characterClass);
        dispatch(mud, persona, writer, "yes");
        dispatch(mud, persona, writer, "Avelorn1!");
        dispatch(mud, persona, writer, "Avelorn1!");
        dispatch(mud, persona, writer, "Celyn Ward");
        dispatch(mud, persona, writer, "female");
        dispatch(mud, persona, writer, characterClass);
        dispatch(mud, persona, writer, "inventory");
        dispatch(mud, persona, writer, "score");
        if ("fighter".equals(characterClass)) {
            dispatch(mud, persona, writer, "look sword");
        }

        String transcript = output.toString();
        assertTrue(transcript.contains("level 1 " + characterClass), transcript);
        assertTrue(transcript.contains(weapon + " (equipped)"), transcript);
        assertTrue(transcript.contains(primaryAttribute), transcript);
        assertTrue(transcript.contains(resourceName + " "), transcript);
        if ("fighter".equals(characterClass)) {
            assertTrue(transcript.contains("Recommended level: 3."), transcript);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "fighter, disciplined mighty blow, Stamina 41/49",
        "ranger, carefully aimed shot, Stamina 46/54",
        "mage, bolt of blue-white arcane fire, Mana 56/66",
        "cleric, radiant Lantern smite, Faith 56/66"
    })
    void givesEveryClassAResourcePoweredCombatTechnique(
            String characterClass,
            String technique,
            String remainingResource) throws Exception {
        Path mudlib = copyAvelornFixture();
        MudInstance mud = MudInstance.boot(mudlib, "jvmud/avelorn.config");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output, true);
        InstancePersona persona = mud.attachPersona(writer, "127.0.0.1");

        createCharacter(
                mud,
                persona,
                writer,
                "ability_" + characterClass,
                "Celyn Ward",
                "female",
                characterClass);
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "down");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "ability rat");
        dispatch(mud, persona, writer, "score");

        String transcript = output.toString();
        assertTrue(transcript.contains("You unleash a " + technique + "."), transcript);
        assertTrue(transcript.contains(remainingResource), transcript);
    }

    @Test
    void exposesOneConnectedAvelornWorldWithNoDanglingPlaceLinks() throws Exception {
        Path sourceRoot = Path.of("mudlibs/avelorn/source").toAbsolutePath().normalize();
        Path places = sourceRoot.resolve("place");
        Map<String, Set<String>> links = new HashMap<>();

        try (var paths = Files.walk(places)) {
            for (Path path : paths
                    .filter(Files::isRegularFile)
                    .filter(candidate -> candidate.toString().endsWith(".c"))
                    .toList()) {
                String place = sourceRoot.relativize(path).toString()
                        .replace('\\', '/')
                        .replaceFirst("\\.c$", "");
                Matcher matcher = PLACE_DESTINATION.matcher(Files.readString(path));
                Set<String> destinations = new HashSet<>();
                while (matcher.find()) {
                    String destination = matcher.group(1);
                    assertTrue(
                            Files.isRegularFile(sourceRoot.resolve(destination + ".c")),
                            () -> place + " links to missing " + destination);
                    if (!destination.equals(place)) {
                        destinations.add(destination);
                    }
                }
                links.put(place, destinations);
            }
        }

        assertEquals(65, links.size());
        Set<String> reached = new HashSet<>();
        ArrayDeque<String> frontier = new ArrayDeque<>();
        frontier.add("place/brindleford/village_green");
        while (!frontier.isEmpty()) {
            String place = frontier.removeFirst();
            if (reached.add(place)) {
                frontier.addAll(links.getOrDefault(place, Set.of()));
            }
        }
        assertEquals(links.keySet(), reached);
    }

    @Test
    void sharesCombatCreditBetweenCompanionsAndSoftGatesTheEncounter() throws Exception {
        Path mudlib = copyAvelornFixture();
        MudInstance mud = MudInstance.boot(mudlib, "jvmud/avelorn.config");
        StringWriter firstOutput = new StringWriter();
        StringWriter secondOutput = new StringWriter();
        PrintWriter firstWriter = new PrintWriter(firstOutput, true);
        PrintWriter secondWriter = new PrintWriter(secondOutput, true);
        InstancePersona first = mud.attachPersona(firstWriter, "127.0.0.1");
        InstancePersona second = mud.attachPersona(secondWriter, "127.0.0.2");

        createCharacter(mud, first, firstWriter, "combat_one", "Alden Pike", "male", "fighter");
        createCharacter(mud, second, secondWriter, "combat_two", "Bryn Mere", "non-binary", "cleric");
        for (InstancePersona persona : new InstancePersona[] {first, second}) {
            PrintWriter writer = persona == first ? firstWriter : secondWriter;
            dispatch(mud, persona, writer, "east");
            dispatch(mud, persona, writer, "east");
            dispatch(mud, persona, writer, "north");
            dispatch(mud, persona, writer, "down");
            dispatch(mud, persona, writer, "east");
        }
        dispatch(mud, first, firstWriter, "look rat");
        dispatch(mud, first, firstWriter, "consider rat");
        dispatch(mud, first, firstWriter, "attack rat");
        dispatch(mud, second, secondWriter, "attack rat");
        dispatch(mud, first, firstWriter, "attack rat");
        dispatch(mud, second, secondWriter, "attack rat");
        for (int attempt = 0; attempt < 4
                && !firstOutput.toString().contains("scarred granary rat is defeated."); attempt++) {
            dispatch(mud, first, firstWriter, "attack rat");
        }
        dispatch(mud, first, firstWriter, "score");
        dispatch(mud, second, secondWriter, "score");

        String firstTranscript = firstOutput.toString();
        String secondTranscript = secondOutput.toString();
        assertTrue(firstTranscript.contains("Mill Grain Cellar"), firstTranscript);
        assertTrue(firstTranscript.contains("She is a level 1 combatant"), firstTranscript);
        assertTrue(firstTranscript.contains("appropriate challenge"), firstTranscript);
        assertTrue(firstTranscript.contains("You help defeat scarred granary rat."), firstTranscript);
        assertTrue(secondTranscript.contains("You help defeat scarred granary rat."), secondTranscript);
        assertTrue(firstTranscript.contains("Experience 35/100"), firstTranscript);
        assertTrue(secondTranscript.contains("Experience 35/100"), secondTranscript);
    }

    @Test
    void completesPersistsAndDoesNotDuplicateTheFirstQuestReward() throws Exception {
        Path mudlib = copyAvelornFixture();
        MudInstance mud = MudInstance.boot(mudlib, "jvmud/avelorn.config");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output, true);
        InstancePersona persona = mud.attachPersona(writer, "127.0.0.1");

        createCharacter(mud, persona, writer, "quest_one", "Elian Reed", "female", "fighter");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "look enid");
        dispatch(mud, persona, writer, "work");
        dispatch(mud, persona, writer, "quests");
        dispatch(mud, persona, writer, "down");
        dispatch(mud, persona, writer, "east");

        defeatRat(mud, persona, writer);
        for (int tick = 0; tick < 19; tick++) {
            mud.advanceWorldTick();
        }
        dispatch(mud, persona, writer, "attack rat");
        mud.advanceWorldTick();
        defeatRat(mud, persona, writer);
        for (int tick = 0; tick < 20; tick++) {
            mud.advanceWorldTick();
        }
        defeatRat(mud, persona, writer);

        dispatch(mud, persona, writer, "west");
        dispatch(mud, persona, writer, "up");
        dispatch(mud, persona, writer, "report");
        dispatch(mud, persona, writer, "report");
        dispatch(mud, persona, writer, "quests");
        dispatch(mud, persona, writer, "score");
        dispatch(mud, persona, writer, "save");

        String transcript = output.toString();
        assertTrue(transcript.contains("Miller Enid Halward is the holder"), transcript);
        assertTrue(transcript.contains("Miller's Unwelcome Guests (recommended level 1)"), transcript);
        assertTrue(transcript.contains("Miller's Unwelcome Guests: 3/3."), transcript);
        assertTrue(transcript.contains("The assignment is ready to report."), transcript);
        assertEquals(1, occurrences(transcript, "You complete Miller's Unwelcome Guests."));
        assertTrue(transcript.contains("already been reported and rewarded"), transcript);
        assertTrue(transcript.contains("Miller's Unwelcome Guests — complete"), transcript);
        assertTrue(transcript.contains("level 2 fighter"), transcript);
        assertTrue(transcript.contains("Experience 80/200"), transcript);
        assertTrue(transcript.contains("Coin: 1 gold, 8 silver, 4 copper"), transcript);
        assertTrue(Files.readString(mudlib.resolve("accounts/quest_one.o"))
                .contains("quest_state"));

        mud.detachPersona(persona);
        StringWriter restoredOutput = new StringWriter();
        PrintWriter restoredWriter = new PrintWriter(restoredOutput, true);
        InstancePersona restored = mud.attachPersona(restoredWriter, "127.0.0.2");
        dispatch(mud, restored, restoredWriter, "quest_one");
        dispatch(mud, restored, restoredWriter, "Avelorn1!");
        dispatch(mud, restored, restoredWriter, "quests");
        String restoredTranscript = restoredOutput.toString();
        assertTrue(restoredTranscript.contains("Miller's Unwelcome Guests — complete"), restoredTranscript);
    }

    @Test
    void completesTheThreeDistinctLanternObjectivesOnTheRoadToGreyhaven() throws Exception {
        Path mudlib = copyAvelornFixture();
        MudInstance mud = MudInstance.boot(mudlib, "jvmud/avelorn.config");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output, true);
        InstancePersona persona = mud.attachPersona(writer, "127.0.0.1");

        createCharacter(mud, persona, writer, "lantern_road", "Mara Fen", "non-binary", "mage");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "train");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "work");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "tend");
        dispatch(mud, persona, writer, "tend");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "tend");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "tend");
        dispatch(mud, persona, writer, "quests");
        for (int westward = 0; westward < 11; westward++) {
            dispatch(mud, persona, writer, "west");
        }
        dispatch(mud, persona, writer, "report");
        dispatch(mud, persona, writer, "report");
        dispatch(mud, persona, writer, "quests");
        dispatch(mud, persona, writer, "score");

        String transcript = output.toString();
        assertTrue(transcript.contains("Light for the Road (recommended level 2)"), transcript);
        assertTrue(transcript.contains("Old Brindle Bridge"), transcript);
        assertTrue(transcript.contains("Crown Road Shelter"), transcript);
        assertTrue(transcript.contains("Greyhaven Western Approach"), transcript);
        assertTrue(transcript.contains("You have already recorded this objective."), transcript);
        assertTrue(transcript.contains("Light for the Road: 3/3."), transcript);
        assertEquals(1, occurrences(transcript, "You complete Light for the Road."));
        assertTrue(transcript.contains("Light for the Road — complete"), transcript);
        assertTrue(transcript.contains("Experience 150/200"), transcript);
        assertTrue(transcript.contains("Coin: 1 gold, 9 silver, 5 copper"), transcript);
    }

    @Test
    void softGatesAndCompletesTheSilentPatrolBellAssignment() throws Exception {
        Path mudlib = copyAvelornFixture();
        MudInstance mud = MudInstance.boot(mudlib, "jvmud/avelorn.config");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output, true);
        InstancePersona persona = mud.attachPersona(writer, "127.0.0.1");

        createCharacter(mud, persona, writer, "patrol_bell", "Tarin Holt", "male", "fighter");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "train");
        dispatch(mud, persona, writer, "south");
        for (int eastward = 0; eastward < 13; eastward++) {
            dispatch(mud, persona, writer, "east");
        }
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "look ilyra");
        dispatch(mud, persona, writer, "work");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "look wraith");
        dispatch(mud, persona, writer, "consider wraith");
        for (int strike = 0; strike < 5; strike++) {
            dispatch(mud, persona, writer, "ability wraith");
        }
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "report");
        dispatch(mud, persona, writer, "report");
        dispatch(mud, persona, writer, "quests");
        dispatch(mud, persona, writer, "score");

        String transcript = output.toString();
        assertTrue(transcript.contains("Watch-Captain Ilyra Venn is the captain"), transcript);
        assertTrue(transcript.contains("They have posted a measured request"), transcript);
        assertTrue(transcript.contains("The Silent Patrol Bell (recommended level 4)"), transcript);
        assertTrue(transcript.contains("beyond your current training"), transcript);
        assertTrue(transcript.contains("They are a level 4 combatant"), transcript);
        assertTrue(transcript.contains("dangerous challenge"), transcript);
        assertTrue(transcript.contains("hollow bell wraith is defeated"), transcript);
        assertTrue(transcript.contains("The Silent Patrol Bell: 1/1."), transcript);
        assertEquals(1, occurrences(transcript, "You complete The Silent Patrol Bell."));
        assertTrue(transcript.contains("The Silent Patrol Bell — complete"), transcript);
        assertTrue(transcript.contains("level 3 fighter"), transcript);
        assertTrue(transcript.contains("Experience 240/300"), transcript);
        assertTrue(transcript.contains("Coin: 3 gold"), transcript);
    }

    @Test
    void completesBlackstoneAndTheAshenwatchCapstone() throws Exception {
        Path mudlib = copyAvelornFixture();
        MudInstance mud = MudInstance.boot(mudlib, "jvmud/avelorn.config");
        StringWriter output = new StringWriter();
        PrintWriter writer = new PrintWriter(output, true);
        InstancePersona persona = mud.attachPersona(writer, "127.0.0.1");

        createCharacter(mud, persona, writer, "blackstone", "Alden Pike", "male", "fighter");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "train");
        dispatch(mud, persona, writer, "south");
        for (int eastward = 0; eastward < 13; eastward++) {
            dispatch(mud, persona, writer, "east");
        }
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "look maelin");
        dispatch(mud, persona, writer, "work");
        dispatch(mud, persona, writer, "west");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "down");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "consider guardian");
        useTechniquesThenAttack(mud, persona, writer, "guardian", 6, 2);
        dispatch(mud, persona, writer, "west");
        dispatch(mud, persona, writer, "up");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "west");
        dispatch(mud, persona, writer, "rest");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "down");
        dispatch(mud, persona, writer, "west");
        useTechniquesThenAttack(mud, persona, writer, "guardian", 6, 2);
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "restore");
        dispatch(mud, persona, writer, "restore");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "up");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "report");
        dispatch(mud, persona, writer, "report");
        dispatch(mud, persona, writer, "quests");
        dispatch(mud, persona, writer, "score");

        String transcript = output.toString();
        assertTrue(transcript.contains("Royal Surveyor Maelin Dorr is the surveyor"), transcript);
        assertTrue(transcript.contains("Beneath Blackstone (recommended level 5)"), transcript);
        assertTrue(transcript.contains("beyond your current training"), transcript);
        assertTrue(transcript.contains("far beyond your present training"), transcript);
        assertTrue(transcript.contains("drowned stone guardian is defeated"), transcript);
        assertTrue(transcript.contains("ashbound iron guardian is defeated"), transcript);
        assertTrue(transcript.contains("Beneath Blackstone: 3/3."), transcript);
        assertTrue(transcript.contains("You have already recorded this objective."), transcript);
        assertEquals(1, occurrences(transcript, "You complete Beneath Blackstone."));
        assertTrue(transcript.contains("Beneath Blackstone — complete"), transcript);
        assertTrue(transcript.contains("level 5 fighter"), transcript);
        assertTrue(transcript.contains("Experience 300/500"), transcript);
        assertTrue(transcript.contains("Coin: 5 gold, 1 silver"), transcript);

        dispatch(mud, persona, writer, "west");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "west");
        dispatch(mud, persona, writer, "west");
        dispatch(mud, persona, writer, "look serin");
        dispatch(mud, persona, writer, "work");
        dispatch(mud, persona, writer, "west");
        dispatch(mud, persona, writer, "west");
        dispatch(mud, persona, writer, "west");
        useTechniquesThenAttack(mud, persona, writer, "hound", 6, 1);
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "rest");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "west");
        useTechniquesThenAttack(mud, persona, writer, "knight", 6, 1);
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "align");
        dispatch(mud, persona, writer, "west");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "rest");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "down");
        dispatch(mud, persona, writer, "east");
        useTechniquesThenAttack(mud, persona, writer, "warden", 8, 1);
        dispatch(mud, persona, writer, "north");
        dispatch(mud, persona, writer, "rekindle");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "west");
        dispatch(mud, persona, writer, "up");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "south");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "east");
        dispatch(mud, persona, writer, "report");
        dispatch(mud, persona, writer, "report");
        dispatch(mud, persona, writer, "quests");
        dispatch(mud, persona, writer, "inventory");
        dispatch(mud, persona, writer, "look medal");
        dispatch(mud, persona, writer, "equip medal");
        dispatch(mud, persona, writer, "score");

        transcript = output.toString();
        assertTrue(transcript.contains("Marshal Serin Vale is the Crown marshal"), transcript);
        assertTrue(transcript.contains("Rekindle the Western Lantern (recommended level 7)"), transcript);
        assertTrue(transcript.contains("Lantern-ash hound is defeated"), transcript);
        assertTrue(transcript.contains("ember-bound tower knight is defeated"), transcript);
        assertTrue(transcript.contains("soot-crowned Lantern warden is defeated"), transcript);
        assertTrue(transcript.contains("Rekindle the Western Lantern: 5/5."), transcript);
        assertEquals(1, occurrences(transcript, "You complete Rekindle the Western Lantern."));
        assertTrue(transcript.contains("The Lantern Crown shines whole"), transcript);
        assertTrue(transcript.contains("Medal of the Western Lantern"), transcript);
        assertTrue(transcript.contains("Recommended level: 10."), transcript);
        assertTrue(transcript.contains("You equip Medal of the Western Lantern"), transcript);
        assertTrue(transcript.contains("charisma is below the recommended 14"), transcript);
        assertTrue(transcript.contains("Rekindle the Western Lantern — complete"), transcript);
        assertTrue(transcript.contains("level 9 fighter"), transcript);
        assertTrue(transcript.contains("Experience 200/900"), transcript);
        assertTrue(transcript.contains("Coin: 13 gold, 6 silver"), transcript);
    }

    private void dispatch(
            MudInstance mud,
            InstancePersona persona,
            PrintWriter writer,
            String command) {
        mud.dispatch(persona, writer, command);
    }

    private void createCharacter(
            MudInstance mud,
            InstancePersona persona,
            PrintWriter writer,
            String account,
            String name,
            String gender,
            String characterClass) {
        dispatch(mud, persona, writer, account);
        dispatch(mud, persona, writer, "yes");
        dispatch(mud, persona, writer, "Avelorn1!");
        dispatch(mud, persona, writer, "Avelorn1!");
        dispatch(mud, persona, writer, name);
        dispatch(mud, persona, writer, gender);
        dispatch(mud, persona, writer, characterClass);
    }

    private void defeatRat(
            MudInstance mud,
            InstancePersona persona,
            PrintWriter writer) {
        for (int attack = 0; attack < 6; attack++) {
            dispatch(mud, persona, writer, "attack rat");
        }
    }

    private void useTechniquesThenAttack(
            MudInstance mud,
            InstancePersona persona,
            PrintWriter writer,
            String target,
            int techniques,
            int attacks) {
        for (int strike = 0; strike < techniques; strike++) {
            dispatch(mud, persona, writer, "ability " + target);
        }
        for (int strike = 0; strike < attacks; strike++) {
            dispatch(mud, persona, writer, "attack " + target);
        }
    }

    private int occurrences(String value, String needle) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }

    private Path copyAvelornFixture() throws IOException {
        Path source = Path.of("mudlibs/avelorn").toAbsolutePath().normalize();
        Path target = tempDir.resolve("avelorn");
        try (var paths = Files.walk(source)) {
            paths.forEach(path -> copyPath(source, target, path));
        }
        return target;
    }

    private String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private void copyPath(Path source, Path target, Path path) {
        try {
            Path destination = target.resolve(source.relativize(path));
            if (Files.isDirectory(path)) {
                Files.createDirectories(destination);
            } else {
                Files.copy(path, destination);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not copy Avelorn fixture", e);
        }
    }
}
