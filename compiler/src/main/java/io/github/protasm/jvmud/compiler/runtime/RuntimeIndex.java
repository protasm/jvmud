package io.github.protasm.jvmud.compiler.runtime;

import java.util.List;

/** Runtime indexing helpers for dynamically typed LPC values. */
public final class RuntimeIndex {
    private RuntimeIndex() {}

    public static Object get(Object target, int index) {
        if (target instanceof CharSequence text) {
            return Integer.valueOf(text.charAt(index));
        }
        if (target instanceof List<?> list) {
            return list.get(index);
        }
        throw new IllegalArgumentException("Cannot index value: " + target);
    }
}
