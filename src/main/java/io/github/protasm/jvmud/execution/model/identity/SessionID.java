package io.github.protasm.jvmud.execution.model.identity;

import io.github.protasm.jvmud.execution.model.support.RuntimeModel;

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
