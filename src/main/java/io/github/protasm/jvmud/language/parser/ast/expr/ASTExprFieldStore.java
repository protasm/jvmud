package io.github.protasm.jvmud.language.parser.ast.expr;

import io.github.protasm.jvmud.language.parser.ast.ASTExpression;
import io.github.protasm.jvmud.language.parser.ast.ASTField;
import io.github.protasm.jvmud.language.parser.type.LPCType;

public final class ASTExprFieldStore extends ASTExpression {
    private final ASTField field;
    private final ASTExpression value;

    public ASTExprFieldStore(int line, ASTField field, ASTExpression value) {
        super(line);

        this.field = field;
        this.value = value;
    }

    public ASTField field() {
        return field;
    }

    public ASTExpression value() {
        return value;
    }

    @Override
    public LPCType lpcType() {
        return field.symbol().lpcType();
    }
}
