package io.github.protasm.jvmud.compiler.formatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;

public final class LPCFormatter {
    private static final String INDENT = "  ";
    private static final Pattern METHOD_HEADER =
            Pattern.compile("^(?:(?:[A-Za-z_][A-Za-z0-9_]*\\s+)|(?:[A-Za-z_][A-Za-z0-9_]*\\s*\\*\\s*))?([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^;]*\\)\\s*\\{\\s*(?://.*|/\\*.*\\*/\\s*)?$");
    private static final Pattern METHOD_DECLARATION =
            Pattern.compile("^(?:(?:[A-Za-z_][A-Za-z0-9_]*\\s+)|(?:[A-Za-z_][A-Za-z0-9_]*\\s*\\*\\s*))?[A-Za-z_][A-Za-z0-9_]*\\s*\\([^;]*\\)\\s*$");
    private static final Pattern CONTROL_HEADER =
            Pattern.compile("^(?:if|else\\s+if|else|for|while)\\b.*");
    private static final Pattern SPLIT_CONTROL_HEADER =
            Pattern.compile("^(?:if|else\\s+if|else|for|while)\\b.*|^}\\s*else(?:\\b.*)?$");
    private static final Pattern DECLARATION =
            Pattern.compile("^(?:int|string|mixed|void|object|mapping|float)(?:\\s+|\\s*\\*)[^;]*;\\s*(?://.*)?$");
    private static final Pattern CALL_STATEMENT =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*(?:\\s*->\\s*[A-Za-z_][A-Za-z0-9_]*)?\\s*\\(.*\\)\\s*;.*$");

    public String format(String source) {
        if (source == null)
            throw new IllegalArgumentException("Source text cannot be null.");

        List<FormattedLine> lines = markMethodLeadingComments(indentLines(source));
        return spaceLines(lines);
    }

    public String format(Path sourcePath) throws IOException {
        return format(Files.readString(sourcePath));
    }

    public void formatFile(Path sourcePath) throws IOException {
        Files.writeString(sourcePath, format(sourcePath));
    }

    private List<FormattedLine> indentLines(String source) {
        String normalized = source.replace("\r\n", "\n").replace('\r', '\n').replace("\t", INDENT);
        String[] rawLines = combineSplitOpeningBraces(normalized.split("\n", -1));
        List<FormattedLine> result = new ArrayList<>();
        ScanState scanState = new ScanState();
        ScanState spacingState = new ScanState();
        int blockIndent = 0;
        int singleLineIndents = 0;
        boolean inPreprocessorContinuation = false;
        Deque<Integer> controlBlockDepths = new ArrayDeque<>();

        for (int i = 0; i < rawLines.length; i++) {
            boolean lastSplitLine = i == rawLines.length - 1;
            String rawLine = stripTrailingWhitespace(rawLines[i]);

            if (lastSplitLine && rawLine.isEmpty())
                continue;

            String trimmed = normalizeCodeSpacing(rawLine.stripLeading(), spacingState);
            if (trimmed.isEmpty()) {
                result.add(FormattedLine.blank());
                continue;
            }

            if (inPreprocessorContinuation || trimmed.startsWith("#")) {
                result.add(new FormattedLine(rawLine, 0, LineKind.PREPROCESSOR));
                inPreprocessorContinuation = rawLine.endsWith("\\");
                continue;
            }

            LineScan scan = scanLine(trimmed, scanState);
            String code = scan.code().strip();
            boolean startsWithCloseBrace = code.startsWith("}");
            boolean consumesSingleLineIndent = singleLineIndents > 0;
            int printIndent = blockIndent + singleLineIndents;

            if (startsWithCloseBrace)
                printIndent--;

            if (printIndent < 0)
                printIndent = 0;

            boolean closesControlBlock = startsWithCloseBrace && controlBlockDepths.removeFirstOccurrence(blockIndent);
            String content = INDENT.repeat(printIndent) + trimmed;
            LineKind kind = classify(trimmed, code, closesControlBlock, consumesSingleLineIndent);
            result.add(new FormattedLine(content, printIndent, kind));

            blockIndent += scan.openBraces() - scan.closeBraces();
            if (blockIndent < 0)
                blockIndent = 0;

            if (consumesSingleLineIndent)
                singleLineIndents--;

            if (isControlHeader(code) && !code.contains("{") && !code.endsWith(";"))
                singleLineIndents++;

            if (isControlHeader(code) && code.contains("{"))
                controlBlockDepths.addFirst(blockIndent);
        }

        return result;
    }

