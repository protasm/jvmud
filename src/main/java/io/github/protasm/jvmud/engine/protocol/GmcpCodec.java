package io.github.protasm.jvmud.engine.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Encodes and decodes the UTF-8, JSON-bearing messages carried by GMCP Telnet frames. */
public final class GmcpCodec {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern PACKAGE_NAME = Pattern.compile("[A-Za-z]+(?:\\.[A-Za-z]+)*");

    private GmcpCodec() {}

    /** Encodes a GMCP package plus an optional LPC-compatible data payload. */
    public static String encode(String packageName, Object payload, boolean hasPayload) {
        validatePackageName(packageName);
        if (!hasPayload) {
            return packageName;
        }
        try {
            return packageName + " " + JSON.writeValueAsString(normalizeForJson(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to encode GMCP payload for " + packageName + ".", e);
        }
    }

    /** Decodes one GMCP message body after Telnet subnegotiation framing has been removed. */
    public static GmcpMessage decode(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("GMCP message must not be blank.");
        }
        int separator = message.indexOf(' ');
        String packageName = separator >= 0 ? message.substring(0, separator) : message;
        validatePackageName(packageName);
        if (separator < 0) {
            return new GmcpMessage(packageName, Integer.valueOf(0), false);
        }
        String json = message.substring(separator + 1).trim();
        if (json.isEmpty()) {
            throw new IllegalArgumentException("GMCP payload must contain JSON when a separator is present.");
        }
        try {
            Object payload = JSON.readValue(json, Object.class);
            return new GmcpMessage(packageName, normalizeFromJson(payload), true);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON payload for GMCP package " + packageName + ".", e);
        }
    }

    private static void validatePackageName(String packageName) {
        if (packageName == null || !PACKAGE_NAME.matcher(packageName).matches()) {
            throw new IllegalArgumentException("Invalid GMCP package name: " + packageName);
        }
    }

    private static Object normalizeForJson(Object value) {
        if (value == null || value instanceof String || value instanceof Boolean || value instanceof Number) {
            return value;
        }
        if (value instanceof Map<?, ?> mapping) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : mapping.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    throw new IllegalArgumentException("GMCP JSON object keys must be strings.");
                }
                result.put(key, normalizeForJson(entry.getValue()));
            }
            return result;
        }
        if (value instanceof Collection<?> collection) {
            ArrayList<Object> result = new ArrayList<>(collection.size());
            for (Object item : collection) {
                result.add(normalizeForJson(item));
            }
            return result;
        }
        if (value.getClass().isArray()) {
            ArrayList<Object> result = new ArrayList<>(Array.getLength(value));
            for (int index = 0; index < Array.getLength(value); index++) {
                result.add(normalizeForJson(Array.get(value, index)));
            }
            return result;
        }
        throw new IllegalArgumentException("Unsupported GMCP JSON value: " + value.getClass().getName());
    }

    private static Object normalizeFromJson(Object value) {
        if (value == null) {
            return Integer.valueOf(0);
        }
        if (value instanceof Map<?, ?> mapping) {
            Map<Object, Object> result = new LinkedHashMap<>();
            mapping.forEach((key, item) -> result.put(key, normalizeFromJson(item)));
            return result;
        }
        if (value instanceof Collection<?> collection) {
            ArrayList<Object> result = new ArrayList<>(collection.size());
            collection.forEach(item -> result.add(normalizeFromJson(item)));
            return result;
        }
        return value;
    }
}
