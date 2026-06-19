package io.github.protasm.jvmud.compiler.ir;

import io.github.protasm.jvmud.compiler.runtime.RuntimeType;
import java.util.Objects;

public record IRProtectedEval(int line, IRExpression body, boolean suppressLogging, RuntimeType type)
        implements IRExpression {
    public IRProtectedEval {
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(type, "type");
    }
}
