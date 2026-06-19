package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

/** IR value for an LPC typed function literal that can be passed as a callable value. */
public record IRTypedFunctionLiteral(int line, String signature, RuntimeType type) implements IRExpression {
    public IRTypedFunctionLiteral {
        Objects.requireNonNull(signature, "signature");
        Objects.requireNonNull(type, "type");
    }
}
