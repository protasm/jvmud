package io.github.protasm.jvmud.instance;

import java.util.Map;
import java.util.Objects;

/** Mudlib-neutral Persona information resolved by an optional host-managed session policy. */
record ManagedPersonaProfile(
        String externalUserId,
        String displayName,
        String gender,
        Map<String, Object> attributes) {
    ManagedPersonaProfile {
        externalUserId = Objects.requireNonNull(externalUserId, "externalUserId");
        displayName = Objects.requireNonNull(displayName, "displayName");
        gender = Objects.requireNonNullElse(gender, "");
        attributes = Map.copyOf(Objects.requireNonNullElse(attributes, Map.of()));
    }
}
