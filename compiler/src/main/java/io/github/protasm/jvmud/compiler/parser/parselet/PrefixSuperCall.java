package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_IDENTIFIER;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedParentCall;
import io.github.protasm.jvmud.compiler.token.Token;

public final class PrefixSuperCall implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        Token<String> nameToken = parser.tokens().consume(T_IDENTIFIER, "Expect inherited method name.");
        ASTArguments arguments = parser.arguments();
        return new ASTExprUnresolvedParentCall(parser.currLine(), nameToken.lexeme(), arguments);
    }
}
