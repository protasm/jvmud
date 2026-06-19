package io.github.protasm.jvmud.compiler.ir;

import java.util.Objects;

public record IRMappingEntry(IRExpression key, IRExpression value) {
    public IRMappingEntry {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
    }
}
