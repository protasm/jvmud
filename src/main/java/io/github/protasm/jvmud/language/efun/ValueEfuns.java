package io.github.protasm.jvmud.language.efun;

import static io.github.protasm.jvmud.language.efun.EfunDefinition.of;

import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.runtime.JSONValueCodec;
import io.github.protasm.jvmud.language.runtime.RuntimeCallable;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;
import io.github.protasm.jvmud.language.runtime.RuntimeValueCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** LPC value conversion, classification, serialization and callable application. */
final class ValueEfuns {
    private ValueEfuns() {}

    /** Declares value operations that require no object registry or other hosted service. */
    static List<Efun> functions() {
        List<Efun> efuns = new ArrayList<>();
        efuns.add(of("jvmud_sqrt", LPCType.LPCFLOAT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> Math.sqrt(toDouble(args[0]))));
        efuns.add(of("jvmud_raise_error", LPCType.LPCVOID, List.of(LPCType.LPCMIXED),
                (runtime, args) -> {
                    throw new IllegalStateException(String.valueOf(args[0]));
                }));
        efuns.add(of("jvmud_to_int", LPCType.LPCINT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> toInt(args[0])));
        efuns.add(of("jvmud_to_string", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> String.valueOf(args[0])));
        efuns.add(of("jvmud_serialize_lpc_value", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> RuntimeValueCodec.serialize(args[0])));
        efuns.add(of("jvmud_deserialize_lpc_value", LPCType.LPCMIXED, List.of(LPCType.LPCSTRING),
                (runtime, args) -> RuntimeValueCodec.deserialize(String.valueOf(args[0]))));
        efuns.add(of("jvmud_parse_json", LPCType.LPCMIXED, List.of(LPCType.LPCSTRING),
                (runtime, args) -> JSONValueCodec.parse(String.valueOf(args[0]))));
        efuns.add(of("jvmud_format_json", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> JSONValueCodec.format(args[0])));
        efuns.add(of("jvmud_is_string", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> args[0] instanceof String ? 1 : 0));
        efuns.add(of("jvmud_is_int", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> args[0] instanceof Number number
                        && !(number instanceof Float)
                        && !(number instanceof Double) ? 1 : 0));
        efuns.add(of("jvmud_is_float", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> args[0] instanceof Float || args[0] instanceof Double ? 1 : 0));
        efuns.add(of("jvmud_is_array", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> args[0] instanceof List<?> ? 1 : 0));
        efuns.add(of("jvmud_is_mapping", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> args[0] instanceof Map<?, ?> ? 1 : 0));
        efuns.add(applyCallableEfun());
        return List.copyOf(efuns);
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return 0.0d;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0d;
        }
    }

    private static Efun applyCallableEfun() {
        return of("jvmud_apply_callable", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED), LPCType.LPCMIXED,
                (runtime, args) -> applyCallable(runtime, args));
    }

    /** LPC apply expands an array only when it is the final supplied argument. */
    private static Object applyCallable(RuntimeContext runtime, Object[] args) {
        if (args.length == 0 || !(args[0] instanceof RuntimeCallable callback)) {
            return 0;
        }
        List<Object> invocationArgs = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            if (i == args.length - 1 && args[i] instanceof List<?> finalArray) {
                invocationArgs.addAll(finalArray);
            } else {
                invocationArgs.add(args[i]);
            }
        }
        return callback.call(runtime, invocationArgs.toArray());
    }
}
