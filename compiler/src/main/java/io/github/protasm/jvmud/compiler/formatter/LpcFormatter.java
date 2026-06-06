package io.github.protasm.jvmud.compiler.formatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LpcFormatter {
    private static final String INDENT = "    ";
    private static final Pattern METHOD_HEADER =
            Pattern.compile("^(?:(?:[A-Za-z_][A-Za-z0-9_]*\\s+)|(?:[A-Za-z_][A-Za-z0-9_]*\\s*\\*\\s*))?([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^;]*\\)\\s*\\{\\s*(?://.*)?$");
    private static final Pattern METHOD_DECLARATION =
            Pattern.compile("^(?:(?:[A-Za-z_][A-Za-z0-9_]*\\s+)|(?:[A-Za-z_][A-Za-z0-9_]*\\s*\\*\\s*))?[A-Za-z_][A-Za-z0-9_]*\\s*\\([^;]*\\)\\s*$");
    private static final Pattern CONTROL_HEADER =
            Pattern.compile("^(?:if|else\\s+if|else|for|while)\\b.*");
    private static final Pattern DECLARATION =
            Pattern.compile("^(?:int|string|mixed|void|object|mapping|float)(?:\\s+|\\s*\\*)[^;]*;\\s*(?://.*)?$");
    private static final Pattern FIELD_DECLARATION_NAME =
            Pattern.compile("^(?:int|string|mixed|void|object|mapping|float)(?:\\s+|\\s*\\*)\\s*([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern CALL_STATEMENT =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*(?:\\s*->\\s*[A-Za-z_][A-Za-z0-9_]*)?\\s*\\(.*\\)\\s*;.*$");

    public String format(String source) {
        if (source == null)
            throw new IllegalArgumentException("Source text cannot be null.");

        List<FormattedLine> lines = sortTopLevelMethods(sortTopLevelFields(indentLines(source)));
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
        String[] rawLines = combineSplitMethodBraces(normalized.split("\n", -1));
        List<FormattedLine> result = new ArrayList<>();
        ScanState scanState = new ScanState();
        int blockIndent = 0;
        int singleLineIndents = 0;
        Deque<Integer> controlBlockDepths = new ArrayDeque<>();

        for (int i = 0; i < rawLines.length; i++) {
            boolean lastSplitLine = i == rawLines.length - 1;
            String rawLine = stripTrailingWhitespace(rawLines[i]);

            if (lastSplitLine && rawLine.isEmpty())
                continue;

            String trimmed = rawLine.stripLeading();
            if (trimmed.isEmpty()) {
                result.add(FormattedLine.blank());
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

    private String[] combineSplitMethodBraces(String[] rawLines) {
        List<String> combined = new ArrayList<>();

        for (int i = 0; i < rawLines.length; i++) {
            String current = rawLines[i];
            if (i + 1 < rawLines.length && METHOD_DECLARATION.matcher(current.strip()).matches()) {
                String next = stripTrailingWhitespace(rawLines[i + 1]).stripLeading();
                if (next.startsWith("{")) {
                    combined.add(stripTrailingWhitespace(current) + " " + next);
                    i++;
                    continue;
                }
            }

            combined.add(current);
        }

        return combined.toArray(String[]::new);
    }

    private List<FormattedLine> sortTopLevelFields(List<FormattedLine> lines) {
        List<FieldDeclaration> fields = new ArrayList<>();
        List<FormattedLine> rest = new ArrayList<>();

        for (FormattedLine line : lines) {
            if (isTopLevelFieldDeclaration(line)) {
                fields.add(new FieldDeclaration(fieldName(line), line));
            } else {
                rest.add(line);
            }
        }

        fields.sort(Comparator.comparing(FieldDeclaration::name));

        List<FormattedLine> sorted = new ArrayList<>();
        for (FieldDeclaration field : fields)
            sorted.add(field.line());

        sorted.addAll(trimLeadingBlanks(rest));
        return sorted;
    }

    private boolean isTopLevelFieldDeclaration(FormattedLine line) {
        return line.indent() == 0 && line.kind() == LineKind.DECLARATION;
    }

    private String fieldName(FormattedLine line) {
        Matcher matcher = FIELD_DECLARATION_NAME.matcher(line.content().strip());
        if (!matcher.find())
            throw new IllegalStateException("Cannot extract field name from: " + line.content());

        return matcher.group(1);
    }

    private List<FormattedLine> sortTopLevelMethods(List<FormattedLine> lines) {
        List<FormattedLine> prefix = new ArrayList<>();
        List<FormattedLine> pending = new ArrayList<>();
        List<MethodBlock> methods = new ArrayList<>();
        boolean seenMethod = false;

        for (int i = 0; i < lines.size(); i++) {
            FormattedLine line = lines.get(i);

            if (!isTopLevelMethodHeader(line)) {
                if (seenMethod)
                    pending.add(line);
                else
                    prefix.add(line);
                continue;
            }

            List<FormattedLine> methodLines = new ArrayList<>();
            if (seenMethod) {
                methodLines.addAll(trimLeadingBlanks(pending));
                pending.clear();
            }

            int braceDepth = 0;
            ScanState methodScanState = new ScanState();
            do {
                FormattedLine methodLine = lines.get(i);
                methodLines.add(methodLine);
                LineScan scan = scanLine(methodLine.content().stripLeading(), methodScanState);
                braceDepth += scan.openBraces() - scan.closeBraces();
                i++;
            } while (i < lines.size() && braceDepth > 0);

            i--;
            methods.add(new MethodBlock(methodName(line), methodLines));
            seenMethod = true;
        }

        methods.sort(Comparator.comparing(MethodBlock::name));

        List<FormattedLine> sorted = new ArrayList<>(trimTrailingBlanks(prefix));
        for (MethodBlock method : methods)
            sorted.addAll(method.lines());
        sorted.addAll(trimLeadingBlanks(pending));

        return sorted;
    }

    private boolean isTopLevelMethodHeader(FormattedLine line) {
        return line.indent() == 0 && line.kind() == LineKind.METHOD_HEADER;
    }

    private String methodName(FormattedLine line) {
        Matcher matcher = METHOD_HEADER.matcher(line.content().strip());
        if (!matcher.matches())
            throw new IllegalStateException("Cannot extract method name from: " + line.content());

        return matcher.group(1);
    }

    private List<FormattedLine> trimLeadingBlanks(List<FormattedLine> lines) {
        int start = 0;
        while (start < lines.size() && lines.get(start).isBlank())
            start++;

        return new ArrayList<>(lines.subList(start, lines.size()));
    }

    private List<FormattedLine> trimTrailingBlanks(List<FormattedLine> lines) {
        int end = lines.size();
        while (end > 0 && lines.get(end - 1).isBlank())
            end--;

        return new ArrayList<>(lines.subList(0, end));
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
        if (current.kind() == LineKind.ELSE || current.kind() == LineKind.CLOSING_BRACE || current.kind() == LineKind.BLOCK_COMMENT)
            return false;

        if (current.kind() == LineKind.COMMENT)
            return previous.kind().statementGroup() || previous.kind() == LineKind.PREPROCESSOR;

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
                    && previous.kind() != LineKind.ASSIGNMENT
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
    }

    private record LineScan(String code, int openBraces, int closeBraces) {
    }

    private record MethodBlock(String name, List<FormattedLine> lines) {
    }

    private record FieldDeclaration(String name, FormattedLine line) {
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
