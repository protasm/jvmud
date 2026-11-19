package io.github.protasm.jvmud.language.efun;

import io.github.protasm.jvmud.language.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;

import java.util.List;
import java.util.Objects;

/** A callable LPC declaration paired with its host-neutral implementation. */
public final class EfunDefinition implements Efun {
    private final String name;
    private final LPCType returnType;
    private final List<LPCType> parameterTypes;
    private final LPCType varargsParameterType;
    private final Body body;

    private EfunDefinition(String name, LPCType returnType, List<LPCType> parameterTypes,
            LPCType varargsParameterType, Body body) {
        this.name = Objects.requireNonNull(name, "name");
        this.returnType = Objects.requireNonNull(returnType, "returnType");
        this.parameterTypes = List.copyOf(parameterTypes);
        this.varargsParameterType = varargsParameterType;
        this.body = Objects.requireNonNull(body, "body");
    }

    /** Defines an LPC function with a fixed parameter list. */
    public static EfunDefinition of(String name, LPCType returnType, List<LPCType> parameters, Body body) {
        return of(name, returnType, parameters, null, body);
    }

    /** Defines an LPC function with fixed parameters and an optional variadic tail type. */
    public static EfunDefinition of(String name, LPCType returnType, List<LPCType> parameters,
            LPCType varargsParameterType, Body body) {
        return new EfunDefinition(name, returnType, parameters, varargsParameterType, body);
    }

    /** Returns a fresh compiler declaration so mutable symbols cannot alter this definition. */
    @Override
    public EfunSignature signature() {
        return new EfunSignature(new Symbol(returnType, name), parameterTypes, varargsParameterType);
    }

    /** Executes the body against the supplied language context; ordinary callers use {@link Efun#invoke(RuntimeContext, Object[])}. */
    @Override
    public Object call(RuntimeContext context, Object[] args) {
        return body.call(context, args);
    }

    /** An implementation that consumes evaluated LPC arguments through the language context. */
    @FunctionalInterface
    public interface Body {
        /** Evaluates the function with already-checked argument arity. */
        Object call(RuntimeContext context, Object[] args);
    }
}
