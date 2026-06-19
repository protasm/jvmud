package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import java.util.List;

public final class ASTExprSequence extends ASTExpression {
    private final List<ASTExpression> expressions;

    public ASTExprSequence(int line, List<ASTExpression> expressions) {
        super(line);
        this.expressions = List.copyOf(expressions);
    }

    public List<ASTExpression> expressions() {
        return expressions;
    }

    @Override
    public LPCType lpcType() {
        if (expressions.isEmpty())
            return LPCType.LPCNULL;

        return expressions.get(expressions.size() - 1).lpcType();
    }
}
