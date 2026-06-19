package io.github.protasm.jvmud.engine;

import java.util.Objects;

/** The complete virtual domain hosted by one JVMud engine instance. */
public record World(String id, String displayName) {
    public World {
        id = requireIdentifier(id, "id");
        displayName = requireIdentifier(displayName, "displayName");
    }

    private static String requireIdentifier(String value, String name) {
        Objects.requireNonNull(value, name);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return trimmed;
    }
}
