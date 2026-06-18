package io.github.protasm.jvmud.compiler.token;

import java.util.Map;

/**
 * Re-classifies identifier tokens into language keywords.
 *
 * <p>The scanner intentionally treats all alpha-numeric sequences as {@link TokenType#T_IDENTIFIER}.
 * This classifier runs after scanning to keep lexing free of language semantics while still
 * providing the parser with the token categories it expects.</p>
 */
public final class TokenClassifier {
    private static final Map<String, TokenType> RESERVED_KEYWORDS =
            Map.ofEntries(
                    Map.entry("break", TokenType.T_BREAK),
                    Map.entry("continue", TokenType.T_CONTINUE),
                    Map.entry("else", TokenType.T_ELSE),
                    Map.entry("false", TokenType.T_FALSE),
                    Map.entry("for", TokenType.T_FOR),
                    Map.entry("foreach", TokenType.T_FOREACH),
                    Map.entry("if", TokenType.T_IF),
                    Map.entry("in", TokenType.T_IN),
                    Map.entry("inherit", TokenType.T_INHERIT),
                    Map.entry("nil", TokenType.T_NIL),
                    Map.entry("return", TokenType.T_RETURN),
                    Map.entry("true", TokenType.T_TRUE),
                    Map.entry("while", TokenType.T_WHILE));

    private TokenClassifier() {}

    public static TokenList classify(TokenList rawTokens) {
        if (rawTokens == null)
            throw new IllegalArgumentException("Token list cannot be null.");

        TokenList classified = new TokenList();

        for (int i = 0; i < rawTokens.size(); i++) {
            Token<?> token = rawTokens.get(i);

            if (token.type() != TokenType.T_IDENTIFIER) {
                classified.add(token);
                continue;
            }

            String lexeme = token.lexeme();

            TokenType keyword = RESERVED_KEYWORDS.get(lexeme);
            if (keyword != null) {
                classified.add(new Token<>(keyword, lexeme, null, token.span()));
                continue;
            }

            classified.add(token);
        }

        return classified;
    }
}
