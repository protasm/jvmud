package io.github.protasm.jvmud.compiler.runtime;

import java.util.HashMap;
import java.util.Map;

/** Runtime helpers for LPC mapping operations that accept legacy false sentinels. */
public final class RuntimeMapping {
    private RuntimeMapping() {}

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