    private String[] combineSplitOpeningBraces(String[] rawLines) {
        List<String> combined = new ArrayList<>();

        for (int i = 0; i < rawLines.length; i++) {
            String current = rawLines[i];
            String strippedCurrent = current.strip();
            int elseIndex = nextNonBlankIndex(rawLines, i + 1);
            if (elseIndex < rawLines.length && strippedCurrent.equals("}") && isElseHeader(rawLines[elseIndex].strip())) {
                String joinedElse = stripTrailingWhitespace(current) + " " + stripTrailingWhitespace(rawLines[elseIndex]).stripLeading();
                int braceIndex = nextNonBlankIndex(rawLines, elseIndex + 1);
                if (braceIndex < rawLines.length && isSplitControlHeader(joinedElse.strip())) {
                    String next = stripTrailingWhitespace(rawLines[braceIndex]).stripLeading();
                    if (next.startsWith("{")) {
                        addJoinedOpeningBrace(combined, joinedElse, next, true);
                        i = braceIndex;
                        continue;
                    }
                }
                combined.add(joinedElse);
                i = elseIndex;
                continue;
            }
            int braceIndex = nextNonBlankIndex(rawLines, i + 1);
            if (braceIndex < rawLines.length && opensBlockOnFollowingLine(strippedCurrent)) {
                String next = stripTrailingWhitespace(rawLines[braceIndex]).stripLeading();
                if (next.startsWith("{")) {
                    addJoinedOpeningBrace(combined, stripTrailingWhitespace(current), next, isSplitControlHeader(strippedCurrent));
                    i = braceIndex;
                    continue;
                }
            }

            combined.add(current);
        }

        return combined.toArray(String[]::new);
    }

    private int nextNonBlankIndex(String[] rawLines, int start) {
        int index = start;
        while (index < rawLines.length && stripTrailingWhitespace(rawLines[index]).strip().isEmpty())
            index++;
        return index;
    }

    private boolean opensBlockOnFollowingLine(String line) {
        return METHOD_DECLARATION.matcher(line).matches() || isSplitControlHeader(line);
    }

    private boolean isSplitControlHeader(String line) {
        return SPLIT_CONTROL_HEADER.matcher(line).matches() && !line.contains("{") && !line.endsWith(";");
    }

    private boolean isElseHeader(String line) {
        return line.equals("else") || line.startsWith("else if") || line.startsWith("else ");
    }

    private void addJoinedOpeningBrace(List<String> output, String current, String braceLine, boolean splitTrailingCode) {
        if (!splitTrailingCode) {
            output.add(current + " " + braceLine);
            return;
        }

        String afterBrace = braceLine.substring(1).stripLeading();
        if (afterBrace.isEmpty() || afterBrace.startsWith("//") || afterBrace.startsWith("/*") || afterBrace.contains("}")) {
            output.add(current + " " + braceLine);
            return;
        }

        output.add(current + " {");
        output.add(afterBrace);
    }

    private List<FormattedLine> markMethodLeadingComments(List<FormattedLine> lines) {
        List<FormattedLine> marked = new ArrayList<>(lines);

        for (int i = 0; i < marked.size(); i++) {
            FormattedLine line = marked.get(i);
            if (line.kind() != LineKind.BLOCK_COMMENT && line.kind() != LineKind.COMMENT)
                continue;

            int commentEnd = i;
            if (line.kind() == LineKind.BLOCK_COMMENT) {
                while (commentEnd + 1 < marked.size()) {
                    FormattedLine nextCommentLine = marked.get(commentEnd + 1);
                    if (nextCommentLine.kind() != LineKind.COMMENT && nextCommentLine.kind() != LineKind.BLOCK_COMMENT)
                        break;
                    commentEnd++;
                    if (nextCommentLine.content().strip().endsWith("*/"))
                        break;
                }
            }

            int next = commentEnd + 1;
            while (next < marked.size() && marked.get(next).isBlank())
                next++;

            if (next < marked.size()
                    && marked.get(next).kind() == LineKind.METHOD_HEADER
                    && marked.get(next).indent() == line.indent())
                marked.set(i, line.asMethodLeading());
        }

        return marked;
    }

