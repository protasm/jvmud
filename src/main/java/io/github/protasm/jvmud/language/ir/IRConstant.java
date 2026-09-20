package io.github.protasm.jvmud.language.ir;

import io.github.protasm.jvmud.language.runtime.RuntimeType;
import java.util.Objects;

public record IRConstant(int line, Object value, RuntimeType type) implements IRExpression {
    public IRConstant {
        Objects.requireNonNull(type, "type");
    }
}
