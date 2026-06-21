package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_AMP_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_CARET_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_COMMA;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DBL_AMP_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DBL_PIPE_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DOT_DOT;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_GREATER_GREATER_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_INT_LITERAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LESS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LESS_LESS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_MINUS_MINUS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_MINUS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PIPE_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PLUS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PLUS_PLUS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RIGHT_BRACKET;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_SLASH_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_STAR_EQUAL;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayMutation;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprArrayStore;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFromEndIndex;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSliceAccess;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSliceStore;
import io.github.protasm.jvmud.compiler.parser.type.AssignOpType;
import io.github.protasm.jvmud.compiler.token.Token;

public class InfixIndex implements InfixParselet {
    @Override
    public ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign) {
        int line = parser.currLine();
        ASTExpression index = parser.tokens().check(T_DOT_DOT) ? zero(line) : indexBound(parser);
        ASTExpression valueIndex = null;

        if (parser.tokens().match(T_DOT_DOT)) {
            ASTExpression end = parser.tokens().check(T_RIGHT_BRACKET) ? null : indexBound(parser);
            parser.tokens().consume(T_RIGHT_BRACKET, "Expect ']' after slice range.");
            if (canAssign && parser.tokens().match(T_EQUAL))
                return new ASTExprSliceStore(line, left, index, end, parser.expression());
            return new ASTExprSliceAccess(line, left, index, end);
        }

        if (parser.tokens().match(T_COMMA)) {
            valueIndex = indexBound(parser);
        }

        parser.tokens().consume(T_RIGHT_BRACKET, "Expect ']' after array element index.");

        if (canAssign) {
            AssignOpType operator = assignmentOperator(parser);
            if (operator != null)
                return new ASTExprArrayStore(line, left, index, operator, parser.expression());
        }
        if (canAssign && parser.tokens().match(T_PLUS_PLUS))
            return new ASTExprArrayMutation(line, left, index, 1);
        if (canAssign && parser.tokens().match(T_MINUS_MINUS))
            return new ASTExprArrayMutation(line, left, index, -1);

        return new ASTExprArrayAccess(line, left, index, valueIndex);
    }

    /** Parses either a normal index expression or an LPC from-end bound such as {@code <1}. */
    private ASTExpression indexBound(Parser parser) {
        int line = parser.currLine();
        if (parser.tokens().match(T_LESS))
            return new ASTExprFromEndIndex(line, parser.expression());
        return parser.expression();
    }

    private ASTExprLiteralInteger zero(int line) {
        return new ASTExprLiteralInteger(line, new Token<>(T_INT_LITERAL, "0", 0, null));
    }

    private AssignOpType assignmentOperator(Parser parser) {
        if (parser.tokens().match(T_EQUAL))
            return AssignOpType.SET;
        if (parser.tokens().match(T_PLUS_EQUAL))
            return AssignOpType.ADD;
        if (parser.tokens().match(T_MINUS_EQUAL))
            return AssignOpType.SUB;
        if (parser.tokens().match(T_STAR_EQUAL))
            return AssignOpType.MULT;
        if (parser.tokens().match(T_SLASH_EQUAL))
            return AssignOpType.DIV;
        if (parser.tokens().match(T_PIPE_EQUAL))
            return AssignOpType.BIT_OR;
        if (parser.tokens().match(T_AMP_EQUAL))
            return AssignOpType.BIT_AND;
        if (parser.tokens().match(T_CARET_EQUAL))
            return AssignOpType.BIT_XOR;
        if (parser.tokens().match(T_DBL_PIPE_EQUAL))
            return AssignOpType.LOGICAL_OR;
        if (parser.tokens().match(T_DBL_AMP_EQUAL))
            return AssignOpType.LOGICAL_AND;
        if (parser.tokens().match(T_LESS_LESS_EQUAL))
            return AssignOpType.SHL;
        if (parser.tokens().match(T_GREATER_GREATER_EQUAL))
            return AssignOpType.SHR;
        return null;
    }
}
