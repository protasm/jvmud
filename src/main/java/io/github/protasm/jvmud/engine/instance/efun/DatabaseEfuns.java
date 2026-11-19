package io.github.protasm.jvmud.engine.instance.efun;

import static io.github.protasm.jvmud.engine.instance.efun.EngineEfunDefinition.of;

import io.github.protasm.jvmud.engine.model.mudlib.EngineCapability;
import io.github.protasm.jvmud.language.compiler.type.LPCType;

import java.util.ArrayList;
import java.util.List;

/** Configured database operations exposed through the mudlib database grant. */
final class DatabaseEfuns {
    private DatabaseEfuns() {}

    /** Defines the operations owned by this service boundary. */
    static List<EngineEfunDefinition> functions() {
        List<EngineEfunDefinition> efuns = new ArrayList<>();
        efuns.add(of("jvmud_db_connect", LPCType.LPCINT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.dbConnect(String.valueOf(args[0])))
                .requiring(EngineCapability.DATABASE));
        efuns.add(of("jvmud_db_connect", LPCType.LPCINT,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> runtime.dbConnect(
                        String.valueOf(args[0]),
                        nullableText(args[1]),
                        nullableText(args[2])))
                .requiring(EngineCapability.DATABASE));
        efuns.add(of("jvmud_db_exec", LPCType.LPCINT, List.of(LPCType.LPCINT, LPCType.LPCSTRING),
                (runtime, args) -> runtime.dbExec(((Number) args[0]).intValue(), String.valueOf(args[1])))
                .requiring(EngineCapability.DATABASE));
        efuns.add(of("jvmud_db_fetch", LPCType.LPCMIXED, List.of(LPCType.LPCINT),
                (runtime, args) -> runtime.dbFetch(((Number) args[0]).intValue()))
                .requiring(EngineCapability.DATABASE));
        efuns.add(of("jvmud_db_error", LPCType.LPCMIXED, List.of(LPCType.LPCINT),
                (runtime, args) -> runtime.dbError(((Number) args[0]).intValue()))
                .requiring(EngineCapability.DATABASE));
        efuns.add(of("jvmud_db_close", LPCType.LPCSTATUS, List.of(LPCType.LPCINT),
                (runtime, args) -> runtime.dbClose(((Number) args[0]).intValue()))
                .requiring(EngineCapability.DATABASE));
        efuns.add(of("jvmud_db_handles", LPCType.LPCARRAY, List.of(),
                (runtime, args) -> runtime.dbHandles())
                .requiring(EngineCapability.DATABASE));
        efuns.add(of("jvmud_db_escape", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.dbEscape(String.valueOf(args[0])))
                .requiring(EngineCapability.DATABASE));
        return List.copyOf(efuns);
    }

    /** Converts an LPC value to text while preserving Java {@code null} for omitted varargs. */
    private static String nullableText(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
