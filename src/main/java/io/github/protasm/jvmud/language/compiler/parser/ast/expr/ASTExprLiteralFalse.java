package io.github.protasm.jvmud.language.compiler.parser.ast.expr;

import io.github.protasm.jvmud.language.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.language.compiler.type.LPCType;

public final class ASTExprLiteralFalse extends ASTExpression {
    public ASTExprLiteralFalse(int line) {
        super(line);
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCSTATUS;
    }
}
