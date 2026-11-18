package io.github.protasm.jvmud.language.execution;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A stable LPC object identity and the execution boundary through which it is invoked.
 *
 * <p>The class is the revision observed when this handle was created. The owning host can replace
 * executable behavior later while {@link #instance()} and existing handles keep the same identity.
 * Invoke through this boundary rather than reflecting on the identity's original Java class.</p>
 */
public final class LPCObjectHandle {
    private final LPCExecution execution;
    private final String internalName;
    private final Class<?> objectClass;
    private final Object instance;

    /** Creates a handle after the owning host has materialized and registered an LPC object. */
    public LPCObjectHandle(LPCExecution execution, String internalName, Class<?> objectClass, Object instance) {
        this.execution = Objects.requireNonNull(execution, "execution");
        this.internalName = Objects.requireNonNull(internalName, "internalName");
        this.objectClass = Objects.requireNonNull(objectClass, "objectClass");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    /** Returns the source-derived LPC name supplied by the materializing host. */
    public String internalName() {
        return internalName;
    }

    /** Returns the generated class for the revision observed when this handle was created. */
    public Class<?> objectClass() {
        return objectClass;
    }

    /** Returns the stable LPC identity, which need not be the host's current Java behavior receiver. */
    public Object instance() {
        return instance;
    }

    /** Evaluates an action with the owning generated-code runtime bound. */
    public <T> T withRuntimeContext(Supplier<T> action) {
        return execution.withRuntimeContext(action);
    }

    /** Runs an action with the owning generated-code runtime bound. */
    public void runWithRuntimeContext(Runnable action) {
        execution.runWithRuntimeContext(action);
    }

    /** Returns the generated JVM binary class name. */
    public String getClassName() {
        return objectClass.getName();
    }

    /** Invokes an LPC method using the same argument adaptation as ordinary runtime calls. */
    public Object invoke(String methodName, Object... arguments) {
        return execution.invokeObject(instance, Objects.requireNonNull(methodName, "methodName"), arguments);
    }

    /** Invokes an LPC method and discards its return value. */
    public void invokeVoid(String methodName, Object... arguments) {
        invoke(methodName, arguments);
    }
}
