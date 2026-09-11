package io.github.protasm.jvmud.instance;

import static org.junit.jupiter.api.Assertions.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Exercises the bundled toy through real session input and world time. */
final class SmallMerciesTest {
    /** Captures one independently connected adventurer's output. */
    private record Guest(InstancePersona persona, StringWriter text, PrintWriter writer) {}

    /** Attaches and completes the mudlib-owned guest creation flow. */
    private Guest login(MudInstance mud, String name, String gender, String profession) {
        StringWriter text = new StringWriter();
        PrintWriter writer = new PrintWriter(text, true);
        InstancePersona persona = mud.attachPersona(writer, "127.0.0.1");
        Guest guest = new Guest(persona, text, writer);
        command(mud, guest, name);
        command(mud, guest, gender);
        assertTrue(command(mud, guest, profession).contains("Village Square"));
        return guest;
    }

    /** Sends input through the same dispatcher as a Telnet session. */
    private String command(MudInstance mud, Guest guest, String line) {
        guest.text().getBuffer().setLength(0);
        mud.dispatch(guest.persona(), guest.writer(), line);
        return guest.text().toString();
    }

    @Test
    void exploreCommunicateAndSparWithoutLeakingAcrossRooms() {
        MudInstance mud = MudInstance.boot(Path.of("mudlibs/smallmercies"), "jvmud/smallmercies.config");
        try {
            Guest alice = login(mud, "Alice", "female", "warrior");
            Guest bob = login(mud, "Bob", "male", "mage");
            assertTrue(command(mud, alice, "score").contains("STR 12  INT 6  DEX 9"));
            assertTrue(command(mud, bob, "score").contains("STR 6  INT 12  DEX 9"));
            bob.text().getBuffer().setLength(0);
            assertTrue(command(mud, alice, "say Hello village").contains("Alice says: Hello village"));
            assertTrue(bob.text().toString().contains("Alice says: Hello village"));
            assertTrue(command(mud, alice, "talk mayor").contains("manageable expectations"));
            assertTrue(command(mud, alice, "attack mayor").contains("friendly"));
            assertTrue(command(mud, alice, "attack bob").contains("sparring NPC"));
            assertTrue(command(mud, alice, "n").contains("Municipal Herb Garden"));
            bob.text().getBuffer().setLength(0);
            command(mud, alice, "say secret parsley");
            assertFalse(bob.text().toString().contains("secret parsley"));
            assertTrue(command(mud, alice, "attack goose").contains("begin sparring"));
            for (int i = 0; i < 6; i++) mud.advanceWorldTick();
            assertTrue(alice.text().toString().contains("Victory!"), alice.text().toString());
            assertTrue(command(mud, alice, "score").contains("Victories 1"));
            assertTrue(command(mud, alice, "attack goose").contains("resting"));
            for (int i = 0; i < 20; i++) mud.advanceWorldTick();
            assertTrue(command(mud, alice, "attack goose").contains("begin sparring"));
            command(mud, alice, "south");
            alice.text().getBuffer().setLength(0);
            for (int i = 0; i < 4; i++) mud.advanceWorldTick();
            assertFalse(alice.text().toString().contains("damage"));
            assertTrue(command(mud, alice, "west").contains("The Resting Hero"));
            assertTrue(command(mud, alice, "rest").contains("full health"));
            command(mud, alice, "e");
            assertTrue(command(mud, alice, "go east").contains("Very Short Lane"));
            assertTrue(command(mud, alice, "d").contains("Cellar of Mild Peril"));
            assertTrue(command(mud, alice, "talk rat").contains("cheese tax"));
            assertTrue(command(mud, alice, "up").contains("Very Short Lane"));
            assertTrue(command(mud, alice, "w").contains("Village Square"));
        } finally { mud.shutdown(null); }
    }

    @Test
    void validatesCreationAndKeepsOneChallengerPerOpponent() {
        MudInstance mud = MudInstance.boot(Path.of("mudlibs/smallmercies"), "jvmud/smallmercies.config");
        try {
            Guest alice = login(mud, "Alice", "female", "mage");
            StringWriter text = new StringWriter();
            PrintWriter writer = new PrintWriter(text, true);
            Guest bob = new Guest(mud.attachPersona(writer, "127.0.0.1"), text, writer);
            assertTrue(command(mud, bob, "A").contains("2-16 letters"));
            assertTrue(command(mud, bob, "a1").contains("Letters only"));
            assertTrue(command(mud, bob, "ALICE").contains("visiting already"));
            command(mud, bob, "Bob");
            assertTrue(command(mud, bob, "invalid").contains("male or female"));
            command(mud, bob, "male");
            assertTrue(command(mud, bob, "invalid").contains("warrior or mage"));
            command(mud, bob, "warrior");
            for (String emote : new String[] {"smile", "wave", "bow", "laugh"}) {
                bob.text().getBuffer().setLength(0);
                assertTrue(command(mud, alice, emote).contains("Alice"));
                assertTrue(bob.text().toString().contains("Alice"));
            }
            assertTrue(command(mud, alice, "who").contains("Bob"));
            assertTrue(command(mud, alice, "down").contains("cannot go"));
            assertTrue(command(mud, alice, "attack missing").contains("sparring NPC"));
            command(mud, alice, "n"); command(mud, bob, "n");
            command(mud, alice, "attack goose");
            assertTrue(command(mud, bob, "attack goose").contains("already sparring"));
            mud.advanceWorldTick(); mud.advanceWorldTick();
            assertTrue(alice.text().toString().contains("cast a spark"));
            command(mud, alice, "stop");
            assertTrue(command(mud, bob, "attack goose").contains("begin sparring"));
            mud.detachPersona(bob.persona());
            assertTrue(command(mud, alice, "attack goose").contains("begin sparring"));
            command(mud, alice, "quit");
            assertFalse(mud.isAttached(alice.persona()));
        } finally { mud.shutdown(null); }
    }

    @Test
    void defeatReturnsToInnAndRestRestoresHealth() throws Exception {
        MudInstance mud = MudInstance.boot(Path.of("mudlibs/smallmercies"), "jvmud/smallmercies.config");
        try {
            Guest alice = login(mud, "Alice", "female", "mage");
            // Set a deterministic nearly-defeated fixture without relying on random dodge outcomes.
            setAttribute(alice, "health", 1);
            setAttribute(alice, "dexterity", 0);
            command(mud, alice, "n"); command(mud, alice, "attack goose");
            mud.advanceWorldTick(); mud.advanceWorldTick();
            assertTrue(alice.text().toString().contains("wheelbarrow"), alice.text().toString());
            assertTrue(command(mud, alice, "look").contains("The Resting Hero"));
            assertTrue(command(mud, alice, "score").contains("HP 1/26"));
            command(mud, alice, "rest");
            assertTrue(command(mud, alice, "score").contains("HP 26/26"));
            alice.text().getBuffer().setLength(0);
            for (int i = 0; i < 6; i++) mud.advanceWorldTick();
            assertFalse(alice.text().toString().contains("damage"));
        } finally { mud.shutdown(null); }
    }

    /** Sets a generated LPC field solely to establish a deterministic combat fixture. */
    private void setAttribute(Guest guest, String name, int value) throws Exception {
        var field = guest.persona().actor().getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(guest.persona().actor(), value);
    }

}
