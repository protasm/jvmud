package io.github.protasm.jvmud.language.bytecode;

public class BytecodeCompileException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BytecodeCompileException(String message) {
        super(message);
    }

    public BytecodeCompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
