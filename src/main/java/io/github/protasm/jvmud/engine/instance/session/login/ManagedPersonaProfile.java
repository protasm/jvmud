package io.github.protasm.jvmud.engine.instance.session.login;

import java.util.Map;
import java.util.Objects;

/** Mudlib-neutral Persona information resolved by an optional host-managed session policy. */
public record ManagedPersonaProfile(
        String externalUserId,
        String displayName,
        String gender,
        Map<String, Object> attributes) {
    public ManagedPersonaProfile {
        externalUserId = Objects.requireNonNull(externalUserId, "externalUserId");
        displayName = Objects.requireNonNull(displayName, "displayName");
        gender = Objects.requireNonNullElse(gender, "");
        attributes = Map.copyOf(Objects.requireNonNullElse(attributes, Map.of()));
    }
}
