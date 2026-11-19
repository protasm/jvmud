package io.github.protasm.jvmud.engine.instance.efun;

import io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntime;
import io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntimeContext;
import io.github.protasm.jvmud.engine.model.mudlib.EngineCapability;
import io.github.protasm.jvmud.language.efun.Efun;
import io.github.protasm.jvmud.language.efun.LanguageEfuns;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Composes the core LPC function vocabulary for one hosted mudlib.
 *
 * <p>Language-only functions come from {@link LanguageEfuns}. Engine operations are grouped by
 * their object, world, session, command, scheduling, file, database and host responsibilities.
 * Each service definition declares its existing capability requirement beside its implementation;
 * selecting a catalog does not execute the operation or acquire its resources.</p>
 *
 * <p>The established {@code jvmud_} names, overloads and registration entry points remain stable.
 * Legacy LPC aliases belong to the mudlib's configuration and compatibility objects.</p>
 */
public final class EngineEfuns {
    private EngineEfuns() {}

    /** Registers the complete language and service catalogs in an engine execution context. */
    public static void registerCore(MudlibRuntimeContext context) {
        registerCore(context, EnumSet.allOf(EngineCapability.class));
    }

    /** Registers language functions and the engine operations granted by the selected mudlib. */
    public static void registerCore(MudlibRuntimeContext context, Set<EngineCapability> capabilities) {
        Objects.requireNonNull(context, "context");
        install(context::registerEfun, capabilities);
    }

    /** Registers the complete language and service catalogs through the host-facing LPC runtime. */
    public static void registerCore(MudlibRuntime runtime) {
        registerCore(runtime, EnumSet.allOf(EngineCapability.class));
    }

    /** Registers language functions and the engine operations granted by the selected mudlib. */
    public static void registerCore(MudlibRuntime runtime, Set<EngineCapability> capabilities) {
        Objects.requireNonNull(runtime, "runtime");
        install(runtime::registerEfun, capabilities);
    }

    private static void install(Consumer<Efun> register, Set<EngineCapability> capabilities) {
        Objects.requireNonNull(capabilities, "capabilities");
        LanguageEfuns.functions().forEach(register);
        for (EngineEfunDefinition function : services()) {
            if (function.availableWith(capabilities)) register.accept(function);
        }
    }

    /** Keeps the hosted composition visible in one place without mixing the service implementations. */
    private static List<EngineEfunDefinition> services() {
        List<EngineEfunDefinition> functions = new ArrayList<>();
        functions.addAll(LPCObjectEfuns.functions());
        functions.addAll(WorldEfuns.functions());
        functions.addAll(SessionEfuns.functions());
        functions.addAll(CommandEfuns.functions());
        functions.addAll(SchedulingEfuns.functions());
        functions.addAll(FileEfuns.functions());
        functions.addAll(DatabaseEfuns.functions());
        functions.addAll(HostEfuns.functions());
        return functions;
    }
}
