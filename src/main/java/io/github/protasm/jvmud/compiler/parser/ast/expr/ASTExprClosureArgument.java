package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;

public final class ASTExprClosureArgument extends ASTExpression {
    private final int index;

    public ASTExprClosureArgument(int line, int index) {
        super(line);
        this.index = index;
    }

    public int index() {
        return index;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
