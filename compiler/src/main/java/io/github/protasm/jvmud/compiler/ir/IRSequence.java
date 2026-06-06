package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

public final class IRSequence implements IRExpression {
    private final int line;
    private final List<IRExpression> expressions;
    private final RuntimeType type;

    public IRSequence(int line, List<IRExpression> expressions, RuntimeType type) {
        this.line = line;
        this.expressions = List.copyOf(expressions);
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public int line() {
        return line;
    }

    public List<IRExpression> expressions() {
        return expressions;
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
