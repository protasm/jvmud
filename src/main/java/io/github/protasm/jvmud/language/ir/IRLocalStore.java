package io.github.protasm.jvmud.language.ir;

import io.github.protasm.jvmud.language.runtime.RuntimeType;
import java.util.Objects;

public record IRLocalStore(int line, IRLocal local, IRExpression value) implements IRExpression {
    public IRLocalStore {
        Objects.requireNonNull(local, "local");
        Objects.requireNonNull(value, "value");
    }

    @Override
    public RuntimeType type() {
        return local.type();
    }
}
