package io.github.protasm.jvmud.compiler.exec;

import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import java.util.List;

/** Signals failures while compiling or instantiating LPC objects for a runtime instance. */
public final class LPCRuntimeException extends RuntimeException {
    private final List<CompilationProblem> problems;

    public LPCRuntimeException(String message) {
        super(message);
        this.problems = List.of();
    }

    public LPCRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.problems = List.of();
    }

    public LPCRuntimeException(String message, List<CompilationProblem> problems) {
        super(message);
        this.problems = (problems != null) ? List.copyOf(problems) : List.of();
    }

    public List<CompilationProblem> problems() {
        return problems;
    }
}
