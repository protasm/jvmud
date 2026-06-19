package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

public record IREfunCall(int line, String name, List<IRExpression> arguments, RuntimeType type) implements IRExpression {
    public IREfunCall {
        Objects.requireNonNull(name, "name");
        arguments = List.copyOf(arguments);
        Objects.requireNonNull(type, "type");
    }
}
