package io.github.protasm.jvmud.language.parser.parselet;

import io.github.protasm.jvmud.language.parser.Parser;
import io.github.protasm.jvmud.language.parser.ast.expr.ASTExprLiteralString;
import io.github.protasm.jvmud.language.parser.ast.ASTExpression;
import io.github.protasm.jvmud.language.token.Token;

public class PrefixString implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        Token<String> previous = parser.tokens().previous();

        return new ASTExprLiteralString(parser.currLine(), previous);
    }
}
