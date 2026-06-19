package io.github.protasm.jvmud.compiler.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Runtime representation for an LPC function literal.
 *
 * <p>The compiler can currently parse, type-check, and pass these values through normal LPC value
 * paths. Executable values either call a generated inline-closure helper or invoke a named method on
 * the object that created the reference.</p>
 */
public final class RuntimeFunctionLiteral implements RuntimeCallable {
    private final String signature;
    private final int arity;
    private final Object target;
    private final String methodName;
    private final Object[] captures;
    private final InvocationKind invocationKind;

    public RuntimeFunctionLiteral(String signature) {
        this(signature, 0);
    }

    public RuntimeFunctionLiteral(String signature, int arity) {
        this(signature, arity, null, null, new Object[0], InvocationKind.NONE);
    }

    public RuntimeFunctionLiteral(String signature, int arity, Object target, String methodName) {
        this(signature, arity, target, methodName, new Object[0], InvocationKind.GENERATED_HELPER);
    }

    /**
     * Creates an executable inline callable bound to a generated helper method and captured local
     * values from the lexical point where the LPC closure literal was evaluated.
     *
     * @param signature display signature for diagnostics
     * @param arity number of LPC callback arguments expected by the closure body
     * @param target generated object that owns the helper method
     * @param methodName generated helper method name
     * @param captures boxed outer-local values captured by the closure literal
     */
    public RuntimeFunctionLiteral(
            String signature, int arity, Object target, String methodName, Object[] captures) {
        this(signature, arity, target, methodName, captures, InvocationKind.GENERATED_HELPER);
    }

    private RuntimeFunctionLiteral(
            String signature,
            int arity,
            Object target,
            String methodName,
            Object[] captures,
            InvocationKind invocationKind) {
        this.signature = Objects.requireNonNull(signature, "signature");
        this.arity = Math.max(0, arity);
        this.target = target;
        this.methodName = methodName;
        this.captures = captures != null ? captures.clone() : new Object[0];
        this.invocationKind = Objects.requireNonNull(invocationKind, "invocationKind");
    }

    /**
     * Creates a callable for a quoted LPC function symbol such as {@code #'helper}.
     *
     * <p>The callable is bound to the generated object that produced it and accepts whatever
     * arguments the consuming efun passes at runtime.</p>
     *
     * @param methodName LPC method name to invoke
     * @param target generated object that owns the method
     * @return executable named-method callable
     */
    public static RuntimeFunctionLiteral namedFunction(String methodName, Object target) {
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(target, "target");
        return new RuntimeFunctionLiteral("#'" + methodName, 0, target, methodName, new Object[0], InvocationKind.NAMED_METHOD);
    }

    public String signature() {
        return signature;
    }

    public int arity() {
        return arity;
    }

    public boolean executable() {
        return invocationKind != InvocationKind.NONE && target != null && methodName != null;
    }

    @Override
    public Object call(RuntimeContext runtime, Object... args) {
        if (!executable())
            throw new UnsupportedOperationException("Callable invocation is not implemented for " + signature + ".");

        if (invocationKind == InvocationKind.NAMED_METHOD) {
            return runtime.invokeOptionalObject(target, methodName, args != null ? args : new Object[0]);
        }

        RuntimeContext previous = RuntimeContextHolder.current();
        try {
            RuntimeContextHolder.setCurrent(runtime);
            Method method = target.getClass().getMethod(methodName, RuntimeContext.class, Object[].class, Object[].class);
            return method.invoke(target, runtime, args != null ? args : new Object[0], captures.clone());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Cannot invoke callable " + signature + ".", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException)
                throw runtimeException;
            if (cause instanceof Error error)
                throw error;
            throw new IllegalStateException("Callable " + signature + " failed.", cause);
        } finally {
            RuntimeContextHolder.setCurrent(previous);
        }
    }

    @Override
    public String toString() {
        return "#<function " + signature + ">";
    }

    private enum InvocationKind {
        NONE,
        GENERATED_HELPER,
        NAMED_METHOD
    }
}
