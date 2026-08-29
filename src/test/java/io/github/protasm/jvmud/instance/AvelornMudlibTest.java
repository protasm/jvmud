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
    private static final Pattern BRINDLEFORD_DESTINATION =
            Pattern.compile("\\\"(place/brindleford/[a-z_]+)\\\"");

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

    @Test
    void exposesAConnectedBrindlefordWithNoDanglingPlaceLinks() throws Exception {
        Path sourceRoot = Path.of("mudlibs/avelorn/source").toAbsolutePath().normalize();
        Path places = sourceRoot.resolve("place/brindleford");
        Map<String, Set<String>> links = new HashMap<>();

        try (var paths = Files.list(places)) {
            for (Path path : paths.filter(candidate -> candidate.toString().endsWith(".c")).toList()) {
                String place = "place/brindleford/"
                        + path.getFileName().toString().replaceFirst("\\.c$", "");
                Matcher matcher = BRINDLEFORD_DESTINATION.matcher(Files.readString(path));
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

        assertEquals(12, links.size());
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
