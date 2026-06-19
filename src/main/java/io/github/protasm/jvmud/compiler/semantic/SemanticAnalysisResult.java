package io.github.protasm.jvmud.compiler.semantic;

import io.github.protasm.jvmud.compiler.pipeline.CompilationProblem;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Result of running semantic analysis over an AST. */
public final class SemanticAnalysisResult {
    private final SemanticModel semanticModel;
    private final List<CompilationProblem> problems;

    public SemanticAnalysisResult(SemanticModel semanticModel, List<CompilationProblem> problems) {
        this.semanticModel = semanticModel;
        this.problems = List.copyOf(Objects.requireNonNull(problems, "problems"));
    }

    public SemanticModel semanticModel() {
        return semanticModel;
    }

    public List<CompilationProblem> problems() {
        return Collections.unmodifiableList(problems);
    }

    public boolean succeeded() {
        return problems.isEmpty();
    }
}
