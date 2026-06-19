package io.github.protasm.jvmud.compiler.runtime;

/** Runtime comparison helpers for LPC mixed/string/numeric relational operators. */
public final class RuntimeComparison {
    private RuntimeComparison() {}

    /** Returns whether {@code left} is greater than {@code right} using LPC-compatible ordering. */
    public static boolean greaterThan(Object left, Object right) {
        return compare(left, right) > 0;
    }

    /**
     * Returns whether {@code left} is greater than or equal to {@code right} using LPC-compatible
     * ordering.
     */
    public static boolean greaterThanOrEqual(Object left, Object right) {
        return compare(left, right) >= 0;
    }

    /** Returns whether {@code left} is less than {@code right} using LPC-compatible ordering. */
    public static boolean lessThan(Object left, Object right) {
        return compare(left, right) < 0;
    }

    /**
     * Returns whether {@code left} is less than or equal to {@code right} using LPC-compatible
     * ordering.
     */
    public static boolean lessThanOrEqual(Object left, Object right) {
        return compare(left, right) <= 0;
    }

    private static int compare(Object left, Object right) {
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue());
        }

        String leftText = left == null ? "" : String.valueOf(left);
        String rightText = right == null ? "" : String.valueOf(right);
        return leftText.compareTo(rightText);
    }
}
