package io.github.protasm.jvmud.engine.instance.lpc;

import io.github.protasm.jvmud.language.runtime.RuntimeCoercions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Selects and invokes public Java methods that implement authored LPC methods.
 *
 * <p>This owner contains reflection, LPC argument coercion and invocation diagnostics. The supplied
 * resolver retains the context's object-loading and lifecycle policy; the invocation stack owns
 * only lexical identities. Commands, actors, worlds and sessions do not participate here.</p>
 *
 * <p>The entry points deliberately retain the distinct historical calling policies: required calls
 * pad missing arguments, optional calls may also trim extras, lifecycle hooks prefer the most-derived
 * declaration, and caller-preserving calls leave the lexical receiver untouched. Their diagnostic
 * and exception-cause contracts are likewise preserved.</p>
 */
final class LPCMethodInvoker {
    private final LPCInvocationStack invocationStack;
    private final Function<Object, Object> targetResolver;
    private final Function<Object, String> objectDescription;

    /** Uses the context's target resolution and display identity without owning either policy. */
    LPCMethodInvoker(LPCInvocationStack invocationStack, Function<Object, Object> targetResolver,
            Function<Object, String> objectDescription) {
        this.invocationStack = Objects.requireNonNull(invocationStack, "invocationStack");
        this.targetResolver = Objects.requireNonNull(targetResolver, "targetResolver");
        this.objectDescription = Objects.requireNonNull(objectDescription, "objectDescription");
    }

