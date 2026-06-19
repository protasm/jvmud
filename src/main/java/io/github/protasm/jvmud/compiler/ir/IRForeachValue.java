package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public record IRForeachValue(int line, IRExpression source, IRExpression key, RuntimeType type) implements IRExpression {
    public IRForeachValue {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
    }
}