    private String spaceLines(List<FormattedLine> lines) {
        List<String> output = new ArrayList<>();
        FormattedLine previousMeaningful = null;

        for (FormattedLine line : lines) {
            if (line.isBlank()) {
                if (previousMeaningful != null && previousMeaningful.opensBlock())
                    continue;

                appendBlank(output);
                continue;
            }

            if (previousMeaningful != null && needsBlankBefore(previousMeaningful, line))
                appendBlank(output);

            output.add(line.content());
            previousMeaningful = line;
        }

        while (!output.isEmpty() && output.get(output.size() - 1).isEmpty())
            output.remove(output.size() - 1);

        return String.join("\n", output) + "\n";
    }

    private boolean needsBlankBefore(FormattedLine previous, FormattedLine current) {
        if (current.kind() == LineKind.METHOD_LEADING_BLOCK_COMMENT)
            return previous.kind() == LineKind.CLOSING_BRACE
                    || previous.kind() == LineKind.PREPROCESSOR
                    || previous.kind().statementGroup();

        if (current.kind() == LineKind.ELSE || current.kind() == LineKind.CLOSING_BRACE || current.kind() == LineKind.BLOCK_COMMENT)
            return false;

        if (current.kind() == LineKind.COMMENT)
            return previous.kind().statementGroup() || previous.kind() == LineKind.PREPROCESSOR;

        if (current.kind() == LineKind.METHOD_LEADING_COMMENT)
            return previous.kind() == LineKind.CLOSING_BRACE
                    || previous.kind() == LineKind.PREPROCESSOR
                    || previous.kind().statementGroup();

        if (current.kind() == LineKind.CONTROL_BLOCK_END)
            return false;

        if (previous.indent() == 0 && previous.kind() == LineKind.CLOSING_BRACE && current.kind() == LineKind.METHOD_HEADER)
            return true;

        if (previous.kind() == LineKind.DECLARATION && current.kind() == LineKind.METHOD_HEADER)
            return true;

        if ((previous.kind() == LineKind.CONTROL_BODY || previous.kind() == LineKind.CONTROL_BLOCK_END)
                && current.kind() != LineKind.CONTROL_BLOCK_END)
            return true;

        if (previous.kind().statementGroup() && current.kind() == LineKind.CONTROL_HEADER)
            return true;

        if (previous.kind() == LineKind.RETURN)
            return current.kind() != LineKind.RETURN;

        if (current.kind() == LineKind.RETURN)
            return previous.kind() != LineKind.RETURN
                    && previous.kind() != LineKind.METHOD_HEADER
                    && previous.kind() != LineKind.CONTROL_HEADER;

        return separatesStatementGroups(previous.kind(), current.kind());
    }

    private boolean separatesStatementGroups(LineKind previous, LineKind current) {
        if (!previous.statementGroup() || !current.statementGroup())
            return false;

        return previous != current;
    }

    private LineKind classify(String line, String code, boolean closesControlBlock, boolean controlBody) {
        if (line.stripLeading().startsWith("//") || line.stripLeading().startsWith("*"))
            return LineKind.COMMENT;

        if (line.stripLeading().startsWith("/*"))
            return LineKind.BLOCK_COMMENT;

        if (code.startsWith("} else"))
            return LineKind.ELSE;

        if (closesControlBlock)
            return LineKind.CONTROL_BLOCK_END;

        if (code.startsWith("}"))
            return LineKind.CLOSING_BRACE;

        if (code.startsWith("#"))
            return LineKind.PREPROCESSOR;

        if (isControlHeader(code))
            return LineKind.CONTROL_HEADER;

        if (controlBody)
            return LineKind.CONTROL_BODY;

        if (METHOD_HEADER.matcher(code).matches())
            return LineKind.METHOD_HEADER;

        if (code.startsWith("return"))
            return LineKind.RETURN;

        if (DECLARATION.matcher(code).matches())
            return LineKind.DECLARATION;

        if (isAssignment(code))
            return LineKind.ASSIGNMENT;

        if (CALL_STATEMENT.matcher(code).matches())
            return LineKind.CALL;

        return LineKind.OTHER;
    }

    private boolean isControlHeader(String code) {
        return CONTROL_HEADER.matcher(code).matches();
    }

    private boolean isAssignment(String code) {
        if (!code.endsWith(";"))
            return false;

        for (int i = 0; i < code.length(); i++) {
            if (code.charAt(i) != '=')
                continue;

            char previous = i > 0 ? code.charAt(i - 1) : '\0';
            char next = i + 1 < code.length() ? code.charAt(i + 1) : '\0';
            if (previous == '=' || previous == '!' || previous == '<' || previous == '>' || next == '=')
                continue;

            return true;
        }

        return false;
    }

