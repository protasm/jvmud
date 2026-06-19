package io.github.protasm.jvmud.compiler.ir;

import java.util.Objects;

public record IRExpressionStatement(int line, IRExpression expression) implements IRStatement {
    public IRExpressionStatement {
        Objects.requireNonNull(expression, "expression");
    }
}
