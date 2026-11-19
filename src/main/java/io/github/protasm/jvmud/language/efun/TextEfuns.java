package io.github.protasm.jvmud.language.efun;

import static io.github.protasm.jvmud.language.efun.EfunDefinition.of;

import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.runtime.RuntimeScanf;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** LPC text formatting, splitting, extraction and scanning. */
final class TextEfuns {
    private TextEfuns() {}

    /** Declares text functions, including the fixed and variadic LPC calling forms. */
    static List<Efun> functions() {
        List<Efun> efuns = new ArrayList<>();
        efuns.add(of("jvmud_lowercase_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> String.valueOf(args[0]).toLowerCase()));
        efuns.add(of("jvmud_uppercase_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> String.valueOf(args[0]).toUpperCase()));
        efuns.add(of("jvmud_split_text", LPCType.LPCARRAY,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> splitText(String.valueOf(args[0]), String.valueOf(args[1]))));
        efuns.add(of("jvmud_capitalize_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> capitalizeText(String.valueOf(args[0]))));
        efuns.add(of("jvmud_wrap_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> wrapText(args[0], 78)));
        efuns.add(of("jvmud_wrap_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> wrapText(args[0], ((Number) args[1]).intValue())));
        efuns.add(formatTextEfun());
        efuns.add(of("jvmud_extract_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> extractText(
                        String.valueOf(args[0]),
                        ((Number) args[1]).intValue(),
                        -1)));
        efuns.add(of("jvmud_extract_text", LPCType.LPCSTRING,
                List.of(LPCType.LPCMIXED, LPCType.LPCINT, LPCType.LPCINT),
                (runtime, args) -> extractText(
                        String.valueOf(args[0]),
                        ((Number) args[1]).intValue(),
                        ((Number) args[2]).intValue())));
        efuns.add(sscanfEfun());
        return List.copyOf(efuns);
    }

    private static String capitalizeText(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    /** Literal splitting preserves a single empty field even with an empty delimiter. */
    private static List<String> splitText(String text, String delimiter) {
        if (text.isEmpty()) return new ArrayList<>(List.of(""));
        if (delimiter.isEmpty()) {
            List<String> characters = new ArrayList<>();
            for (int i = 0; i < text.length(); i++) {
                characters.add(String.valueOf(text.charAt(i)));
            }
            return characters;
        }
        return new ArrayList<>(List.of(text.split(java.util.regex.Pattern.quote(delimiter), -1)));
    }

    private static String extractText(String value, int from, int to) {
        int length = value.length();
        int start = Math.max(0, from);
        int end = to < 0 ? length - 1 : Math.min(to, length - 1);
        if (start >= length || end < start) {
            return "";
        }
        return value.substring(start, end + 1);
    }

    private static Efun formatTextEfun() {
        return of("jvmud_format_text", LPCType.LPCSTRING, List.of(LPCType.LPCSTRING), LPCType.LPCMIXED,
                (runtime, args) -> formatText(args));
    }

    /** Keeps LPC object formatting and column directives alongside ordinary Java format tokens. */
    private static String formatText(Object[] args) {
        String format = String.valueOf(args[0]).replace("%O", "%s");
        Object[] values = new Object[args.length - 1];
        if (values.length > 0) {
            System.arraycopy(args, 1, values, 0, values.length);
        }
        if (format.contains("%=")) {
            return formatColumnText(format, values);
        }
        return String.format(Locale.ROOT, format, values);
    }

    private static String formatColumnText(String format, Object[] values) {
        StringBuilder output = new StringBuilder();
        int valueIndex = 0;
        for (int i = 0; i < format.length(); i++) {
            char current = format.charAt(i);
            if (current != '%') {
                output.append(current);
                continue;
            }
            if (i + 1 >= format.length()) {
                output.append(current);
                continue;
            }
            char next = format.charAt(i + 1);
            if (next == '%') {
                output.append('%');
                i++;
                continue;
            }
            if (next == '^') {
                output.append("%^");
                i++;
                continue;
            }

            int specifierIndex = findFormatSpecifier(format, i + 1);
            if (specifierIndex < 0) {
                output.append(current);
                continue;
            }
            String token = format.substring(i, specifierIndex + 1);
            FormatArgumentSlice slice = consumeFormatArguments(token, values, valueIndex);
            valueIndex = slice.nextIndex();
            char specifier = format.charAt(specifierIndex);
            if (token.indexOf('=') >= 0 && specifier == 's') {
                output.append(formatStringColumn(token, slice.arguments()));
            } else {
                String javaToken = token.replace("=", "");
                output.append(String.format(Locale.ROOT, javaToken, slice.arguments()));
            }
            i = specifierIndex;
        }
        return output.toString();
    }

    private static int findFormatSpecifier(String format, int start) {
        for (int i = start; i < format.length(); i++) {
            if ("sdiboxXeEfgGcOQ".indexOf(format.charAt(i)) >= 0) {
                return i;
            }
        }
        return -1;
    }

    private static FormatArgumentSlice consumeFormatArguments(String token, Object[] values, int valueIndex) {
        int argumentCount = 1;
        for (int i = 0; i < token.length(); i++) {
            if (token.charAt(i) == '*') {
                argumentCount++;
            }
        }
        Object[] arguments = new Object[argumentCount];
        for (int i = 0; i < argumentCount; i++) {
            arguments[i] = valueIndex + i < values.length ? values[valueIndex + i] : 0;
        }
        return new FormatArgumentSlice(arguments, valueIndex + argumentCount);
    }

    private static String formatStringColumn(String token, Object[] arguments) {
        int argumentIndex = 0;
        int width = 0;
        int precision = 0;
        boolean precisionMode = false;
        boolean leftAligned = token.indexOf('-') >= 0;
        boolean centered = token.indexOf('|') >= 0;
        for (int i = 1; i < token.length() - 1; i++) {
            char current = token.charAt(i);
            if (current == '.') {
                precisionMode = true;
                continue;
            }
            if (current == '*') {
                int value = numericFormatArgument(arguments[argumentIndex], argumentIndex);
                argumentIndex++;
                if (precisionMode) {
                    precision = Math.abs(value);
                } else {
                    width = Math.abs(value);
                    leftAligned |= value < 0;
                }
                continue;
            }
            if (Character.isDigit(current)) {
                int start = i;
                while (i + 1 < token.length() - 1 && Character.isDigit(token.charAt(i + 1))) {
                    i++;
                }
                int value = Integer.parseInt(token.substring(start, i + 1));
                if (precisionMode) {
                    precision = value;
                } else {
                    width = value;
                }
            }
        }

        Object value = argumentIndex < arguments.length ? arguments[argumentIndex] : "";
        int fieldWidth = width > 0 ? width : String.valueOf(value).length();
        int wrapWidth = precision > 0 ? precision : fieldWidth;
        return formatColumnLines(String.valueOf(value), fieldWidth, wrapWidth, leftAligned, centered);
    }

    private static int numericFormatArgument(Object value, int fallback) {
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static String formatColumnLines(
            String text, int fieldWidth, int wrapWidth, boolean leftAligned, boolean centered) {
        List<String> lines = wrappedColumnLines(text, Math.max(1, wrapWidth));
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                output.append('\n');
            }
            output.append(padColumnLine(lines.get(i), fieldWidth, leftAligned, centered));
        }
        return output.toString();
    }

    private static List<String> wrappedColumnLines(String text, int width) {
        List<String> lines = new ArrayList<>();
        for (String sourceLine : text.split("\n", -1)) {
            String trimmed = sourceLine.strip();
            if (trimmed.isEmpty()) {
                if (!lines.isEmpty()) {
                    lines.add("");
                }
                continue;
            }
            StringBuilder current = new StringBuilder();
            for (String word : trimmed.split("\\s+")) {
                if (current.isEmpty()) {
                    current.append(word);
                } else if (current.length() + 1 + word.length() <= width) {
                    current.append(' ').append(word);
                } else {
                    lines.add(current.toString());
                    current.setLength(0);
                    current.append(word);
                }
            }
            lines.add(current.toString());
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }

    private static String padColumnLine(String line, int fieldWidth, boolean leftAligned, boolean centered) {
        int padding = Math.max(0, fieldWidth - line.length());
        if (padding == 0) {
            return line;
        }
        if (centered) {
            int left = padding / 2;
            int right = padding - left;
            return " ".repeat(left) + line + " ".repeat(right);
        }
        if (leftAligned) {
            return line + " ".repeat(padding);
        }
        return " ".repeat(padding) + line;
    }

    private record FormatArgumentSlice(Object[] arguments, int nextIndex) {}

    /**
     * Wraps mudlib-owned description text while preserving LPC's false/empty text convention.
     *
     * <p>This helper intentionally lives beside the efuns instead of using the engine's outgoing
     * presentation formatter: mudlibs may store or compare the result, and legacy compatibility
     * expects a trailing newline for non-empty formatted text.</p>
     */
    private static String wrapText(Object value, int requestedWidth) {
        String text = String.valueOf(value);
        if (text.isEmpty()) {
            return "";
        }

        int width = requestedWidth > 0 ? requestedWidth : 78;
        StringBuilder wrapped = new StringBuilder(text.length() + 1);
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                wrapped.append('\n');
            }
            appendWrappedPlainLine(wrapped, lines[i], width);
        }
        if (!wrapped.isEmpty() && wrapped.charAt(wrapped.length() - 1) != '\n') {
            wrapped.append('\n');
        }
        return wrapped.toString();
    }

    private static void appendWrappedPlainLine(StringBuilder output, String line, int width) {
        String trimmed = line.strip();
        if (trimmed.isEmpty()) {
            return;
        }

        StringBuilder current = new StringBuilder();
        for (String word : trimmed.split("\\s+")) {
            if (current.isEmpty()) {
                current.append(word);
            } else if (current.length() + 1 + word.length() <= width) {
                current.append(' ').append(word);
            } else {
                output.append(current).append('\n');
                current.setLength(0);
                current.append(word);
            }
        }
        output.append(current);
    }

    private static Efun sscanfEfun() {
        return of("jvmud_sscanf", LPCType.LPCINT,
                List.of(LPCType.LPCMIXED, LPCType.LPCMIXED, LPCType.LPCMIXED), LPCType.LPCMIXED,
                (runtime, args) -> RuntimeScanf.scan(args[0], args[1], args.length - 2)[0]);
    }
}
