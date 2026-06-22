package io.github.protasm.jvmud.compiler.runtime;

import java.util.List;
import java.util.Map;

/** Runtime coercion helpers for LPC compatibility values. */
public final class RuntimeCoercions {
    private RuntimeCoercions() {}

    public static Object zeroToNullReference(Object value) {
        if (value instanceof Number number && number.doubleValue() == 0.0d) {
            return null;
        }
        return value;
    }

    /**
     * Coerces a dynamically typed LPC value into an explicit {@code string} context.
     *
     * <p>Classic LPC mudlibs frequently use {@code mixed} property bags and the numeric
     * false sentinel {@code 0}. In dynamic JVMud string contexts, that sentinel remains
     * a false reference value so checks like {@code saved == 0} keep working. Other
     * dynamic values are rendered textually instead of relying on a raw Java
     * {@link String} cast.</p>
     */
    public static String toStringValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number && number.doubleValue() == 0.0d) {
            return null;
        }
        return String.valueOf(value);
    }

    /**
     * Coerces a dynamically typed LPC value into an explicit array context.
     *
     * <p>Some legacy mudlibs use {@code ([])} as a falsey empty collection placeholder before
     * passing it into array-typed helpers that only check {@code sizeof()}. LDMud tolerates that at
     * runtime; JVMud preserves the practical behavior only for the empty-mapping case and leaves
     * non-empty mappings incompatible with arrays.</p>
     */
    public static Object toArrayValue(Object value) {
        if (value instanceof Number number && number.doubleValue() == 0.0d) {
            return null;
        }
        if (value instanceof Map<?, ?> map && map.isEmpty()) {
            return List.of();
        }
        return value;
    }

    /** Returns an incremented numeric value for dynamically typed mutation expressions. */
    public static Object incrementNumber(Object value, int delta) {
        int oldNumber = value instanceof Number number ? number.intValue() : 0;
        return Integer.valueOf(oldNumber + delta);
    }
}
