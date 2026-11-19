package io.github.protasm.jvmud.language.efun;

import static io.github.protasm.jvmud.language.efun.EfunDefinition.of;

import io.github.protasm.jvmud.language.compiler.type.LPCType;
import io.github.protasm.jvmud.language.runtime.RuntimeCallable;
import io.github.protasm.jvmud.language.runtime.RuntimeCollectionTransform;
import io.github.protasm.jvmud.language.runtime.RuntimeContext;
import io.github.protasm.jvmud.language.runtime.RuntimeMapping;
import io.github.protasm.jvmud.language.runtime.Truth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** LPC collection queries, mutation and callable transformations. */
final class CollectionEfuns {
    private CollectionEfuns() {}

    /** Declares collection queries, mutable allocation and callback transformations. */
    static List<Efun> functions() {
        List<Efun> efuns = new ArrayList<>();
        efuns.add(of("jvmud_size", LPCType.LPCINT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> sizeOf(args[0])));
        efuns.add(of("jvmud_member", LPCType.LPCINT, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> member(args[0], args[1])));
        efuns.add(of("jvmud_mapping_keys", LPCType.LPCARRAY, List.of(LPCType.LPCMAPPING),
                (runtime, args) -> mappingKeys(args[0])));
        efuns.add(of("jvmud_mapping_values", LPCType.LPCARRAY, List.of(LPCType.LPCMAPPING),
                (runtime, args) -> mappingValues(args[0])));
        efuns.add(of("jvmud_mapping_values", LPCType.LPCARRAY, List.of(LPCType.LPCMAPPING, LPCType.LPCINT),
                (runtime, args) -> mappingValues(args[0], args[1])));
        efuns.add(of("jvmud_mapping_from_keys", LPCType.LPCMAPPING, List.of(LPCType.LPCARRAY),
                (runtime, args) -> mappingFromKeys(args[0])));
        efuns.add(of("jvmud_mapping_delete", LPCType.LPCMAPPING, List.of(LPCType.LPCMAPPING, LPCType.LPCMIXED),
                (runtime, args) -> mappingDelete(args[0], args[1])));
        efuns.add(filterIndicesEfun());
        efuns.add(filterEfun());
        efuns.add(mapEfun());
        efuns.add(sortArrayEfun());
        efuns.add(of("jvmud_allocate", LPCType.LPCARRAY, List.of(LPCType.LPCINT),
                (runtime, args) -> new ArrayList<>(
                        Collections.nCopies(Math.max(0, ((Number) args[0]).intValue()), Integer.valueOf(0)))));
        return List.copyOf(efuns);
    }

