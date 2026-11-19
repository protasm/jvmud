package io.github.protasm.jvmud.engine.instance.efun;

import static io.github.protasm.jvmud.engine.instance.efun.EngineEfunDefinition.of;
import static io.github.protasm.jvmud.engine.instance.efun.LPCObjectEfuns.resolveTarget;

import io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntimeContext;
import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.runtime.Truth;

import java.util.ArrayList;
import java.util.List;

/** Containment, entity aliases, light and perception in the hosted world. */
final class WorldEfuns {
    private WorldEfuns() {}

    /** Defines the operations owned by this service boundary. */
    static List<EngineEfunDefinition> functions() {
        List<EngineEfunDefinition> efuns = new ArrayList<>();
        efuns.add(of("jvmud_emit_world_event", LPCType.LPCVOID,
                List.of(LPCType.LPCMIXED, LPCType.LPCSTRING, LPCType.LPCMIXED, LPCType.LPCSTRING),
                (runtime, args) -> {
                    runtime.emitWorldEvent(resolveTarget(runtime, args[0]), (String) args[1], args[2], (String) args[3]);
                    return null;
                }));
        efuns.add(of("jvmud_deliver_world_event", LPCType.LPCVOID,
                List.of(LPCType.LPCMIXED, LPCType.LPCSTRING, LPCType.LPCMIXED, LPCType.LPCSTRING),
                (runtime, args) -> {
                    runtime.deliverWorldEvent(resolveTarget(runtime, args[0]), (String) args[1], args[2], (String) args[3]);
                    return null;
                }));
        efuns.add(of("jvmud_emit_perceivable", LPCType.LPCVOID, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> emitPerceivable(runtime, args[0], args[1])));
        efuns.add(of("jvmud_emit_perceivable_except", LPCType.LPCVOID,
                List.of(LPCType.LPCMIXED, LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> emitPerceivableExcept(runtime, args[0], args[1], args[2])));
        efuns.add(of("jvmud_emit_perceivable_at", LPCType.LPCVOID, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> emitPerceivableAt(runtime, args[0], args[1])));
        efuns.add(of("jvmud_move_entity", LPCType.LPCVOID, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.moveObject(resolveTarget(runtime, args[0]), resolveTarget(runtime, args[1]));
                    return null;
                }));
        efuns.add(of("jvmud_find_entity", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.present(args[0], null)));
        efuns.add(of("jvmud_find_entity", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> runtime.present(args[0], args[1])));
        efuns.add(of("jvmud_bind_entity_alias", LPCType.LPCVOID,
                List.of(LPCType.LPCOBJECT, LPCType.LPCSTRING, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.bindEntityAlias(args[0], args[1], args[2]);
                    return null;
                }));
        efuns.add(of("jvmud_find_entity_alias", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING, LPCType.LPCMIXED),
                (runtime, args) -> runtime.findEntityAlias(args[0], args[1])));
        efuns.add(of("jvmud_entity_has_alias", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED, LPCType.LPCSTRING),
                (runtime, args) -> runtime.entityHasAlias(args[0], args[1])));
        efuns.add(of("jvmud_set_entity_location", LPCType.LPCVOID,
                List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.moveObject(args[0], args[1]);
                    return null;
                }));
        efuns.add(of("jvmud_set_entity_translucent", LPCType.LPCVOID,
                List.of(LPCType.LPCMIXED, LPCType.LPCSTATUS),
                (runtime, args) -> {
                    runtime.setEntityTranslucent(args[0], Truth.isTruthy(args[1]));
                    return null;
                }));
        efuns.add(of("jvmud_entity_translucent", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.entityTranslucent(args[0]) ? 1 : 0));
        efuns.add(of("jvmud_first_entity_at", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.firstInventory(args[0])));
        efuns.add(of("jvmud_next_entity_at", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.nextInventory(args[0])));
        efuns.add(of("jvmud_set_light", LPCType.LPCINT, List.of(LPCType.LPCINT),
                (runtime, args) -> runtime.setLight(((Number) args[0]).intValue())));
        efuns.add(of("jvmud_entity_location", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.environment(null)));
        efuns.add(of("jvmud_entity_location", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.environment(args[0])));
        return List.copyOf(efuns);
    }

    private static Object emitPerceivable(MudlibRuntimeContext runtime, Object emitter, Object message) {
        runtime.emitPerceivable(resolveTarget(runtime, emitter), message);
        return null;
    }

    private static Object emitPerceivableExcept(
            MudlibRuntimeContext runtime, Object emitter, Object message, Object excluded) {
        runtime.emitPerceivableExcept(
                resolveTarget(runtime, emitter),
                message,
                resolveTarget(runtime, excluded));
        return null;
    }

    private static Object emitPerceivableAt(MudlibRuntimeContext runtime, Object location, Object message) {
        runtime.emitPerceivableAt(resolveTarget(runtime, location), message);
        return null;
    }
}
