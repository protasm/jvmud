package io.github.protasm.jvmud.language.compiler.pipeline;

public enum CompilationStage {
    SCAN,
    TRANSPILE,
    PARSE,
    ANALYZE,
    LOWER,
    COMPILE
}
