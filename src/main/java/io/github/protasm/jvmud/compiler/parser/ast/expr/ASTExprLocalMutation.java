package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTLocal;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

/** Bound local increment/decrement expression with prefix or postfix result semantics. */
public final class ASTExprLocalMutation extends ASTExpression {
    private final ASTLocal local;
    private final int delta;
    private final boolean prefix;

    public ASTExprLocalMutation(int line, ASTLocal local, int delta, boolean prefix) {
        super(line);
        this.local = Objects.requireNonNull(local, "local");
        this.delta = delta;
        this.prefix = prefix;
    }

    public ASTLocal local() {
        return local;
    }

    public int delta() {
        return delta;
    }

    /** Returns true when the expression should evaluate to the updated local value. */
    public boolean isPrefix() {
        return prefix;
    }

    @Override
    public LPCType lpcType() {
        return local.symbol().lpcType();
    }
}
