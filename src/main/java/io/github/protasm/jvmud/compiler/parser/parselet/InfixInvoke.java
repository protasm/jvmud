package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_IDENTIFIER;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprDynamicInvoke;
import io.github.protasm.jvmud.compiler.token.Token;

public class InfixInvoke implements InfixParselet {
    @Override
    public ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign) {
        int line = parser.currLine();
        Token<String> methodToken = parser.tokens().consume(T_IDENTIFIER, "Expect method name after '->'.");
        ASTArguments args = parser.arguments();
        return new ASTExprDynamicInvoke(line, left, methodToken.lexeme(), args);
    }
}
