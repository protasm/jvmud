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
}
