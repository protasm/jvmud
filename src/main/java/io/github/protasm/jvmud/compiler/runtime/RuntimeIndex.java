package io.github.protasm.jvmud.compiler.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Runtime indexing helpers for dynamically typed LPC values. */
public final class RuntimeIndex {
    private RuntimeIndex() {}

    /** Runtime marker for an LPC from-end index bound such as {@code <1}. */
    public record FromEnd(int distance) {}

    public static Object fromEnd(int distance) {
        return new FromEnd(distance);
    }

    public static Object get(Object target, int index) {
        if (target instanceof CharSequence text) {
            return Integer.valueOf(stringCharCode(text.toString(), index));
        }
        if (target instanceof List<?> list) {
            return list.get(index);
        }
        throw new IllegalArgumentException("Cannot index value: " + target);
    }

    /**
     * Reads a value through a dynamically typed LPC index target.
     *
     * <p>Missing mapping keys evaluate as LPC false, represented by {@code 0}, rather than leaking
     * Java {@code null} into generated LPC code.
     */
    public static Object get(Object target, Object index) {
        if (target instanceof Map<?, ?> map) {
            Object value = map.get(index);
            return value != null ? value : Integer.valueOf(0);
        }
        int numericIndex = resolveIndex(index, sizeOf(target));
        return get(target, numericIndex);
    }

    /** Stores a value through a dynamically typed LPC index target. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object set(Object target, Object index, Object value) {
        if (target instanceof Map map) {
            return map.put(index, value);
        }
        if (target instanceof List list) {
            int numericIndex = resolveIndex(index, list.size());
            return list.set(numericIndex, value);
        }
        throw new IllegalArgumentException("Cannot assign indexed value: " + target);
    }

    /** Mutates a numeric indexed value and returns the previous value for postfix operators. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object mutateNumber(Object target, Object index, int delta) {
        if (target instanceof List list) {
            int numericIndex = resolveIndex(index, list.size());
            Object oldValue = list.get(numericIndex);
            int oldNumber = oldValue instanceof Number number ? number.intValue() : 0;
            list.set(numericIndex, Integer.valueOf(oldNumber + delta));
            return oldValue;
        }
        throw new IllegalArgumentException("Cannot mutate indexed value: " + target);
    }

    /** Mutates a numeric indexed value and returns the updated value for prefix operators. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object mutateNumberPrefix(Object target, Object index, int delta) {
        if (target instanceof List list) {
            int numericIndex = resolveIndex(index, list.size());
            Object oldValue = list.get(numericIndex);
            int oldNumber = oldValue instanceof Number number ? number.intValue() : 0;
            Integer newValue = Integer.valueOf(oldNumber + delta);
            list.set(numericIndex, newValue);
            return newValue;
        }
        throw new IllegalArgumentException("Cannot mutate indexed value: " + target);
    }

    public static int stringCharCode(String text, Object index) {
        int numericIndex = resolveIndex(index, text == null ? 0 : text.length());
        return stringCharCodeAt(text, numericIndex);
    }

    private static int stringCharCodeAt(String text, int index) {
        if (text == null || index < 0 || index >= text.length()) {
            return 0;
        }
        return text.charAt(index);
    }

    public static Object slice(Object target, Object startValue, Object endValue) {
        if (target instanceof CharSequence text) {
            int start = resolveIndex(startValue, text.length());
            int end = inclusiveEnd(endValue, text.length());
            return text.subSequence(start, end + 1).toString();
        }
        if (target instanceof List<?> list) {
            int start = resolveIndex(startValue, list.size());
            int end = inclusiveEnd(endValue, list.size());
            return new ArrayList<>(list.subList(start, end + 1));
        }
        throw new IllegalArgumentException("Cannot slice value: " + target);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object replaceSlice(Object target, Object startValue, Object endValue, Object replacement) {
        if (!(target instanceof List list))
            throw new IllegalArgumentException("Cannot assign slice on value: " + target);
        if (!(replacement instanceof List<?> replacementList))
            throw new IllegalArgumentException("Slice assignment expects array replacement: " + replacement);

        int start = resolveIndex(startValue, list.size());
        int end = inclusiveEnd(endValue, list.size());
        if (start < 0 || start > list.size())
            throw new IndexOutOfBoundsException("Slice start out of range: " + start);
        if (end < start - 1)
            throw new IndexOutOfBoundsException("Slice end before start: " + end);
        if (end >= list.size())
            throw new IndexOutOfBoundsException("Slice end out of range: " + end);

        list.subList(start, end + 1).clear();
        list.addAll(start, replacementList);
        return replacement;
    }

    private static int inclusiveEnd(Object endValue, int size) {
        if (size == 0) {
            return -1;
        }
        if (endValue == null) {
            return size - 1;
        }
        if (endValue instanceof Number || endValue instanceof FromEnd)
            return resolveIndex(endValue, size);
        throw new IllegalArgumentException("Slice end expects integer or omitted bound: " + endValue);
    }

    private static int resolveIndex(Object index, int size) {
        if (index instanceof FromEnd fromEnd)
            return size - fromEnd.distance();
        if (index instanceof Number number)
            return number.intValue();
        return 0;
    }

    private static int sizeOf(Object target) {
        if (target instanceof CharSequence text)
            return text.length();
        if (target instanceof List<?> list)
            return list.size();
        return 0;
    }
}
