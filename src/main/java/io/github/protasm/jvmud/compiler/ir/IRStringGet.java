package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public final class IRStringGet implements IRExpression {
    private final int line;
    private final IRExpression string;
    private final IRExpression index;
    private final RuntimeType type;

    public IRStringGet(int line, IRExpression string, IRExpression index, RuntimeType type) {
        this.line = line;
        this.string = Objects.requireNonNull(string, "string");
        this.index = Objects.requireNonNull(index, "index");
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public int line() {
        return line;
    }

    public IRExpression string() {
        return string;
    }

    public IRExpression index() {
        return index;
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
