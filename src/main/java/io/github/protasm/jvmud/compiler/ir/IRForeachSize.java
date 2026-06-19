package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public record IRForeachSize(int line, IRExpression source, RuntimeType type) implements IRExpression {
    public IRForeachSize {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(type, "type");
    }
}
