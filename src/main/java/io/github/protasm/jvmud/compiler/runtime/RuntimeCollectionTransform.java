package io.github.protasm.jvmud.compiler.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Runtime helpers for LPC collection callbacks. */
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

    /**
     * Applies callable-form LPC {@code sort_array()} semantics.
     *
     * <p>The callback receives the two candidate values as {@code $1} and {@code $2}. Additional
     * arguments are forwarded starting at {@code $3}. A truthy callback result swaps the two
     * candidate values, matching the selection-sort behavior historically used by JVMud's compiler
     * lowering for {@code sort_array(values, (: ... :))}.</p>
     */
    public static Object sortArray(Object source, RuntimeCallable callback, Object[] extras, RuntimeContext runtime) {
        List<Object> items = new ArrayList<>(RuntimeForeach.items(source));
        int size = items.size();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (Truth.isTruthy(callback.call(runtime, callbackArgs(items.get(i), items.get(j), extras)))) {
                    Object swap = items.get(i);
                    items.set(i, items.get(j));
                    items.set(j, swap);
                }
            }
        }
        return items;
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
