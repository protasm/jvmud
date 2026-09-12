package io.github.protasm.jvmud.transpiler;

import static io.github.protasm.jvmud.compiler.token.TokenType.*;

import io.github.protasm.jvmud.compiler.token.Token;
import io.github.protasm.jvmud.compiler.token.TokenList;
import io.github.protasm.jvmud.compiler.token.TokenType;
import java.util.HashSet;
import java.util.Set;

/**
 * Adds explicit {@code mixed} types to legacy method signatures in expanded LPC tokens.
 *
 * <p>Run after preprocessing so active declarations in includes and macros receive the same
 * treatment as the main source. Existing types, method bodies, and fields are retained verbatim
 * as tokens. Inserted tokens borrow the declaration's source span, preserving original diagnostic
 * locations. No files are written. This is declaration normalization, not type inference or a
 * general legacy-dialect converter; unsupported syntax still reaches the strict compiler.</p>
 */
public final class UntypedMethodTranspiler {
    private static final Set<String> TYPES = Set.of(
            "int", "float", "string", "object", "mixed", "mapping", "function", "status", "void");
    private static final Set<String> MODIFIERS = Set.of(
            "public", "private", "protected", "static", "nomask", "varargs", "nosave", "deprecated");

    /** Returns a fresh token list with missing method return and parameter types made explicit. */
    public TokenList transpile(TokenList input) {
        Set<Integer> insertions = new HashSet<>();
        int start = 0;
        while (start < input.size() && input.get(start).type() != T_EOF) {
            int signature = start;
            while (signature < input.size() && input.get(signature).type() == T_IDENTIFIER
                    && MODIFIERS.contains(input.get(signature).lexeme()))
                signature++;
            int open = signature;
            while (open < input.size() && isDeclarator(input.get(open).type()))
                open++;
            if (open > signature && open < input.size() && input.get(open).type() == T_LEFT_PAREN
                    && input.get(open - 1).type() == T_IDENTIFIER) {
                int close = matching(input, open, T_LEFT_PAREN, T_RIGHT_PAREN);
                if (close >= 0 && close + 1 < input.size()
                        && (input.get(close + 1).type() == T_LEFT_BRACE
                            || input.get(close + 1).type() == T_SEMICOLON)) {
                    int identifiers = 0;
                    for (int i = signature; i < open; i++)
                        if (input.get(i).type() == T_IDENTIFIER) identifiers++;
                    if (identifiers == 1) insertions.add(signature);
                    int parameter = open + 1;
                    for (int i = parameter; i <= close; i++) {
                        if (i == close || input.get(i).type() == T_COMMA) {
                            addParameterType(input, parameter, i, insertions);
                            parameter = i + 1;
                        }
                    }
                    if (input.get(close + 1).type() == T_SEMICOLON) {
                        start = close + 2;
                    } else {
                        int bodyEnd = matching(input, close + 1, T_LEFT_BRACE, T_RIGHT_BRACE);
                        if (bodyEnd < 0) break; // Let the parser diagnose an unfinished body.
                        start = bodyEnd + 1;
                    }
                    continue;
                }
            }
            // Skip one non-method declaration, including nested initializers and function literals.
            int braces = 0, parens = 0, brackets = 0;
            do {
                TokenType type = input.get(start++).type();
                if (type == T_LEFT_BRACE) braces++;
                if (type == T_RIGHT_BRACE) braces--;
                if (type == T_LEFT_PAREN) parens++;
                if (type == T_RIGHT_PAREN) parens--;
                if (type == T_LEFT_BRACKET) brackets++;
                if (type == T_RIGHT_BRACKET) brackets--;
                if (type == T_SEMICOLON && braces == 0 && parens == 0 && brackets == 0) break;
            } while (start < input.size());
        }
        TokenList result = new TokenList();
        for (int i = 0; i < input.size(); i++) {
            Token<?> original = input.get(i);
            if (insertions.contains(i))
                result.add(new Token<>(T_IDENTIFIER, "mixed", "mixed", original.span()));
            result.add(original);
        }
        return result;
    }

    private static void addParameterType(TokenList tokens, int start, int end, Set<Integer> insertions) {
        if (start < end && "varargs".equals(tokens.get(start).lexeme())) start++;
        if (start >= end) return;
        int identifiers = 0;
        for (int i = start; i < end; i++) {
            if (!isDeclarator(tokens.get(i).type())) return;
            if (tokens.get(i).type() == T_IDENTIFIER) identifiers++;
        }
        // A type without a name is malformed; do not disguise it as an untyped parameter.
        if (identifiers == 1 && !TYPES.contains(tokens.get(start).lexeme())) insertions.add(start);
    }

    private static boolean isDeclarator(TokenType type) {
        return type == T_IDENTIFIER || type == T_STAR || type == T_LEFT_BRACKET || type == T_RIGHT_BRACKET;
    }

    private static int matching(TokenList tokens, int start, TokenType open, TokenType close) {
        int depth = 0;
        for (int i = start; i < tokens.size(); i++) {
            if (tokens.get(i).type() == open) depth++;
            if (tokens.get(i).type() == close && --depth == 0) return i;
        }
        return -1;
    }
}
