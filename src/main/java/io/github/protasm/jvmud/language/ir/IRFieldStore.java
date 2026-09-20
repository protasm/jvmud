package io.github.protasm.jvmud.language.ir;

import io.github.protasm.jvmud.language.runtime.RuntimeType;
import java.util.Objects;

public record IRFieldStore(int line, IRField field, IRExpression value) implements IRExpression {
    public IRFieldStore {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(value, "value");
    }

    @Override
    public RuntimeType type() {
        return field.type();
    }
}
