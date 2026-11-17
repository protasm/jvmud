package io.github.protasm.jvmud.language.compiler.parser.ast.expr;

import io.github.protasm.jvmud.language.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.language.compiler.token.Token;
import io.github.protasm.jvmud.language.compiler.type.LPCType;

public final class ASTExprLiteralString extends ASTExpression {
    private final String value;

    public ASTExprLiteralString(int line, Token<String> token) {
        super(line);

        this.value = token.literal();
    }

    public String value() {
        return value;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCSTRING;
    }
}
