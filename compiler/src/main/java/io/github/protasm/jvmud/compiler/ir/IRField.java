package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public record IRField(
        int line, String ownerInternalName, String name, RuntimeType type, IRExpression initializer) {
    public IRField {
        Objects.requireNonNull(ownerInternalName, "ownerInternalName");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }
}
