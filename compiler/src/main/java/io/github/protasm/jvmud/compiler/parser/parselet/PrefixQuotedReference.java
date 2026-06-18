package io.github.protasm.jvmud.compiler.parser.parselet;

import static io.github.protasm.jvmud.compiler.token.TokenType.T_APOSTROPHE;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.jvmud.compiler.token.TokenType.T_SUPER;

import io.github.protasm.jvmud.compiler.parser.Parser;
import io.github.protasm.jvmud.compiler.parser.ast.ASTExpression;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprFunctionReference;
import io.github.protasm.jvmud.compiler.parser.ast.expr.ASTExprSymbolLiteral;
import io.github.protasm.jvmud.compiler.token.Token;

public final class PrefixQuotedReference implements PrefixParselet {
    private final boolean functionReference;

    public PrefixQuotedReference(boolean functionReference) {
        this.functionReference = functionReference;
    }

    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        int line = parser.tokens().previous().line();

        if (functionReference)
            parser.tokens().consume(T_APOSTROPHE, "Expect apostrophe after '#'.");

        String name = referenceName(parser);
        return functionReference
                ? new ASTExprFunctionReference(line, name)
                : new ASTExprSymbolLiteral(line, name);
    }

    private String referenceName(Parser parser) {
        Token<String> identifier = parser.tokens().consume(T_IDENTIFIER, "Expect identifier after quote.");
        StringBuilder name = new StringBuilder(identifier.lexeme());

        while (parser.tokens().match(T_SUPER)) {
            Token<String> part =
                    parser.tokens().consume(T_IDENTIFIER, "Expect identifier after '::' in function reference.");
            name.append("::").append(part.lexeme());
        }

        return name.toString();
    }
}
