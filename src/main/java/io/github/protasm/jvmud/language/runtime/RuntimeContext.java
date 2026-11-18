package io.github.protasm.jvmud.language.runtime;

import java.util.function.Supplier;

/**
 * Language operations available to generated LPC bytecode and callable values.
 *
 * <p>This contract deliberately knows nothing about worlds, connections, databases, or installation
 * state. A host supplies an implementation and binds it through {@link RuntimeContextHolder} while
 * executing generated code. Keeping this boundary small lets the compiler and its generated classes
 * depend on LPC semantics rather than on a particular host.</p>
 */
public abstract class RuntimeContext {
    /** Resolves and invokes an LPC-facing function using the host's configured lookup policy. */
    public abstract Object invokeEfun(String name, int arity, Object[] arguments);

    /** Invokes a built-in function without passing through mudlib global-function overrides. */
    public abstract Object invokeEngineEfun(String name, int arity, Object[] arguments);

    /** Invokes a required LPC object method, reporting an error when it cannot be resolved. */
    public abstract Object invokeObject(Object target, String methodName, Object... arguments);

    /** Invokes an optional LPC object method, returning LPC zero when it is absent. */
    public abstract Object invokeOptionalObject(Object target, String methodName, Object... arguments);

    /** Returns whether an LPC object exposes a method accepting the supplied argument count. */
    public abstract boolean hasObjectMethod(Object target, String methodName, int arity);

    /**
     * Runs an inline callable with the Java receiver that owns its captured code.
     * Hosts that replace object behavior override this boundary to retain live state and identity.
     */
    public <T> T withObjectReceiver(Object receiver, Supplier<T> action) {
        return action.get();
    }

    /** Returns whether a value represents a destroyed object and therefore tests false in LPC. */
    public abstract boolean isDestroyedObject(Object value);
}
