package io.github.protasm.jvmud.engine.identity;

import io.github.protasm.jvmud.engine.support.RuntimeModel;

/** Stable engine identifier for the human or account-like controller. */
public record PlayerID(String value) {
    public PlayerID {
        value = RuntimeModel.requireIdentifier(value, "value");
    }

    @Override
    public String toString() {
        return value;
    }
}
