package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_COLON;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.PrattParser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprTernary;

public class InfixTernary implements InfixParselet {
    @Override
    public ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign) {
        int line = parser.currLine();
        ASTExpression thenBranch = parser.parsePrecedence(PrattParser.Precedence.PREC_TERNARY);
        parser.tokens().consume(T_COLON, "Expect ':' after ternary expression branch.");
        ASTExpression elseBranch = parser.parsePrecedence(PrattParser.Precedence.PREC_TERNARY);
        return new ASTExprTernary(line, left, thenBranch, elseBranch);
    }
}
