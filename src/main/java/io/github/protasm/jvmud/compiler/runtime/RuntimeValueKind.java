package io.github.protasm.jvmud.compiler.runtime;

/** Describes the runtime shape for LPC values after semantic typing. */
public enum RuntimeValueKind {
    INT,
    FLOAT,
    STATUS,
    STRING,
    OBJECT,
    MAPPING,
    MIXED,
    ARRAY,
    EFUN,
    /** Internal Java {@code null} marker used by helper calls, not an LPC source value. */
    INTERNAL_NULL,
    /** Compiler recovery marker for expressions that should not reach successful code generation. */
    ERROR,
    VOID
}
