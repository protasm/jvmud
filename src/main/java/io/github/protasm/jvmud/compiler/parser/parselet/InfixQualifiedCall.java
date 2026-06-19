package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_IDENTIFIER;

import io.github.protasm.jvmud.compiler.parser.ParseException;
import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedIdentifier;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedQualifiedCall;
import io.github.protasm.jvmud.compiler.token.Token;

public final class InfixQualifiedCall implements InfixParselet {
    @Override
    public ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign) {
        if (!(left instanceof ASTExprUnresolvedIdentifier qualifier))
            throw new ParseException("Expect qualifier before '::'.", parser.tokens().previous());

        int line = parser.currLine();
        Token<String> nameToken = parser.tokens().consume(T_IDENTIFIER, "Expect function name after '::'.");
        ASTArguments arguments = parser.arguments();
        return new ASTExprUnresolvedQualifiedCall(line, qualifier.name(), nameToken.lexeme(), arguments);
    }
}
