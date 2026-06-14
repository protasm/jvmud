package io.github.protasm.jvmud.server;

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
import io.github.protasm.jvmud.runtime.MudlibProjectionRole;
import io.github.protasm.jvmud.runtime.Place;
import io.github.protasm.jvmud.runtime.World;
import io.github.protasm.jvmud.runtime.WorldRuntime;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
        Path museum = repositoryRoot().resolve("mudlibs/lpmuseum");

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, museum, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();
            assertEquals("preload manifest: none declared.", server.preloadSummary());

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                String initial = readUntilQuietAfterContains(socket, "Account ID: ");
                assertTrue(initial.contains("Attached player 1 as persona/visitor#clone in place/concourse"), initial);
                assertTrue(initial.contains("Account ID: "), initial);

                String greeting = createLpmuseumAccountAndEnter(socket, uniqueAccountId("protasm"), "Valid1!",
                        "protasm", "neutral");
                assertTrue(greeting.contains("Hi, Protasm! Welcome to LPMuseum."), greeting);
                assertTrue(greeting.contains("Protasm enters LPMuseum through the museum doors."), greeting);

                try (Socket second = new Socket("127.0.0.1", server.port())) {
                    second.setSoTimeout(5000);
                    assertTrue(readUntilQuietAfterContains(second, "Account ID: ")
                            .contains("Attached player 2 as persona/visitor#clone"));
                    assertTrue(createLpmuseumAccountAndEnter(second, uniqueAccountId("solfeggio"), "Valid1!",
                            "solfeggio", "other").contains("Hi, Solfeggio! Welcome to LPMuseum."));
                    assertTrue(readUntilQuietAfterContains(socket, "Solfeggio enters LPMuseum through the museum doors.")
                            .contains("Solfeggio enters LPMuseum through the museum doors."));

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String concourse = readUntilQuietAfterContains(socket, "directory and a docent");
                assertTrue(concourse.contains("Grand Concourse of LPMuseum"), concourse);
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
                assertTrue(who.contains("persona/visitor#clone"), who);
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
                assertTrue(readUntilQuietAfterContains(socket, "vended curio #1")
                        .contains("vended curio #1"));

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
    void lpmuseumRestoresAccountAndDisconnectsAfterThreeBadPasswords() throws Exception {
        Path museum = repositoryRoot().resolve("mudlibs/lpmuseum");
        String accountId = uniqueAccountId("returning");

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, museum, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Account ID: ").contains("Account ID: "));
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
                assertTrue(readUntilQuietAfterContains(socket, "Account ID: ").contains("Account ID: "));
                socket.getOutputStream().write((accountId + "\n").getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String passwordPrompt = readUntilQuietAfterContains(socket, "Password: ");
                assertFalse(passwordPrompt.contains("Create it?"), passwordPrompt);
                assertFalse(passwordPrompt.contains("Password: > "), passwordPrompt);

                socket.getOutputStream().write("Valid1!\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String welcomeBack = readUntilQuietAfterContains(socket, "Hi, Solfeggio! Welcome to LPMuseum.");
                assertTrue(welcomeBack.contains("Hi, Solfeggio! Welcome to LPMuseum."), welcomeBack);
                assertFalse(welcomeBack.contains("Email address"), welcomeBack);
                assertFalse(welcomeBack.contains("Persona name"), welcomeBack);

                socket.getOutputStream().write("quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                assertTrue(readUntilSocketClosed(socket).contains("You step away from LPMuseum."));
            }

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Account ID: ").contains("Account ID: "));
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
        Path museum = repositoryRoot().resolve("mudlibs/lpmuseum");

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, museum, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Account ID: ").contains("Account ID: "));
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
        Path museum = repositoryRoot().resolve("mudlibs/lpmuseum");

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, museum, MudlibBoot.DEFAULT_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                assertTrue(readUntilQuietAfterContains(socket, "Account ID: ").contains("Account ID: "));
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
    void lpmuseumSuppressesAmbientMessagesUntilAccountLoginCompletes() {
        Path museum = repositoryRoot().resolve("mudlibs/lpmuseum");
        TelnetMud mud = TelnetMud.boot(museum, MudlibBoot.DEFAULT_CONFIG_PATH);
        StringWriter output = new StringWriter();
        PrintWriter out = new PrintWriter(output, true);

        TelnetPersona persona = mud.attachPersona(out, "127.0.0.1");

        String initial = output.toString();
        assertTrue(initial.contains("Account ID: "), initial);
        assertFalse(initial.contains("Museum Security Staffer heads"), initial);

        for (int i = 0; i < 30; i++) {
            mud.advanceWorldTick();
        }

        String duringLogin = output.toString();
        assertTrue(duringLogin.contains("Account ID: "), duringLogin);
        assertFalse(duringLogin.contains("Museum Security Staffer heads"), duringLogin);

        mud.detachPersona(persona);
    }

    @Test
    void lpmuseumStafferPatrolsNoMoreThanEveryThirtyTicks() {
        Path museum = repositoryRoot().resolve("mudlibs/lpmuseum");
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(museum)
                .build());
        EngineEfuns.registerCore(runtime);

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
    void lpmuseumVendedEntitiesExpireAfterTwoMinutes() {
        Path museum = repositoryRoot().resolve("mudlibs/lpmuseum");
        LPCRuntime runtime = new LPCRuntime(LPCRuntimeConfig.builder()
                .baseIncludePath(museum)
                .build());
        EngineEfuns.registerCore(runtime);

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
                void long(mixed str) {
                    write("A test green.\\n");
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
                void long(mixed str) {
                    write("A spaced-path green.\\n");
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
    void telnetLookFallbackAcceptsNoArgumentRoomLong() throws Exception {
        installMfunShim();
        Files.createDirectories(tempDir.resolve("room/village"));
        Files.writeString(tempDir.resolve("room/village/vill_green.c"), """
                void long() {
                    write("A no-argument long room.\\n");
                }
                """);

        try (TelnetServer server = new TelnetServer(
                "127.0.0.1", 0, tempDir, MudlibBoot.LP245_CONFIG_PATH)) {
            server.start();

            try (Socket socket = new Socket("127.0.0.1", server.port())) {
                socket.setSoTimeout(5000);
                readUntilContains(socket, "Attached player 1");

                socket.getOutputStream().write("look\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                String look = readUntilContains(socket, "A no-argument long room.");
                assertTrue(look.contains("A no-argument long room."), look);

                socket.getOutputStream().write("/quit\n".getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
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
                assertTrue(readUntilContains(socket, "Attached player 1 as obj/test_player#clone")
                        .contains("Attached player 1 as obj/test_player#clone"));

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
                assertTrue(reattached.contains("Attached player 4 as obj/test_player#clone"), reattached);
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
        EngineEfuns.registerCore(runtime);
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
        EngineEfuns.registerCore(runtime);

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
        EngineEfuns.registerCore(runtime);

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
        EngineEfuns.registerCore(runtime);

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

    private String uniqueAccountId(String prefix) {
        String suffix = Long.toString(System.nanoTime(), 36);
        return (prefix + suffix).toLowerCase();
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
                    jvmud_send_to_entity(target, value);
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

                object this_player() {
                    return jvmud_current_actor();
                }

                int transfer_player_to_game(string game_id) {
                    return jvmud_transfer_player_to_game(game_id);
                }

                mixed call_other(mixed target, string method) {
                    return jvmud_invoke_entity(target, method);
                }

                mixed call_other(mixed target, string method, mixed arg) {
                    return jvmud_invoke_entity(target, method, arg);
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
                    return jvmud_current_entity();
                }

                void destruct(object ob) {
                    jvmud_destroy_entity(ob);
                }
                """);
    }
}
