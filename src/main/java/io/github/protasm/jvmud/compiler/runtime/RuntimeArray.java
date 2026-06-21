package io.github.protasm.jvmud.compiler.runtime;

import java.util.ArrayList;
import java.util.List;

/** Runtime helpers for LPC array operations that need dynamic equality semantics. */
public final class RuntimeArray {
    private RuntimeArray() {}

    /**
     * Concatenates LPC array operands while treating the false sentinel as an empty array.
     *
     * <p>LPC code commonly writes expressions such as {@code values + 0} when an optional array is
     * absent. The sentinel remains falsey at rest, but array arithmetic treats it as empty.</p>
     */
    public static List<Object> concat(Object left, Object right) {
        List<Object> result = new ArrayList<>();
        result.addAll(asList(left));
        result.addAll(asList(right));
        return result;
    }

    /**
     * Computes the LPC array difference while treating the false sentinel as an empty array.
     */
    public static List<Object> difference(Object left, Object right) {
        List<?> leftValues = asList(left);
        List<?> rightValues = asList(right);
        List<Object> result = new ArrayList<>();

        for (Object value : leftValues) {
            if (!contains(rightValues, value))
                result.add(value);
        }

        return result;
    }

    private static List<?> asList(Object value) {
        if (value instanceof List<?> list)
            return list;
        if (value == null)
            return List.of();
        throw new IllegalArgumentException("Array operation expects array value: " + value);
    }

    private static boolean contains(List<?> values, Object needle) {
        for (Object value : values) {
            if (RuntimeEquality.equals(value, needle))
                return true;
        }
        return false;
    }
}
