package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_INT_LITERAL;

import io.github.protasm.jvmud.compiler.parser.ParseException;
import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprClosureArgument;
import io.github.protasm.jvmud.compiler.token.Token;

public final class PrefixClosureArgument implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        int line = parser.tokens().previous().line();
        Token<Integer> indexToken = parser.tokens().consume(T_INT_LITERAL, "Expect closure argument number after '$'.");
        int index = indexToken.literal();

        if (index < 1)
            throw new ParseException("Closure argument numbers start at $1.", indexToken);

        return new ASTExprClosureArgument(line, index);
    }
}
