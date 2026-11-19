package io.github.protasm.jvmud.engine.instance.efun;

import io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntimeContext;
import io.github.protasm.jvmud.engine.model.mudlib.EngineCapability;
import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.efun.Efun;
import io.github.protasm.jvmud.language.efun.EfunDefinition;
import io.github.protasm.jvmud.language.efun.EfunSignature;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** An engine service adapter with its registration-time host capability declared beside its body. */
final class EngineEfunDefinition implements Efun {
    private final EfunDefinition function;
    private final Optional<EngineCapability> requiredCapability;

    private EngineEfunDefinition(EfunDefinition function, Optional<EngineCapability> requiredCapability) {
        this.function = function;
        this.requiredCapability = requiredCapability;
    }

    /** Defines an engine operation that is available without an additional host-resource grant. */
    static EngineEfunDefinition of(String name, LPCType result, List<LPCType> parameters, Body body) {
        return of(name, result, parameters, null, body);
    }

    /** Defines an engine operation with fixed arguments and an optional variadic tail. */
    static EngineEfunDefinition of(String name, LPCType result, List<LPCType> parameters,
            LPCType tail, Body body) {
        Objects.requireNonNull(body, "body");
        EfunDefinition function = EfunDefinition.of(name, result, parameters, tail, (context, arguments) -> {
            if (context instanceof MudlibRuntimeContext mudlib) {
                return body.call(mudlib, arguments);
            }
            throw new IllegalArgumentException("Engine efuns require a mudlib execution context.");
        });
        return new EngineEfunDefinition(function, Optional.empty());
    }

    /** Adds the existing host-resource grant required to expose this operation to authored code. */
    EngineEfunDefinition requiring(EngineCapability capability) {
        return new EngineEfunDefinition(function, Optional.of(capability));
    }

    /** Reports whether the selected mudlib grants the resource used by this operation. */
    boolean availableWith(Set<EngineCapability> capabilities) {
        return requiredCapability.map(capabilities::contains).orElse(true);
    }

    @Override
    public EfunSignature signature() {
        return function.signature();
    }

    @Override
    public Object call(RuntimeContext context, Object[] arguments) {
        return function.call(context, arguments);
    }

    /** Receives the verified engine context at the service boundary. */
    @FunctionalInterface
    interface Body {
        Object call(MudlibRuntimeContext context, Object[] arguments);
    }
}
