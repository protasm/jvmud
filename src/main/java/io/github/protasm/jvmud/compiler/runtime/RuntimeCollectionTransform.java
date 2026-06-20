package io.github.protasm.jvmud.compiler.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Runtime helpers for LPC {@code filter()} and {@code map()} collection callbacks. */
public final class RuntimeCollectionTransform {
    private RuntimeCollectionTransform() {}

    /**
     * Applies LPC {@code filter()} semantics to arrays, strings, and mappings.
     *
     * <p>Mappings are filtered as key/value pairs and return a mapping. Other iterable values keep
     * the existing JVMud behavior: each item is passed as the first callback argument and matching
     * items are returned as an array.</p>
     */
    public static Object filter(Object source, RuntimeCallable callback, Object[] extras, RuntimeContext runtime) {
        if (source instanceof Map<?, ?> map) {
            Map<Object, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (Truth.isTruthy(callback.call(runtime, callbackArgs(entry.getKey(), entry.getValue(), extras)))) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        }

        List<Object> result = new ArrayList<>();
        for (Object item : RuntimeForeach.items(source)) {
            if (Truth.isTruthy(callback.call(runtime, callbackArgs(item, extras)))) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Applies LPC {@code map()} semantics to arrays, strings, and mappings.
     *
     * <p>Mappings are traversed as key/value pairs and return a mapping with the original keys and
     * transformed values. Other iterable values return an array of callback results.</p>
     */
    public static Object map(Object source, RuntimeCallable callback, Object[] extras, RuntimeContext runtime) {
        if (source instanceof Map<?, ?> map) {
            Map<Object, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(entry.getKey(), callback.call(runtime, callbackArgs(entry.getKey(), entry.getValue(), extras)));
            }
            return result;
        }

        List<Object> result = new ArrayList<>();
        for (Object item : RuntimeForeach.items(source)) {
            result.add(callback.call(runtime, callbackArgs(item, extras)));
        }
        return result;
    }

    private static Object[] callbackArgs(Object item, Object[] extras) {
        Object[] args = new Object[1 + extras.length];
        args[0] = item;
        System.arraycopy(extras, 0, args, 1, extras.length);
        return args;
    }

    private static Object[] callbackArgs(Object key, Object value, Object[] extras) {
        Object[] args = new Object[2 + extras.length];
        args[0] = key;
        args[1] = value;
        System.arraycopy(extras, 0, args, 2, extras.length);
        return args;
    }
}
