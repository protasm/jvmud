package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public record IRLocalLoad(int line, IRLocal local) implements IRExpression {
    public IRLocalLoad {
        Objects.requireNonNull(local, "local");
    }

    @Override
    public RuntimeType type() {
        return local.type();
    }
}
