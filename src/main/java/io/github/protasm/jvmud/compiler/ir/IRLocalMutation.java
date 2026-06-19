package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

/** Numeric local increment/decrement expression with prefix or postfix result semantics. */
public record IRLocalMutation(int line, IRLocal local, int delta, boolean prefix) implements IRExpression {
    public IRLocalMutation {
        Objects.requireNonNull(local, "local");
    }

    /** Returns true when the expression should evaluate to the updated local value. */
    public boolean isPrefix() {
        return prefix;
    }

    @Override
    public RuntimeType type() {
        return local.type();
    }
}
