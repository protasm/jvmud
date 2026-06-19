package io.github.protasm.jvmud.compiler.runtime;

/**
 * Runtime contract for LPC callable values.
 *
 * <p>JVMud uses this interface for values produced by LPC closure syntax such as {@code (: ... :)}
 * and typed function literals. Implementations may capture generated object state and locals; efuns
 * that consume callbacks should depend on this interface rather than on a particular closure
 * syntax.</p>
 */
@FunctionalInterface
public interface RuntimeCallable {
    /**
     * Invokes the callable in an LPC runtime context.
     *
     * @param runtime active runtime context for callbacks that need object/session state
     * @param args LPC callback arguments
     * @return callback result as a JVMud runtime value
     */
    Object call(RuntimeContext runtime, Object... args);
}
