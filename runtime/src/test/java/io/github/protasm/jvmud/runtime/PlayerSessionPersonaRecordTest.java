package io.github.protasm.jvmud.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class PlayerSessionPersonaRecordTest {
    @Test
    void identifiersTrimAndRejectBlankValues() {
        assertEquals("alice", new PlayerId(" alice ").value());
        assertEquals("session/1", new SessionId(" session/1 ").value());
        assertEquals("persona/alice", new PersonaId(" persona/alice ").value());

        assertThrows(IllegalArgumentException.class, () -> new PlayerId(" "));
        assertThrows(IllegalArgumentException.class, () -> new SessionId(""));
        assertThrows(IllegalArgumentException.class, () -> new PersonaId("\t"));
    }

    @Test
    void playerRecordKeepsEngineStateSeparateFromMudlibProfileProjection() {
        PlayerId playerId = new PlayerId("player/alice");
        PersonaId personaId = new PersonaId("persona/alice");
        Object profileProjection = new Object();
        Set<SessionId> sessions = new HashSet<>();
        sessions.add(new SessionId("session/1"));

        PlayerRecord player = new PlayerRecord(
                playerId,
                sessions,
                Optional.of(personaId),
                Optional.of(profileProjection));
        sessions.add(new SessionId("session/2"));

        assertEquals(playerId, player.id());
        assertEquals(Set.of(new SessionId("session/1")), player.activeSessionIds());
        assertEquals(Optional.of(personaId), player.activePersonaId());
        assertEquals(Optional.of(profileProjection), player.mudlibProfileProjection());
    }

    @Test
    void sessionRecordCanExistBeforePersonaAttachment() {
        Instant connectedAt = Instant.parse("2026-06-10T12:00:00Z");

        SessionRecord session = new SessionRecord(
                new SessionId("session/1"),
                new PlayerId("player/alice"),
                Optional.of(" 127.0.0.1 "),
                connectedAt);

        assertEquals(Optional.of("127.0.0.1"), session.remoteAddress());
        assertEquals(connectedAt, session.connectedAt());
        assertEquals(connectedAt, session.lastActivityAt());
        assertTrue(session.attachedPersonaId().isEmpty());
    }

    @Test
    void personaRecordKeepsEntityAndMudlibBehaviorProjectionSeparate() {
        Entity entity = new Entity("entity/alice", "Alice", Set.of(Capability.ACTOR));
        PersonaId personaId = new PersonaId("persona/alice");
        PlayerId playerId = new PlayerId("player/alice");
        Object behaviorProjection = new Object();

        PersonaRecord persona = new PersonaRecord(
                personaId,
                Optional.of(entity),
                Optional.of(playerId),
                Optional.of(behaviorProjection));

        assertEquals(personaId, persona.id());
        assertEquals(Optional.of(entity), persona.entity());
        assertEquals(Optional.of(playerId), persona.controllingPlayerId());
        assertEquals(Optional.of(behaviorProjection), persona.mudlibBehaviorProjection());
    }

    @Test
    void personaRecordCanRepresentCompatibilityProjectionBeforeEntityAttachment() {
        PersonaRecord persona = new PersonaRecord(new PersonaId("persona/logon"));

        assertTrue(persona.entity().isEmpty());
        assertTrue(persona.controllingPlayerId().isEmpty());
        assertTrue(persona.mudlibBehaviorProjection().isEmpty());
    }
}
