package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.parser.type.UnaryOpType;
import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public record IRUnaryOperation(int line, UnaryOpType operator, IRExpression operand, RuntimeType type)
        implements IRExpression {
    public IRUnaryOperation {
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(operand, "operand");
        Objects.requireNonNull(type, "type");
    }
}
