package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTLocal;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;

public final class ASTExprLocalAccess extends ASTExpression {
    private final ASTLocal local;

    public ASTExprLocalAccess(int line, ASTLocal local) {
        super(line);

        this.local = local;
    }

    public ASTLocal local() {
        return local;
    }

    @Override
    public LPCType lpcType() {
        return local.symbol().lpcType();
    }
}
