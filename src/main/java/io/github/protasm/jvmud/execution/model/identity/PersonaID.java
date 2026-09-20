package io.github.protasm.jvmud.execution.model.identity;

import io.github.protasm.jvmud.execution.model.support.RuntimeModel;

/** Stable engine identifier for a Player's in-World manifestation. */
public record PersonaID(String value) {
    public PersonaID {
        value = RuntimeModel.requireIdentifier(value, "value");
    }

    @Override
    public String toString() {
        return value;
    }
}
