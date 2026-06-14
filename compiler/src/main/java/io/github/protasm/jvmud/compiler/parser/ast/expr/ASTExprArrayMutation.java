package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.Objects;

public final class ASTExprArrayMutation extends ASTExpression {
    private final ASTExpression target;
    private final ASTExpression index;
    private final int delta;

    public ASTExprArrayMutation(int line, ASTExpression target, ASTExpression index, int delta) {
        super(line);
        this.target = Objects.requireNonNull(target, "target");
        this.index = Objects.requireNonNull(index, "index");
        this.delta = delta;
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

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