    /**
     * Calls a required method, padding omitted trailing parameters and reporting a missing method.
     * An unresolved target retains the existing LPC-zero result.
     */
    Object invokeRequired(Object target, String methodName, Object... args) {
        Object resolvedTarget = targetResolver.apply(target);
        if (resolvedTarget == null) {
            return 0;
        }

        Object[] actualArgs = args == null ? new Object[0] : args;
        try {
            InvocationPlan invocation = findInvocation(resolvedTarget.getClass(), methodName, actualArgs);
            return invocationStack.withCurrentObject(resolvedTarget, () -> {
                try {
                    return invocation.invoke(resolvedTarget);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + describeInvocation(invocation, resolvedTarget)
                                    + causeSummary(e),
                            e);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    if (targetException instanceof LinkageError linkageError) {
                        throw new IllegalStateException("Failed to call generated LPC method", linkageError);
                    }
                    throw new IllegalArgumentException("Failed to call generated LPC method" + causeSummary(targetException), e);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + invocation.method().getName() + " on " + objectIdOrDescription(resolvedTarget)
                                    + causeSummary(e),
                            e);
                }
            });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Failed to call " + methodName + " on " + objectIdOrDescription(resolvedTarget)
                            + causeSummary(e),
                    e);
        }
    }

    /**
     * Calls an optional method using exact arity first, then padding or trimming trailing arguments.
     * An absent method or unresolved target returns LPC zero.
     */
    Object invokeOptional(Object target, String methodName, Object... args) {
        return invokeOptional(target, methodName, args, OptionalSelection.ORDINARY);
    }

    /**
     * Calls the most-derived lifecycle declaration before considering arity compatibility.
     * This prevents an inherited exact-arity hook from bypassing a descendant's hook.
     */
    Object invokeLifecycle(Object target, String methodName, Object... args) {
        return invokeOptional(target, methodName, args, OptionalSelection.MOST_DERIVED_LIFECYCLE);
    }

    private Object invokeOptional(Object target, String methodName, Object[] args, OptionalSelection selection) {
        Object resolvedTarget = targetResolver.apply(target);
        if (resolvedTarget == null) {
            return 0;
        }

        Object[] actualArgs = args == null ? new Object[0] : args;
        try {
            InvocationPlan invocation = switch (selection) {
                case ORDINARY -> findOptionalInvocation(resolvedTarget.getClass(), methodName, actualArgs);
                case MOST_DERIVED_LIFECYCLE -> findLifecycleInvocation(resolvedTarget.getClass(), methodName, actualArgs);
            };
            return invocationStack.withCurrentObject(resolvedTarget, () -> {
                try {
                    return invocation.invoke(resolvedTarget);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + describeInvocation(invocation, resolvedTarget)
                                    + causeSummary(e),
                            e);
                } catch (InvocationTargetException e) {
                    Throwable targetException = e.getTargetException();
                    String message = new StringBuilder("Failed to call ")
                            .append(safeDescribeInvocation(invocation, resolvedTarget))
                            .append(safeCauseSummary(targetException))
                            .toString();
                    if (targetException instanceof LinkageError linkageError) {
                        throw new IllegalStateException(message, linkageError);
                    }
                    throw new IllegalArgumentException(message, e);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(
                            "Failed to call " + invocation.method().getName() + " on " + objectIdOrDescription(resolvedTarget)
                                    + causeSummary(e),
                            e);
                } catch (LinkageError e) {
                    throw new IllegalStateException(
                            "Failed to link " + describeInvocation(invocation, resolvedTarget)
                                    + causeSummary(e),
                            e);
                }
            });
        } catch (NoSuchMethodException e) {
            return 0;
        }
    }

    /**
     * Uses optional argument adaptation while retaining the caller's current lexical object.
     * Unlike an ordinary optional call, an absent method is reported as a required-call failure.
     * An unresolved target retains the existing LPC-zero result.
     */
    Object invokePreservingCaller(Object target, String methodName, Object... args) {
        target = targetResolver.apply(target);
        if (target == null) {
            return 0;
        }

        Object[] actualArgs = args == null ? new Object[0] : args;
        try {
            InvocationPlan invocation = findOptionalInvocation(target.getClass(), methodName, actualArgs);
            try {
                return invocation.invoke(target);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Failed to call " + describeInvocation(invocation, target)
                                + causeSummary(e),
                        e);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                if (targetException instanceof LinkageError linkageError) {
                    throw new IllegalStateException("Failed to call generated LPC method", linkageError);
                }
                throw new IllegalArgumentException("Failed to call generated LPC method" + causeSummary(targetException), e);
            } catch (ReflectiveOperationException e) {
                throw new IllegalArgumentException(
                        "Failed to call " + invocation.method().getName() + " on " + objectIdOrDescription(target)
                                + causeSummary(e),
                        e);
            } catch (LinkageError e) {
                throw new IllegalStateException(
                        "Failed to link " + describeInvocation(invocation, target)
                                + causeSummary(e),
                        e);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "Failed to call " + methodName + " on " + objectIdOrDescription(target)
                            + causeSummary(e),
                    e);
        }
    }

    /** Reports whether a public method has exactly the requested arity. */
    boolean hasMethod(Class<?> targetClass, String methodName, int arity) {
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == arity) {
                return true;
            }
        }
        return false;
    }

    /** Reports whether a public method can accept leading arguments after LPC-zero padding. */
    boolean hasMethodAcceptingMissingArguments(Class<?> targetClass, String methodName, int arity) {
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() >= arity) {
                return true;
            }
        }
        return false;
    }

    /** Prefers the nearest declaring class among public methods with the exact requested arity. */
    private Method findMethod(Class<?> targetClass, String methodName, int arity) throws NoSuchMethodException {
        Method best = null;
        for (Method method : targetClass.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == arity) {
                if (best == null || declaredCloserToTarget(method, best, targetClass)) {
                    best = method;
                }
            }
        }
        if (best != null) {
            return best;
        }
        throw new NoSuchMethodException(targetClass.getName() + "." + methodName + "/" + arity);
    }

    /**
     * Plans exact-arity or LPC-zero-padded invocation without applying a missing-method policy.
     * Identifier probes use this narrower seam because absence permits an object-ID fallback,
     * while an exception from an existing identifier method must simply reject the identifier.
     */
    InvocationPlan findInvocation(Class<?> targetClass, String methodName, Object[] args)
            throws NoSuchMethodException {
        try {
            Method method = findMethod(targetClass, methodName, args.length);
            return new InvocationPlan(method, adaptArguments(method, args));
        } catch (NoSuchMethodException exactMiss) {
            Method best = null;
            for (Method method : targetClass.getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() < args.length) {
                    continue;
                }
                if (best == null || method.getParameterCount() < best.getParameterCount()
                        || (method.getParameterCount() == best.getParameterCount()
                                && declaredCloserToTarget(method, best, targetClass))) {
                    best = method;
                }
            }
            if (best == null) {
                throw exactMiss;
            }
            return new InvocationPlan(best, adaptArguments(best, padMissingArguments(best, args)));
        }
    }

    /**
     * Plans optional padding or trimming without deciding what an absent method means.
     * Message-routing probes keep their opt-out default and reflected-failure handling in the
     * context rather than introducing message policy into this reflection owner.
     */
    InvocationPlan findOptionalInvocation(Class<?> targetClass, String methodName, Object[] args)
            throws NoSuchMethodException {
        try {
            return findInvocation(targetClass, methodName, args);
        } catch (NoSuchMethodException exactMiss) {
            Method best = null;
            for (Method method : targetClass.getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() > args.length) {
                    continue;
                }
                if (best == null || method.getParameterCount() > best.getParameterCount()
                        || (method.getParameterCount() == best.getParameterCount()
                                && declaredCloserToTarget(method, best, targetClass))) {
                    best = method;
                }
            }
            if (best == null) {
                throw exactMiss;
            }
            Object[] trimmed = new Object[best.getParameterCount()];
            System.arraycopy(args, 0, trimmed, 0, trimmed.length);
            return new InvocationPlan(best, adaptArguments(best, trimmed));
        }
    }

    /**
     * Prioritizes declaring-class proximity before arity, then prefers padding over trimming and
     * the smallest argument-count adjustment. The superclass walk preserves the existing treatment
     * of public interface methods as farther away than declarations in the class hierarchy.
     */
    private InvocationPlan findLifecycleInvocation(Class<?> targetClass, String methodName, Object[] args)
            throws NoSuchMethodException {
        Method method = java.util.Arrays.stream(targetClass.getMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .min(java.util.Comparator
                        .comparingInt((Method candidate) -> inheritanceDistance(targetClass, candidate.getDeclaringClass()))
                        .thenComparingInt(candidate -> candidate.getParameterCount() >= args.length ? 0 : 1)
                        .thenComparingInt(candidate -> Math.abs(candidate.getParameterCount() - args.length)))
                .orElseThrow(() -> new NoSuchMethodException(targetClass.getName() + "." + methodName));
        Object[] supplied = method.getParameterCount() >= args.length
                ? padMissingArguments(method, args)
                : java.util.Arrays.copyOf(args, method.getParameterCount());
        return new InvocationPlan(method, adaptArguments(method, supplied));
    }

    private boolean declaredCloserToTarget(Method candidate, Method current, Class<?> targetClass) {
        return inheritanceDistance(targetClass, candidate.getDeclaringClass())
                < inheritanceDistance(targetClass, current.getDeclaringClass());
    }

    private int inheritanceDistance(Class<?> targetClass, Class<?> declaringClass) {
        int distance = 0;
        Class<?> cursor = targetClass;
        while (cursor != null) {
            if (cursor.equals(declaringClass)) {
                return distance;
            }
            cursor = cursor.getSuperclass();
            distance++;
        }
        return Integer.MAX_VALUE;
    }

    private Object[] adaptArguments(Method method, Object[] args) {
        Object[] adapted = new Object[args.length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            adapted[i] = adaptArgument(parameterTypes[i], args[i]);
        }
        return adapted;
    }

    /** Adapts LPC zero, arrays and numeric values to the selected generated method's Java types. */
    private Object adaptArgument(Class<?> parameterType, Object arg) {
        if (arg == null) {
            return defaultArgumentValue(parameterType);
        }
        if (parameterType == String.class) {
            // LPC zero is the absent string value; authored numeric text otherwise remains text.
            if (arg instanceof Number number && number.intValue() == 0) {
                return null;
            }
            return String.valueOf(arg);
        }
        if (List.class.isAssignableFrom(parameterType)) {
            return RuntimeCoercions.toArrayValue(arg);
        }
        if (parameterType == boolean.class && arg instanceof Number number) {
            return number.intValue() != 0;
        }
        if (parameterType == int.class && arg instanceof Number number) {
            return number.intValue();
        }
        if (parameterType == long.class && arg instanceof Number number) {
            return number.longValue();
        }
        if (parameterType == float.class && arg instanceof Number number) {
            return number.floatValue();
        }
        if (parameterType == double.class && arg instanceof Number number) {
            return number.doubleValue();
        }
        if (parameterType == byte.class && arg instanceof Number number) {
            return number.byteValue();
        }
        if (parameterType == short.class && arg instanceof Number number) {
            return number.shortValue();
        }
        if (parameterType == char.class && arg instanceof Number number) {
            return (char) number.intValue();
        }
        if (!parameterType.isPrimitive()) {
            return arg;
        }
        return arg;
    }

    /** Supplies LPC-zero defaults only for omitted trailing parameters, preserving supplied values. */
    private Object[] padMissingArguments(Method method, Object[] args) {
        Object[] padded = new Object[method.getParameterCount()];
        System.arraycopy(args, 0, padded, 0, args.length);
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = args.length; i < padded.length; i++) {
            padded[i] = defaultArgumentValue(parameterTypes[i]);
        }
        return padded;
    }

    private Object defaultArgumentValue(Class<?> parameterType) {
        if (!parameterType.isPrimitive()) {
            return null;
        }
        if (parameterType == boolean.class) {
            return false;
        }
        if (parameterType == float.class) {
            return 0.0f;
        }
        if (parameterType == double.class) {
            return 0.0d;
        }
        if (parameterType == long.class) {
            return 0L;
        }
        if (parameterType == byte.class) {
            return (byte) 0;
        }
        if (parameterType == short.class) {
            return (short) 0;
        }
        if (parameterType == char.class) {
            return (char) 0;
        }
        return 0;
    }

    /** Selects how optional calls prioritize declarations; presence and diagnostics stay unchanged. */
    private enum OptionalSelection {
        ORDINARY,
        MOST_DERIVED_LIFECYCLE
    }

    private String objectIdOrDescription(Object object) {
        return objectDescription.apply(object);
    }

    /**
     * A selected public method and its fully adapted arguments. Exposed within the package so
     * specialized probes can keep their domain-specific missing-method and failure policies.
     */
    record InvocationPlan(Method method, Object[] arguments) {
        /** Invokes this prepared call while leaving scope and failure policy to the caller. */
        Object invoke(Object receiver) throws ReflectiveOperationException {
            return method.invoke(receiver, arguments);
        }
    }

    /**
     * Reports the deepest useful cause, retaining the historical bounded traversal and wording.
     * Specialized probes share this formatter while retaining their own exception policy.
     */
    static String causeSummary(Throwable throwable) {
        Throwable cause = throwable;
        int depth = 0;
        while (cause.getCause() != null && depth < 25) {
            cause = cause.getCause();
            depth++;
        }
        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            message = cause.getClass().getSimpleName();
        }
        return new StringBuilder(": ").append(message).toString();
    }

    /**
     * Summarizes a reflected failure while preserving the original throwable if cause inspection
     * itself trips over a linkage problem.
     */
    private String safeCauseSummary(Throwable throwable) {
        try {
            return causeSummary(throwable);
        } catch (LinkageError | RuntimeException e) {
            return ": " + throwable.getClass().getSimpleName();
        }
    }

    private String describeInvocation(InvocationPlan invocation, Object target) {
        Method method = invocation.method();
        StringBuilder builder = new StringBuilder(method.getName())
                .append("/")
                .append(method.getParameterCount())
                .append(" on ")
                .append(objectIdOrDescription(target))
                .append(" with parameter types [");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameterTypes[i].getSimpleName());
        }
        builder.append("] and argument types [");
        Object[] args = invocation.arguments();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(args[i] == null ? "null" : args[i].getClass().getSimpleName());
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Describes a reflective LPC call without letting diagnostic formatting hide the original
     * invocation failure.
     */
    private String safeDescribeInvocation(InvocationPlan invocation, Object target) {
        try {
            return describeInvocation(invocation, target);
        } catch (LinkageError | RuntimeException e) {
            return invocation.method().getName() + "/" + invocation.method().getParameterCount()
                    + " on " + target.getClass().getName();
        }
    }

}
