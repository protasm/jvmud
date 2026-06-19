package io.github.protasm.jvmud.engine;

import java.util.Objects;
import java.util.Optional;

/**
 * Engine-owned record for the Player's in-World manifestation.
 *
 * <p>The entity link may be absent while a compatibility runtime is still attaching a legacy
 * mudlib object before full World entity integration.</p>
 *
 * <p>The mudlib behavior projection is opaque to the runtime model. Compatibility layers may use it
 * to associate LPC-authored behavior, such as LP245's combined player object.</p>
 */
public record PersonaRecord(
        PersonaId id,
        Optional<Entity> entity,
        Optional<PlayerId> controllingPlayerId,
        Optional<Object> mudlibBehaviorProjection) {
    public PersonaRecord {
        id = Objects.requireNonNull(id, "id");
        entity = Objects.requireNonNull(entity, "entity");
        controllingPlayerId = Objects.requireNonNull(controllingPlayerId, "controllingPlayerId");
        mudlibBehaviorProjection = Objects.requireNonNull(mudlibBehaviorProjection, "mudlibBehaviorProjection");
    }

    public PersonaRecord(PersonaId id, Entity entity) {
        this(id, Optional.of(Objects.requireNonNull(entity, "entity")), Optional.empty(), Optional.empty());
    }

    public PersonaRecord(PersonaId id) {
        this(id, Optional.empty(), Optional.empty(), Optional.empty());
    }
}