    private String normalizeCodeSpacing(String line, ScanState state) {
        if (line.isEmpty() || state.inBlockComment()) {
            updateBlockCommentState(line, state);
            return line;
        }

        StringBuilder normalized = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;
        boolean pendingSpace = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            char next = i + 1 < line.length() ? line.charAt(i + 1) : '\0';

            if (!inString && c == '/' && next == '/') {
                if (pendingSpace) {
                    normalized.append(' ');
                    pendingSpace = false;
                }
                normalized.append(line.substring(i));
                break;
            }

            if (!inString && c == '/' && next == '*') {
                if (pendingSpace) {
                    normalized.append(' ');
                    pendingSpace = false;
                }
                normalized.append(line.substring(i));
                if (!line.substring(i + 2).contains("*/"))
                    state.setInBlockComment(true);
                break;
            }

            if (!inString && c == ' ') {
                pendingSpace = true;
                continue;
            }

            if (pendingSpace) {
                normalized.append(' ');
                pendingSpace = false;
            }

            normalized.append(c);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"')
                inString = true;
        }

        return normalized.toString();
    }

    private void updateBlockCommentState(String line, ScanState state) {
        if (state.inBlockComment() && line.contains("*/"))
            state.setInBlockComment(false);
    }

    private LineScan scanLine(String line, ScanState state) {
        int openBraces = 0;
        int closeBraces = 0;
        StringBuilder code = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            char next = i + 1 < line.length() ? line.charAt(i + 1) : '\0';

            if (state.inBlockComment()) {
                if (c == '*' && next == '/') {
                    state.setInBlockComment(false);
                    i++;
                }
                continue;
            }

            if (!inString && c == '/' && next == '/')
                break;

            if (!inString && c == '/' && next == '*') {
                state.setInBlockComment(true);
                i++;
                continue;
            }

            code.append(c);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                openBraces++;
            } else if (c == '}') {
                closeBraces++;
            }
        }

        return new LineScan(code.toString(), openBraces, closeBraces);
    }

    private void appendBlank(List<String> output) {
        if (!output.isEmpty() && !output.get(output.size() - 1).isEmpty())
            output.add("");
    }

    private String stripTrailingWhitespace(String line) {
        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1)))
            end--;

        return line.substring(0, end);
    }

    private enum LineKind {
        ASSIGNMENT(true),
        BLOCK_COMMENT(false),
        CALL(true),
        CLOSING_BRACE(false),
        COMMENT(false),
        CONTROL_BLOCK_END(false),
        CONTROL_BODY(false),
        CONTROL_HEADER(false),
        DECLARATION(true),
        ELSE(false),
        METHOD_HEADER(false),
        METHOD_LEADING_BLOCK_COMMENT(false),
        METHOD_LEADING_COMMENT(false),
        OTHER(true),
        PREPROCESSOR(false),
        RETURN(false);

        private final boolean statementGroup;

        LineKind(boolean statementGroup) {
            this.statementGroup = statementGroup;
        }

        boolean statementGroup() {
            return statementGroup;
        }

    }

    private record FormattedLine(String content, int indent, LineKind kind) {
        static FormattedLine blank() {
            return new FormattedLine("", 0, LineKind.OTHER);
        }

        boolean isBlank() {
            return content.isEmpty();
        }

        boolean opensBlock() {
            return (kind == LineKind.METHOD_HEADER || kind == LineKind.CONTROL_HEADER || kind == LineKind.ELSE)
                    && content.contains("{");
        }

        FormattedLine asMethodLeading() {
            if (kind == LineKind.BLOCK_COMMENT)
                return new FormattedLine(content, indent, LineKind.METHOD_LEADING_BLOCK_COMMENT);

            if (kind == LineKind.COMMENT)
                return new FormattedLine(content, indent, LineKind.METHOD_LEADING_COMMENT);

            return this;
        }
    }

    private record LineScan(String code, int openBraces, int closeBraces) {
    }

    private static final class ScanState {
        private boolean inBlockComment;

        boolean inBlockComment() {
            return inBlockComment;
        }

        void setInBlockComment(boolean inBlockComment) {
            this.inBlockComment = inBlockComment;
        }
    }
}
