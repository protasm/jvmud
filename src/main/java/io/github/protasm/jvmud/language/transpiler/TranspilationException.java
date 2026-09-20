package io.github.protasm.jvmud.language.transpiler;

/** A checked source expectation failed; compilation must not continue with a partial adaptation. */
public final class TranspilationException extends IllegalArgumentException {
    private final Integer line;
    public TranspilationException(String message, Integer line) {
        super(message);
        this.line = line;
    }
    /** Original source line of a mismatched declaration, or null for a missing declaration. */
    public Integer line() { return line; }
}
