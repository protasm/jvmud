package io.github.protasm.jvmud.compiler.engine;

import io.github.protasm.jvmud.compiler.efun.Efun;
import io.github.protasm.jvmud.compiler.efun.EfunSignature;
import io.github.protasm.jvmud.compiler.exec.LpcRuntime;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Registers the core LPC-facing engine functions needed by compiled mudlib objects.
 *
 * <p>The functions in this class use JVMud engine vocabulary. Legacy LP driver names stay in
 * mudlib-side compatibility objects, where they can delegate to engine operations such as output
 * delivery, entity identity, location containment, command dispatch, and scheduling.</p>
 */
public final class EngineEfuns {
    private EngineEfuns() {}

    /** Registers the core efun set directly into a generated-code runtime context. */
    public static void registerCore(RuntimeContext context) {
        Objects.requireNonNull(context, "context");

        for (Efun efun : coreEfuns()) {
            context.registerEfun(efun);
        }
    }

    /** Registers the core efun set into a host-facing LPC runtime. */
    public static void registerCore(LpcRuntime runtime) {
        Objects.requireNonNull(runtime, "runtime");

        for (Efun efun : coreEfuns()) {
            runtime.registerEfun(efun);
        }
    }

    private static List<Efun> coreEfuns() {
        List<Efun> efuns = new ArrayList<>();
        efuns.add(efun("jvmud_write", LPCType.LPCVOID, List.of(LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.writeOutput(args[0]);
                    return null;
                }));
        efuns.add(efun("jvmud_send_to_entity", LPCType.LPCVOID, List.of(LPCType.LPCOBJECT, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.tellObject(args[0], args[1]);
                    return null;
                }));
        efuns.add(efun("jvmud_emit_perceivable", LPCType.LPCVOID, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> emitPerceivable(runtime, args[0], args[1])));
        efuns.add(efun("jvmud_emit_perceivable_at", LPCType.LPCVOID, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> emitPerceivableAt(runtime, args[0], args[1])));
        efuns.add(efun("jvmud_current_entity", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.currentObject()));
        efuns.add(efun("jvmud_current_actor", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.currentCommandActor() != null
                        ? runtime.currentCommandActor()
                        : runtime.currentObject()));
        efuns.add(efun("jvmud_current_verb", LPCType.LPCSTRING, List.of(),
                (runtime, args) -> runtime.currentCommandVerb()));
        efuns.add(efun("jvmud_time", LPCType.LPCINT, List.of(),
                (runtime, args) -> (int) (System.currentTimeMillis() / 1000L)));
        efuns.add(efun("jvmud_entity_id", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.objectId(args[0])));
        efuns.add(efun("jvmud_size", LPCType.LPCINT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> sizeOf(args[0])));
        efuns.add(efun("jvmud_users", LPCType.LPCARRAY, List.of(),
                (runtime, args) -> runtime.users()));
        efuns.add(efun("jvmud_query_idle", LPCType.LPCINT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.queryIdle(args[0])));
        efuns.add(efun("jvmud_query_ip_number", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.queryIpNumber(args[0])));
        efuns.add(efun("jvmud_read_mudlib_text", LPCType.LPCMIXED, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.readMudlibText(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_lowercase_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> String.valueOf(args[0]).toLowerCase()));
        efuns.add(efun("jvmud_capitalize_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> capitalizeText(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_capture_session_input", LPCType.LPCVOID, List.of(LPCType.LPCSTRING, LPCType.LPCINT),
                (runtime, args) -> {
                    runtime.captureSessionInput(
                            String.valueOf(args[0]),
                            ((Number) args[1]).intValue() != 0);
                    return null;
                }));
        efuns.add(efun("jvmud_is_string", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> args[0] instanceof String ? 1 : 0));
        efuns.add(efun("jvmud_is_array", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> args[0] instanceof List<?> ? 1 : 0));
        efuns.add(efun("allocate", LPCType.LPCARRAY, List.of(LPCType.LPCINT),
                (runtime, args) -> new ArrayList<>(
                        Collections.nCopies(Math.max(0, ((Number) args[0]).intValue()), Integer.valueOf(0)))));
        efuns.add(efun("jvmud_invoke_entity", LPCType.LPCMIXED,
                List.of(LPCType.LPCMIXED, LPCType.LPCSTRING),
                (runtime, args) -> invokeEntity(runtime, args[0], String.valueOf(args[1]))));
        efuns.add(efun("jvmud_invoke_entity", LPCType.LPCMIXED,
                List.of(LPCType.LPCMIXED, LPCType.LPCSTRING, LPCType.LPCMIXED),
                (runtime, args) -> invokeEntity(runtime, args[0], String.valueOf(args[1]), args[2])));
        efuns.add(efun("jvmud_spawn_entity", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.cloneObject(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_move_entity", LPCType.LPCVOID, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.moveObject(args[0], args[1]);
                    return null;
                }));
        efuns.add(efun("jvmud_find_entity", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.present(args[0], null)));
        efuns.add(efun("jvmud_find_entity", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> runtime.present(args[0], args[1])));
        efuns.add(efun("jvmud_first_entity_at", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.firstInventory(args[0])));
        efuns.add(efun("jvmud_next_entity_at", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.nextInventory(args[0])));
        efuns.add(efun("jvmud_set_light", LPCType.LPCINT, List.of(LPCType.LPCINT),
                (runtime, args) -> runtime.setLight(((Number) args[0]).intValue())));
        efuns.add(efun("jvmud_set_heart_beat", LPCType.LPCVOID, List.of(LPCType.LPCINT),
                (runtime, args) -> null));
        efuns.add(efun("jvmud_call_out", LPCType.LPCVOID, List.of(LPCType.LPCSTRING, LPCType.LPCINT),
                (runtime, args) -> null));
        efuns.add(efun("jvmud_call_out", LPCType.LPCVOID, List.of(LPCType.LPCSTRING, LPCType.LPCINT, LPCType.LPCMIXED),
                (runtime, args) -> null));
        efuns.add(efun("jvmud_remove_call_out", LPCType.LPCINT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> -1));
        efuns.add(efun("jvmud_enable_commands", LPCType.LPCVOID, List.of(),
                (runtime, args) -> null));
        efuns.add(efun("jvmud_add_action", LPCType.LPCVOID, List.of(LPCType.LPCSTRING),
                (runtime, args) -> {
                    runtime.rememberActionMethod(String.valueOf(args[0]));
                    return null;
                }));
        efuns.add(efun("jvmud_add_action", LPCType.LPCVOID, List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> {
                    runtime.rememberActionMethod(String.valueOf(args[0]));
                    runtime.registerVerb(String.valueOf(args[1]));
                    return null;
                }));
        efuns.add(efun("jvmud_add_verb", LPCType.LPCVOID, List.of(LPCType.LPCSTRING),
                (runtime, args) -> {
                    runtime.registerVerb(String.valueOf(args[0]));
                    return null;
                }));
        efuns.add(efun("jvmud_entity_location", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.environment(null)));
        efuns.add(efun("jvmud_entity_location", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.environment(args[0])));
        efuns.add(efun("jvmud_destroy_entity", LPCType.LPCVOID, List.of(LPCType.LPCOBJECT),
                (runtime, args) -> {
                    runtime.destructObject(args[0]);
                    return null;
                }));
        return efuns;
    }

    private static int sizeOf(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof CharSequence text) {
            return text.length();
        }
        if (value instanceof Collection<?> collection) {
            return collection.size();
        }
        if (value instanceof Map<?, ?> map) {
            return map.size();
        }
        if (value.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(value);
        }
        throw new IllegalArgumentException("sizeof expects array, mapping, or string value");
    }

