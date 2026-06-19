package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_COLON;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_COMMA;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_LEFT_BRACKET;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RIGHT_BRACKET;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_RIGHT_PAREN;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprMappingEntry;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprMappingLiteral;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprInlineCallable;
import java.util.ArrayList;
import java.util.List;

public class PrefixLParen implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        if (parser.tokens().match(T_LEFT_BRACKET)) {
            ASTExpression mapping = parseMappingLiteral(parser);
            parser.tokens().consume(T_RIGHT_PAREN, "Expect ')' after mapping literal.");
            return mapping;
        }

        if (parser.tokens().match(T_COLON))
            return parseInlineCallable(parser);

        ASTExpression expr = parser.expression();

        parser.tokens().consume(T_RIGHT_PAREN, "Expect ')' after expression.");

        return expr;
    }

    private ASTExpression parseInlineCallable(Parser parser) {
        int line = parser.currLine();
        ASTExpression body = parser.expression();
        parser.tokens().consume(T_COLON, "Expect ':' before ')' after inline callable expression.");
        parser.tokens().consume(T_RIGHT_PAREN, "Expect ')' after inline callable expression.");
        return new ASTExprInlineCallable(line, body);
    }

    private ASTExpression parseMappingLiteral(Parser parser) {
        int line = parser.currLine();
        List<ASTExprMappingEntry> entries = new ArrayList<>();

        if (!parser.tokens().check(T_RIGHT_BRACKET)) {
            while (true) {
                ASTExpression key = parser.expression();
                parser.tokens().consume(T_COLON, "Expect ':' after mapping key.");
                ASTExpression value = parser.expression();
                entries.add(new ASTExprMappingEntry(key, value));

                if (!parser.tokens().match(T_COMMA)) {
                    break;
                }
                if (parser.tokens().check(T_RIGHT_BRACKET)) {
                    break;
                }
            }
        }

        parser.tokens().consume(T_RIGHT_BRACKET, "Expect ']' after mapping literal.");
        return new ASTExprMappingLiteral(line, entries);
    }
}
