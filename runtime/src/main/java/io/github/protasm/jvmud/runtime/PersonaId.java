package io.github.protasm.jvmud.runtime;

/** Stable engine identifier for a Player's in-World manifestation. */
public record PersonaId(String value) {
    public PersonaId {
        value = RuntimeModel.requireIdentifier(value, "value");
    }

    @Override
    public String toString() {
        return value;
    }
}
