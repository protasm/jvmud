package io.github.protasm.jvmud.transpiler;

import java.nio.file.Path;
import java.util.Objects;

/** One checked field-type substitution, scoped to a mudlib-relative compilation unit. */
public record FieldTypeOverride(String file, String field, String expectedType, String replacementType) {
    public FieldTypeOverride {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(field, "field");
        Path path = Path.of(file);
        if (file.isBlank() || path.isAbsolute() || file.contains("\\")
                || !path.normalize().toString().equals(file) || path.startsWith(".."))
            throw new IllegalArgumentException("Override file must be a normalized mudlib-relative path: " + file);
        if (!field.matches("[A-Za-z_][A-Za-z_0-9]*"))
            throw new IllegalArgumentException("Invalid override field name: " + field);
        expectedType = normalizeType(expectedType);
        replacementType = normalizeType(replacementType);
    }

    /** Validates a field type and normalizes whitespace around array stars. */
    public static String normalizeType(String type) {
        Objects.requireNonNull(type, "field type");
        if (!type.trim().matches("(?:int|float|string|object|mixed|mapping|function|status)(?:\\s*\\*)*"))
            throw new IllegalArgumentException("Invalid override field type: " + type);
        return type.trim().replaceAll("\\s+", "");
    }
}
