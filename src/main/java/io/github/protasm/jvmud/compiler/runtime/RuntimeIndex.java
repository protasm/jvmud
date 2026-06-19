package io.github.protasm.jvmud.compiler.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Runtime indexing helpers for dynamically typed LPC values. */
public final class RuntimeIndex {
    private RuntimeIndex() {}

    public static Object get(Object target, int index) {
        if (target instanceof CharSequence text) {
            return Integer.valueOf(stringCharCode(text.toString(), index));
        }
        if (target instanceof List<?> list) {
            return list.get(index);
        }
        throw new IllegalArgumentException("Cannot index value: " + target);
    }

    public static Object get(Object target, Object index) {
        if (target instanceof Map<?, ?> map) {
            return map.get(index);
        }
        int numericIndex = index instanceof Number number ? number.intValue() : 0;
        return get(target, numericIndex);
    }

    /** Stores a value through a dynamically typed LPC index target. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object set(Object target, Object index, Object value) {
        if (target instanceof Map map) {
            return map.put(index, value);
        }
        if (target instanceof List list) {
            int numericIndex = index instanceof Number number ? number.intValue() : 0;
            return list.set(numericIndex, value);
        }
        throw new IllegalArgumentException("Cannot assign indexed value: " + target);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object mutateNumber(Object target, int index, int delta) {
        if (target instanceof List list) {
            Object oldValue = list.get(index);
            int oldNumber = oldValue instanceof Number number ? number.intValue() : 0;
            list.set(index, Integer.valueOf(oldNumber + delta));
            return oldValue;
        }
        throw new IllegalArgumentException("Cannot mutate indexed value: " + target);
    }

    public static int stringCharCode(String text, int index) {
        if (text == null || index < 0 || index >= text.length()) {
            return 0;
        }
        return text.charAt(index);
    }

    public static Object slice(Object target, int start, Object endValue) {
        if (target instanceof CharSequence text) {
            int end = inclusiveEnd(endValue, text.length());
            return text.subSequence(start, end + 1).toString();
        }
        if (target instanceof List<?> list) {
            int end = inclusiveEnd(endValue, list.size());
            return new ArrayList<>(list.subList(start, end + 1));
        }
        throw new IllegalArgumentException("Cannot slice value: " + target);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object replaceSlice(Object target, int start, Object endValue, Object replacement) {
        if (!(target instanceof List list))
            throw new IllegalArgumentException("Cannot assign slice on value: " + target);
        if (!(replacement instanceof List<?> replacementList))
            throw new IllegalArgumentException("Slice assignment expects array replacement: " + replacement);

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
        if (endValue instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalArgumentException("Slice end expects integer or omitted bound: " + endValue);
    }
}
