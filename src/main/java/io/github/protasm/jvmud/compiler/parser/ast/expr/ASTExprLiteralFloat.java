package io.github.protasm.jvmud.compiler.parser.ast.expr;

import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import io.github.protasm.jvmud.compiler.token.Token;

/** AST node for an LPC floating-point literal. */
public final class ASTExprLiteralFloat extends ASTExpression {
    private final Float value;

    public ASTExprLiteralFloat(int line, Token<Float> token) {
        super(line);
        this.value = token.literal();
    }

    public Float value() {
        return value;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCFLOAT;
    }
}
