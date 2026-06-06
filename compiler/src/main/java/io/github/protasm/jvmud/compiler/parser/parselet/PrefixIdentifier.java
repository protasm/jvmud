package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_AMP_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_CARET_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_INT_LITERAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LEFT_PAREN;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LESS_LESS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_MINUS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_MINUS_MINUS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PIPE_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PLUS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PLUS_PLUS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_GREATER_GREATER_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_SLASH_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_STAR_EQUAL;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedAssignment;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedCall;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedIdentifier;
import io.github.protasm.jvmud.compiler.parser.type.AssignOpType;
import io.github.protasm.jvmud.compiler.token.Token;

public class PrefixIdentifier implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        int line = parser.currLine();
        String identifier = parser.tokens().previous().lexeme();

        if (parser.tokens().check(T_LEFT_PAREN)) {
            ASTArguments args = parser.arguments();
            return new ASTExprUnresolvedCall(line, identifier, args);
        }

        if (canAssign && parser.tokens().match(T_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.SET, parser.expression());
        else if (canAssign && parser.tokens().match(T_PLUS_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.ADD, parser.expression());
        else if (canAssign && parser.tokens().match(T_MINUS_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.SUB, parser.expression());
        else if (canAssign && parser.tokens().match(T_STAR_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.MULT, parser.expression());
        else if (canAssign && parser.tokens().match(T_SLASH_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.DIV, parser.expression());
        else if (canAssign && parser.tokens().match(T_PIPE_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.BIT_OR, parser.expression());
        else if (canAssign && parser.tokens().match(T_AMP_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.BIT_AND, parser.expression());
        else if (canAssign && parser.tokens().match(T_CARET_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.BIT_XOR, parser.expression());
        else if (canAssign && parser.tokens().match(T_LESS_LESS_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.SHL, parser.expression());
        else if (canAssign && parser.tokens().match(T_GREATER_GREATER_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.SHR, parser.expression());
        else if (canAssign && parser.tokens().match(T_PLUS_PLUS))
            return new ASTExprUnresolvedAssignment(
                    line,
                    identifier,
                    AssignOpType.ADD,
                    new ASTExprLiteralInteger(line, new Token<>(T_INT_LITERAL, "1", 1, null)));
        else if (canAssign && parser.tokens().match(T_MINUS_MINUS))
            return new ASTExprUnresolvedAssignment(
                    line,
                    identifier,
                    AssignOpType.SUB,
                    new ASTExprLiteralInteger(line, new Token<>(T_INT_LITERAL, "1", 1, null)));

        return new ASTExprUnresolvedIdentifier(line, identifier);
    }
}
