package io.github.protasm.jvmud.engine;

/** Stable engine identifier for the human or account-like controller. */
public record PlayerId(String value) {
    public PlayerId {
        value = RuntimeModel.requireIdentifier(value, "value");
    }

    @Override
    public String toString() {
        return value;
    }
}
