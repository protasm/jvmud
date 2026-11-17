package io.github.protasm.jvmud.engine.model.identity;

/** Stable engine identifier for one active or resumable connection context. */
public record SessionID(String value) {
    public SessionID {
        value = IdentifierValidation.requireIdentifier(value, "value");
    }

    @Override
    public String toString() {
        return value;
    }
}
