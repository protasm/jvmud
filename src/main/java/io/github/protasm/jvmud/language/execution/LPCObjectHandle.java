package io.github.protasm.jvmud.language.execution;

import java.util.Objects;
import java.util.function.Supplier;

/** A materialized LPC object and the execution boundary through which it is invoked. */
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

    /** Returns the generated class that supplies this object's compiled behavior. */
    public Class<?> objectClass() {
        return objectClass;
    }

    /** Returns the materialized LPC receiver. */
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
