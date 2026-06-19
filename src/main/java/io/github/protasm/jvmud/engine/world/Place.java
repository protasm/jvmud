package io.github.protasm.jvmud.engine.world;

import java.util.Objects;

/** A discrete occupiable location in a JVMud world. */
public record Place(String id, String displayName) implements Location {
    public Place {
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
