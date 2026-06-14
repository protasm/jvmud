package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DOT_DOT;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_MINUS_MINUS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PLUS_PLUS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RIGHT_BRACKET;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSliceAccess;

public class InfixIndex implements InfixParselet {
    @Override
    public ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign) {
        int line = parser.currLine();
        ASTExpression index = parser.expression();

        if (parser.tokens().match(T_DOT_DOT)) {
            ASTExpression end = parser.tokens().check(T_RIGHT_BRACKET) ? null : parser.expression();
            parser.tokens().consume(T_RIGHT_BRACKET, "Expect ']' after slice range.");
            return new ASTExprSliceAccess(line, left, index, end);
        }

        parser.tokens().consume(T_RIGHT_BRACKET, "Expect ']' after array element index.");

        if (canAssign && parser.tokens().match(T_EQUAL))
            return new ASTExprArrayStore(line, left, index, parser.expression());
        if (canAssign && parser.tokens().match(T_PLUS_PLUS))
            return new ASTExprArrayMutation(line, left, index, 1);
        if (canAssign && parser.tokens().match(T_MINUS_MINUS))
            return new ASTExprArrayMutation(line, left, index, -1);

        return new ASTExprArrayAccess(line, left, index);
    }
}
