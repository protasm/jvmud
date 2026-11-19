package io.github.protasm.jvmud.engine.instance.efun;

import static io.github.protasm.jvmud.engine.instance.efun.EngineEfunDefinition.of;
import static io.github.protasm.jvmud.engine.instance.efun.LPCObjectEfuns.methodExists;

import io.github.protasm.jvmud.communication.protocol.GMCPCodec;
import io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntimeContext;
import io.github.protasm.jvmud.engine.model.mudlib.EngineCapability;
import io.github.protasm.jvmud.language.compiler.type.LPCType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Interactive connections, private output, input capture and negotiated client protocols. */
final class SessionEfuns {
    private SessionEfuns() {}

    /** Defines the operations owned by this service boundary. */
    static List<EngineEfunDefinition> functions() {
        List<EngineEfunDefinition> efuns = new ArrayList<>();
        efuns.add(of("jvmud_write", LPCType.LPCVOID, List.of(LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.write(args[0]);
                    return null;
                }));
        efuns.add(of("jvmud_write_to_lpc_object", LPCType.LPCVOID, List.of(LPCType.LPCOBJECT, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.writeToLpcObject(args[0], args[1]);
                    return null;
                }));
        efuns.add(of("jvmud_rebind_session_lpc_object", LPCType.LPCSTATUS,
                List.of(LPCType.LPCOBJECT, LPCType.LPCOBJECT),
                (runtime, args) -> runtime.rebindSessionLpcObject(args[0], args[1]) ? 1 : 0)
                .requiring(EngineCapability.SESSION_CONTROL));
        efuns.add(of("jvmud_gmcp_enabled", LPCType.LPCSTATUS, List.of(),
                (runtime, args) -> runtime.isSessionProtocolEnabled(currentInteractive(runtime), "GMCP") ? 1 : 0)
                .requiring(EngineCapability.SESSION_CONTROL));
        efuns.add(of("jvmud_gmcp_enabled", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.isSessionProtocolEnabled(args[0], "GMCP") ? 1 : 0)
                .requiring(EngineCapability.SESSION_CONTROL));
        efuns.add(of("jvmud_send_gmcp", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.sendSessionProtocolMessage(
                        currentInteractive(runtime),
                        "GMCP",
                        GMCPCodec.encode(String.valueOf(args[0]), null, false)) ? 1 : 0)
                .requiring(EngineCapability.SESSION_CONTROL));
        efuns.add(of("jvmud_send_gmcp", LPCType.LPCSTATUS,
                List.of(LPCType.LPCSTRING, LPCType.LPCMIXED),
                (runtime, args) -> runtime.sendSessionProtocolMessage(
                        currentInteractive(runtime),
                        "GMCP",
                        GMCPCodec.encode(String.valueOf(args[0]), args[1], true)) ? 1 : 0)
                .requiring(EngineCapability.SESSION_CONTROL));
        efuns.add(of("jvmud_users", LPCType.LPCARRAY, List.of(),
                (runtime, args) -> runtime.users())
                .requiring(EngineCapability.SESSION_CONTROL));
        efuns.add(of("jvmud_find_player", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> findPlayer(runtime, String.valueOf(args[0])))
                .requiring(EngineCapability.SESSION_CONTROL));
        efuns.add(of("jvmud_interactive", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.isInteractive(args[0]) ? 1 : 0)
                .requiring(EngineCapability.SESSION_CONTROL));
        // This legacy information query retains its existing LPC-zero placeholder result.
        efuns.add(of("jvmud_interactive_info", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> 0)
                .requiring(EngineCapability.SESSION_CONTROL));
        efuns.add(of("jvmud_query_idle", LPCType.LPCINT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.queryIdle(args[0]))
                .requiring(EngineCapability.SESSION_CONTROL));
        efuns.add(of("jvmud_query_ip_number", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.queryIpNumber(args[0]))
                .requiring(EngineCapability.SESSION_CONTROL));
        efuns.add(of("jvmud_query_ip_name", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.queryIpNumber(args[0]))
                .requiring(EngineCapability.SESSION_CONTROL));
        efuns.add(captureSessionInputEfun()
                .requiring(EngineCapability.SESSION_CONTROL));
        return List.copyOf(efuns);
    }

    private static Object findPlayer(MudlibRuntimeContext runtime, String name) {
        String normalizedName = normalizePlayerName(name);
        if (normalizedName.isEmpty()) {
            return 0;
        }
        for (Object user : runtime.users()) {
            if (user != null && playerNameMatches(runtime, user, normalizedName)) {
                return user;
            }
        }
        return 0;
    }

    private static boolean playerNameMatches(MudlibRuntimeContext runtime, Object user, String normalizedName) {
        for (String methodName : List.of("RealName", "Name", "query_name", "name")) {
            if (!methodExists(runtime, user, methodName)) {
                continue;
            }
            Object value = runtime.invokeOptionalObject(user, methodName);
            if (normalizePlayerName(value).equals(normalizedName)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizePlayerName(Object value) {
        if (value == null || (value instanceof Number number && number.intValue() == 0)) {
            return "";
        }
        return String.valueOf(value).trim().toLowerCase(Locale.ROOT);
    }

    private static Object currentInteractive(MudlibRuntimeContext runtime) {
        Object actor = runtime.currentCommandActor();
        return actor != null ? actor : runtime.currentObject();
    }

    private static EngineEfunDefinition captureSessionInputEfun() {
        return of("jvmud_capture_session_input", LPCType.LPCVOID,
                List.of(LPCType.LPCSTRING, LPCType.LPCINT), LPCType.LPCMIXED,
                (runtime, args) -> {
                    Object[] extraArgs = new Object[Math.max(0, args.length - 2)];
                    if (extraArgs.length > 0) {
                        System.arraycopy(args, 2, extraArgs, 0, extraArgs.length);
                    }
                    runtime.captureSessionInput(
                            String.valueOf(args[0]),
                            (((Number) args[1]).intValue() & 1) != 0,
                            extraArgs);
                    return null;
                });
    }
}
