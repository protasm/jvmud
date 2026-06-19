package io.github.protasm.jvmud.compiler.pipeline;

/** Receives coarse-grained compiler progress events for tools such as the admin CLI. */
public interface CompilationObserver {
    CompilationObserver NONE = new CompilationObserver() {};

    default void stageStarted(CompilationUnit unit, CompilationStage stage) {}

    default void stageSucceeded(CompilationUnit unit, CompilationStage stage) {}

    default void stageFailed(CompilationUnit unit, CompilationStage stage, CompilationProblem problem) {}
}
