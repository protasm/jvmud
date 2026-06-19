package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public final class IRArrayMutation implements IRExpression {
    private final int line;
    private final IRExpression array;
    private final IRExpression index;
    private final int delta;
    private final boolean prefix;
    private final RuntimeType type;

    public IRArrayMutation(int line, IRExpression array, IRExpression index, int delta, RuntimeType type) {
        this(line, array, index, delta, false, type);
    }

    /** Creates an indexed numeric mutation that can return either postfix old value or prefix new value. */
    public IRArrayMutation(
            int line, IRExpression array, IRExpression index, int delta, boolean prefix, RuntimeType type) {
        this.line = line;
        this.array = Objects.requireNonNull(array, "array");
        this.index = Objects.requireNonNull(index, "index");
        this.delta = delta;
        this.prefix = prefix;
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

    /** Returns true when the expression should evaluate to the updated indexed value. */
    public boolean isPrefix() {
        return prefix;
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
