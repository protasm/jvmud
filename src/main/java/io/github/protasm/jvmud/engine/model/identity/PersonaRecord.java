package io.github.protasm.jvmud.engine.model.identity;

import io.github.protasm.jvmud.engine.model.world.Entity;

import java.util.Objects;
import java.util.Optional;

/**
 * Engine-owned record for the Player's in-World manifestation.
 *
 * <p>Detached or not-yet-realized model records may omit the Entity. A live session attachment
 * requires the actual native Entity that supplies its world presence.</p>
 *
 * <p>The mudlib behavior projection is opaque to the runtime model. Compatibility layers may use it
 * to associate LPC-authored behavior, including a mudlib's combined player/Persona object.</p>
 */
public record PersonaRecord(
        PersonaID id,
        Optional<Entity> entity,
        Optional<PlayerID> controllingPlayerId,
        Optional<Object> mudlibBehaviorProjection) {
    public PersonaRecord {
        id = Objects.requireNonNull(id, "id");
        entity = Objects.requireNonNull(entity, "entity");
        controllingPlayerId = Objects.requireNonNull(controllingPlayerId, "controllingPlayerId");
        mudlibBehaviorProjection = Objects.requireNonNull(mudlibBehaviorProjection, "mudlibBehaviorProjection");
    }

    public PersonaRecord(PersonaID id, Entity entity) {
        this(id, Optional.of(Objects.requireNonNull(entity, "entity")), Optional.empty(), Optional.empty());
    }

    public PersonaRecord(PersonaID id) {
        this(id, Optional.empty(), Optional.empty(), Optional.empty());
    }
}
