package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public final class IRSliceSet implements IRExpression {
    private final int line;
    private final IRExpression target;
    private final IRExpression start;
    private final IRExpression end;
    private final IRExpression value;
    private final RuntimeType type;

    public IRSliceSet(
            int line, IRExpression target, IRExpression start, IRExpression end, IRExpression value, RuntimeType type) {
        this.line = line;
        this.target = Objects.requireNonNull(target, "target");
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        this.value = Objects.requireNonNull(value, "value");
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public int line() {
        return line;
    }

    public IRExpression target() {
        return target;
    }

    public IRExpression start() {
        return start;
    }

    public IRExpression end() {
        return end;
    }

    public IRExpression value() {
        return value;
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
