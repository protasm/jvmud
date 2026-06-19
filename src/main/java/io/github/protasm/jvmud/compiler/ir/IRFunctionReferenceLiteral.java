package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

/**
 * IR value for an LPC named function reference such as {@code #'helper}.
 *
 * <p>The value is emitted as a runtime callable bound to the generated object that created it. The
 * callable performs late reflective invocation when an efun such as {@code filter} or
 * {@code jvmud_filter_indices} consumes it.</p>
 */
public record IRFunctionReferenceLiteral(int line, String methodName, RuntimeType type) implements IRExpression {
    public IRFunctionReferenceLiteral {
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(type, "type");
    }
}
