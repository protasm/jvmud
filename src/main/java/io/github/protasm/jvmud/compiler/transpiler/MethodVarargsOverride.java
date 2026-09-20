package io.github.protasm.jvmud.compiler.transpiler;

/** Opts one method into existing varargs semantics after checking its declared parameter count. */
public record MethodVarargsOverride(String file, String method, int expectedParameterCount) {
    public MethodVarargsOverride {
        var validated = new FieldTypeOverride(file, method, "mixed", "mixed");
        file = validated.file();
        if (expectedParameterCount < 0)
            throw new IllegalArgumentException("Expected parameter count must be nonnegative");
    }
}
