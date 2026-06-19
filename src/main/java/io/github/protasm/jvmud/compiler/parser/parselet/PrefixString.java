package io.github.protasm.jvmud.compiler.parser.parselet;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralString;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.token.Token;

public class PrefixString implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        Token<String> previous = parser.tokens().previous();

        return new ASTExprLiteralString(parser.currLine(), previous);
    }
}
