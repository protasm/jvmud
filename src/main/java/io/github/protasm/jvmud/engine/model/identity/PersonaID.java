package io.github.protasm.jvmud.engine.model.identity;

/** Stable engine identifier for a Player's in-World manifestation. */
public record PersonaID(String value) {
    public PersonaID {
        value = IdentifierValidation.requireIdentifier(value, "value");
    }

    @Override
    public String toString() {
        return value;
    }
}
