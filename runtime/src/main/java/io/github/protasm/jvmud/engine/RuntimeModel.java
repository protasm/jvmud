package io.github.protasm.jvmud.engine;

import java.util.Objects;

final class RuntimeModel {
    private RuntimeModel() {
    }

    static String requireIdentifier(String value, String name) {
        Objects.requireNonNull(value, name);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return trimmed;
    }
}
