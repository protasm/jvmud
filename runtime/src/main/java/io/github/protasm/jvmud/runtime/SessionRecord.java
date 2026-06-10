package io.github.protasm.jvmud.runtime;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Engine-owned record for a connection and transport context.
 *
 * <p>A Session is bound to a Player as soon as the connection is known. It may remain unattached to
 * any Persona during login, reconnect, or character selection.</p>
 */
public record SessionRecord(
        SessionId id,
        PlayerId playerId,
        Optional<String> remoteAddress,
        Instant connectedAt,
        Instant lastActivityAt,
        Optional<PersonaId> attachedPersonaId) {
    public SessionRecord {
        id = Objects.requireNonNull(id, "id");
        playerId = Objects.requireNonNull(playerId, "playerId");
        remoteAddress = Objects.requireNonNull(remoteAddress, "remoteAddress")
                .map(address -> RuntimeModel.requireIdentifier(address, "remoteAddress"));
        connectedAt = Objects.requireNonNull(connectedAt, "connectedAt");
        lastActivityAt = Objects.requireNonNull(lastActivityAt, "lastActivityAt");
        attachedPersonaId = Objects.requireNonNull(attachedPersonaId, "attachedPersonaId");
    }

    public SessionRecord(SessionId id, PlayerId playerId, Optional<String> remoteAddress, Instant connectedAt) {
        this(id, playerId, remoteAddress, connectedAt, connectedAt, Optional.empty());
    }
}
