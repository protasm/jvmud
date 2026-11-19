package io.github.protasm.jvmud.engine.instance.session;

import io.github.protasm.jvmud.engine.instance.session.login.ManagedPersonaProfile;
import io.github.protasm.jvmud.engine.model.identity.PersonaRecord;
import io.github.protasm.jvmud.engine.model.world.Entity;

import java.util.Objects;
import java.util.Optional;

/**
 * The selected Persona, its native Entity and its authored behavior for one live session.
 *
 * <p>A connection can exist without this value during login. Once attached, the Persona has an
 * actual Entity in the instance's authoritative world; an LPC login object is never substituted
 * for the connection itself.</p>
 *
 * @param personaRecord engine identity and current controller relationship
 * @param objectId canonical authored object identity
 * @param behavior opaque authored behavior attached to the native Entity
 * @param profile optional neutral profile supplied by a managed login policy
 */
public record PersonaAttachment(
        PersonaRecord personaRecord,
        String objectId,
        Object behavior,
        Optional<ManagedPersonaProfile> profile) {
    /** Creates an attachment whose presentation is supplied by the authored object and Entity. */
    public PersonaAttachment(PersonaRecord personaRecord, String objectId, Object behavior) {
        this(personaRecord, objectId, behavior, Optional.empty());
    }

    /** Validates the complete Persona-to-Entity relationship at the attachment boundary. */
    public PersonaAttachment {
        Objects.requireNonNull(personaRecord, "personaRecord");
        Objects.requireNonNull(objectId, "objectId");
        Objects.requireNonNull(behavior, "behavior");
        profile = Objects.requireNonNull(profile, "profile");
        if (personaRecord.entity().isEmpty()) {
            throw new IllegalArgumentException("An attached Persona requires a native Entity.");
        }
        if (objectId.isBlank()) {
            throw new IllegalArgumentException("An attached Persona requires an authored object id.");
        }
    }

    /** Returns the actual Entity used by the world's containment and perception model. */
    public Entity entity() {
        return personaRecord.entity().orElseThrow();
    }

    /** Returns the authored display name when supplied by a managed login policy. */
    public String displayName() {
        return profile.map(ManagedPersonaProfile::displayName).orElseGet(() -> entity().displayName());
    }

    /** Returns the authored account identity, falling back to the object's canonical id. */
    public String userId() {
        return profile.map(ManagedPersonaProfile::externalUserId).orElse(objectId);
    }

    /** Returns optional authored gender text without making it an engine identity concept. */
    public String gender() {
        return profile.map(ManagedPersonaProfile::gender).orElse("");
    }

    /** Adds neutral login presentation without changing Persona, Entity or behavior identity. */
    public PersonaAttachment withProfile(ManagedPersonaProfile profile) {
        return new PersonaAttachment(personaRecord, objectId, behavior, Optional.of(profile));
    }
}
