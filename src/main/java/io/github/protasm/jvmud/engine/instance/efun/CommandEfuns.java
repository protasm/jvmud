package io.github.protasm.jvmud.engine.instance.efun;

import static io.github.protasm.jvmud.engine.instance.efun.EngineEfunDefinition.of;
import static io.github.protasm.jvmud.engine.instance.efun.LPCObjectEfuns.resolveTarget;

import io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntimeContext;
import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.runtime.Truth;

import java.util.ArrayList;
import java.util.List;

/** Command actors, action registration and mudlib-owned command dispatch. */
final class CommandEfuns {
    private CommandEfuns() {}

    /** Defines the operations owned by this service boundary. */
    static List<EngineEfunDefinition> functions() {
        List<EngineEfunDefinition> efuns = new ArrayList<>();
        efuns.add(of("jvmud_current_actor", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.currentCommandActor() != null
                        ? runtime.currentCommandActor()
                        : runtime.currentObject()));
        efuns.add(of("jvmud_current_agent", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> currentAgent(runtime)));
        efuns.add(of("jvmud_current_verb", LPCType.LPCSTRING, List.of(),
                (runtime, args) -> runtime.currentCommandVerb()));
        efuns.add(of("jvmud_configure_command_aliases", LPCType.LPCVOID, List.of(LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.configureCommandAliases(args[0]);
                    return null;
                }));
        efuns.add(of("jvmud_notify_fail", LPCType.LPCINT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.notifyCommandFailure(args[0])));
        efuns.add(of("jvmud_dispatch_entity_command", LPCType.LPCMIXED,
                List.of(LPCType.LPCMIXED, LPCType.LPCSTRING),
                (runtime, args) -> dispatchEntityCommand(runtime, args[0], String.valueOf(args[1]))));
        // Retain the compatibility no-op; command actor scope is owned by the runtime.
        efuns.add(of("jvmud_set_this_player", LPCType.LPCVOID, List.of(LPCType.LPCMIXED),
                (runtime, args) -> null));
        efuns.add(of("jvmud_entity_commands_enabled", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.entityCommandsEnabled(args[0])));
        efuns.add(of("jvmud_enable_commands", LPCType.LPCVOID, List.of(),
                (runtime, args) -> {
                    runtime.enableEntityCommands(runtime.currentObject());
                    return null;
                }));
        efuns.add(of("jvmud_add_action", LPCType.LPCVOID, List.of(LPCType.LPCSTRING),
                (runtime, args) -> {
                    runtime.rememberActionMethod(String.valueOf(args[0]));
                    return null;
                }));
        efuns.add(of("jvmud_add_action", LPCType.LPCVOID, List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> {
                    runtime.rememberActionMethod(String.valueOf(args[0]));
                    runtime.registerVerb(String.valueOf(args[1]));
                    return null;
                }));
        efuns.add(of("jvmud_add_action", LPCType.LPCVOID,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING, LPCType.LPCSTATUS),
                (runtime, args) -> {
                    runtime.rememberActionMethod(String.valueOf(args[0]));
                    runtime.registerVerb(String.valueOf(args[1]), Truth.isTruthy(args[2]));
                    return null;
                }));
        efuns.add(of("jvmud_remove_action", LPCType.LPCINT, List.of(LPCType.LPCINT),
                (runtime, args) -> runtime.removeCommandActions(
                        runtime.currentCommandActor(), runtime.currentObject())));
        efuns.add(of("jvmud_remove_action", LPCType.LPCINT, List.of(LPCType.LPCINT, LPCType.LPCMIXED),
                (runtime, args) -> runtime.removeCommandActions(args[1], runtime.currentObject())));
        efuns.add(of("jvmud_add_verb", LPCType.LPCVOID, List.of(LPCType.LPCSTRING),
                (runtime, args) -> {
                    runtime.registerVerb(String.valueOf(args[0]));
                    return null;
                }));
        return List.copyOf(efuns);
    }

    private static Object dispatchEntityCommand(MudlibRuntimeContext runtime, Object actor, String commandLine) {
        Object resolvedActor = resolveTarget(runtime, actor);
        if (resolvedActor == null) {
            return 0;
        }

        return runtime.withCommandActor(resolvedActor, () -> runtime.dispatchCommand(resolvedActor, commandLine));
    }

    /**
     * Returns the active world-present agent for legacy command-giver semantics.
     *
     * <p>This deliberately does not fall back to the currently executing LPC object: service objects,
     * daemons, and other non-agent code can execute during an action without becoming the action's
     * agent.</p>
     */
    private static Object currentAgent(MudlibRuntimeContext runtime) {
        Object agent = runtime.currentCommandActor();
        return agent != null ? agent : 0;
    }
}
