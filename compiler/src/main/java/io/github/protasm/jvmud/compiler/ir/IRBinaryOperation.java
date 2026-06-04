package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.parser.type.BinaryOpType;
import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public record IRBinaryOperation(
        int line, BinaryOpType operator, IRExpression left, IRExpression right, RuntimeType type) implements IRExpression {
    public IRBinaryOperation {
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        Objects.requireNonNull(type, "type");
    }
}
