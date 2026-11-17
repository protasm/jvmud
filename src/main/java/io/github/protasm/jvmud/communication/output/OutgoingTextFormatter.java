package io.github.protasm.jvmud.communication.output;

/** Applies engine-owned presentation formatting to text leaving the runtime. */
public final class OutgoingTextFormatter {
    /** Default presentation width for mudlib output. */
    public static final int DEFAULT_LINE_LENGTH = 80;
    /** Minimum supported presentation width. */
    public static final int MIN_LINE_LENGTH = 20;
    /** Maximum supported presentation width. */
    public static final int MAX_LINE_LENGTH = 140;

    private static final char ESCAPE = '\u001B';

    private OutgoingTextFormatter() {}

    public static String wrap(String text, int maxLineLength) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        validateMaxLineLength(maxLineLength);

        StringBuilder output = new StringBuilder(text.length());
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            line.append(ch);
            if (ch == '\n') {
                appendWrappedLine(output, line.substring(0, line.length() - 1), maxLineLength);
                output.append('\n');
                line.setLength(0);
            }
        }
        appendWrappedLine(output, line.toString(), maxLineLength);
        return output.toString();
    }

    public static String ruler(int length) {
        validateMaxLineLength(length);
        StringBuilder ruler = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            ruler.append(i % 10 == 0 ? '+' : '-');
        }
        return ruler.toString();
    }

    private static void appendWrappedLine(StringBuilder output, String line, int maxLineLength) {
        String remaining = line;
        while (visibleLength(remaining) > maxLineLength) {
            int breakAt = lastWhitespaceAtOrBeforeVisibleColumn(remaining, maxLineLength);
            if (breakAt < 0) {
                output.append(remaining);
                return;
            }
            output.append(remaining, 0, breakAt).append('\n');
            remaining = stripLeadingWhitespace(remaining.substring(breakAt + 1));
        }
        output.append(remaining);
    }

    private static int visibleLength(String text) {
        int visible = 0;
        for (int i = 0; i < text.length(); ) {
            int ansiEnd = ansiEscapeEnd(text, i);
            if (ansiEnd > i) {
                i = ansiEnd;
                continue;
            }
            int codePoint = text.codePointAt(i);
            visible++;
            i += Character.charCount(codePoint);
        }
        return visible;
    }

    private static int lastWhitespaceAtOrBeforeVisibleColumn(String text, int maxLineLength) {
        int lastWhitespace = -1;
        int visible = 0;
        for (int i = 0; i < text.length(); ) {
            int ansiEnd = ansiEscapeEnd(text, i);
            if (ansiEnd > i) {
                i = ansiEnd;
                continue;
            }
            int codePoint = text.codePointAt(i);
            if (visible > maxLineLength) {
                break;
            }
            if (Character.isWhitespace(codePoint)) {
                lastWhitespace = i;
            }
            visible++;
            i += Character.charCount(codePoint);
        }
        return lastWhitespace;
    }

    private static int ansiEscapeEnd(String text, int index) {
        if (index + 1 >= text.length() || text.charAt(index) != ESCAPE || text.charAt(index + 1) != '[') {
            return index;
        }
        int cursor = index + 2;
        while (cursor < text.length()) {
            char current = text.charAt(cursor);
            cursor++;
            if (current == 'm') {
                return cursor;
            }
        }
        return index;
    }

    private static String stripLeadingWhitespace(String text) {
        int firstNonWhitespace = 0;
        while (firstNonWhitespace < text.length()
                && Character.isWhitespace(text.charAt(firstNonWhitespace))
                && text.charAt(firstNonWhitespace) != '\n') {
            firstNonWhitespace++;
        }
        return text.substring(firstNonWhitespace);
    }

    private static void validateMaxLineLength(int maxLineLength) {
        if (maxLineLength < MIN_LINE_LENGTH
                || maxLineLength > MAX_LINE_LENGTH) {
            throw new IllegalArgumentException("maxLineLength must be between "
                    + MIN_LINE_LENGTH + " and " + MAX_LINE_LENGTH + ".");
        }
    }
}
