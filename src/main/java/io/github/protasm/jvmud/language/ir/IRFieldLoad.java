package io.github.protasm.jvmud.language.ir;

import io.github.protasm.jvmud.language.runtime.RuntimeType;
import java.util.Objects;

public record IRFieldLoad(int line, IRField field) implements IRExpression {
    public IRFieldLoad {
        Objects.requireNonNull(field, "field");
    }

    @Override
    public RuntimeType type() {
        return field.type();
    }
}
