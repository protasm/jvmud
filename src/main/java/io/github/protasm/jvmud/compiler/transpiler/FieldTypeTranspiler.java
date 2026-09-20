package io.github.protasm.jvmud.compiler.transpiler;

import static io.github.protasm.jvmud.compiler.token.TokenType.*;

import io.github.protasm.jvmud.compiler.token.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Applies exact field overrides to expanded compiler tokens without writing source files.
 * A rule's file selects a compilation unit (including its expanded headers), never a global name.
 * Grouped declarations are split to avoid changing neighboring fields; modifiers and initializer
 * order are retained. Locals, methods, comments, and string values are never override targets.
 */
public final class FieldTypeTranspiler {
    private static final Set<String> MODIFIERS = Set.of(
            "public", "private", "protected", "static", "nomask", "nosave", "deprecated", "varargs");

    /** Validates every rule for this unit before returning rewritten tokens; unchanged units pass through. */
    public TokenList transpile(Path source, Path root, TokenList tokens, List<FieldTypeOverride> rules) {
        if (source == null || root == null || rules.isEmpty()) return tokens;
        Path absolute = source.toAbsolutePath().normalize();
        Path base = root.toAbsolutePath().normalize();
        if (!absolute.startsWith(base)) return tokens;
        String file = base.relativize(absolute).toString().replace('\\', '/');
        Map<String, FieldTypeOverride> selected = new LinkedHashMap<>();
        for (var rule : rules) if (rule.file().equals(file)) {
            if (selected.put(rule.field(), rule) != null)
                throw new TranspilationException("Duplicate field override for " + file + ":" + rule.field(), null);
        }
        if (selected.isEmpty()) return tokens;
        Map<String, Integer> matches = new HashMap<>();
        Map<Integer, Edit> edits = new HashMap<>();
        int start = 0, braces = 0, parens = 0, brackets = 0;
        boolean initializer = false, methodBody = false;
        for (int i = 0; i < tokens.size(); i++) {
            TokenType type = tokens.get(i).type();
            if (type == T_EQUAL && braces == 0 && parens == 0 && brackets == 0) initializer = true;
            if (type == T_LEFT_BRACE) {
                if (braces == 0 && parens == 0 && brackets == 0 && !initializer) methodBody = true;
                braces++;
            }
            if (type == T_RIGHT_BRACE) {
                braces--;
                if (braces == 0 && methodBody) { start = i + 1; methodBody = false; initializer = false; }
            }
            if (type == T_LEFT_PAREN) parens++;
            if (type == T_RIGHT_PAREN) parens--;
            if (type == T_LEFT_BRACKET) brackets++;
            if (type == T_RIGHT_BRACKET) brackets--;
            if (type == T_SEMICOLON && braces == 0 && parens == 0 && brackets == 0) {
                rewriteDeclaration(tokens, start, i, selected, matches, edits, file);
                start = i + 1;
                initializer = false;
            }
        }
        for (String field : selected.keySet()) {
            int count = matches.getOrDefault(field, 0);
            if (count != 1) throw new TranspilationException(
                    "Field override " + file + ":" + field + " expected exactly one declaration; found " + count, null);
        }
        TokenList result = new TokenList();
        for (int i = 0; i < tokens.size(); i++) {
            Edit edit = edits.get(i);
            if (edit == null) result.add(tokens.get(i));
            else { edit.tokens().forEach(result::add); i = edit.end(); }
        }
        return result;
    }

    private void rewriteDeclaration(TokenList tokens, int start, int end,
            Map<String, FieldTypeOverride> rules, Map<String, Integer> matches,
            Map<Integer, Edit> edits, String file) {
        int typeIndex = start;
        while (typeIndex < end && tokens.get(typeIndex).type() == T_IDENTIFIER
                && MODIFIERS.contains(tokens.get(typeIndex).lexeme())) typeIndex++;
        if (typeIndex >= end || tokens.get(typeIndex).type() != T_IDENTIFIER) return;
        String baseType = tokens.get(typeIndex).lexeme();
        int name = typeIndex + 1;
        String stars = "";
        while (name < end && tokens.get(name).type() == T_STAR) { stars += "*"; name++; }
        if (name >= end || tokens.get(name).type() != T_IDENTIFIER) return;
        if (name + 1 < end && tokens.get(name + 1).type() == T_LEFT_PAREN) return;
        List<Declarator> fields = new ArrayList<>();
        int from = name, parens = 0, braces = 0, brackets = 0;
        for (int i = name; i <= end; i++) {
            TokenType type = tokens.get(i).type();
            if (i == end || (type == T_COMMA && parens == 0 && braces == 0 && brackets == 0)) {
                int fieldName = from;
                String extraStars = "";
                while (fieldName < i && tokens.get(fieldName).type() == T_STAR) { extraStars += "*"; fieldName++; }
                if (fieldName >= i || tokens.get(fieldName).type() != T_IDENTIFIER) return;
                String actualType = baseType + (stars.isEmpty() ? extraStars : stars);
                fields.add(new Declarator(fieldName, i, actualType));
                from = i + 1;
            }
            if (type == T_LEFT_PAREN) parens++;
            if (type == T_RIGHT_PAREN) parens--;
            if (type == T_LEFT_BRACE) braces++;
            if (type == T_RIGHT_BRACE) braces--;
            if (type == T_LEFT_BRACKET) brackets++;
            if (type == T_RIGHT_BRACKET) brackets--;
        }
        boolean changed = false;
        List<Token<?>> replacement = new ArrayList<>();
        for (Declarator field : fields) {
            Token<?> target = tokens.get(field.name());
            FieldTypeOverride rule = rules.get(target.lexeme());
            String outputType = field.type();
            if (rule != null) {
                matches.merge(rule.field(), 1, Integer::sum);
                if (!rule.expectedType().equals(field.type())) throw new TranspilationException(
                        "Field override " + file + ":" + rule.field() + " expected " + rule.expectedType()
                                + " but found " + field.type(), target.line());
                changed = true;
                outputType = rule.replacementType();
            }
            for (int i = start; i < typeIndex; i++) replacement.add(tokens.get(i));
            int star = outputType.indexOf('*');
            String word = star < 0 ? outputType : outputType.substring(0, star);
            replacement.add(new Token<>(T_IDENTIFIER, word, word, tokens.get(typeIndex).span()));
            if (star >= 0) for (int i = star; i < outputType.length(); i++)
                replacement.add(new Token<>(T_STAR, "*", null, target.span()));
            for (int i = field.name(); i < field.end(); i++) replacement.add(tokens.get(i));
            replacement.add(new Token<>(T_SEMICOLON, ";", null, tokens.get(field.end()).span()));
        }
        if (changed) edits.put(start, new Edit(end, replacement));
    }

    private record Declarator(int name, int end, String type) {}
    private record Edit(int end, List<Token<?>> tokens) {}
}