    private static int sizeOf(Object value) {
        if (value == null) {
            return 0;
        }
        if (Integer.valueOf(0).equals(value)) {
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

    private static int member(Object value, Object needle) {
        if (value instanceof Map<?, ?> map) {
            return map.containsKey(needle) ? 1 : 0;
        }
        if (value instanceof List<?> list) {
            return list.indexOf(needle);
        }
        if (value instanceof CharSequence text) {
            String haystack = text.toString();
            if (needle instanceof Number number) {
                return haystack.indexOf((char) number.intValue());
            }
            return haystack.indexOf(String.valueOf(needle));
        }
        return -1;
    }

    private static List<Object> mappingKeys(Object value) {
        if (value instanceof Map<?, ?> map) {
            return new ArrayList<>(map.keySet());
        }
        throw new IllegalArgumentException("jvmud_mapping_keys expects a mapping value");
    }

    private static List<Object> mappingValues(Object value) {
        if (value instanceof Map<?, ?> map) {
            List<Object> values = new ArrayList<>();
            for (Object entryValue : map.values()) {
                values.add(RuntimeMapping.select(entryValue, 0));
            }
            return values;
        }
        throw new IllegalArgumentException("jvmud_mapping_values expects a mapping value");
    }

    private static List<Object> mappingValues(Object value, Object index) {
        if (value instanceof Map<?, ?> map) {
            List<Object> values = new ArrayList<>();
            for (Object entryValue : map.values()) {
                values.add(RuntimeMapping.select(entryValue, index));
            }
            return values;
        }
        throw new IllegalArgumentException("jvmud_mapping_values expects a mapping value");
    }

    private static Map<Object, Object> mappingFromKeys(Object value) {
        if (value == null || (value instanceof Number number && number.doubleValue() == 0.0d)) {
            return new LinkedHashMap<>();
        }
        if (value instanceof List<?> keys) {
            Map<Object, Object> mapping = new LinkedHashMap<>();
            for (Object key : keys) {
                mapping.put(key, 1);
            }
            return mapping;
        }
        throw new IllegalArgumentException("jvmud_mapping_from_keys expects an array value");
    }

    private static Map<?, ?> mappingDelete(Object value, Object key) {
        if (value instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> mutable = (Map<Object, Object>) map;
            mutable.remove(key);
            return map;
        }
        throw new IllegalArgumentException("jvmud_mapping_delete expects a mapping value");
    }

    private static Efun filterIndicesEfun() {
        return of("jvmud_filter_indices", LPCType.LPCMAPPING,
                List.of(LPCType.LPCMAPPING, LPCType.LPCFUNCTION), LPCType.LPCMIXED,
                (runtime, args) -> filterIndices(runtime, args));
    }

    private static Efun filterEfun() {
        return of("jvmud_filter", LPCType.LPCMIXED,
                List.of(LPCType.LPCMIXED, LPCType.LPCFUNCTION), LPCType.LPCMIXED,
                (runtime, args) -> filter(runtime, args));
    }

    private static Object filter(RuntimeContext runtime, Object[] args) {
        if (!(args[1] instanceof RuntimeCallable callback)) {
            throw new IllegalArgumentException("jvmud_filter expects a callable as its second argument");
        }
        Object[] extraArgs = new Object[Math.max(0, args.length - 2)];
        if (extraArgs.length > 0) {
            System.arraycopy(args, 2, extraArgs, 0, extraArgs.length);
        }
        return RuntimeCollectionTransform.filter(args[0], callback, extraArgs, runtime);
    }

    private static Efun mapEfun() {
        return of("jvmud_map", LPCType.LPCMIXED,
                List.of(LPCType.LPCMIXED, LPCType.LPCFUNCTION), LPCType.LPCMIXED,
                (runtime, args) -> map(runtime, args));
    }

    private static Object map(RuntimeContext runtime, Object[] args) {
        if (!(args[1] instanceof RuntimeCallable callback)) {
            throw new IllegalArgumentException("jvmud_map expects a callable as its second argument");
        }
        Object[] extraArgs = new Object[Math.max(0, args.length - 2)];
        if (extraArgs.length > 0) {
            System.arraycopy(args, 2, extraArgs, 0, extraArgs.length);
        }
        return RuntimeCollectionTransform.map(args[0], callback, extraArgs, runtime);
    }

    private static Efun sortArrayEfun() {
        return of("jvmud_sort_array", LPCType.LPCARRAY,
                List.of(LPCType.LPCARRAY, LPCType.LPCMIXED), LPCType.LPCMIXED,
                (runtime, args) -> sortArray(runtime, args));
    }

    private static Object sortArray(RuntimeContext runtime, Object[] args) {
        Object[] extraArgs = new Object[Math.max(0, args.length - 2)];
        if (extraArgs.length > 0) {
            System.arraycopy(args, 2, extraArgs, 0, extraArgs.length);
        }
        if (args[1] instanceof RuntimeCallable callback) {
            return RuntimeCollectionTransform.sortArray(args[0], callback, extraArgs, runtime);
        }
        if (args[1] instanceof String methodName) {
            // A named comparator belongs to the caller's lexical object, rather than a world lookup.
            Object current = runtime.currentObject();
            RuntimeCallable comparator = (callbackRuntime, callbackArgs) -> callbackRuntime.invokeOptionalObject(
                    current,
                    methodName,
                    callbackArgs);
            return RuntimeCollectionTransform.sortArray(args[0], comparator, extraArgs, runtime);
        }
        throw new IllegalArgumentException("jvmud_sort_array expects a callable or method name as its second argument");
    }

    private static Object filterIndices(RuntimeContext runtime, Object[] args) {
        if (!(args[0] instanceof Map<?, ?> source)) {
            throw new IllegalArgumentException("jvmud_filter_indices expects a mapping as its first argument");
        }
        if (!(args[1] instanceof RuntimeCallable callback)) {
            throw new IllegalArgumentException("jvmud_filter_indices expects a callable as its second argument");
        }

        Map<Object, Object> result = new LinkedHashMap<>();
        Object[] callbackArgs = new Object[args.length - 1];
        if (args.length > 2) {
            System.arraycopy(args, 2, callbackArgs, 1, args.length - 2);
        }

        for (Map.Entry<?, ?> entry : source.entrySet()) {
            callbackArgs[0] = entry.getKey();
            if (Truth.isTruthy(callback.call(runtime, callbackArgs))) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
