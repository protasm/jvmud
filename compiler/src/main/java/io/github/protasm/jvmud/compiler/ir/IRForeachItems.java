package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public record IRForeachItems(int line, IRExpression source, boolean keys, RuntimeType type) implements IRExpression {
    public IRForeachItems {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(type, "type");
    }
}
