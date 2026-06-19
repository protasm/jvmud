package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

public final class ASTExprArrayMutation extends ASTExpression {
    private final ASTExpression target;
    private final ASTExpression index;
    private final int delta;
    private final boolean prefix;

    public ASTExprArrayMutation(int line, ASTExpression target, ASTExpression index, int delta) {
        this(line, target, index, delta, false);
    }

    /** Creates an indexed numeric mutation expression for prefix or postfix syntax. */
    public ASTExprArrayMutation(int line, ASTExpression target, ASTExpression index, int delta, boolean prefix) {
        super(line);
        this.target = Objects.requireNonNull(target, "target");
        this.index = Objects.requireNonNull(index, "index");
        this.delta = delta;
        this.prefix = prefix;
    }

    public ASTExpression target() {
        return target;
    }

    public ASTExpression index() {
        return index;
    }

    public int delta() {
        return delta;
    }

    /** Returns true when this mutation came from prefix syntax and should evaluate to the updated value. */
    public boolean isPrefix() {
        return prefix;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
