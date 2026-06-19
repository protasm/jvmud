package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_AMP_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_CARET_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DBL_AMP_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_DBL_PIPE_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LEFT_BRACE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LEFT_PAREN;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LESS_LESS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_MINUS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_MINUS_MINUS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RETURN;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RIGHT_BRACE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RIGHT_PAREN;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PIPE_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PLUS_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_PLUS_PLUS;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_GREATER_GREATER_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_SLASH_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_STAR_EQUAL;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_COMMA;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_SEMICOLON;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.ASTArguments;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameter;
import io.github.protasm.jvmud.compiler.parser.ast.ASTParameters;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprTypedFunctionLiteral;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedAssignment;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedCall;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedIdentifier;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprUnresolvedMutation;
import io.github.protasm.jvmud.compiler.parser.type.AssignOpType;
import io.github.protasm.jvmud.compiler.token.Token;
import io.github.protasm.jvmud.compiler.token.TokenType;

public class PrefixIdentifier implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        int line = parser.currLine();
        String identifier = parser.tokens().previous().lexeme();

        if ("catch".equals(identifier) && parser.tokens().check(T_LEFT_PAREN))
            return parser.protectedEval(line);

        if ("function".equals(identifier) && parser.tokens().check(T_IDENTIFIER))
            return typedFunctionLiteral(parser, line);

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
        else if (canAssign && parser.tokens().match(T_DBL_PIPE_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.LOGICAL_OR, parser.expression());
        else if (canAssign && parser.tokens().match(T_DBL_AMP_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.LOGICAL_AND, parser.expression());
        else if (canAssign && parser.tokens().match(T_LESS_LESS_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.SHL, parser.expression());
        else if (canAssign && parser.tokens().match(T_GREATER_GREATER_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.SHR, parser.expression());
        else if (canAssign && parser.tokens().match(T_PLUS_PLUS))
            return new ASTExprUnresolvedMutation(line, identifier, 1, false);
        else if (canAssign && parser.tokens().match(T_MINUS_MINUS))
            return new ASTExprUnresolvedMutation(line, identifier, -1, false);

        return new ASTExprUnresolvedIdentifier(line, identifier);
    }

    /**
     * Parses the expression-return subset of typed LPC function literals.
     *
     * <p>The supported form is {@code function return_type (typed_params) { return expression; }}.
     * It is deliberately narrower than full statement-bodied LDMud closures.</p>
     */
    private ASTExpression typedFunctionLiteral(Parser parser, int line) {
        Symbol returnSymbol = new Symbol(typeName(parser, "Expect function literal return type."), "$function_return");
        parser.tokens().consume(T_LEFT_PAREN, "Expect '(' after function literal return type.");
        ASTParameters parameters = typedFunctionParameters(parser);
        parser.tokens().consume(T_LEFT_BRACE, "Expect '{' before function literal body.");
        parser.tokens().consume(T_RETURN, "Only return-expression typed function literals are currently supported.");
        ASTExpression body = parser.expression();
        parser.tokens().match(T_SEMICOLON);
        parser.tokens().consume(T_RIGHT_BRACE, "Expect '}' after function literal body.");
        return new ASTExprTypedFunctionLiteral(line, returnSymbol, parameters, body);
    }

    private ASTParameters typedFunctionParameters(Parser parser) {
        ASTParameters parameters = new ASTParameters(parser.currLine());
        if (parser.tokens().match(T_RIGHT_PAREN))
            return parameters;

        do {
            int line = parser.currLine();
            String typeName = typeName(parser, "Expect function literal parameter type.");
            Token<String> nameToken = parser.tokens().consume(T_IDENTIFIER, "Expect function literal parameter name.");
            parameters.add(new ASTParameter(line, new Symbol(typeName, nameToken.lexeme())));
        } while (parser.tokens().match(T_COMMA));

        parser.tokens().consume(T_RIGHT_PAREN, "Expect ')' after function literal parameters.");
        return parameters;
    }

    private String typeName(Parser parser, String message) {
        Token<String> typeToken = parser.tokens().consume(T_IDENTIFIER, message);
        int dimensions = 0;
        while (parser.tokens().match(TokenType.T_STAR))
            dimensions++;
        return typeToken.lexeme() + "*".repeat(dimensions);
    }
}
