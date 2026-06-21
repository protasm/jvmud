package io.github.protasm.jvmud.compiler.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Runtime helpers for LPC mapping operations that accept legacy false sentinels. */
public final class RuntimeMapping {
    private RuntimeMapping() {}

    /** Runtime representation for an LDMud mapping entry with multiple semicolon-separated values. */
    public record MultiValue(List<Object> values) {
        public MultiValue {
            if (values == null || values.isEmpty())
                throw new IllegalArgumentException("mapping multi-value entry requires at least one value");
            values = Collections.unmodifiableList(new ArrayList<>(values));
        }
    }

    /** Wraps an LDMud mapping entry's semicolon-separated values as one Java map value. */
    public static Object multiValue(Object... values) {
        return new MultiValue(Arrays.asList(values));
    }

    /** Selects one value slot from a possibly multi-value mapping entry. */
    public static Object select(Object value, Object index) {
        if (value instanceof MultiValue multiValue) {
            int numericIndex = index instanceof Number number ? number.intValue() : 0;
            return numericIndex >= 0 && numericIndex < multiValue.values().size()
                    ? multiValue.values().get(numericIndex)
                    : Integer.valueOf(0);
        }
        int numericIndex = index instanceof Number number ? number.intValue() : 0;
        return numericIndex == 0 ? value : Integer.valueOf(0);
    }

    /**
     * Merges LPC mapping operands while treating the false sentinel as an empty mapping.
     *
     * <p>The right operand wins duplicate keys, matching the direct {@code putAll} order the
     * bytecode emitter used before this helper.</p>
     */
    public static Map<Object, Object> merge(Object left, Object right) {
        Map<Object, Object> result = new HashMap<>();
        result.putAll(asMap(left));
        result.putAll(asMap(right));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<?, ?> asMap(Object value) {
        if (value instanceof Map<?, ?> map)
            return map;
        if (value == null)
            return Map.of();
        throw new IllegalArgumentException("Mapping operation expects mapping value: " + value);
    }
}
