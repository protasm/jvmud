package io.github.protasm.jvmud.compiler.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Runtime support for LPC sscanf-style output captures. */
public final class RuntimeScanf {
    private RuntimeScanf() {}

    public static Object[] scan(Object inputValue, Object formatValue, int captureCount) {
        Object[] result = new Object[Math.max(0, captureCount) + 1];
        result[0] = 0;
        if (inputValue == null || formatValue == null || captureCount <= 0) {
            return result;
        }

        String input = String.valueOf(inputValue);
        FormatPattern format = compileFormat(String.valueOf(formatValue));
        if (format.captureTypes().isEmpty()) {
            return result;
        }

        Matcher matcher = format.pattern().matcher(input);
        if (!matcher.matches()) {
            return result;
        }

        int matched = Math.min(captureCount, format.captureTypes().size());
        for (int i = 0; i < matched; i++) {
            String capture = matcher.group(i + 1);
            result[i + 1] = format.captureTypes().get(i) == CaptureType.INT
                    ? Integer.valueOf(capture)
                    : capture;
        }
        result[0] = matched;
        return result;
    }

    private static FormatPattern compileFormat(String format) {
        StringBuilder regex = new StringBuilder("^");
        List<CaptureType> captureTypes = new ArrayList<>();
        StringBuilder literal = new StringBuilder();

        for (int i = 0; i < format.length(); i++) {
            char ch = format.charAt(i);
            if (ch == '%' && i + 1 < format.length()) {
                char specifier = format.charAt(++i);
                if (specifier == 's' || specifier == 'd') {
                    appendLiteral(regex, literal);
                    captureTypes.add(specifier == 'd' ? CaptureType.INT : CaptureType.STRING);
                    regex.append(specifier == 'd' ? "(-?\\d+)" : "(.*?)");
                    continue;
                }
                literal.append('%').append(specifier);
                continue;
            }
            literal.append(ch);
        }

        appendLiteral(regex, literal);
        regex.append("$");
        return new FormatPattern(Pattern.compile(regex.toString(), Pattern.DOTALL), captureTypes);
    }

    private static void appendLiteral(StringBuilder regex, StringBuilder literal) {
        if (literal.isEmpty()) {
            return;
        }
        regex.append(Pattern.quote(literal.toString()));
        literal.setLength(0);
    }

    private enum CaptureType {
        STRING,
        INT
    }

    private record FormatPattern(Pattern pattern, List<CaptureType> captureTypes) {}
}
