package io.github.protasm.jvmud.language.execution;

import java.util.function.Supplier;

/**
 * Host-neutral invocation boundary for a materialized LPC object.
 *
 * <p>An object handle uses this contract to enter its owning execution context. Implementations own
 * that context and the method-resolution policy; a handle does not create another reflection path or
 * infer anything about the host's worlds, sessions, or persistence.</p>
 */
public interface LPCExecution {
    /** Evaluates an action with the owning generated-code runtime bound to the current thread. */
    <T> T withRuntimeContext(Supplier<T> action);

    /** Evaluates an action with the supplied LPC receiver as the current object. */
    <T> T withCurrentObject(Object object, Supplier<T> action);

    /** Invokes a required method through the owner's LPC method-resolution policy. */
    Object invokeObject(Object object, String methodName, Object... arguments);

    /** Runs an action with the owning generated-code runtime bound to the current thread. */
    default void runWithRuntimeContext(Runnable action) {
        withRuntimeContext(() -> {
            action.run();
            return null;
        });
    }
}
