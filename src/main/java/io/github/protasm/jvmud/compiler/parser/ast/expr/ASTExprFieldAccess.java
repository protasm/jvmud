package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTField;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;

public final class ASTExprFieldAccess extends ASTExpression {
    private final ASTField field;

    public ASTExprFieldAccess(int line, ASTField field) {
        super(line);

        this.field = field;
    }

    public ASTField field() {
        return field;
    }

    @Override
    public LPCType lpcType() {
        return field.symbol().lpcType();
    }
}
