package io.github.protasm.jvmud.runtime;

import java.util.Objects;
import java.util.Optional;

/**
 * Engine-owned record for the Player's in-World manifestation.
 *
 * <p>The mudlib behavior projection is opaque to the runtime model. Compatibility layers may use it
 * to associate LPC-authored behavior, such as LP245's combined player object.</p>
 */
public record PersonaRecord(
        PersonaId id,
        Entity entity,
        Optional<PlayerId> controllingPlayerId,
        Optional<Object> mudlibBehaviorProjection) {
    public PersonaRecord {
        id = Objects.requireNonNull(id, "id");
        entity = Objects.requireNonNull(entity, "entity");
        controllingPlayerId = Objects.requireNonNull(controllingPlayerId, "controllingPlayerId");
        mudlibBehaviorProjection = Objects.requireNonNull(mudlibBehaviorProjection, "mudlibBehaviorProjection");
    }

    public PersonaRecord(PersonaId id, Entity entity) {
        this(id, entity, Optional.empty(), Optional.empty());
    }
}
