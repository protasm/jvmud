package io.github.protasm.jvmud.engine.output;

import io.github.protasm.jvmud.engine.mudlib.MudlibBoundary;

/** Applies engine-owned presentation formatting to text leaving the runtime. */
public final class OutgoingTextFormatter {
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
        while (remaining.length() > maxLineLength) {
            int breakAt = lastWhitespaceAtOrBefore(remaining, maxLineLength);
            if (breakAt < 0) {
                output.append(remaining);
                return;
            }
            output.append(remaining, 0, breakAt).append('\n');
            remaining = stripLeadingWhitespace(remaining.substring(breakAt + 1));
        }
        output.append(remaining);
    }

    private static int lastWhitespaceAtOrBefore(String text, int maxLineLength) {
        int last = Math.min(maxLineLength, text.length() - 1);
        for (int i = last; i >= 0; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
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
        if (maxLineLength < MudlibBoundary.MIN_MAX_LINE_LENGTH
                || maxLineLength > MudlibBoundary.MAX_MAX_LINE_LENGTH) {
            throw new IllegalArgumentException("maxLineLength must be between "
                    + MudlibBoundary.MIN_MAX_LINE_LENGTH + " and " + MudlibBoundary.MAX_MAX_LINE_LENGTH + ".");
        }
    }
}
