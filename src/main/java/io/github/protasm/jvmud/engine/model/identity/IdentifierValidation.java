package io.github.protasm.jvmud.engine.model.identity;

import java.util.Objects;

/** Shared validation helpers for engine model records. */
public final class IdentifierValidation {
    private IdentifierValidation() {
    }

    public static String requireIdentifier(String value, String name) {
        Objects.requireNonNull(value, name);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return trimmed;
    }
}
