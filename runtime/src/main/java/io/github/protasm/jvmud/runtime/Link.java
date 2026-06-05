package io.github.protasm.jvmud.runtime;

import java.util.Objects;

/** A navigable relationship from one place to another place. Entities are not link endpoints. */
public record Link(String action, Place origin, Place destination, boolean visible) {
    public Link {
        action = requireAction(action);
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(destination, "destination");
    }

    private static String requireAction(String value) {
        Objects.requireNonNull(value, "action");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("action must not be blank.");
        }
        return trimmed;
    }
}
