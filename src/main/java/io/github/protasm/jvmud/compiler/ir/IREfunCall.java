package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

/** A function call retaining explicit engine dispatch through bytecode generation. */
public record IREfunCall(int line, String name, List<IRExpression> arguments, RuntimeType type,
        boolean engineOnly) implements IRExpression {
    /** Creates an ordinary call that permits mudlib function shadowing. */
    public IREfunCall(int line, String name, List<IRExpression> arguments, RuntimeType type) {
        this(line, name, arguments, type, false);
    }

    /** Copies arguments and validates the resolved call metadata. */
    public IREfunCall {
        Objects.requireNonNull(name, "name");
        arguments = List.copyOf(arguments);
        Objects.requireNonNull(type, "type");
    }
}
