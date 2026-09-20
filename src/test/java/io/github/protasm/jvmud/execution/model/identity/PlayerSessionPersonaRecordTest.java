package io.github.protasm.jvmud.execution.model.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.execution.model.identity.PersonaID;
import io.github.protasm.jvmud.execution.model.identity.PersonaRecord;
import io.github.protasm.jvmud.execution.model.identity.PlayerID;
import io.github.protasm.jvmud.execution.model.identity.PlayerRecord;
import io.github.protasm.jvmud.execution.model.identity.SessionID;
import io.github.protasm.jvmud.execution.model.identity.SessionRecord;
import io.github.protasm.jvmud.execution.model.mudlib.MudlibProjection;
import io.github.protasm.jvmud.execution.model.mudlib.MudlibProjectionRole;
import io.github.protasm.jvmud.execution.model.world.Capability;
import io.github.protasm.jvmud.execution.model.world.Entity;
import io.github.protasm.jvmud.execution.model.world.Place;
import io.github.protasm.jvmud.execution.model.world.World;
import io.github.protasm.jvmud.execution.model.world.WorldRuntime;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class PlayerSessionPersonaRecordTest {
    @Test
    void identifiersTrimAndRejectBlankValues() {
        assertEquals("alice", new PlayerID(" alice ").value());
        assertEquals("session/1", new SessionID(" session/1 ").value());
        assertEquals("persona/alice", new PersonaID(" persona/alice ").value());

        assertThrows(IllegalArgumentException.class, () -> new PlayerID(" "));
        assertThrows(IllegalArgumentException.class, () -> new SessionID(""));
        assertThrows(IllegalArgumentException.class, () -> new PersonaID("\t"));
    }

    @Test
    void playerRecordKeepsEngineStateSeparateFromMudlibProfileProjection() {
        PlayerID playerId = new PlayerID("player/alice");
        PersonaID personaId = new PersonaID("persona/alice");
        Object profileProjection = new Object();
        Set<SessionID> sessions = new HashSet<>();
        sessions.add(new SessionID("session/1"));

        PlayerRecord player = new PlayerRecord(
                playerId,
                sessions,
                Optional.of(personaId),
                Optional.of(profileProjection));
        sessions.add(new SessionID("session/2"));

        assertEquals(playerId, player.id());
        assertEquals(Set.of(new SessionID("session/1")), player.activeSessionIds());
        assertEquals(Optional.of(personaId), player.activePersonaId());
        assertEquals(Optional.of(profileProjection), player.mudlibProfileProjection());
    }

    @Test
    void sessionRecordCanExistBeforePersonaAttachment() {
        Instant connectedAt = Instant.parse("2026-06-10T12:00:00Z");

        SessionRecord session = new SessionRecord(
                new SessionID("session/1"),
                new PlayerID("player/alice"),
                Optional.of(" 127.0.0.1 "),
                connectedAt);

        assertEquals(Optional.of("127.0.0.1"), session.remoteAddress());
        assertEquals(connectedAt, session.connectedAt());
        assertEquals(connectedAt, session.lastActivityAt());
        assertTrue(session.attachedPersonaId().isEmpty());
    }

    @Test
    void personaRecordKeepsEntityAndMudlibBehaviorProjectionSeparate() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place start = runtime.createPlace("place/start", "Start");
        Entity entity = runtime.createEntity("entity/alice", "Alice", start, Capability.ACTOR);
        PersonaID personaId = new PersonaID("persona/alice");
        PlayerID playerId = new PlayerID("player/alice");
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
        PersonaRecord persona = new PersonaRecord(new PersonaID("persona/logon"));

        assertTrue(persona.entity().isEmpty());
        assertTrue(persona.controllingPlayerId().isEmpty());
        assertTrue(persona.mudlibBehaviorProjection().isEmpty());
    }

    @Test
    void mudlibProjectionCanDescribeCombinedLegacyPlayerObject() {
        Object playerObject = new Object();

        MudlibProjection projection = MudlibProjection.combinedPlayerPersona("obj/player", playerObject);

        assertEquals("obj/player", projection.sourcePath());
        assertEquals(playerObject, projection.object());
        assertTrue(projection.hasRole(MudlibProjectionRole.PLAYER_PROFILE));
        assertTrue(projection.hasRole(MudlibProjectionRole.PERSONA_BEHAVIOR));
        assertTrue(projection.hasRole(MudlibProjectionRole.COMBINED_PLAYER_PERSONA));
    }
}
