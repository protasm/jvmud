package io.github.protasm.jvmud.language.ir;

import io.github.protasm.jvmud.language.runtime.RuntimeType;
import java.util.Objects;

public record IRForeachSize(int line, IRExpression source, RuntimeType type) implements IRExpression {
    public IRForeachSize {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(type, "type");
    }
}
