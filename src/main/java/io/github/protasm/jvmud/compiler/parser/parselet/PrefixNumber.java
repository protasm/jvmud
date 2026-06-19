package io.github.protasm.jvmud.compiler.parser.parselet;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralFloat;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.token.Token;
import io.github.protasm.jvmud.compiler.token.TokenType;

public class PrefixNumber implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        TokenType tType = parser.tokens().previous().type();

        switch (tType) {
        case T_INT_LITERAL:
            Token<Integer> previous = parser.tokens().previous();

            return new ASTExprLiteralInteger(parser.currLine(), previous);
        case T_FLOAT_LITERAL:
            Token<Float> floatToken = parser.tokens().previous();

            return new ASTExprLiteralFloat(parser.currLine(), floatToken);
        default:
            return null;
        } // switch (numType)
    }
}
