package io.github.protasm.jvmud.transpiler;

/** A checked local declaration substitution, selected by source file, method and local name. */
public record LocalTypeOverride(String file, String method, String local, String expectedType, String replacementType) {
    public LocalTypeOverride {
        var validated = new FieldTypeOverride(file, local, expectedType, replacementType);
        if (method == null || !method.matches("[A-Za-z_][A-Za-z_0-9]*"))
            throw new IllegalArgumentException("Invalid override method name: " + method);
        file = validated.file();
        expectedType = validated.expectedType();
        replacementType = validated.replacementType();
    }
}
