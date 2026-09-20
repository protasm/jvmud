package io.github.protasm.jvmud.language.ir;

import io.github.protasm.jvmud.language.runtime.RuntimeType;
import java.util.Objects;

public record IRLocal(int line, String name, RuntimeType type, int slot, boolean parameter) {
    public IRLocal {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }
}
