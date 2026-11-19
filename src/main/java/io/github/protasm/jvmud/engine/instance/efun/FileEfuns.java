package io.github.protasm.jvmud.engine.instance.efun;

import static io.github.protasm.jvmud.engine.instance.efun.EngineEfunDefinition.of;

import io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntimeContext;
import io.github.protasm.jvmud.engine.model.mudlib.EngineCapability;
import io.github.protasm.jvmud.language.compiler.type.LPCType;

import java.util.ArrayList;
import java.util.List;

/** Mudlib-rooted file access and authored-object persistence adapters. */
final class FileEfuns {
    private FileEfuns() {}

    /** Defines the operations owned by this service boundary. */
    static List<EngineEfunDefinition> functions() {
        List<EngineEfunDefinition> efuns = new ArrayList<>();
        efuns.add(of("jvmud_read_mudlib_text", LPCType.LPCMIXED, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.readMudlibText(String.valueOf(args[0])))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_read_mudlib_text", LPCType.LPCMIXED,
                List.of(LPCType.LPCSTRING, LPCType.LPCINT, LPCType.LPCINT),
                (runtime, args) -> readMudlibText(
                        runtime,
                        String.valueOf(args[0]),
                        ((Number) args[1]).intValue(),
                        ((Number) args[2]).intValue()))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_read_mudlib_json", LPCType.LPCMIXED, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.readMudlibJson(String.valueOf(args[0])))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_read_mudlib_json_array", LPCType.LPCMIXED,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING, LPCType.LPCINT, LPCType.LPCINT),
                (runtime, args) -> runtime.readMudlibJsonArray(
                        String.valueOf(args[0]),
                        String.valueOf(args[1]),
                        ((Number) args[2]).intValue(),
                        ((Number) args[3]).intValue()))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_list_mudlib_paths", LPCType.LPCARRAY, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.listMudlibPaths(String.valueOf(args[0]), 1))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_list_mudlib_paths", LPCType.LPCARRAY,
                List.of(LPCType.LPCSTRING, LPCType.LPCINT),
                (runtime, args) -> runtime.listMudlibPaths(
                        String.valueOf(args[0]),
                        ((Number) args[1]).intValue()))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_append_mudlib_text", LPCType.LPCSTATUS,
                List.of(LPCType.LPCSTRING, LPCType.LPCMIXED),
                (runtime, args) -> runtime.appendMudlibText(String.valueOf(args[0]), args[1]))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_remove_mudlib_text", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.removeMudlibText(String.valueOf(args[0])))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_copy_mudlib_text", LPCType.LPCINT,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> runtime.copyMudlibText(String.valueOf(args[0]), String.valueOf(args[1])))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_rename_mudlib_text", LPCType.LPCINT,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> runtime.renameMudlibText(String.valueOf(args[0]), String.valueOf(args[1])))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_create_mudlib_directory", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.createMudlibDirectory(String.valueOf(args[0])))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_remove_mudlib_directory", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.removeMudlibDirectory(String.valueOf(args[0])))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_save_lpc_object_state", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.saveCurrentLPCObjectState(String.valueOf(args[0])))
                .requiring(EngineCapability.MUDLIB_FILES));
        efuns.add(of("jvmud_restore_lpc_object_state", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.restoreCurrentLPCObjectState(String.valueOf(args[0])))
                .requiring(EngineCapability.MUDLIB_FILES));
        return List.copyOf(efuns);
    }

    private static Object readMudlibText(MudlibRuntimeContext runtime, String path, int startLine, int lineCount) {
        Object text = runtime.readMudlibText(path);
        if (!(text instanceof String content) || lineCount <= 0) {
            return text;
        }

        String[] lines = content.split("(?<=\\n)", -1);
        int start = Math.max(0, startLine - 1);
        if (start >= lines.length) {
            return "";
        }
        int end = Math.min(lines.length, start + lineCount);
        StringBuilder selected = new StringBuilder();
        for (int i = start; i < end; i++) {
            selected.append(lines[i]);
        }
        return selected.toString();
    }
}
