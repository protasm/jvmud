package io.github.protasm.jvmud.engine.identity;

import io.github.protasm.jvmud.engine.support.RuntimeModel;

/** Stable engine identifier for one active or resumable connection context. */
public record SessionID(String value) {
    public SessionID {
        value = RuntimeModel.requireIdentifier(value, "value");
    }

    @Override
    public String toString() {
        return value;
    }
}
