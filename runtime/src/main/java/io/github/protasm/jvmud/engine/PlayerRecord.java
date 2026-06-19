package io.github.protasm.jvmud.engine;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Engine-owned record for the human or account-like controller.
 *
 * <p>The mudlib profile projection is opaque to the runtime model. Compatibility layers may use it
 * to associate mudlib-authored profile state without making that state part of the engine ontology.</p>
 */
public record PlayerRecord(
        PlayerId id,
        Set<SessionId> activeSessionIds,
        Optional<PersonaId> activePersonaId,
        Optional<Object> mudlibProfileProjection) {
    public PlayerRecord {
        id = Objects.requireNonNull(id, "id");
        activeSessionIds = Set.copyOf(Objects.requireNonNull(activeSessionIds, "activeSessionIds"));
        activePersonaId = Objects.requireNonNull(activePersonaId, "activePersonaId");
        mudlibProfileProjection = Objects.requireNonNull(mudlibProfileProjection, "mudlibProfileProjection");
    }

    public PlayerRecord(PlayerId id) {
        this(id, Set.of(), Optional.empty(), Optional.empty());
    }
}
