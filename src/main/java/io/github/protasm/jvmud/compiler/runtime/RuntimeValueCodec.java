package io.github.protasm.jvmud.compiler.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Serializes and deserializes LPC data values using a JVMud-owned tagged JSON format. */
public final class RuntimeValueCodec {
    private static final ObjectMapper JSON = new ObjectMapper();

    private RuntimeValueCodec() {}

    /**
     * Serializes an LPC value into a stable string representation.
     *
     * <p>This codec is for expression-level data values, not live LPC object instances. Supported
     * values are LPC false/null, strings, booleans, numbers, arrays, Java arrays, and mappings with
     * recursively serializable keys and values.</p>
     *
     * @param value LPC data value to serialize
     * @return tagged JSON string
     * @throws IllegalArgumentException when {@code value} contains an unsupported live object or
     *         cannot be written as JSON
     */
    public static String serialize(Object value) {
        try {
            return JSON.writeValueAsString(encode(value));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to serialize LPC value.", e);
        }
    }

    /**
     * Deserializes a value produced by {@link #serialize(Object)}.
     *
     * @param data tagged JSON string
     * @return equivalent LPC data value, with LPC false restored as numeric {@code 0}
     * @throws IllegalArgumentException when {@code data} is not a JVMud LPC value payload
     */
    public static Object deserialize(String data) {
        try {
            return decode(JSON.readTree(data));
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to deserialize LPC value.", e);
        }
    }

    private static ObjectNode encode(Object value) {
        ObjectNode node = JSON.createObjectNode();
        if (value == null) {
            node.put("type", "false");
            return node;
        }
        if (value instanceof String string) {
            node.put("type", "string");
            node.put("value", string);
            return node;
        }
        if (value instanceof Boolean status) {
            node.put("type", "status");
            node.put("value", status);
            return node;
        }
        if (value instanceof Number number) {
            encodeNumber(node, number);
            return node;
        }
        if (value instanceof Collection<?> collection) {
            node.put("type", "array");
            ArrayNode values = node.putArray("values");
            for (Object item : collection) {
                values.add(encode(item));
            }
            return node;
        }
        if (value.getClass().isArray()) {
            node.put("type", "array");
            ArrayNode values = node.putArray("values");
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                values.add(encode(Array.get(value, i)));
            }
            return node;
        }
        if (value instanceof Map<?, ?> mapping) {
            node.put("type", "mapping");
            ArrayNode entries = node.putArray("entries");
            for (Map.Entry<?, ?> entry : mapping.entrySet()) {
                ObjectNode encodedEntry = entries.addObject();
                encodedEntry.set("key", encode(entry.getKey()));
                encodedEntry.set("value", encode(entry.getValue()));
            }
            return node;
        }
        throw new IllegalArgumentException("Unsupported LPC value for serialization: " + value.getClass().getName());
    }

    private static void encodeNumber(ObjectNode node, Number number) {
        if (number instanceof Float || number instanceof Double) {
            node.put("type", "float");
            node.put("value", number.doubleValue());
            return;
        }
        node.put("type", "int");
        node.put("value", number.longValue());
    }

    private static Object decode(JsonNode node) {
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("Invalid LPC value payload.");
        }
        String type = node.path("type").asText(null);
        return switch (type) {
            case "false" -> 0;
            case "string" -> node.path("value").asText();
            case "status" -> node.path("value").asBoolean();
            case "int" -> decodeInteger(node.path("value"));
            case "float" -> node.path("value").asDouble();
            case "array" -> decodeArray(node.path("values"));
            case "mapping" -> decodeMapping(node.path("entries"));
            default -> throw new IllegalArgumentException("Unknown LPC value type: " + type);
        };
    }

    private static Object decodeInteger(JsonNode value) {
        long longValue = value.asLong();
        if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
            return (int) longValue;
        }
        return longValue;
    }

    private static ArrayList<Object> decodeArray(JsonNode values) {
        if (!values.isArray()) {
            throw new IllegalArgumentException("Invalid LPC array payload.");
        }
        ArrayList<Object> result = new ArrayList<>();
        for (JsonNode value : values) {
            result.add(decode(value));
        }
        return result;
    }

    private static LinkedHashMap<Object, Object> decodeMapping(JsonNode entries) {
        if (!entries.isArray()) {
            throw new IllegalArgumentException("Invalid LPC mapping payload.");
        }
        LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
        for (JsonNode entry : entries) {
            result.put(decode(entry.path("key")), decode(entry.path("value")));
        }
        return result;
    }
}
