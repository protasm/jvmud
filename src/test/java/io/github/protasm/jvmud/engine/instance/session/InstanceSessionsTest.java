package io.github.protasm.jvmud.engine.instance.session;

import static org.junit.jupiter.api.Assertions.*;

import io.github.protasm.jvmud.engine.instance.session.login.ManagedLoginResult;
import io.github.protasm.jvmud.engine.instance.session.login.ManagedLoginSession;
import io.github.protasm.jvmud.engine.instance.session.login.ManagedPersonaProfile;
import io.github.protasm.jvmud.engine.model.identity.PersonaRecord;
import io.github.protasm.jvmud.engine.model.world.Capability;
import io.github.protasm.jvmud.engine.model.world.World;
import io.github.protasm.jvmud.engine.model.world.WorldRuntime;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/** Exercises connection transitions through the one owner rather than manipulating its indexes. */
final class InstanceSessionsTest {
    private final WorldRuntime world = new WorldRuntime(new World("session-test", "Session Test"));
    private final StringBuilder transcript = new StringBuilder();
    private final InstanceSessions sessions = new InstanceSessions(Object::toString, transcript::append,
            Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void connectionExistsDuringLoginAndAttachmentHasTheActualNativeEntity() {
        StringBuilder output = new StringBuilder();
        InstanceSession session = sessions.open("one", " 127.0.0.1 ", output::append);
        ManagedLoginSession login = new WaitingLogin();
        sessions.beginLogin("one", login);
        var connected = session.record().orElseThrow();
        var player = session.player().orElseThrow();
        assertTrue(session.attachment().isEmpty());
        assertSame(login, session.login().orElseThrow());
        assertEquals("127.0.0.1", session.remoteAddress().orElseThrow());
        assertTrue(sessions.writeToPlayer(player.id(), "login prompt"));

        PersonaAttachment selected = attachment("hero").withProfile(
                new ManagedPersonaProfile("account/hero", "Hero", "other", Map.of()));
        assertSame(session, sessions.attach("one", selected));

        PersonaAttachment attached = session.attachment().orElseThrow();
        assertSame(selected.entity(), attached.entity());
        assertSame(selected.entity(), attached.personaRecord().entity().orElseThrow());
        assertEquals(player.id(), attached.personaRecord().controllingPlayerId().orElseThrow());
        assertEquals(player.id(), session.player().orElseThrow().id());
        assertEquals(connected.connectedAt(), session.record().orElseThrow().connectedAt());
        assertEquals(attached.personaRecord().id(), session.record().orElseThrow().attachedPersonaId().orElseThrow());
        assertEquals("Hero", attached.displayName());
        assertEquals("account/hero", attached.userId());
        assertTrue(session.login().isEmpty());
        assertEquals("login prompt", output.toString());
    }

    @Test
    void negotiatedProtocolAndOutputRouteSurviveLoginAttachmentAndBehaviorRebind() {
        InstanceSession session = sessions.open("one", "127.0.0.1", ignored -> {});
        List<String> packets = new ArrayList<>();
        sessions.bindProtocolSink("one", (protocol, text) -> packets.add(protocol + ":" + text));
        sessions.setProtocolEnabled("one", "gmcp", true);
        PersonaAttachment controller = attachment("controller");
        sessions.attach("one", controller);
        assertTrue(sessions.sendProtocolMessage(controller.behavior(), "GMCP", "login"));

        PersonaAttachment avatar = attachment("avatar");
        sessions.attach("one", avatar);

        assertSame(avatar.behavior(), session.personaObject());
        assertTrue(sessions.sessionFor(controller.behavior()).isEmpty());
        assertSame(session, sessions.sessionFor(avatar.behavior()).orElseThrow());
        assertTrue(sessions.isProtocolEnabled(avatar.behavior(), "gmcp"));
        assertTrue(sessions.sendProtocolMessage(avatar.behavior(), "GMCP", "play"));
        assertEquals(List.of("GMCP:login", "GMCP:play"), packets);
        assertTrue(sessions.personaRecordForProjection(controller.behavior()).orElseThrow()
                .controllingPlayerId().isEmpty());
    }

    @Test
    void capturedInputBelongsToCurrentBehaviorAndASuccessorIsNotClearedByItsPredecessor() {
        sessions.open("one", null, ignored -> {});
        PersonaAttachment first = attachment("first");
        sessions.attach("one", first);
        Object pager = new Object();
        sessions.captureInput(first.behavior(), pager, "page", true, "chapter");
        InstanceSessions.CapturedInput initial = sessions.pendingInput(first.behavior()).orElseThrow();
        sessions.attach("one", first.withProfile(new ManagedPersonaProfile("one", "One", "", Map.of())));
        assertSame(initial, sessions.pendingInput(first.behavior()).orElseThrow(), "Profile changes retain input capture");
        sessions.captureInput(first.behavior(), pager, "next_page", false);
        InstanceSession session = sessions.session("one").orElseThrow();
        sessions.completeInput(session, initial);
        assertEquals("next_page", sessions.pendingInput(first.behavior()).orElseThrow().methodName());

        PersonaAttachment second = attachment("second");
        sessions.attach("one", second);
        assertTrue(sessions.pendingInput(second.behavior()).isEmpty(), "Old behavior cannot intercept the new Persona's input");
        assertTrue(sessions.pendingInput(first.behavior()).isEmpty());
    }

    @Test
    void privateBufferedOutputFollowsTheSelectedBehaviorAndDisconnectDropsLiveRoutes() {
        StringBuilder firstOutput = new StringBuilder();
        StringBuilder secondOutput = new StringBuilder();
        InstanceSession first = sessions.open("one", null, firstOutput::append);
        sessions.open("two", null, secondOutput::append);
        PersonaAttachment hero = attachment("hero");
        PersonaAttachment other = attachment("other");
        sessions.bufferOutput(hero.behavior(), "private greeting");
        sessions.attach("two", other);
        assertEquals("", firstOutput.toString());
        assertEquals("", secondOutput.toString());
        sessions.attach("one", hero);
        assertEquals("private greeting", firstOutput.toString());
        assertEquals("", secondOutput.toString());
        sessions.captureInput(hero.behavior(), hero.behavior(), "password", true);

        sessions.close("one");

        assertFalse(first.isOpen());
        assertTrue(first.attachment().isEmpty());
        assertFalse(sessions.writeToSession(first.id(), "late output"));
        assertFalse(sessions.isInteractive(hero.behavior()));
        assertTrue(sessions.pendingInput(hero.behavior()).isEmpty());
        assertTrue(sessions.personaRecordForProjection(hero.behavior()).orElseThrow().controllingPlayerId().isEmpty());
        assertEquals(List.of(other.behavior()), sessions.users());
    }

    @Test
    void controllerHandoffIsCommittedBeforeEitherAttachmentListenerRuns() {
        InstanceSession former = sessions.open("one", null, ignored -> {});
        InstanceSession next = sessions.open("two", null, ignored -> {});
        PersonaAttachment hero = attachment("hero");
        sessions.attach("one", hero);
        List<InstanceSessions.AttachmentChange> changes = new ArrayList<>();
        sessions.setAttachmentListener(change -> {
            assertSame(next, sessions.sessionFor(hero.behavior()).orElseThrow());
            assertFalse(former.isOpen());
            changes.add(change);
        });

        sessions.attach("two", hero);

        assertEquals(2, changes.size());
        assertTrue(changes.get(0).current().isEmpty());
        assertEquals(next.player().orElseThrow().id(), next.attachment().orElseThrow()
                .personaRecord().controllingPlayerId().orElseThrow());
    }

    @Test
    void destructionDetachesTheConnectionWithoutClaimingTheDestructorsWorldCleanup() {
        InstanceSession connection = sessions.open("one", null, ignored -> {});
        PersonaAttachment hero = attachment("hero");
        sessions.attach("one", hero);
        List<InstanceSessions.AttachmentChange> changes = new ArrayList<>();
        sessions.setAttachmentListener(changes::add);

        sessions.discardObject(hero.behavior());

        assertFalse(connection.isOpen());
        assertEquals(InstanceSessions.AttachmentChange.Cause.BEHAVIOR_DESTROYED, changes.getFirst().cause());
        assertSame(hero.behavior(), changes.getFirst().previous().orElseThrow().behavior());
        assertTrue(changes.getFirst().current().isEmpty());
    }

    @Test
    void staleSessionHandleCannotObserveAReplacementWithTheSameExternalId() {
        InstanceSession old = sessions.open("reused", "old address", ignored -> {});
        sessions.close("reused");
        InstanceSession replacement = sessions.open("reused", "new address", ignored -> {});
        sessions.attach("reused", attachment("replacement"));

        assertNotSame(old, replacement);
        assertFalse(old.isOpen());
        assertTrue(old.record().isEmpty());
        assertTrue(old.player().isEmpty());
        assertTrue(old.login().isEmpty());
        assertTrue(old.attachment().isEmpty());
        assertTrue(replacement.isOpen());
    }

    @Test
    void activityClockCoversLoginAndPlayWithoutChangingConnectionTime() {
        MutableClock clock = new MutableClock();
        InstanceSessions timed = new InstanceSessions(Object::toString, ignored -> {}, clock);
        InstanceSession connection = timed.open("timed", null, ignored -> {});
        Instant connectedAt = connection.record().orElseThrow().connectedAt();
        clock.now = clock.now.plusSeconds(7);
        timed.touchSession("timed");
        assertEquals(clock.instant(), connection.record().orElseThrow().lastActivityAt());
        PersonaAttachment hero = attachment("timed-hero");
        timed.attach("timed", hero);
        clock.now = clock.now.plusSeconds(9);
        assertEquals(9, timed.idleSeconds(hero.behavior()));
        timed.touch(hero.behavior());
        assertEquals(0, timed.idleSeconds(hero.behavior()));
        assertEquals(connectedAt, connection.record().orElseThrow().connectedAt());
    }

    @Test
    void attachmentRequiresARealEntityAndCapturedArgumentsAreDefensive() {
        Object behavior = new Object();
        PersonaRecord missingEntity = new PersonaRecord(sessions.personaId(behavior));
        assertThrows(IllegalArgumentException.class, () -> new PersonaAttachment(missingEntity, "missing", behavior));
        Object[] arguments = {"original"};
        InstanceSessions.CapturedInput input = new InstanceSessions.CapturedInput(behavior, "reply", true, arguments);
        arguments[0] = "changed";
        Object[] exposed = input.extraArgs();
        exposed[0] = "changed again";
        assertArrayEquals(new Object[] {"original"}, input.extraArgs());
    }

    private PersonaAttachment attachment(String name) {
        Object behavior = new NamedBehavior(name);
        var entity = world.createDetachedEntity("entity/" + name, name, Capability.ACTOR);
        PersonaRecord persona = new PersonaRecord(sessions.personaId(behavior), Optional.of(entity), Optional.empty(),
                Optional.of(behavior));
        return new PersonaAttachment(persona, name, behavior);
    }

    private record NamedBehavior(String name) { @Override public String toString() { return name; } }

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-11-19T12:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    private static final class WaitingLogin implements ManagedLoginSession {
        @Override public void start() {}
        @Override public boolean noEcho() { return false; }
        @Override public ManagedLoginResult handle(String line) { return ManagedLoginResult.continueLogin(); }
    }
}
