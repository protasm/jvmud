package io.github.protasm.jvmud.transport.telnet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.compiler.efun.builtin.CoreEfuns;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.exec.LPCRuntimeConfig;
import io.github.protasm.jvmud.engine.world.Capability;
import io.github.protasm.jvmud.engine.world.Entity;
import io.github.protasm.jvmud.engine.world.Link;
import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;
import io.github.protasm.jvmud.engine.mudlib.MudlibLifecycleEvent;
import io.github.protasm.jvmud.engine.mudlib.MudlibProjectionRole;
import io.github.protasm.jvmud.engine.world.Place;
import io.github.protasm.jvmud.engine.world.World;
import io.github.protasm.jvmud.engine.world.WorldRuntime;
import io.github.protasm.jvmud.instance.LegacyPlayerObjectAdapter;
import io.github.protasm.jvmud.instance.LocalSessionActor;
import io.github.protasm.jvmud.instance.MudInstance;
import io.github.protasm.jvmud.instance.MudlibBoot;
import io.github.protasm.jvmud.instance.MudlibBootResult;
import io.github.protasm.jvmud.instance.InstancePersona;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class TelnetServerTest {
    @TempDir
    Path tempDir;

    @Test
    void telnetServerLaunchOptionsDefaultToLocalMudlibStart() {
        TelnetServer.LaunchOptions options = TelnetServer.parseLaunchOptions(new String[0]);

        assertEquals(repositoryRoot().resolve("mudlibs/lpmuseum"), options.mudlibRoot());
        assertEquals(4000, options.port());
        assertEquals("localhost", options.bindAddress());
        assertEquals(MudlibBoot.DEFAULT_CONFIG_PATH, options.configObjectPath());
        assertFalse(options.help());
    }

    @Test
    void telnetServerLaunchOptionsAcceptSingleConfigFileArgument() {
        TelnetServer.LaunchOptions options = TelnetServer.parseLaunchOptions(new String[] {
                "mudlibs/lp245/jvmud/lp245.config"
        });

        assertEquals(repositoryRoot().resolve("mudlibs/lp245"), options.mudlibRoot());
        assertEquals(4000, options.port());
        assertEquals("localhost", options.bindAddress());
        assertEquals("jvmud/lp245.config", options.configObjectPath());
    }

    @Test
    void telnetServerLaunchOptionsAcceptHelp() {
        TelnetServer.LaunchOptions options = TelnetServer.parseLaunchOptions(new String[] {"--help"});

        assertTrue(options.help());
    }

    @Test
    void telnetServerLaunchOptionsRejectBadFlags() {
        assertThrows(IllegalArgumentException.class, () ->
                TelnetServer.parseLaunchOptions(new String[] {"-port"}));
        assertThrows(IllegalArgumentException.class, () ->
                TelnetServer.parseLaunchOptions(new String[] {"mudlibs/lpmuseum/jvmud/lpmuseum.config", "extra"}));
        assertThrows(IllegalArgumentException.class, () ->
                TelnetServer.parseLaunchOptions(new String[] {"-bogus", "value"}));
    }

    @Test
    void lpmuseumIsDefaultStandaloneNativeMudlib() throws Exception {
        Path museum = lpmuseumTestRoot();

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, museum, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();
            assertEquals("preload manifest: none declared.", server.preloadSummary());

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                String initial = readUntilQuietAfterContains(socket, "Please enter your user ID: ");
                assertTrue(initial.contains("Please enter your user ID: "), initial);
                assertFalse(initial.contains("Attached player"), initial);

                String greeting = createLpmuseumAccountAndEnter(socket, uniqueAccountId("protasm"), "Valid1!",
                        "protasm", "neutral");
                assertTrue(greeting.contains("Hi, Protasm! Welcome to LPMuseum."), greeting);
                assertTrue(greeting.contains("Protasm enters LPMuseum through the museum doors."), greeting);
                assertTrue(greeting.contains("Grand Concourse of LPMuseum"), greeting);
                assertTrue(greeting.contains("A directory and a docent are here."), greeting);

                try (Socket second = new Socket("127.0.0.1", server.port())) {
                    second.setSoTimeout(5000);
                    assertFalse(readUntilQuietAfterContains(second, "Please enter your user ID: ")
                            .contains("Attached player"));
                    assertTrue(createLpmuseumAccountAndEnter(second, uniqueAccountId("solfeggio"), "Valid1!",
                            "solfeggio", "other").contains("Hi, Solfeggio! Welcome to LPMuseum."));
                    assertTrue(readUntilQuietAfterContains(socket, "Solfeggio enters LPMuseum through the museum doors.")
                            .contains("Solfeggio enters LPMuseum through the museum doors."));

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String concourse = readUntilQuietAfterContains(socket, "directory and a docent");
                assertTrue(concourse.contains("Grand Concourse of LPMuseum"), concourse);
                assertTrue(concourse.contains("mudlib is required.\n\nNorth leads"), concourse);
                assertTrue(concourse.contains("the Archive.\n\nMuseum Security Staffer"), concourse);
                assertTrue(concourse.contains("Solfeggio is here.\n\nA directory and a docent are here."), concourse);
                assertTrue(concourse.contains("Museum Security Staffer"), concourse);
                assertFalse(concourse.contains("soft blue jacket"), concourse);
                assertFalse(concourse.contains("gentle patrol is driven"), concourse);
                assertTrue(concourse.contains("Solfeggio is here."), concourse);

                socket.getOutputStream().write("exa solfeggio\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Solfeggio is a visiting Persona exploring LPMuseum.")
                        .contains("Solfeggio is a visiting Persona exploring LPMuseum."));

                socket.getOutputStream().write("look staffer\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String staffer = readUntilQuietAfterContains(socket, "soft blue jacket");
                assertTrue(staffer.contains("gentle patrol is driven by LPMuseum's timed heartbeat"), staffer);

                socket.getOutputStream().write("north\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String originsWithStaffer = readUntilQuietAfterContains(socket, "Origins Gallery");
                assertFalse(originsWithStaffer.contains("Protasm leaves north."), originsWithStaffer);
                assertFalse(originsWithStaffer.contains("Protasm arrives."), originsWithStaffer);
                assertFalse(originsWithStaffer.contains("enters LPMuseum through the museum doors"), originsWithStaffer);
                assertTrue(readUntilQuietAfterContains(second, "Protasm leaves north.")
                        .contains("Protasm leaves north."));

                socket.getOutputStream().write("south\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String returnedToConcourse = readUntilQuietAfterContains(socket, "Grand Concourse of LPMuseum");
                assertTrue(returnedToConcourse.contains("Grand Concourse of LPMuseum"));
                assertFalse(returnedToConcourse.contains("Protasm leaves south."), returnedToConcourse);
                assertFalse(returnedToConcourse.contains("Protasm arrives."), returnedToConcourse);
                assertTrue(readUntilQuietAfterContains(second, "Protasm arrives.")
                        .contains("Protasm arrives."));

                socket.getOutputStream().write("examine directory\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "four native JVMud Places").contains("four native JVMud Places"));

                socket.getOutputStream().write("south\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "You can't go that way.").contains("You can't go that way."));

                socket.getOutputStream().write("say hello museum\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Protasm says: hello museum").contains("Protasm says: hello museum"));

                socket.getOutputStream().write("say to docent hello\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Protasm says to docent: hello").contains("Protasm says to docent: hello"));

                socket.getOutputStream().write("who\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String who = readUntilQuietAfterContains(socket, "Connected Personas in LPMuseum: 2");
                assertTrue(who.contains("Protasm"), who);
                assertTrue(who.contains("Solfeggio"), who);
                assertTrue(who.contains("persona/visitor#clone1"), who);
                assertTrue(who.contains("from 127.0.0.1"), who);

                socket.getOutputStream().write("smile\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Protasm smiles.").contains("Protasm smiles."));

                socket.getOutputStream().write("wave docent\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Protasm waves docent.").contains("Protasm waves docent."));

                socket.getOutputStream().write("go east\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Creator Workshop").contains("Creator Workshop"));

                socket.getOutputStream().write("examine machine\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String machine = readUntilQuietAfterContains(socket, "Try: vend entity");
                assertTrue(machine.contains("at most ten vended Entities"), machine);

                socket.getOutputStream().write("vend entity\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "vended curio #1 onto the floor")
                        .contains("vended curio #1"));

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String workshopWithCurio = readUntilQuietAfterContains(socket, "vended curio #1");
                assertTrue(workshopWithCurio.contains("Try demo time, demo users, demo inventory, demo dispatch, or demo signal.\n\nThe concourse is west."), workshopWithCurio);
                assertTrue(workshopWithCurio.contains("Entity Vending Machine\nvended curio #1"), workshopWithCurio);

                socket.getOutputStream().write("take curio\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "You take vended curio #1.")
                        .contains("You take vended curio #1."));

                socket.getOutputStream().write("inventory\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "vended curio #1")
                        .contains("You are carrying:"));

                socket.getOutputStream().write("examine curio\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "self-destruct two minutes")
                        .contains("JVMud identity"));

                socket.getOutputStream().write("drop curio\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "You drop vended curio #1.")
                        .contains("You drop vended curio #1."));

                for (int i = 2; i <= 10; i++) {
                    socket.getOutputStream().write("vend entity\n".getBytes(StandardCharsets.UTF_8));
                    socket.getOutputStream().flush();
                    assertTrue(readUntilQuietAfterContains(socket, "vended curio #" + i + " onto the floor")
                            .contains("vended curio #" + i));
                }

                socket.getOutputStream().write("vend entity\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "MAX 10 LIVE")
                        .contains("MAX 10 LIVE"));

                socket.getOutputStream().write("demo time\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "ctime(time())").contains("time() ->"));

                socket.getOutputStream().write("go west\nnorth\neast\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Portal Hall").contains("Portal Hall"));

                socket.getOutputStream().write("enter portal\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String portal = readUntilQuietAfterContains(socket, "No exhibit is mounted here yet.");
                assertTrue(portal.contains("The portal hums and points toward the Vanilla LPMUD 2.4.5 exhibit."),
                        portal);
                assertTrue(portal.contains("The portal is quiet."), portal);
                assertFalse(portal.contains("The exhibit portal opens."), portal);

                socket.getOutputStream().write("quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String quit = readUntilSocketClosed(socket);
                assertTrue(quit.contains("You step away from LPMuseum."), quit);
                assertFalse(quit.contains("You can't do that."), quit);
                }
            }
        }
    }

    @Test
    void lpmuseumPortalConnectsToLp245ExhibitWithMuseumUserIdAndNoPassword() throws Exception {
        Path museum = mountedLpmuseumTestRoot();

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, museum, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your user ID: ")
                        .contains("Please enter your user ID: "));
                String greeting = createLpmuseumAccountAndEnter(socket, "protasm", "Valid1!",
                        "Museum Persona", "female");
                assertTrue(greeting.contains("Hi, Museum persona! Welcome to LPMuseum."), greeting);

                socket.getOutputStream().write("go north\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Origins Gallery").contains("Origins Gallery"));

                socket.getOutputStream().write("go east\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Portal Hall").contains("Portal Hall"));

                socket.getOutputStream().write("enter portal\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String lp245Entry = readUntilQuietAfterContains(socket, "> ");
                assertTrue(lp245Entry.contains("The exhibit portal opens."), lp245Entry);
                assertTrue(lp245Entry.contains("What is your name: protasm"), lp245Entry);
                assertTrue(lp245Entry.contains("Version: "), lp245Entry);
                assertFalse(lp245Entry.contains("Password:"), lp245Entry);
                assertFalse(lp245Entry.contains("Please enter your email address"), lp245Entry);
                assertFalse(lp245Entry.contains("Are you, male, female or other"), lp245Entry);
                assertFalse(lp245Entry.contains("Attached player"), lp245Entry);

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String church = readUntilQuietAfterContains(socket, "You are in the local village church.");
                assertTrue(church.contains("You are in the local village church."), church);
                assertTrue(church.contains("> "), church);

                socket.getOutputStream().write("say this is cool\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String say = readUntilQuietAfterContains(socket, "Ok.");
                assertTrue(say.contains("Ok."), say);
                assertFalse(say.contains("You can't do that."), say);

                socket.getOutputStream().write("help\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String help = readUntilQuietAfterContains(socket, "brief");
                assertTrue(help.contains("brief"), help);
                assertFalse(help.contains("You can't do that."), help);

                socket.getOutputStream().write("exa portal\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String examine = readUntilQuietAfterContains(socket, "The portal leads back to LPMuseum.");
                assertTrue(examine.contains("The portal leads back to LPMuseum."), examine);
                assertFalse(examine.contains("You can't do that."), examine);

                try (Socket second = new Socket("127.0.0.1", server.port())) {
                    second.setSoTimeout(5000);
                    assertTrue(readUntilQuietAfterContains(second, "Please enter your user ID: ")
                            .contains("Please enter your user ID: "));
                    assertTrue(createLpmuseumAccountAndEnter(second, "solfeggio", "Valid1!",
                            "Solfeggio", "neutral").contains("Hi, Solfeggio! Welcome to LPMuseum."));

                    second.getOutputStream().write("go north\ngo east\nenter portal\n".getBytes(StandardCharsets.UTF_8));
                    second.getOutputStream().flush();
                    String secondLp245Entry = readUntilQuietAfterContains(second, "> ");
                    assertTrue(secondLp245Entry.contains("What is your name: solfeggio"), secondLp245Entry);

                    second.getOutputStream().write("exa protasm\n".getBytes(StandardCharsets.UTF_8));
                    second.getOutputStream().flush();
                    String playerExamine = readUntilQuietAfterContains(second, "Protasm the title less");
                    assertTrue(playerExamine.contains("Protasm the title less"), playerExamine);
                    assertFalse(playerExamine.contains("Error:"), playerExamine);
                    assertFalse(playerExamine.contains("You can't do that."), playerExamine);
                }

                socket.getOutputStream().write("enter portal\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String returned = readUntilQuietAfterContains(socket, "You step back into LPMuseum as Museum persona.");
                assertTrue(returned.contains("The museum return portal hums."), returned);
                assertTrue(returned.contains("The return portal opens."), returned);
                assertTrue(returned.contains("You step back into LPMuseum as Museum persona."), returned);
                assertFalse(returned.contains("Please enter your user ID:"), returned);

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String returnedLook = readUntilQuietAfterContains(socket, "Portal Hall");
                assertTrue(returnedLook.contains("A quiet exhibit portal waits here as an Entity."), returnedLook);

                socket.getOutputStream().write("exa me\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String self = readUntilQuietAfterContains(socket, "You are Museum persona");
                assertTrue(self.contains("You are Museum persona, a visiting Persona exploring LPMuseum."), self);
            }
        }
    }

    @Test
    void lp245TrollHuntKeepsHeartbeatAfterExaminingMonster() throws Exception {
        Path lp245 = lp245TestRoot();

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, lp245, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "What is your name: ")
                        .contains("What is your name: "));

                socket.getOutputStream().write("hbtest\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: ").contains("Password: "));

                socket.getOutputStream().write("secret1\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: (again) ")
                        .contains("Password: (again) "));

                socket.getOutputStream().write("secret1\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your email address")
                        .contains("Please enter your email address"));

                socket.getOutputStream().write("none\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Are you, male, female or other")
                        .contains("Are you, male, female or other"));

                socket.getOutputStream().write("o\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "> ").contains("Welcome, Creature!"));

                socket.getOutputStream().write("south\nwest\nwest\nwest\nwest\nwest\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String trollRoom = readUntilQuietAfterContains(socket, "A troll.");
                assertTrue(trollRoom.contains("You are in a big forest."), trollRoom);

                socket.getOutputStream().write("exa troll\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String troll = readUntilQuietAfterContains(socket, "Troll is carrying:");
                assertTrue(troll.contains("It is a nasty troll that look very aggressive."), troll);

                String firstAttack = readUntilQuietAfterContains(socket, "Troll ");
                assertFalse(firstAttack.contains("Your sensitive mind notices a wrongness"), firstAttack);
                assertFalse(firstAttack.contains("You have no heart beat"), firstAttack);

                socket.getOutputStream().write("east\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String fled = readUntilQuietAfterContains(socket, "You are now hunted by Troll.");
                assertTrue(fled.contains("A small clearing."), fled);
                assertFalse(fled.contains("Your sensitive mind notices a wrongness"), fled);
                assertFalse(fled.contains("You have no heart beat"), fled);

                socket.getOutputStream().write("west\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String returned = readUntilQuietAfterContains(socket, "A troll.");
                assertTrue(returned.contains("You are in a big forest."), returned);
                assertFalse(returned.contains("Your sensitive mind notices a wrongness"), returned);
                assertFalse(returned.contains("You have no heart beat"), returned);

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                readUntilSocketClosed(socket);
            }
        }
    }

    @Test
    void lp245WizardHallMissingSouthActionFallsThroughCleanly() throws Exception {
        Path lp245 = lp245TestRoot();
        Path player = lp245.resolve("source/obj/player.c");
        Files.writeString(player, Files.readString(player)
                .replace("move_object(myself, \"room/church\");", "move_object(myself, \"room/wiz_hall\");"));

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, lp245, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "What is your name: ")
                        .contains("What is your name: "));

                socket.getOutputStream().write("wizbug\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: ").contains("Password: "));

                socket.getOutputStream().write("secret1\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: (again) ")
                        .contains("Password: (again) "));

                socket.getOutputStream().write("secret1\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your email address")
                        .contains("Please enter your email address"));

                socket.getOutputStream().write("none\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Are you, male, female or other")
                        .contains("Are you, male, female or other"));

                socket.getOutputStream().write("o\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String greeting = readUntilQuietAfterContains(socket, "> ");
                assertTrue(greeting.contains("Welcome, Creature!"), greeting);
                assertTrue(greeting.contains("Leo the Archwizard."), greeting);

                socket.getOutputStream().write("south\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String south = readUntilQuietAfterContains(socket, "You can't do that.");
                assertFalse(south.contains("Your sensitive mind notices a wrongness"), south);
                assertTrue(south.contains("You can't do that."), south);

                socket.getOutputStream().write("north\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String north = readUntilQuietAfterContains(socket, "A strong magic force stops you.");
                assertFalse(north.contains("Your sensitive mind notices a wrongness"), north);

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                readUntilSocketClosed(socket);
            }
        }
    }

    @Test
    void lpmuseumRestoresAccountAndDisconnectsAfterThreeBadPasswords() throws Exception {
        Path museum = lpmuseumTestRoot();
        String accountId = uniqueAccountId("returning");

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, museum, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your user ID: ").contains("Please enter your user ID: "));
                assertTrue(createLpmuseumAccountAndEnter(socket, accountId, "Valid1!", "solfeggio", "female")
                        .contains("Hi, Solfeggio! Welcome to LPMuseum."));
                socket.getOutputStream().write("quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilSocketClosed(socket).contains("You step away from LPMuseum."));
            }

            Path savedAccount = museum.resolve("accounts").resolve(accountId + ".o");
            assertSavedPlayerJsonFile(savedAccount);
            String saved = Files.readString(savedAccount);
            assertTrue(saved.contains("pbkdf2-sha256"), saved);
            assertFalse(saved.contains("Valid1!"), saved);

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your user ID: ").contains("Please enter your user ID: "));
                socket.getOutputStream().write((accountId + "\n").getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String passwordPrompt = readUntilQuietAfterContains(socket, "Password: ");
                assertFalse(passwordPrompt.contains("Create it?"), passwordPrompt);
                assertFalse(passwordPrompt.contains("Password: > "), passwordPrompt);

                socket.getOutputStream().write("Valid1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String welcomeBack = readUntilQuietAfterContains(socket, "Hi, Solfeggio! Welcome to LPMuseum.");
                assertTrue(welcomeBack.contains("Hi, Solfeggio! Welcome to LPMuseum."), welcomeBack);
                assertTrue(welcomeBack.contains("Grand Concourse of LPMuseum"), welcomeBack);
                assertTrue(welcomeBack.contains("A directory and a docent are here."), welcomeBack);
                assertFalse(welcomeBack.contains("Email address"), welcomeBack);
                assertFalse(welcomeBack.contains("Persona name"), welcomeBack);

                socket.getOutputStream().write("email\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "No email address is set.")
                        .contains("No email address is set."));

                socket.getOutputStream().write("email solfeggio@example.test\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Email address updated.")
                        .contains("Email address updated."));

                socket.getOutputStream().write("email\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Email address: solfeggio@example.test")
                        .contains("Email address: solfeggio@example.test"));

                socket.getOutputStream().write("password\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Use 'password change' to change it.")
                        .contains("Your password is set."));

                socket.getOutputStream().write("password change\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Current password: ")
                        .contains("Current password: "));
                socket.getOutputStream().write("Valid1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "New password: ")
                        .contains("New password: "));
                socket.getOutputStream().write("Changed1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "New password again: ")
                        .contains("New password again: "));
                socket.getOutputStream().write("Changed1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password changed.")
                        .contains("Password changed."));

                socket.getOutputStream().write("quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilSocketClosed(socket).contains("You step away from LPMuseum."));
            }

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your user ID: ")
                        .contains("Please enter your user ID: "));
                socket.getOutputStream().write((accountId + "\n").getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: ").contains("Password: "));
                socket.getOutputStream().write("Changed1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Hi, Solfeggio! Welcome to LPMuseum.")
                        .contains("Hi, Solfeggio! Welcome to LPMuseum."));
                socket.getOutputStream().write("quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilSocketClosed(socket).contains("You step away from LPMuseum."));
            }

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your user ID: ").contains("Please enter your user ID: "));
                socket.getOutputStream().write((accountId + "\n").getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: ").contains("Password: "));

                socket.getOutputStream().write("wrong1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String firstBadPassword = readUntilQuietAfterContains(socket, "Password: ");
                assertTrue(firstBadPassword.contains("That password did not match. Please try again."), firstBadPassword);
                assertFalse(firstBadPassword.contains("> "), firstBadPassword);

                socket.getOutputStream().write("wrong1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String secondBadPassword = readUntilQuietAfterContains(socket, "Password: ");
                assertTrue(secondBadPassword.contains("That password did not match. Please try again."), secondBadPassword);
                assertFalse(secondBadPassword.contains("> "), secondBadPassword);

                socket.getOutputStream().write("wrong1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String closeTail = readUntilSocketClosed(socket);
                assertTrue(closeTail.contains("Please reconnect when you are ready to try again."), closeTail);
                assertFalse(closeTail.contains("> "), closeTail);
                assertFalse(closeTail.contains("You can't do that."), closeTail);
            }
        }
    }

    @Test
    void lpmuseumAccountCreationEnforcesPasswordPolicy() throws Exception {
        Path museum = lpmuseumTestRoot();

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, museum, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your user ID: ").contains("Please enter your user ID: "));
                socket.getOutputStream().write((uniqueAccountId("policy") + "\n").getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Create it? (yes/no) ").contains("Create it?"));
                socket.getOutputStream().write("yes\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password: ").contains("Password: "));

                assertPasswordRejected(socket, "Aa1!", "Password must be at least 6 characters.");
                assertPasswordRejected(socket, "lowercase1!", "Password must include an uppercase letter.");
                assertPasswordRejected(socket, "UPPERCASE1!", "Password must include a lowercase letter.");
                assertPasswordRejected(socket, "NoNumber!", "Password must include a number.");
                assertPasswordRejected(socket, "NoSpecial1", "Password must include a special character.");
                assertPasswordRejected(socket, "Bad Space1!", "Password may use letters, numbers");

                socket.getOutputStream().write("Good1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password again: ").contains("Password again: "));
                socket.getOutputStream().write("Other1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String mismatch = readUntilQuietAfterContains(socket, "Password: ");
                assertTrue(mismatch.contains("Those passwords did not match."), mismatch);

                socket.getOutputStream().write("Good1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password again: ").contains("Password again: "));
                socket.getOutputStream().write("Good1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Email address (optional): ")
                        .contains("Email address (optional): "));
                socket.getOutputStream().write("not-an-email\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Email address (optional): ")
                        .contains("does not look valid"));
                socket.getOutputStream().write("visitor@example.test\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Persona name: ").contains("Persona name: "));
                socket.getOutputStream().write("Policy Persona\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Gender (female/male/neutral/none/other): ")
                        .contains("Gender"));
                socket.getOutputStream().write("neutral\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Hi, Policy persona! Welcome to LPMuseum.")
                        .contains("Hi, Policy persona! Welcome to LPMuseum."));
            }
        }
    }

    @Test
    void lpmuseumPasswordPromptsNegotiateNoEcho() throws Exception {
        Path museum = lpmuseumTestRoot();

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, museum, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Please enter your user ID: ").contains("Please enter your user ID: "));
                socket.getOutputStream().write((uniqueAccountId("echo") + "\n").getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Create it? (yes/no) ").contains("Create it?"));
                socket.getOutputStream().write("yes\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String passwordPrompt = readUntilQuietAfterContains(socket, "Password: ");
                assertTrue(containsTelnetCommand(passwordPrompt, 251, 1), passwordPrompt);
                assertFalse(passwordPrompt.contains("Password: > "), passwordPrompt);

                socket.getOutputStream().write("Valid1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilQuietAfterContains(socket, "Password again: ").contains("Password again: "));
                socket.getOutputStream().write("Valid1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String emailPrompt = readUntilQuietAfterContains(socket, "Email address (optional): ");
                assertTrue(containsTelnetCommand(emailPrompt, 252, 1), emailPrompt);
                assertFalse(emailPrompt.contains("> "), emailPrompt);
            }
        }
    }

    @Test
    void lpmuseumSuppressesAmbientMessagesUntilAccountLoginCompletes() throws Exception {
        Path museum = lpmuseumTestRoot();
        MudInstance mud = MudInstance.boot(museum, MudlibBoot.DEFAULT_CONFIG_PATH);
        StringWriter output = new StringWriter();
        PrintWriter out = new PrintWriter(output, true);

        InstancePersona persona = mud.attachPersona(out, "127.0.0.1");

        String initial = output.toString();
        assertTrue(initial.contains("Please enter your user ID: "), initial);
        assertFalse(initial.contains("Museum Security Staffer heads"), initial);

        for (int i = 0; i < 30; i++) {
            mud.advanceWorldTick();
        }

        String duringLogin = output.toString();
        assertTrue(duringLogin.contains("Please enter your user ID: "), duringLogin);
        assertFalse(duringLogin.contains("Museum Security Staffer heads"), duringLogin);

        mud.detachPersona(persona);
    }

    @Test
    void lpmuseumStafferPatrolsNoMoreThanEveryThirtyTicks() throws Exception {
        Path museum = lpmuseumTestRoot();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(museum)
                .build());
        CoreEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, museum, MudlibBoot.DEFAULT_CONFIG_PATH, false).boot();
        Object concourse = runtime.loadOrGetObject("place/concourse");
        Object staffer = runtime.present("staffer", concourse);
        Object preloadedStaffer = runtime.loadOrGetObject("entity/staffer");

        assertTrue(staffer != null);
        assertEquals("place/concourse", runtime.objectId(runtime.environment(staffer)));
        assertEquals(null, runtime.environment(preloadedStaffer));

        result.worldRuntime().scheduler().advanceBy(29);
        assertEquals("place/concourse", runtime.objectId(runtime.environment(staffer)));
        assertEquals(null, runtime.environment(preloadedStaffer));

        result.worldRuntime().scheduler().advanceBy(1);
        String destination = runtime.objectId(runtime.environment(staffer));
        assertTrue(List.of("place/origins", "place/workshop", "place/archive").contains(destination), destination);
        assertEquals(null, runtime.environment(preloadedStaffer));
    }

    @Test
    void lpmuseumVendedEntitiesExpireAfterTwoMinutes() throws Exception {
        Path museum = lpmuseumTestRoot();
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(museum)
                .build());
        CoreEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, museum, MudlibBoot.DEFAULT_CONFIG_PATH, false).boot();
        Object workshop = runtime.loadOrGetObject("place/workshop");
        Object machine = runtime.present("vending machine", workshop);

        assertTrue(machine != null);
        assertEquals(1, runtime.invokeObject(machine, "vend", "entity"));
        Object curio = runtime.present("curio", workshop);
        assertTrue(curio != null);
        assertEquals("place/workshop", runtime.objectId(runtime.environment(curio)));
        assertEquals(1, runtime.invokeObject(machine, "live_count"));

        result.worldRuntime().scheduler().advanceBy(119);
        assertEquals(curio, runtime.present("curio", workshop));

        result.worldRuntime().scheduler().advanceBy(1);
        assertEquals(null, runtime.present("curio", workshop));
        assertEquals(0, runtime.invokeObject(machine, "live_count"));
    }

    @Test
    void telnetServerReportsExplicitPreloadManifestSummary() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve(MudlibBoot.LP245_CONFIG_PATH), """
                mfun_object = jvmud/mfuns
                initial_place = room/village/vill_green
                preload_file = init_file
                """);
        Files.writeString(tempDir.resolve("init_file"), """
                obj/preload
                obj/broken
                """);
        Files.writeString(tempDir.resolve("obj/preload.c"), """
                string short() {
                    return "preload";
                }
                """);
        Files.writeString(tempDir.resolve("obj/broken.c"), "int broken( { return 1; }\n");
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                string short() {
                    return "green";
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            assertEquals(
                    "preload manifest init_file: compiled 1 object(s), skipped 1 object(s). Skipped: obj/broken",
                    server.preloadSummary());
        }
    }

    @Test
    void legacyPlayerObjectAdapterMarksConfiguredPlayerAsCombinedProjection() {
        Object playerObject = new Object();

        var projection = new LegacyPlayerObjectAdapter("obj/player").combinedProjection(playerObject);

        assertEquals("obj/player", projection.sourcePath());
        assertEquals(playerObject, projection.object());
        assertTrue(projection.hasRole(MudlibProjectionRole.PLAYER_PROFILE));
        assertTrue(projection.hasRole(MudlibProjectionRole.PERSONA_BEHAVIOR));
        assertTrue(projection.hasRole(MudlibProjectionRole.COMBINED_PLAYER_PERSONA));
    }

    @Test
    void telnetSessionAcceptsPlayerCommandsOverSocket() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void init() {
                    add_action("look");
                    add_verb("look");
                }

                void long(mixed str) {
                    write("A test green.\\n");
                }

                int look(mixed str) {
                    long(str);
                    return 1;
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                String initial = readUntilContains(socket, "Attached player 1");
                assertTrue(initial.contains("JVMud telnet."));

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String look = readUntilContains(socket, "A test green.");
                assertTrue(look.contains("A test green."));

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetSessionBootsMudlibRootWithSpaces() throws Exception {
        Path mudlibRoot = tempDir.resolve("mud lib");
        installMfunShim(mudlibRoot);
        Files.createDirectories(mudlibRoot.resolve("room/village"));
        Files.writeString(mudlibRoot.resolve("room/village/vill_green.c"), """
                void init() {
                    add_action("look");
                    add_verb("look");
                }

                void long(mixed str) {
                    write("A spaced-path green.\\n");
                }

                int look(mixed str) {
                    long(str);
                    return 1;
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, mudlibRoot, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                readUntilContains(socket, "Attached player 1");

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String look = readUntilContains(socket, "A spaced-path green.");
                assertTrue(look.contains("A spaced-path green."));

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void hostedMudlibWorldAdvancesConfiguredTemporalTicks() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                mfun_object = jvmud/mfuns
                player_object = obj/player
                initial_place = room/village/vill_green
                lifecycle.player_session_connected = logon
                temporal_tick_method = heart_beat
                temporal_tick_interval = 0.01
                """);
        Files.writeString(tempDir.resolve("jvmud/mfuns.c"), """
                void write(mixed value) {
                    jvmud_write(value);
                }

                void set_heart_beat(int enabled) {
                    jvmud_schedule_recurring_tick(enabled, 0);
                }
                """);
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                string short() {
                    return "green";
                }
                """);
        Files.writeString(tempDir.resolve("obj/player.c"), """
                void logon() {
                    set_heart_beat(1);
                }

                void heart_beat() {
                    write("world tick delivered\\n");
                    set_heart_beat(0);
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);

                String tick = readUntilContains(socket, "world tick delivered");

                assertTrue(tick.contains("JVMud telnet."));
                assertTrue(tick.contains("world tick delivered"));
            }
        }
    }

    @Test
    void telnetSessionCanAttachConfiguredMudlibPlayerObject() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                initial_place = room/village/vill_green
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                string name;

                void reset(mixed arg) {
                    name = "mudlib player";
                }

                string query_name() {
                    return name;
                }

                string query_real_name() {
                    return name;
                }

                int query_level() {
                    return 0;
                }

                int query_invis() {
                    return 0;
                }

                int remove_ghost() {
                    return 1;
                }

                int id(mixed str) {
                    return str == "player";
                }

                void init() {
                    add_action("look");
                    add_verb("look");
                }

                int look(mixed str) {
                    call_other(environment(this_object()), "long", 0);
                    return 1;
                }

                int move_player(mixed dir_dest) {
                    if (dir_dest != "north#room/village/church")
                        return 0;

                    say(query_name() + " leaves north.\\n");
                    move_object(this_object(), "room/village/church");
                    say(query_name() + " arrives.\\n");
                    call_other(environment(this_object()), "long", 0);
                    return 1;
                }
                """);
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void init() {
                    add_action("north");
                    add_verb("north");
                }

                void long(mixed str) {
                    write("You are on the green.\\n");
                }

                int north(mixed str) {
                    call_other(this_player(), "move_player", "north#room/village/church");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("room/village/church.c"), """
                void long(mixed str) {
                    write("You are in the church.\\n");
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilContains(socket, "Attached player 1 as obj/test_player#clone1")
                        .contains("Attached player 1 as obj/test_player#clone1"));

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "You are on the green.").contains("You are on the green."));

                socket.getOutputStream().write("north\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String north = readUntilContains(socket, "You are in the church.");
                assertTrue(north.contains("You are in the church."), north);
                assertFalse(north.contains("mudlib player leaves north."), north);
                assertFalse(north.contains("mudlib player arrives."), north);

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void runtimeErrorsCanBeHandledByMudlibBoundaryObject() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                mudlib_object = jvmud/mudlib
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                player_prompt = "> "
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                lifecycle.runtime_error = runtime_error
                """);
        Files.writeString(tempDir.resolve("jvmud/mudlib.c"), """
                void runtime_error(mixed actor, mixed context, mixed operation, mixed detail) {
                    jvmud_write_to_lpc_object(actor, "A velvet curtain falls over the machinery.\\n");
                    jvmud_append_mudlib_text("/log/RUNTIME", context + ":" + operation + ":" + detail + "\\n");
                }
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                void init() {
                    add_action("boom", "boom");
                }

                int boom(mixed arg) {
                    int divisor;

                    return 1 / divisor;
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilContains(socket, "Attached player 1").contains("Attached player 1"));

                socket.getOutputStream().write("boom\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String output = readUntilQuietAfterContains(socket, "A velvet curtain falls over the machinery.");
                assertTrue(output.contains("A velvet curtain falls over the machinery."), output);
                assertFalse(output.contains("Error:"), output);
                assertFalse(output.contains("/ by zero"), output);
            }
        }

        String log = Files.readString(tempDir.resolve("log/RUNTIME"));
        assertTrue(log.contains("command:boom"), log);
        assertTrue(log.contains("/ by zero"), log);
    }

    @Test
    void compileErrorsCanBeLoggedByMudlibBoundaryObject() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                mudlib_object = jvmud/mudlib
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                player_prompt = "> "
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                lifecycle.log_error = log_error
                lifecycle.runtime_error = runtime_error
                """);
        Files.writeString(tempDir.resolve("jvmud/mudlib.c"), """
                void log_error(mixed file, mixed err) {
                    jvmud_append_mudlib_text("/log/COMPILER", file + "\\n");
                    jvmud_append_mudlib_text("/log/COMPILER", err + "\\n");
                }

                void runtime_error(mixed actor, mixed context, mixed operation, mixed detail) {
                    jvmud_write_to_lpc_object(actor, "Your sensitive mind notices a wrongness in the fabric of space.\\n");
                    jvmud_append_mudlib_text("/log/RUNTIME", context + ":" + operation + ":" + detail + "\\n");
                }
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                void init() {
                    add_action("loadbroken", "loadbroken");
                }

                int loadbroken(mixed arg) {
                    jvmud_clone_lpc_object("obj/broken");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("obj/broken.c"), """
                int broken() {
                    return 1
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilContains(socket, "Attached player 1").contains("Attached player 1"));

                socket.getOutputStream().write("loadbroken\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String output = readUntilQuietAfterContains(socket, "Your sensitive mind notices a wrongness");
                assertTrue(output.contains("Your sensitive mind notices a wrongness in the fabric of space."), output);
                assertFalse(output.contains("Compilation failed:"), output);
                assertFalse(output.contains("Expect ';' after return statement"), output);
            }
        }

        String compilerLog = Files.readString(tempDir.resolve("log/COMPILER"));
        assertTrue(compilerLog.contains("/obj/broken"), compilerLog);
        assertTrue(compilerLog.contains("Compilation failed:"), compilerLog);
        assertTrue(compilerLog.contains("Expect ';' after return statement"), compilerLog);
        String runtimeLog = Files.readString(tempDir.resolve("log/RUNTIME"));
        assertTrue(runtimeLog.contains("command:loadbroken"), runtimeLog);
    }

    @Test
    void serverShutdownCanNotifyMudlibBoundaryObject() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                mudlib_object = jvmud/mudlib
                mfun_object = jvmud/mfuns
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.server_shutdown = notify_shutdown
                """);
        Files.writeString(tempDir.resolve("jvmud/mudlib.c"), """
                void notify_shutdown(mixed reason) {
                    if (reason) {
                        jvmud_append_mudlib_text("/log/SHUTDOWN", "PANIC! " + reason + "\\n");
                    } else {
                        jvmud_append_mudlib_text("/log/SHUTDOWN", "LPmud shutting down immediately.\\n");
                    }
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        TelnetServer server = new TelnetServer("127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH);
        server.start();
        server.close();
        server.close();

        assertEquals("LPmud shutting down immediately.\n", Files.readString(tempDir.resolve("log/SHUTDOWN")));
    }

    @Test
    void telnetSessionRoutesCapturedInputThroughMfunInputTo() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                player_prompt = "> "
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                string name;

                void reset(mixed arg) {
                    name = "unnamed";
                }

                string query_name() {
                    return name;
                }

                string query_real_name() {
                    return name;
                }

                int query_level() {
                    return 0;
                }

                int query_invis() {
                    return 0;
                }

                int remove_ghost() {
                    return 1;
                }

                int id(mixed str) {
                    return str == "player";
                }

                void init() {
                    add_action("ask_name", "name");
                }

                int ask_name(mixed str) {
                    write("Name: ");
                    input_to("set_name");
                    return 1;
                }

                void set_name(mixed str) {
                    name = str;
                    write("Hello " + name + "\\n");
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                readUntilContains(socket, "Attached player 1");

                socket.getOutputStream().write("name\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String namePrompt = readUntilQuietAfterContains(socket, "Name: ");
                assertTrue(namePrompt.contains("Name: "));
                assertFalse(namePrompt.contains("Name: > "), namePrompt);

                socket.getOutputStream().write("Alice\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String greeting = readUntilContains(socket, "Hello Alice\n> ");
                assertTrue(greeting.contains("Hello Alice"));

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetSessionInvokesConfiguredPlayerConnectionLifecycleHook() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("WELCOME"), "Welcome login.\\n");
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.player_session_connected = logon
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                string name;

                void reset(mixed arg) {
                    name = "logon";
                }

                int logon() {
                    cat("/WELCOME");
                    write("What is your name: ");
                    input_to("logon2");
                    return 1;
                }

                void logon2(mixed str) {
                    name = lower_case(str);
                    write("Hello " + capitalize(name) + "\\n");
                }

                string query_name() {
                    return name;
                }

                string query_real_name() {
                    return name;
                }

                int query_level() {
                    return 0;
                }

                int query_invis() {
                    return 0;
                }

                int remove_ghost() {
                    return 1;
                }

                int id(mixed str) {
                    return str == "player";
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                String greeting = readUntilQuietAfterContains(socket, "What is your name: ");
                assertTrue(greeting.contains("Welcome login."), greeting);
                assertTrue(greeting.contains("What is your name: "), greeting);
                assertFalse(greeting.contains("What is your name: > "), greeting);
                assertTrue(greeting.indexOf("JVMud telnet.") < greeting.indexOf("What is your name: "), greeting);

                socket.getOutputStream().write("Alice\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Hello Alice").contains("Hello Alice"));

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetInputCaptureDeliversExtraCompatibilityArguments() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                initial_place = room/start
                lifecycle.player_session_connected = logon
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                int logon() {
                    write("Code: ");
                    input_to("capture", 0, "left", 7);
                    return 1;
                }

                void capture(string line, string label, int count) {
                    write(label + ":" + line + ":" + count + "\\n");
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                String prompt = readUntilQuietAfterContains(socket, "Code: ");
                assertTrue(prompt.contains("Code: "), prompt);

                socket.getOutputStream().write("blue\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "left:blue:7").contains("left:blue:7"));

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetLoginRestoresSavedPlayerInsteadOfCreatingAgain() throws Exception {
        installMfunShim();
        Files.writeString(tempDir.resolve("jvmud/lp245.config"), """
                mfun_object = jvmud/mfuns
                player_object = obj/test_player
                initial_place = room/start
                lifecycle.object_loaded = reset
                lifecycle.player_session_connected = logon
                lifecycle.player_session_disconnected = quit
                """);
        Files.createDirectories(tempDir.resolve("obj"));
        Files.writeString(tempDir.resolve("obj/test_player.c"), """
                string name;
                string password;
                string mailaddr;
                int gender;
                int level;
                int password_attempts;

                void reset(mixed arg) {
                    gender = -1;
                    level = -1;
                }

                int logon() {
                    write("Name: ");
                    input_to("logon2");
                    return 1;
                }

                void logon2(mixed str) {
                    name = lower_case(str);
                    if (!restore_object("players/" + name))
                        write("New character.\\n");

                    if (level != -1)
                        input_to("check_password", 1);
                    else
                        input_to("new_password", 1);

                    write("Password: ");
                }

                void new_password(mixed str) {
                    if (!password) {
                        password = str;
                        input_to("new_password", 1);
                        write("Password: (again) ");
                        return;
                    }

                    password = str;
                    level = 1;
                    write("Email: ");
                    input_to("getmailaddr");
                }

                void getmailaddr(mixed str) {
                    mailaddr = str;
                    write("Gender: ");
                    input_to("getgender");
                }

                void getgender(mixed str) {
                    gender = 1;
                    enable_commands();
                    write("Welcome new " + capitalize(name) + "\\n");
                }

                void check_password(mixed str) {
                    if (str == password) {
                        password_attempts = 0;
                        write("Welcome back " + capitalize(name) + "\\n");
                    } else {
                        write("Wrong password!\\n");
                        password_attempts = password_attempts + 1;
                        if (password_attempts < 3) {
                            input_to("check_password", 1);
                            write("Password: ");
                            return;
                        }
                        destruct(this_object());
                    }
                }

                int quit() {
                    save_object("players/" + name);
                    write("Saving " + capitalize(name) + ".\\n");
                    return 1;
                }
                """);
        Files.createDirectories(tempDir.resolve("room"));
        Files.writeString(tempDir.resolve("room/start.c"), """
                void long(mixed str) {
                    write("Start room.\\n");
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                String firstNamePrompt = readUntilQuietAfterContains(socket, "Name: ");
                assertTrue(firstNamePrompt.contains("Name: "));
                assertFalse(firstNamePrompt.contains("Name: > "), firstNamePrompt);

                socket.getOutputStream().write("alice\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String firstPassword = readUntilQuietAfterContains(socket, "Password: ");
                assertTrue(firstPassword.contains("New character."), firstPassword);
                assertTrue(firstPassword.contains("Password: "), firstPassword);
                assertFalse(firstPassword.contains("Password: > "), firstPassword);

                socket.getOutputStream().write("secret\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Password: (again) ").contains("Password: (again) "));

                socket.getOutputStream().write("secret\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Email: ").contains("Email: "));

                socket.getOutputStream().write("alice@example.test\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Gender: ").contains("Gender: "));

                socket.getOutputStream().write("female\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Welcome new Alice").contains("Welcome new Alice"));

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                socket.shutdownOutput();
                assertSavedPlayerJsonFile(tempDir.resolve("players/alice.o"));
            }
            String savedPlayer = Files.readString(tempDir.resolve("players/alice.o"));
            assertTrue(savedPlayer.contains("\"format\""), savedPlayer);
            assertTrue(savedPlayer.contains("\"jvmud.lpc-object-state\""), savedPlayer);
            assertTrue(savedPlayer.contains("\"obj.test_player.name\""), savedPlayer);
            assertTrue(savedPlayer.contains("\"value\""), savedPlayer);
            assertTrue(savedPlayer.contains("\"alice\""), savedPlayer);

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilContains(socket, "Name: ").contains("Name: "));

                socket.getOutputStream().write("alice\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String secondPassword = readUntilContains(socket, "Password: ");
                assertFalse(secondPassword.contains("New character."), secondPassword);
                assertTrue(secondPassword.contains("Password: "), secondPassword);

                socket.getOutputStream().write("secret\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String welcomeBack = readUntilContains(socket, "Welcome back Alice");
                assertTrue(welcomeBack.contains("Welcome back Alice"), welcomeBack);
                assertFalse(welcomeBack.contains("Email: "), welcomeBack);
                assertFalse(welcomeBack.contains("Gender: "), welcomeBack);

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilContains(socket, "Name: ").contains("Name: "));

                socket.getOutputStream().write("alice\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilContains(socket, "Password: ").contains("Password: "));

                socket.getOutputStream().write("wrong\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String firstBadPassword = readUntilContains(socket, "Password: ");
                assertTrue(firstBadPassword.contains("Wrong password!"), firstBadPassword);
                assertTrue(firstBadPassword.contains("Password: "), firstBadPassword);
                assertFalse(firstBadPassword.contains("> "), firstBadPassword);

                socket.getOutputStream().write("wrong\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String secondBadPassword = readUntilContains(socket, "Password: ");
                assertTrue(secondBadPassword.contains("Wrong password!"), secondBadPassword);
                assertTrue(secondBadPassword.contains("Password: "), secondBadPassword);
                assertFalse(secondBadPassword.contains("> "), secondBadPassword);

                socket.getOutputStream().write("wrong\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String thirdBadPassword = readUntilContains(socket, "Wrong password!");
                assertTrue(thirdBadPassword.contains("Wrong password!"), thirdBadPassword);
                assertFalse(thirdBadPassword.contains("> "), thirdBadPassword);
                String closeTail = readUntilSocketClosed(socket);
                assertFalse(closeTail.contains("> "), closeTail);
                assertFalse(closeTail.contains("You can't do that."), closeTail);
            }

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                String reattached = readUntilContains(socket, "Name: ");
                assertTrue(reattached.contains("Attached player 4 as obj/test_player#clone3"), reattached);
                assertTrue(reattached.contains("Name: "), reattached);

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
            }
        }
    }

    @Test
    void telnetConnectionsShareOneBootedMudRuntime() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                int touches = 0;

                void init() {
                    add_action("touch");
                    add_verb("touch");
                }

                int touch(mixed str) {
                    touches = touches + 1;
                    write("touch " + touches + "\\n");
                    return 1;
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket first = new Socket("127.0.0.1", server.port())) {
                first.setSoTimeout(5000);
                assertTrue(readUntilContains(first, "Attached player 1").contains("Attached player 1"));

                try (Socket second = new Socket("127.0.0.1", server.port())) {
                    second.setSoTimeout(5000);
                    assertTrue(readUntilContains(second, "Attached player 2").contains("Attached player 2"));

                    first.getOutputStream().write("touch\n".getBytes(StandardCharsets.UTF_8));
                    first.getOutputStream().flush();
                    assertTrue(readUntilContains(first, "touch 1").contains("touch 1"));

                    second.getOutputStream().write("touch\n".getBytes(StandardCharsets.UTF_8));
                    second.getOutputStream().flush();
                    assertTrue(readUntilContains(second, "touch 2").contains("touch 2"));

                    first.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                    second.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                    first.getOutputStream().flush();
                    second.getOutputStream().flush();
                }
            }
        }
    }

    @Test
    void telnetConnectionsBindRuntimeSessionsAndRouteTargetedOutput() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void init() {
                    add_action("who");
                    add_verb("who");
                    add_action("poke");
                    add_verb("poke");
                }

                int who(mixed str) {
                    write("users=" + sizeof(users()) + " ip=" + query_ip_number(this_player()) + "\\n");
                    return 1;
                }

                int poke(mixed str) {
                    object *list;

                    list = users();
                    tell_object(list[1], "poke from " + query_ip_number(this_player()) + "\\n");
                    write("sent\\n");
                    return 1;
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket first = new Socket("127.0.0.1", server.port());
                    Socket second = new Socket("127.0.0.1", server.port())) {
                first.setSoTimeout(5000);
                second.setSoTimeout(5000);
                assertTrue(readUntilContains(first, "Attached player 1").contains("Attached player 1"));
                assertTrue(readUntilContains(second, "Attached player 2").contains("Attached player 2"));

                first.getOutputStream().write("who\n".getBytes(StandardCharsets.UTF_8));
                first.getOutputStream().flush();
                String who = readUntilContains(first, "users=2 ip=127.0.0.1");
                assertTrue(who.contains("users=2 ip=127.0.0.1"));

                first.getOutputStream().write("poke\n".getBytes(StandardCharsets.UTF_8));
                first.getOutputStream().flush();
                assertTrue(readUntilContains(first, "sent").contains("sent"));
                assertTrue(readUntilContains(second, "poke from 127.0.0.1").contains("poke from 127.0.0.1"));

                first.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                second.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                first.getOutputStream().flush();
                second.getOutputStream().flush();
            }
        }
    }

    @Test
    void localSessionMovementRegistersNativeWorldLinks() throws Exception {
        Files.createDirectories(tempDir.resolve("room"));
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void long(mixed str) {
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder().baseIncludePath(tempDir).build());
        CoreEfuns.registerCore(runtime);
        WorldRuntime worldRuntime = new WorldRuntime(new World("test", "Test World"));
        Place church = worldRuntime.createPlace("room/village/church", "Village church");
        Entity actorEntity = worldRuntime.createEntity(
                "session/local", "local session", church, Capability.ACTOR, Capability.PERCEPTIVE);
        LocalSessionActor actor = new LocalSessionActor(runtime, worldRuntime, actorEntity, "tester");

        assertEquals(1, actor.move_player("south#room/village/vill_green"));

        Place green = worldRuntime.place("room/village/vill_green");
        Link south = worldRuntime.linkFrom(church, "south");
        assertEquals(green, south.destination());
        assertEquals(church, south.origin());
        assertEquals(green, worldRuntime.locationOf(actorEntity));

        Files.writeString(tempDir.resolve("room/bad.c"), "int broken( { return 1; }\n");
        assertEquals(0, actor.move_player("east#room/bad"));
        assertEquals(green, worldRuntime.locationOf(actorEntity));
    }

    @Test
    void bootPreloadsExplicitManifestAndRegistersStartingPlaceWithoutPlayerHandle() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("room"));
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve(MudlibBoot.DEFAULT_CONFIG_PATH), """
                mfun_object = jvmud/mfuns
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                preload_file = init_file
                """);
        Files.writeString(tempDir.resolve("init_file"), """
                # preload one simple object
                obj/preload.c
                """);
        Files.writeString(tempDir.resolve("obj/preload.c"), """
                string short() {
                    return "preloaded object";
                }
                """);
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void init() {
                    add_action("north");
                    add_verb("north");
                }

                void long(mixed str) {
                    write("You are on the green.\\n");
                }

                int north(mixed str) {
                    call_other(this_player(), "move_player", "north#room/village/church");
                    return 1;
                }
                """);
        Files.writeString(tempDir.resolve("room/village/church.c"), """
                void init() {
                    add_action("south");
                    add_verb("south");
                }

                void long(mixed str) {
                    write("You are in the church.\\n");
                }

                int south(mixed str) {
                    call_other(this_player(), "move_player", "south#room/village/vill_green");
                    return 1;
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .build());
        CoreEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, tempDir, MudlibBoot.DEFAULT_CONFIG_PATH, false).boot();

        assertEquals(1, result.preloadedObjects().size());
        assertTrue(result.preloadedObjects().contains("obj/preload"));
        assertEquals("room/village/vill_green", result.startingRoom());
        assertEquals(null, result.actorHandle());
        assertEquals(null, result.actor());
        assertTrue(result.skippedPreloads().isEmpty());
        assertEquals(List.of("obj/preload"), result.preloadManifestPreloadedObjects());
        assertTrue(result.preloadManifestSkippedPreloads().isEmpty());
    }

    @Test
    void bootDiscoversDedicatedMudlibDeclarationObject() throws Exception {
        Files.createDirectories(tempDir.resolve("jvmud"));
        Files.writeString(tempDir.resolve("jvmud/mudlib.c"), """
                string mfun_object() {
                    return "/jvmud/functions.c";
                }

                string player_prompt() {
                    return "% ";
                }

                mixed handled_lifecycle_events() {
                    return ({ "object-initialized", "scheduled tick" });
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .build());
        CoreEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, tempDir).boot();
        MudlibBoundary boundary = result.worldRuntime().mudlibBoundary();

        assertEquals("jvmud/mudlib", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("jvmud/functions", boundary.mfunObjectPath().orElseThrow());
        assertEquals("% ", boundary.playerPrompt().orElseThrow());
        assertTrue(boundary.handles(MudlibLifecycleEvent.OBJECT_LOADED));
        assertTrue(boundary.handles(MudlibLifecycleEvent.SCHEDULED_TICK));
        assertEquals(boundary, runtime.mudlibBoundary());
        assertTrue(result.preloadedObjects().contains("jvmud/mudlib"));
    }

    @Test
    void bootReadsMudlibConfigObjectFromExplicitPath() throws Exception {
        Files.createDirectories(tempDir.resolve("config"));
        Files.createDirectories(tempDir.resolve("obj"));
        Files.createDirectories(tempDir.resolve("place"));
        Files.writeString(tempDir.resolve("config/startup"), """
                game_id = strange-new-mudlib
                game_name = Strange New Mudlib
                mudlib_object = config/mudlib
                mfun_object = config/mfuns
                player_prompt = "$ "
                initial_place = place/start
                preload_objects = obj/preload
                lifecycle.object_loaded = on_loaded
                lifecycle.interaction_scope_started = on_scope
                temporal_tick_method = heartbeat
                temporal_tick_interval = 5
                """);
        Files.writeString(tempDir.resolve("obj/preload.c"), """
                string short() {
                    return "preload";
                }
                """);
        Files.writeString(tempDir.resolve("config/mudlib.c"), """
                string player_prompt() {
                    return "% ";
                }
                """);
        Files.writeString(tempDir.resolve("place/start.c"), """
                string short() {
                    return "start";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .build());
        CoreEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, tempDir, "config/startup").boot();
        MudlibBoundary boundary = result.worldRuntime().mudlibBoundary();

        assertEquals("strange-new-mudlib", boundary.gameId().orElseThrow());
        assertEquals("Strange New Mudlib", boundary.gameName().orElseThrow());
        assertEquals("config/mudlib", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("config/mfuns", boundary.mfunObjectPath().orElseThrow());
        assertEquals("$ ", boundary.playerPrompt().orElseThrow());
        assertEquals("place/start", boundary.initialPlacePath().orElseThrow());
        assertEquals("heartbeat", boundary.temporalTickMethod().orElseThrow());
        assertEquals(5, boundary.temporalTickIntervalSeconds());
        assertEquals("on_loaded", boundary.lifecycleMethod(MudlibLifecycleEvent.OBJECT_LOADED).orElseThrow());
        assertEquals("on_scope", boundary.lifecycleMethod(MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED).orElseThrow());
        assertTrue(result.preloadedObjects().contains("config/mudlib"));
        assertTrue(result.preloadedObjects().contains("obj/preload"));
        assertEquals("place/start", result.startingRoom());
        assertEquals("local/player", result.actorHandle());
    }

    @Test
    void bootPreservesConfigCompatibilityDataWhenBoundaryObjectIsMerged() throws Exception {
        Files.createDirectories(tempDir.resolve("config"));
        Files.writeString(tempDir.resolve("config/startup"), """
                mudlib_object = config/mudlib
                database.url = jdbc:test://localhost/mud
                database.user = muduser
                database.password = mudpass
                direct_efun.sizeof = jvmud_size
                ldmud_compat_predefine.__VERSION_MAJOR__ = 3
                ldmud_compat_predefine.__VERSION_MINOR__ = 6
                ldmud_compat_function_predefine.__EFUN_DEFINED__.text_width = 0
                """);
        Files.writeString(tempDir.resolve("config/mudlib.c"), """
                string player_prompt() {
                    return "% ";
                }
                """);

        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(tempDir)
                .build());
        CoreEfuns.registerCore(runtime);

        MudlibBootResult result = new MudlibBoot(runtime, tempDir, "config/startup", false).boot();
        MudlibBoundary boundary = result.worldRuntime().mudlibBoundary();

        assertEquals("config/mudlib", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("% ", boundary.playerPrompt().orElseThrow());
        assertEquals("jdbc:test://localhost/mud", boundary.databaseJdbcUrl().orElseThrow());
        assertEquals("muduser", boundary.databaseUser().orElseThrow());
        assertEquals("mudpass", boundary.databasePassword().orElseThrow());
        assertEquals("jvmud_size", boundary.directEfunAliases().get("sizeof"));
        assertEquals("3", boundary.compatibilityPredefines().get("__VERSION_MAJOR__"));
        assertEquals("6", boundary.compatibilityPredefines().get("__VERSION_MINOR__"));
        assertEquals(
                Map.of("text_width", "0"),
                boundary.compatibilityFunctionPredefines().get("__EFUN_DEFINED__"));
        assertEquals(boundary, runtime.mudlibBoundary());
    }

    private String uniqueAccountId(String prefix) {
        String suffix = Long.toString(System.nanoTime(), 36);
        return (prefix + suffix).toLowerCase();
    }

    private Path lpmuseumTestRoot() throws IOException {
        Path source = repositoryRoot().resolve("mudlibs/lpmuseum");
        Path target = tempDir.resolve("lpmuseum-" + Long.toString(System.nanoTime(), 36));
        copyMudlibTreeWithoutSavedAccounts(source, target);
        return target;
    }

    private Path lp245TestRoot() throws IOException {
        Path source = repositoryRoot().resolve("mudlibs/lp245");
        Path target = tempDir.resolve("lp245-" + Long.toString(System.nanoTime(), 36));
        copyMudlibTreeWithoutSavedAccounts(source, target);
        return target;
    }

    private Path mountedLpmuseumTestRoot() throws IOException {
        Path sourceRoot = repositoryRoot().resolve("mudlibs");
        Path targetRoot = tempDir.resolve("mounted-" + Long.toString(System.nanoTime(), 36));
        copyMudlibTreeWithoutSavedAccounts(sourceRoot.resolve("lpmuseum"), targetRoot.resolve("lpmuseum"));
        copyMudlibTreeWithoutSavedAccounts(sourceRoot.resolve("lp245"), targetRoot.resolve("lp245"));
        return targetRoot.resolve("lpmuseum");
    }

    private void copyMudlibTreeWithoutSavedAccounts(Path source, Path target) throws IOException {
        try (var paths = Files.walk(source)) {
            paths.forEach(path -> copyMudlibPathWithoutSavedAccounts(source, target, path));
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private void copyMudlibPathWithoutSavedAccounts(Path source, Path target, Path path) {
        Path relative = source.relativize(path);
        if (isSavedAccountFile(relative)) {
            return;
        }

        Path destination = target.resolve(relative.toString());
        try {
            if (Files.isDirectory(path)) {
                Files.createDirectories(destination);
            } else {
                Files.copy(path, destination);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isSavedAccountFile(Path relative) {
        return relative.getNameCount() == 2
                && "accounts".equals(relative.getName(0).toString())
                && relative.getFileName().toString().endsWith(".o");
    }

    private String createLpmuseumAccountAndEnter(
            Socket socket,
            String accountId,
            String password,
            String personaName,
            String gender) throws Exception {
        socket.getOutputStream().write((accountId + "\n").getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        String creation = readUntilQuietAfterContains(socket, "Create it? (yes/no) ");
        assertTrue(creation.contains("No LPMuseum account exists for " + accountId), creation);
        assertFalse(creation.contains("> "), creation);

        socket.getOutputStream().write("yes\n".getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        String passwordPrompt = readUntilQuietAfterContains(socket, "Password: ");
        assertTrue(passwordPrompt.contains("Password: "), passwordPrompt);
        assertFalse(passwordPrompt.contains("Password: > "), passwordPrompt);

        socket.getOutputStream().write((password + "\n").getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        assertTrue(readUntilQuietAfterContains(socket, "Password again: ").contains("Password again: "));

        socket.getOutputStream().write((password + "\n").getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        assertTrue(readUntilQuietAfterContains(socket, "Email address (optional): ")
                .contains("Email address (optional): "));

        socket.getOutputStream().write("\n".getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        assertTrue(readUntilQuietAfterContains(socket, "Persona name: ").contains("Persona name: "));

        socket.getOutputStream().write((personaName + "\n").getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        assertTrue(readUntilQuietAfterContains(socket, "Gender (female/male/neutral/none/other): ")
                .contains("Gender"));

        socket.getOutputStream().write((gender + "\n").getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        return readUntilQuietAfterContains(socket, "Welcome to LPMuseum.");
    }

    private void assertPasswordRejected(Socket socket, String password, String expected) throws Exception {
        socket.getOutputStream().write((password + "\n").getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        String rejection = readUntilQuietAfterContains(socket, "Password: ");
        assertTrue(rejection.contains(expected), rejection);
        assertFalse(rejection.contains("Password: > "), rejection);
    }

    private boolean containsTelnetCommand(String text, int command, int option) {
        return text.indexOf("" + (char) 255 + (char) command + (char) option) >= 0;
    }

    private String readUntilContains(Socket socket, String expected) throws Exception {
        StringBuilder output = new StringBuilder();
        while (!output.toString().contains(expected)) {
            int value = socket.getInputStream().read();
            if (value == -1) {
                break;
            }
            output.append((char) value);
        }
        return output.toString();
    }

    private Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))
                    && Files.isDirectory(current.resolve("mudlibs"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate JVMud repository root.");
    }

    private String readUntilQuietAfterContains(Socket socket, String expected) throws Exception {
        StringBuilder output = new StringBuilder(readUntilContains(socket, expected));
        long deadline = System.nanoTime() + 50_000_000L;
        while (System.nanoTime() < deadline) {
            while (socket.getInputStream().available() > 0) {
                int value = socket.getInputStream().read();
                if (value == -1) {
                    return output.toString();
                }
                output.append((char) value);
            }
            Thread.sleep(5);
        }
        return output.toString();
    }

    private String readUntilSocketClosed(Socket socket) throws Exception {
        StringBuilder output = new StringBuilder();
        while (true) {
            int value = socket.getInputStream().read();
            if (value == -1) {
                return output.toString();
            }
            output.append((char) value);
        }
    }

    private void assertSavedPlayerFile(Path path) throws Exception {
        long deadline = System.nanoTime() + 1_000_000_000L;
        while (System.nanoTime() < deadline) {
            if (Files.isRegularFile(path)) {
                return;
            }
            Thread.sleep(10);
        }
        assertTrue(Files.isRegularFile(path));
    }

    private void assertSavedPlayerJsonFile(Path path) throws Exception {
        long deadline = System.nanoTime() + 1_000_000_000L;
        while (System.nanoTime() < deadline) {
            if (Files.isRegularFile(path) && Files.readString(path).contains("\"format\"")) {
                return;
            }
            Thread.sleep(10);
        }
        assertTrue(Files.isRegularFile(path));
        assertTrue(Files.readString(path).contains("\"format\""));
    }

    private void installMfunShim() throws Exception {
        installMfunShim(tempDir);
    }

    private void installMfunShim(Path mudlibRoot) throws Exception {
        Files.createDirectories(mudlibRoot.resolve("jvmud"));
        String config = """
                mfun_object = jvmud/mfuns
                lifecycle.object_loaded = reset
                lifecycle.interaction_scope_started = init
                """;
        Files.writeString(mudlibRoot.resolve(MudlibBoot.DEFAULT_CONFIG_PATH), config);
        Files.writeString(mudlibRoot.resolve(MudlibBoot.LP245_CONFIG_PATH), config);
        Files.writeString(mudlibRoot.resolve("jvmud/mudlib.c"), """
                string mfun_object() {
                    return "jvmud/mfuns";
                }
                """);
        Files.writeString(mudlibRoot.resolve("jvmud/mfuns.c"), """
                void write(mixed value) {
                    jvmud_write(value);
                }

                void tell_object(object target, mixed value) {
                    jvmud_write_to_lpc_object(target, value);
                }

                void say(mixed value) {
                    jvmud_emit_perceivable(jvmud_current_actor(), value);
                }

                void say(mixed value, object excluded) {
                    jvmud_emit_perceivable_except(jvmud_current_actor(), value, excluded);
                }

                int sizeof(mixed value) {
                    return jvmud_size(value);
                }

                object *users() {
                    return jvmud_users();
                }

                int query_idle(mixed player) {
                    return jvmud_query_idle(player);
                }

                mixed query_ip_number(mixed player) {
                    return jvmud_query_ip_number(player);
                }

                int save_object(string path) {
                    return jvmud_save_lpc_object_state(path);
                }

                int restore_object(string path) {
                    return jvmud_restore_lpc_object_state(path);
                }

                string ctime(int timestamp) {
                    return jvmud_format_time(timestamp);
                }

                void enable_commands() {
                    jvmud_enable_commands();
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

                void input_to(string method) {
                    jvmud_capture_session_input(method, 0);
                }

                void input_to(string method, int noecho) {
                    jvmud_capture_session_input(method, noecho);
                }

                void input_to(string method, int noecho, mixed arg1) {
                    jvmud_capture_session_input(method, noecho, arg1);
                }

                void input_to(string method, int noecho, mixed arg1, mixed arg2) {
                    jvmud_capture_session_input(method, noecho, arg1, arg2);
                }

                object this_player() {
                    return jvmud_current_actor();
                }

                int transfer_player_to_game(string game_id) {
                    return jvmud_transfer_player_to_game(game_id);
                }

                mixed call_other(mixed target, string method) {
                    return jvmud_invoke_lpc_object(target, method);
                }

                mixed call_other(mixed target, string method, mixed arg) {
                    return jvmud_invoke_lpc_object(target, method, arg);
                }

                int cat(string path) {
                    mixed text;

                    text = jvmud_read_mudlib_text(path);
                    if (!stringp(text))
                        return 0;

                    write(text);
                    return 1;
                }

                string capitalize(mixed value) {
                    return jvmud_capitalize_text(value);
                }

                mixed creator(mixed ob) {
                    return 0;
                }

                object environment() {
                    return jvmud_entity_location();
                }

                object environment(mixed ob) {
                    return jvmud_entity_location(ob);
                }

                string extract(mixed value, int from) {
                    return jvmud_extract_text(value, from);
                }

                string extract(mixed value, int from, int to) {
                    return jvmud_extract_text(value, from, to);
                }

                void move_object(mixed ob, mixed destination) {
                    jvmud_move_entity(ob, destination);
                }

                string lower_case(mixed value) {
                    return jvmud_lowercase_text(value);
                }

                status stringp(mixed value) {
                    return jvmud_is_string(value);
                }

                object this_object() {
                    return jvmud_current_lpc_object();
                }

                void destruct(object ob) {
                    jvmud_destroy_lpc_object(ob);
                }
                """);
    }
}
