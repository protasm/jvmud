package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public record IRConstant(int line, Object value, RuntimeType type) implements IRExpression {
    public IRConstant {
        Objects.requireNonNull(type, "type");
    }
}
