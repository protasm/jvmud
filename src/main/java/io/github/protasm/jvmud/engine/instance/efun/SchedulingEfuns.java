package io.github.protasm.jvmud.engine.instance.efun;

import static io.github.protasm.jvmud.engine.instance.efun.EngineEfunDefinition.of;

import io.github.protasm.jvmud.language.compiler.type.LPCType;

import java.util.ArrayList;
import java.util.List;

/** Recurring ticks and deferred callbacks on the owning mudlib clock. */
final class SchedulingEfuns {
    private SchedulingEfuns() {}

    /** Defines the operations owned by this service boundary. */
    static List<EngineEfunDefinition> functions() {
        List<EngineEfunDefinition> efuns = new ArrayList<>();
        efuns.add(of("jvmud_schedule_recurring_tick", LPCType.LPCVOID,
                List.of(LPCType.LPCINT, LPCType.LPCINT),
                (runtime, args) -> {
                    runtime.scheduleRecurringTick(
                            ((Number) args[0]).intValue(),
                            ((Number) args[1]).intValue());
                    return null;
                }));
        efuns.add(of("jvmud_schedule_deferred_callback", LPCType.LPCVOID,
                List.of(LPCType.LPCSTRING, LPCType.LPCINT), LPCType.LPCMIXED,
                (runtime, args) -> {
                    Object[] callbackArgs = new Object[Math.max(0, args.length - 2)];
                    if (callbackArgs.length > 0) {
                        System.arraycopy(args, 2, callbackArgs, 0, callbackArgs.length);
                    }
                    runtime.scheduleDeferredCallback(
                            String.valueOf(args[0]),
                            ((Number) args[1]).intValue(),
                            callbackArgs);
                    return null;
                }));
        efuns.add(of("jvmud_cancel_deferred_callback", LPCType.LPCINT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.cancelDeferredCallback(String.valueOf(args[0]))));
        return List.copyOf(efuns);
    }
}
