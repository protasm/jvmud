package io.github.protasm.jvmud.compiler.efun;

import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;

/**
 * Runtime implementation of one LPC-facing engine function.
 *
 * <p>Efuns are registered by name and arity. Generated LPC bytecode invokes them through
 * {@link RuntimeContext}, which supplies current object, command Persona, output, object identity, and
 * other execution state.</p>
 */
public interface Efun {
    /** Returns the function signature exposed to the compiler and runtime lookup tables. */
    EfunSignature signature();

    /** Executes the function with arguments already checked against the signature arity. */
    Object call(RuntimeContext context, Object[] args);

    /** Returns the compiler symbol associated with this function. */
    default Symbol symbol() {
        return signature().symbol();
    }

    /** Returns the number of LPC arguments this implementation accepts. */
    default int arity() {
        return signature().arity();
    }

    /**
     * Checks arity and invokes this function.
     *
     * @throws IllegalArgumentException if the supplied argument count differs from the signature
     */
    default Object invoke(RuntimeContext context, Object[] args) {
        Object[] a = (args == null) ? new Object[0] : args;

        if (a.length != arity())
            throw new IllegalArgumentException(
                    "engine function '" + symbol().name() + "' expects " + arity() + " arg(s); got " + a.length);

        return call(context, a);
    }
}
