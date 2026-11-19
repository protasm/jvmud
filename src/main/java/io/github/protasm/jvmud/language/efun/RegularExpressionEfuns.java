package io.github.protasm.jvmud.language.efun;

import static io.github.protasm.jvmud.language.efun.EfunDefinition.of;

import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.runtime.RuntimeCallable;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** LPC regular-expression matching, replacement and compatibility pattern translation. */
final class RegularExpressionEfuns {
    private RegularExpressionEfuns() {}

    /** Declares matching, replacement and split forms with their established LPC signatures. */
    static List<Efun> functions() {
        List<Efun> efuns = new ArrayList<>();
        efuns.add(of("jvmud_regex_match", LPCType.LPCMIXED,
                List.of(LPCType.LPCARRAY, LPCType.LPCSTRING),
                (runtime, args) -> regexMatch(args[0], String.valueOf(args[1]), 0)));
        efuns.add(of("jvmud_regex_match", LPCType.LPCMIXED,
                List.of(LPCType.LPCARRAY, LPCType.LPCSTRING, LPCType.LPCINT),
                (runtime, args) -> regexMatch(args[0], String.valueOf(args[1]), ((Number) args[2]).intValue())));
        efuns.add(of("jvmud_regex_replace", LPCType.LPCSTRING,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING, LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> regexReplace(
                        runtime,
                        String.valueOf(args[0]),
                        String.valueOf(args[1]),
                        args[2],
                        ((Number) args[3]).intValue())));
        efuns.add(of("jvmud_regex_explode", LPCType.LPCARRAY,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> regexExplode(String.valueOf(args[0]), String.valueOf(args[1]))));
        return List.copyOf(efuns);
    }

    /** A zero replacement flag changes only the first match; any nonzero flag changes every match. */
    private static String regexReplace(RuntimeContext runtime, String input, String pattern, Object replacement, int flags) {
        String javaPattern = javaRegexPattern(pattern);
        if (replacement instanceof RuntimeCallable callback) {
            return regexReplaceWithCallback(runtime, input, javaPattern, callback, flags);
        }
        String javaReplacement = javaRegexReplacement(String.valueOf(replacement));
        Matcher matcher = Pattern.compile(javaPattern, Pattern.DOTALL).matcher(input);
        return flags == 0
                ? matcher.replaceFirst(javaReplacement)
                : matcher.replaceAll(javaReplacement);
    }

    private static String regexReplaceWithCallback(
            RuntimeContext runtime, String input, String javaPattern, RuntimeCallable callback, int flags) {
        Matcher matcher = Pattern.compile(javaPattern, Pattern.DOTALL).matcher(input);
        StringBuffer output = new StringBuffer();
        int replacements = 0;
        while (matcher.find()) {
            if (flags == 0 && replacements > 0) {
                break;
            }
            Object replacement = callback.call(runtime, matcher.group());
            matcher.appendReplacement(output, Matcher.quoteReplacement(String.valueOf(replacement)));
            replacements++;
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private static Object regexMatch(Object values, String pattern, int flags) {
        if (!(values instanceof List<?> source)) {
            throw new IllegalArgumentException("jvmud_regex_match expects an array of values");
        }
        int javaFlags = flags == 0 ? 0 : java.util.regex.Pattern.DOTALL;
        java.util.regex.Pattern compiled = java.util.regex.Pattern.compile(javaRegexPattern(pattern), javaFlags);
        List<Object> matches = new ArrayList<>();
        for (Object value : source) {
            String text = String.valueOf(value);
            if (compiled.matcher(text).find()) {
                matches.add(value);
            }
        }
        return matches.isEmpty() ? 0 : matches;
    }

    private static List<String> regexExplode(String input, String pattern) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(javaRegexPattern(pattern)).matcher(input);
        List<String> parts = new ArrayList<>();
        int lastEnd = 0;
        while (matcher.find()) {
            parts.add(input.substring(lastEnd, matcher.start()));
            parts.add(matcher.group());
            lastEnd = matcher.end();
        }
        parts.add(input.substring(lastEnd));
        return parts;
    }

    private static String javaRegexPattern(String pattern) {
        return escapeAnchorQuestionMark(escapeLiteralLeftBracketsInCharacterClasses(pattern));
    }

    /** Escapes compatibility-regexp literal {@code [} characters embedded inside character classes. */
    private static String escapeLiteralLeftBracketsInCharacterClasses(String pattern) {
        String literalLeftBracket = "\\" + "u005B";
        StringBuilder translated = new StringBuilder(pattern.length());
        boolean inCharacterClass = false;
        boolean escaped = false;
        for (int index = 0; index < pattern.length(); index++) {
            char current = pattern.charAt(index);
            if (escaped) {
                translated.append(current);
                escaped = false;
                continue;
            }
            if (current == '\\') {
                translated.append(current);
                escaped = true;
                continue;
            }
            if (current == '[') {
                if (inCharacterClass) {
                    translated.append(literalLeftBracket);
                } else {
                    translated.append(current);
                    inCharacterClass = true;
                }
                continue;
            }
            if (current == ']') {
                inCharacterClass = false;
            }
            translated.append(current);
        }
        return translated.toString();
    }

    /**
     * Keeps command aliases such as {@code ? [-v]} literal when a mudlib turns them into
     * an anchored pattern like {@code ^?( -v)*$}. Java treats {@code ?} after {@code ^} as a
     * quantifier; compatibility mudlibs commonly intend it as command text in that position.
     */
    private static String escapeAnchorQuestionMark(String pattern) {
        StringBuilder translated = new StringBuilder(pattern.length());
        boolean inCharacterClass = false;
        boolean escaped = false;
        char previous = 0;
        for (int index = 0; index < pattern.length(); index++) {
            char current = pattern.charAt(index);
            if (current == '?' && previous == '^' && !inCharacterClass && !escaped) {
                translated.append('\\');
            }
            translated.append(current);

            if (escaped) {
                escaped = false;
            } else if (current == '\\') {
                escaped = true;
            } else if (current == '[') {
                inCharacterClass = true;
            } else if (current == ']') {
                inCharacterClass = false;
            }
            previous = current;
        }
        return translated.toString();
    }

    /** Converts LPC backslash group references while keeping literal dollar signs literal. */
    private static String javaRegexReplacement(String replacement) {
        StringBuilder converted = new StringBuilder();
        for (int i = 0; i < replacement.length(); i++) {
            char current = replacement.charAt(i);
            if (current == '\\' && i + 1 < replacement.length() && Character.isDigit(replacement.charAt(i + 1))) {
                converted.append('$').append(replacement.charAt(++i));
            } else if (current == '\\') {
                converted.append("\\\\");
            } else if (current == '$') {
                converted.append("\\$");
            } else {
                converted.append(current);
            }
        }
        return converted.toString();
    }
}
