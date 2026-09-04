package io.github.protasm.jvmud.cli;

import java.util.ArrayList;
import java.util.List;

/** Small quoted-token parser for the local admin shell. */
final class CommandLine {
    private final List<String> tokens;

    private CommandLine(List<String> tokens) {
        this.tokens = tokens;
    }

    static CommandLine parse(String line) {
        if (line == null || line.isBlank()) {
            return new CommandLine(List.of());
        }

        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                quoted = !quoted;
                continue;
            }
            if (Character.isWhitespace(c) && !quoted) {
                addToken(tokens, current);
                continue;
            }
            current.append(c);
        }
        addToken(tokens, current);
        return new CommandLine(tokens);
    }

    boolean isBlank() {
        return tokens.isEmpty();
    }

    String name() {
        return tokens.get(0);
    }

    String required(int index) {
        int tokenIndex = index + 1;
        if (tokenIndex >= tokens.size()) {
            throw new IllegalArgumentException("Missing argument " + index + " for " + name());
        }
        return tokens.get(tokenIndex);
    }

    String optional(int index, String defaultValue) {
        int tokenIndex = index + 1;
        return tokenIndex < tokens.size() ? tokens.get(tokenIndex) : defaultValue;
    }

    String[] argumentsAfter(int index) {
        int tokenIndex = index + 1;
        if (tokenIndex >= tokens.size()) {
            return new String[0];
        }
        return tokens.subList(tokenIndex, tokens.size()).toArray(String[]::new);
    }

    private static void addToken(List<String> tokens, StringBuilder current) {
        if (!current.isEmpty()) {
            tokens.add(current.toString());
            current.setLength(0);
        }
    }
}
