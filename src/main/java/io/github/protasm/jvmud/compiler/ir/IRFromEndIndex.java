package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import io.github.protasm.jvmud.compiler.runtime.RuntimeTypes;
import java.util.Objects;

/** Runtime marker expression for an LPC from-end index bound such as {@code <1}. */
public final class IRFromEndIndex implements IRExpression {
    private final int line;
    private final IRExpression distance;

    public IRFromEndIndex(int line, IRExpression distance) {
        this.line = line;
        this.distance = Objects.requireNonNull(distance, "distance");
    }

    @Override
    public int line() {
        return line;
    }

    public IRExpression distance() {
        return distance;
    }

    @Override
    public RuntimeType type() {
        return RuntimeTypes.MIXED;
    }
}
