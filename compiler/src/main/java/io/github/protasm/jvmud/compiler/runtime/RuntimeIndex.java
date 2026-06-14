package io.github.protasm.jvmud.compiler.runtime;

import java.util.ArrayList;
import java.util.List;

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
        throw new IllegalArgumentException("Slice end expects integer or nil: " + endValue);
    }
}
