package io.github.protasm.jvmud.compiler.runtime;

/** Runtime helpers for LPC string operations that differ from Java operators. */
public final class RuntimeString {
    private RuntimeString() {}

    public static String difference(Object left, Object right) {
        String text = left == null ? "" : String.valueOf(left);
        String needle = right == null ? "" : String.valueOf(right);
        if (needle.isEmpty())
            return text;
        return text.replace(needle, "");
    }
}
