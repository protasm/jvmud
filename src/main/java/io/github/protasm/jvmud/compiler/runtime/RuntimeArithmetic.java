package io.github.protasm.jvmud.compiler.runtime;

import java.util.List;

/** Runtime arithmetic helpers for dynamically typed LPC expressions. */
public final class RuntimeArithmetic {
    private RuntimeArithmetic() {}

    /**
     * Adds two dynamic LPC values.
     *
     * <p>LPC mudlibs often combine values returned from object calls before the compiler can know
     * whether they are numbers or strings. JVMud preserves that late choice here: numeric pairs add
     * numerically, while either text operand produces string concatenation.</p>
     */
    public static Object add(Object left, Object right) {
        if (left instanceof CharSequence || right instanceof CharSequence) {
            return String.valueOf(left) + right;
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            if (left instanceof Float
                    || left instanceof Double
                    || right instanceof Float
                    || right instanceof Double) {
                return Float.valueOf(leftNumber.floatValue() + rightNumber.floatValue());
            }
            return Integer.valueOf(leftNumber.intValue() + rightNumber.intValue());
        }
        return String.valueOf(left) + right;
    }

    /**
     * Subtracts two dynamic LPC values.
     *
     * <p>Runtime strings and arrays use LPC's difference operators. Numeric pairs subtract
     * numerically. Other pairs are rejected so incompatible dynamic values fail loudly instead of
     * being silently coerced into unrelated behavior.</p>
     */
    public static Object subtract(Object left, Object right) {
        if (left instanceof List<?> || right instanceof List<?>) {
            return RuntimeArray.difference(left, right);
        }
        if (left instanceof CharSequence || right instanceof CharSequence) {
            return RuntimeString.difference(left, right);
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            if (left instanceof Float
                    || left instanceof Double
                    || right instanceof Float
                    || right instanceof Double) {
                return Float.valueOf(leftNumber.floatValue() - rightNumber.floatValue());
            }
            return Integer.valueOf(leftNumber.intValue() - rightNumber.intValue());
        }
        throw new IllegalArgumentException("Subtraction expects numeric, string, or array values");
    }
}
