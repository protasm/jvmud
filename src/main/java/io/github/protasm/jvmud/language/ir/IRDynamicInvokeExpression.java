package io.github.protasm.jvmud.language.ir;

import io.github.protasm.jvmud.language.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

/** Dynamic dispatch; required calls fail if the actual target lacks an implementation. */
public record IRDynamicInvokeExpression(
        int line, IRExpression target, String methodName, List<IRExpression> arguments, RuntimeType type, boolean required)
        implements IRExpression {
    /** Existing explicit arrow calls remain optional when the target method is absent. */
    public IRDynamicInvokeExpression(int line, IRExpression target, String methodName,
            List<IRExpression> arguments, RuntimeType type) {
        this(line, target, methodName, arguments, type, false);
    }

    public IRDynamicInvokeExpression {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(methodName, "methodName");
        arguments = List.copyOf(arguments);
        Objects.requireNonNull(type, "type");
    }
}
