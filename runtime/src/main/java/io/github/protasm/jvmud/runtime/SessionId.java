package io.github.protasm.jvmud.runtime;

/** Stable engine identifier for one active or resumable connection context. */
public record SessionId(String value) {
    public SessionId {
        value = RuntimeModel.requireIdentifier(value, "value");
    }

    @Override
    public String toString() {
        return value;
    }
}
