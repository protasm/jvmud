package io.github.protasm.jvmud.compiler.runtime;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Runtime iteration helpers for LPC foreach lowering. */
public final class RuntimeForeach {
    private RuntimeForeach() {}

    public static List<Object> items(Object source) {
        if (source == null)
            return List.of();

        if (source instanceof Map<?, ?> map)
            return new ArrayList<>(map.keySet());

        return values(source);
    }

    public static List<Object> keys(Object source) {
        if (source == null)
            return List.of();

        if (source instanceof Map<?, ?> map)
            return new ArrayList<>(map.keySet());

        if (source instanceof List<?> list) {
            List<Object> indices = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++)
                indices.add(Integer.valueOf(i));
            return indices;
        }

        if (source instanceof CharSequence text) {
            List<Object> indices = new ArrayList<>(text.length());
            for (int i = 0; i < text.length(); i++)
                indices.add(Integer.valueOf(i));
            return indices;
        }

        if (source.getClass().isArray()) {
            int length = Array.getLength(source);
            List<Object> indices = new ArrayList<>(length);
            for (int i = 0; i < length; i++)
                indices.add(Integer.valueOf(i));
            return indices;
        }

        throw new IllegalArgumentException("Cannot iterate keys for value: " + source);
    }

    public static Object value(Object source, Object key) {
        if (source instanceof Map<?, ?> map)
            return map.get(key);

        int index = key instanceof Number number ? number.intValue() : 0;
        if (source instanceof List<?> list)
            return index >= 0 && index < list.size() ? list.get(index) : null;

        if (source instanceof CharSequence text)
            return index >= 0 && index < text.length() ? String.valueOf(text.charAt(index)) : "";

        if (source != null && source.getClass().isArray())
            return index >= 0 && index < Array.getLength(source) ? Array.get(source, index) : null;

        throw new IllegalArgumentException("Cannot fetch foreach value from: " + source);
    }

    public static int size(Object source) {
        if (source instanceof List<?> list)
            return list.size();
        return items(source).size();
    }

    private static List<Object> values(Object source) {
        if (source instanceof List<?> list)
            return new ArrayList<>(list);

        if (source instanceof CharSequence text) {
            List<Object> chars = new ArrayList<>(text.length());
            for (int i = 0; i < text.length(); i++)
                chars.add(String.valueOf(text.charAt(i)));
            return chars;
        }

        if (source.getClass().isArray()) {
            int length = Array.getLength(source);
            List<Object> values = new ArrayList<>(length);
            for (int i = 0; i < length; i++)
                values.add(Array.get(source, i));
            return values;
        }

        throw new IllegalArgumentException("Cannot iterate value: " + source);
    }
}
