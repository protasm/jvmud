package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

/** Numeric field increment/decrement expression with prefix or postfix result semantics. */
public record IRFieldMutation(int line, IRField field, int delta, boolean prefix) implements IRExpression {
    public IRFieldMutation {
        Objects.requireNonNull(field, "field");
    }

    /** Returns true when the expression should evaluate to the updated field value. */
    public boolean isPrefix() {
        return prefix;
    }

    @Override
    public RuntimeType type() {
        return field.type();
    }
}
