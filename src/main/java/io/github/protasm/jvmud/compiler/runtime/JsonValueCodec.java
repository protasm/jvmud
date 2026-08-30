package io.github.protasm.jvmud.compiler.runtime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Converts ordinary JSON documents to and from LPC-compatible data values. */
public final class JsonValueCodec {
    private static final ObjectMapper JSON = new ObjectMapper();

    private JsonValueCodec() {}

    /**
     * Parses an ordinary JSON document.
     *
     * <p>Objects become insertion-ordered mappings with string keys, arrays become mutable LPC
     * arrays, integer-looking numbers remain integral, booleans become {@code 1}/{@code 0}, and
     * JSON {@code null} becomes LPC false ({@code 0}).</p>
     *
     * @param document complete JSON document
     * @return LPC-compatible value
     * @throws IllegalArgumentException when the document is invalid JSON
     */
    public static Object parse(String document) {
        try {
            JsonNode root = JSON.readTree(document);
            if (root == null) {
                throw new IllegalArgumentException("JSON document is empty.");
            }
            return fromJson(root);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON document.", e);
        }
    }

    /**
     * Parses an ordinary JSON document from a byte stream.
     *
     * @param input JSON input stream; the Jackson parser closes it
     * @return LPC-compatible value
     * @throws IllegalArgumentException when the document cannot be read as JSON
     */
    public static Object parse(InputStream input) {
        try (JsonParser parser = JSON.getFactory().createParser(input)) {
            JsonNode root = JSON.readTree(parser);
            if (root == null) {
                throw new IllegalArgumentException("JSON document is empty.");
            }
            return fromJson(root);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON document.", e);
        }
    }

    /**
     * Formats an LPC-compatible value as ordinary JSON.
     *
     * <p>Mapping keys must be strings. Live objects and other host values are rejected.</p>
     *
     * @param value LPC-compatible value
     * @return compact JSON document
     * @throws IllegalArgumentException when the value cannot be represented as ordinary JSON
     */
    public static String format(Object value) {
        try {
            return JSON.writeValueAsString(toJson(value));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to format LPC value as JSON.", e);
        }
    }

    /**
     * Streams a bounded slice from an array selected by an RFC 6901 JSON Pointer.
     *
     * <p>Entries before {@code offset}, entries after the requested slice, and unrelated document
     * branches are skipped without being materialized. The parser still scans forward through the
     * file because ordinary JSON has no random-access index.</p>
     *
     * @param input JSON input stream; the Jackson parser closes it
     * @param pointer JSON Pointer selecting an array; empty selects a root array
     * @param offset zero-based first array entry to return
     * @param count maximum number of entries to return
     * @return mutable LPC-compatible array containing at most {@code count} entries
     * @throws IllegalArgumentException for invalid bounds, JSON, pointer syntax, or a non-array target
     */
    public static List<Object> readArraySlice(InputStream input, String pointer, int offset, int count) {
        if (offset < 0 || count < 0) {
            throw new IllegalArgumentException("JSON array offset and count must not be negative.");
        }
        List<String> segments = pointerSegments(pointer);
        try (JsonParser parser = JSON.getFactory().createParser(input)) {
            JsonToken root = parser.nextToken();
            if (root == null) {
                throw new IllegalArgumentException("JSON document is empty.");
            }
            if (!seek(parser, root, segments, 0) || parser.currentToken() != JsonToken.START_ARRAY) {
                throw new IllegalArgumentException("JSON Pointer does not select an array: " + pointer);
            }

            ArrayList<Object> result = new ArrayList<>(Math.min(count, 1024));
            int index = 0;
            while (true) {
                JsonToken item = nextRequired(parser, "JSON array");
                if (item == JsonToken.END_ARRAY) {
                    break;
                }
                if (index++ < offset) {
                    parser.skipChildren();
                    continue;
                }
                if (result.size() >= count) {
                    break;
                }
                result.add(fromJson(JSON.readTree(parser)));
            }
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON document.", e);
        }
    }

