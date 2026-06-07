package io.github.protasm.jvmud.compiler.runtime;

import java.util.Objects;

/** Runtime equality helpers for LPC compatibility semantics. */
public final class RuntimeEquality {
    private RuntimeEquality() {}

    public static boolean equals(Object left, Object right) {
        if (isNullEquivalent(left) && isNullEquivalent(right)) {
            return true;
        }
        return Objects.equals(left, right);
    }

    private static boolean isNullEquivalent(Object value) {
        return value == null || (value instanceof Number number && number.doubleValue() == 0.0d);
    }
}
