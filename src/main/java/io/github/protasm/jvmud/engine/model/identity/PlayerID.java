package io.github.protasm.jvmud.engine.model.identity;

/** Stable engine identifier for the human-facing controller. */
public record PlayerID(String value) {
    public PlayerID {
        value = IdentifierValidation.requireIdentifier(value, "value");
    }

    @Override
    public String toString() {
        return value;
    }
}
