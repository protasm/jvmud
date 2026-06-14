package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public final class IRArrayMutation implements IRExpression {
    private final int line;
    private final IRExpression array;
    private final IRExpression index;
    private final int delta;
    private final RuntimeType type;

    public IRArrayMutation(int line, IRExpression array, IRExpression index, int delta, RuntimeType type) {
        this.line = line;
        this.array = Objects.requireNonNull(array, "array");
        this.index = Objects.requireNonNull(index, "index");
        this.delta = delta;
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public int line() {
        return line;
    }

    public IRExpression array() {
        return array;
    }

    public IRExpression index() {
        return index;
    }

    public int delta() {
        return delta;
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