    private static String capitalizeText(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private static Object emitPerceivable(RuntimeContext runtime, Object emitter, Object message) {
        return emitPerceivableAt(runtime, runtime.environment(emitter), message);
    }

    private static Object emitPerceivableAt(RuntimeContext runtime, Object location, Object message) {
        // Until location/session routing exists, perceivable emissions are captured in the same sink
        // as direct writes so tests and the admin CLI can still observe output.
        runtime.writeOutput(message);
        return null;
    }

    private static Object invokeEntity(RuntimeContext runtime, Object target, String methodName) {
        return invokeEntity(runtime, target, methodName, null);
    }

    private static Object invokeEntity(RuntimeContext runtime, Object target, String methodName, Object argument) {
        Object resolvedTarget = resolveTarget(runtime, target);
        if (resolvedTarget == null) {
            return 0;
        }

        if (isNoArgumentSentinel(argument)) {
            try {
                return runtime.invokeObject(resolvedTarget, methodName);
            } catch (IllegalArgumentException e) {
                return runtime.invokeObject(resolvedTarget, methodName, new Object[] {null});
            }
        }
        return runtime.invokeObject(resolvedTarget, methodName, argument);
    }

    private static Object resolveTarget(RuntimeContext runtime, Object target) {
        if (target instanceof String path) {
            return runtime.loadOrGetObject(stripLeadingSlash(path));
        }
        return target;
    }

    private static boolean isNoArgumentSentinel(Object argument) {
        return argument == null || Integer.valueOf(0).equals(argument);
    }

    private static String stripLeadingSlash(String path) {
        String stripped = path;
        while (stripped.startsWith("/")) {
            stripped = stripped.substring(1);
        }
        return stripped;
    }

    private static Efun efun(String name, LPCType returnType, List<LPCType> parameters, EfunBody body) {
        return new Efun() {
            @Override
            public EfunSignature signature() {
                return new EfunSignature(new Symbol(returnType, name), parameters);
            }

            @Override
            public Object call(RuntimeContext context, Object[] args) {
                return body.call(context, args);
            }
        };
    }

    @FunctionalInterface
    private interface EfunBody {
        Object call(RuntimeContext context, Object[] args);
    }
}
