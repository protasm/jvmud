package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

/** A lowered LPC field, including whether it participates in object-state persistence. */
public record IRField(
        int line,
        String ownerInternalName,
        String name,
        RuntimeType type,
        IRExpression initializer,
        boolean persistent) {
    /** Creates a persistent field for callers that do not need explicit persistence metadata. */
    public IRField(
            int line, String ownerInternalName, String name, RuntimeType type, IRExpression initializer) {
        this(line, ownerInternalName, name, type, initializer, true);
    }

    public IRField {
        Objects.requireNonNull(ownerInternalName, "ownerInternalName");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }
}
