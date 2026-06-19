package io.github.protasm.jvmud.compiler.runtime;

/** Runtime coercion helpers for LPC compatibility values. */
public final class RuntimeCoercions {
    private RuntimeCoercions() {}

    public static Object zeroToNullReference(Object value) {
        if (value instanceof Number number && number.doubleValue() == 0.0d) {
            return null;
        }
        return value;
    }

    /** Returns an incremented numeric value for dynamically typed mutation expressions. */
    public static Object incrementNumber(Object value, int delta) {
        int oldNumber = value instanceof Number number ? number.intValue() : 0;
        return Integer.valueOf(oldNumber + delta);
    }
}