    private static boolean seek(
            JsonParser parser,
            JsonToken current,
            List<String> segments,
            int segmentIndex) throws IOException {
        if (segmentIndex == segments.size()) {
            return true;
        }
        String segment = segments.get(segmentIndex);
        if (current == JsonToken.START_OBJECT) {
            while (true) {
                JsonToken fieldToken = nextRequired(parser, "JSON object");
                if (fieldToken == JsonToken.END_OBJECT) {
                    break;
                }
                if (fieldToken != JsonToken.FIELD_NAME) {
                    throw new IOException("Expected a JSON object field.");
                }
                String field = parser.currentName();
                JsonToken value = nextRequired(parser, "JSON object field");
                if (segment.equals(field)) {
                    return seek(parser, value, segments, segmentIndex + 1);
                }
                parser.skipChildren();
            }
            return false;
        }
        if (current == JsonToken.START_ARRAY) {
            int target = pointerArrayIndex(segment);
            int index = 0;
            while (true) {
                JsonToken item = nextRequired(parser, "JSON array");
                if (item == JsonToken.END_ARRAY) {
                    break;
                }
                if (index++ == target) {
                    return seek(parser, item, segments, segmentIndex + 1);
                }
                parser.skipChildren();
            }
        }
        return false;
    }

    private static JsonToken nextRequired(JsonParser parser, String context) throws IOException {
        JsonToken token = parser.nextToken();
        if (token == null) {
            throw new IOException("Unexpected end of " + context + ".");
        }
        return token;
    }

    private static List<String> pointerSegments(String pointer) {
        if (pointer == null) {
            throw new IllegalArgumentException("JSON Pointer must not be null.");
        }
        if (pointer.isEmpty()) {
            return List.of();
        }
        if (!pointer.startsWith("/")) {
            throw new IllegalArgumentException("JSON Pointer must be empty or begin with '/'.");
        }
        String[] encoded = pointer.substring(1).split("/", -1);
        ArrayList<String> result = new ArrayList<>(encoded.length);
        for (String segment : encoded) {
            result.add(decodePointerSegment(segment));
        }
        return result;
    }

    private static String decodePointerSegment(String segment) {
        StringBuilder decoded = new StringBuilder(segment.length());
        for (int index = 0; index < segment.length(); index++) {
            char character = segment.charAt(index);
            if (character != '~') {
                decoded.append(character);
                continue;
            }
            if (++index >= segment.length()) {
                throw new IllegalArgumentException("Invalid '~' escape in JSON Pointer.");
            }
            char escaped = segment.charAt(index);
            if (escaped == '0') {
                decoded.append('~');
            } else if (escaped == '1') {
                decoded.append('/');
            } else {
                throw new IllegalArgumentException("Invalid '~" + escaped + "' escape in JSON Pointer.");
            }
        }
        return decoded.toString();
    }

    private static int pointerArrayIndex(String segment) {
        if (segment.isEmpty() || (segment.length() > 1 && segment.startsWith("0"))) {
            throw new IllegalArgumentException("Invalid array index in JSON Pointer: " + segment);
        }
        try {
            int index = Integer.parseInt(segment);
            if (index < 0) {
                throw new NumberFormatException();
            }
            return index;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid array index in JSON Pointer: " + segment, e);
        }
    }

    private static Object fromJson(JsonNode node) {
        if (node == null || node.isNull()) {
            return Integer.valueOf(0);
        }
        if (node.isObject()) {
            LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
            node.properties().forEach(entry -> result.put(entry.getKey(), fromJson(entry.getValue())));
            return result;
        }
        if (node.isArray()) {
            ArrayList<Object> result = new ArrayList<>(node.size());
            node.forEach(item -> result.add(fromJson(item)));
            return result;
        }
        if (node.isTextual()) {
            return node.textValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue() ? Integer.valueOf(1) : Integer.valueOf(0);
        }
        if (node.isInt()) {
            return node.intValue();
        }
        if (node.isIntegralNumber()) {
            return node.canConvertToLong() ? node.longValue() : node.bigIntegerValue();
        }
        if (node.isFloatingPointNumber()) {
            return node.doubleValue();
        }
        throw new IllegalArgumentException("Unsupported JSON value: " + node.getNodeType());
    }

    private static Object toJson(Object value) {
        if (value == null || value instanceof String || value instanceof Boolean || value instanceof Number) {
            return value;
        }
        if (value instanceof Map<?, ?> mapping) {
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : mapping.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    throw new IllegalArgumentException("JSON object keys must be strings.");
                }
                result.put(key, toJson(entry.getValue()));
            }
            return result;
        }
        if (value instanceof Collection<?> collection) {
            ArrayList<Object> result = new ArrayList<>(collection.size());
            for (Object item : collection) {
                result.add(toJson(item));
            }
            return result;
        }
        if (value.getClass().isArray()) {
            ArrayList<Object> result = new ArrayList<>(Array.getLength(value));
            for (int index = 0; index < Array.getLength(value); index++) {
                result.add(toJson(Array.get(value, index)));
            }
            return result;
        }
        throw new IllegalArgumentException("Unsupported LPC value for JSON: " + value.getClass().getName());
    }
}
