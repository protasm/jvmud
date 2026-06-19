package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public record IRCoerce(int line, IRExpression value, RuntimeType targetType) implements IRExpression {
    public IRCoerce {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(targetType, "targetType");
    }

    @Override
    public RuntimeType type() {
        return targetType;
    }
}
