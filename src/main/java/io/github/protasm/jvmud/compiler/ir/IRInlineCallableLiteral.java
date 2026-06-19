package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

/**
 * IR value for an LPC inline callable literal, such as {@code (: $1 > 0 :)}.
 *
 * <p>The body is lowered with synthetic callback argument locals so later callable invocation
 * support can execute the expression through the same IR/bytecode machinery used by existing
 * special-form callbacks.</p>
 */
public record IRInlineCallableLiteral(
        int line, IRExpression body, int arity, List<IRLocal> argumentLocals, RuntimeType type)
        implements IRExpression {
    public IRInlineCallableLiteral {
        Objects.requireNonNull(body, "body");
        argumentLocals = List.copyOf(argumentLocals);
        Objects.requireNonNull(type, "type");
        if (arity < 0)
            throw new IllegalArgumentException("arity must be non-negative");
    }
}
