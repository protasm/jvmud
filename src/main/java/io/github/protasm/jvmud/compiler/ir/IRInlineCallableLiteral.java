package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

/**
 * IR value for an LPC inline callable literal, such as {@code (: $1 > 0 :)}.
 *
 * <p>The body is lowered with synthetic callback argument locals so callable invocation can
 * execute expression and LDMud statement-block bodies through the same IR/bytecode machinery used
 * by existing special-form callbacks.</p>
 */
public record IRInlineCallableLiteral(
        int line,
        IRExpression body,
        List<IRBlock> blocks,
        int arity,
        List<IRLocal> argumentLocals,
        List<IRLocal> captureLocals,
        List<IRLocal> helperLocals,
        RuntimeType type)
        implements IRExpression {
    public IRInlineCallableLiteral(
            int line,
            IRExpression body,
            int arity,
            List<IRLocal> argumentLocals,
            List<IRLocal> captureLocals,
            RuntimeType type) {
        this(line, body, List.of(), arity, argumentLocals, captureLocals, List.of(), type);
    }

    public IRInlineCallableLiteral {
        if (body == null && (blocks == null || blocks.isEmpty()))
            throw new IllegalArgumentException("inline callable requires expression body or block body");
        blocks = List.copyOf(blocks != null ? blocks : List.of());
        argumentLocals = List.copyOf(argumentLocals);
        captureLocals = List.copyOf(captureLocals);
        helperLocals = List.copyOf(helperLocals != null ? helperLocals : List.of());
        Objects.requireNonNull(type, "type");
        if (arity < 0)
            throw new IllegalArgumentException("arity must be non-negative");
    }

    public boolean hasBlockBody() {
        return !blocks.isEmpty();
    }
}
