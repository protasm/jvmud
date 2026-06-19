package io.github.protasm.jvmud.compiler.ir;

import java.util.Objects;

public record IRConditionalJump(int line, IRExpression condition, String trueLabel, String falseLabel)
        implements IRTerminator {
    public IRConditionalJump {
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(trueLabel, "trueLabel");
        Objects.requireNonNull(falseLabel, "falseLabel");
    }
}
