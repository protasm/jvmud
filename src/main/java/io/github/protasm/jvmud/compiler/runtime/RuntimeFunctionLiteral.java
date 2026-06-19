package io.github.protasm.jvmud.compiler.runtime;

import java.util.Objects;

/**
 * Runtime placeholder for an LPC typed function literal.
 *
 * <p>The compiler can currently parse, type-check, and pass these values through calls. Executing
 * the captured body is a separate runtime feature, because callable invocation needs an explicit
 * contract for captured locals and callback arguments.</p>
 */
public final class RuntimeFunctionLiteral {
    private final String signature;

    public RuntimeFunctionLiteral(String signature) {
        this.signature = Objects.requireNonNull(signature, "signature");
    }

    public String signature() {
        return signature;
    }

    @Override
    public String toString() {
        return "#<function " + signature + ">";
    }
}
