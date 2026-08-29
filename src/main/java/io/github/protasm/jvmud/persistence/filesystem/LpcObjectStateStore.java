package io.github.protasm.jvmud.persistence.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/** Filesystem-backed persistence for scalar generated LPC object fields. */
public final class LpcObjectStateStore {
    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /** Saves scalar LPC object fields to the supplied state file, returning LP-style success. */
    public int save(Path path, Object object) {
        Map<String, PersistentValue> fields = new LinkedHashMap<>();
        forEachPersistentField(object, field -> {
            try {
                field.setAccessible(true);
                Object value = field.get(object);
                PersistentValue persistentValue = encodePersistentValue(value);
                if (persistentValue != null) {
                    fields.put(fieldKey(field), persistentValue);
                }
            } catch (IllegalAccessException ignored) {
                // Inaccessible fields are not LPC-visible persistence state.
            }
        });

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            JSON.writeValue(path.toFile(), new LPCObjectStateFile(
                    "jvmud.lpc-object-state",
                    1,
                    fields));
            return 1;
        } catch (IOException | IllegalArgumentException e) {
            return 0;
        }
    }

    /** Restores scalar LPC object fields from the supplied state file, returning LP-style success. */
    public int restore(Path path, Object object) {
        if (!Files.isRegularFile(path)) {
            return 0;
        }

        Map<String, PersistentValue> fields;
        try {
            fields = readLPCObjectState(path);
        } catch (IOException e) {
            return 0;
        }

        forEachPersistentField(object, field -> {
            PersistentValue encoded = fields.get(fieldKey(field));
            if (encoded == null) {
                return;
            }
            Object value = decodePersistentValue(encoded, field.getType());
            if (value == UnsupportedValue.INSTANCE) {
                return;
            }
            try {
                field.setAccessible(true);
                if (field.getType() == int.class) {
                    field.setInt(object, ((Number) value).intValue());
                } else if (field.getType() == boolean.class) {
                    field.setBoolean(object, (Boolean) value);
                } else {
                    field.set(object, value);
                }
            } catch (IllegalAccessException ignored) {
                // Ignore fields that cannot be restored on this generated class version.
            }
        });
        return 1;
    }

    private void forEachPersistentField(Object object, Consumer<Field> action) {
        Class<?> type = object.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (field.isSynthetic() || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                    continue;
                }
                action.accept(field);
            }
            type = type.getSuperclass();
        }
    }

    private String fieldKey(Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }

    private PersistentValue encodePersistentValue(Object value) {
        if (value == null) {
            return new PersistentValue("null", null);
        }
        if (value instanceof String text) {
            return new PersistentValue("string", text);
        }
        if (value instanceof Integer number) {
            return new PersistentValue("int", number);
        }
        if (value instanceof Boolean bool) {
            return new PersistentValue("boolean", bool);
        }
        return null;
    }

    private Object decodePersistentValue(PersistentValue encoded, Class<?> targetType) {
        if ("null".equals(encoded.type())) {
            return targetType.isPrimitive() ? UnsupportedValue.INSTANCE : null;
        }
        if ((targetType == String.class || targetType == Object.class) && "string".equals(encoded.type())) {
            return encoded.value();
        }
        if ((targetType == int.class || targetType == Integer.class || targetType == Object.class)
                && "int".equals(encoded.type())) {
            return encoded.value() instanceof Number number
                    ? Integer.valueOf(number.intValue())
                    : UnsupportedValue.INSTANCE;
        }
        if ((targetType == boolean.class || targetType == Boolean.class || targetType == Object.class)
                && "boolean".equals(encoded.type())) {
            return encoded.value() instanceof Boolean ? encoded.value() : UnsupportedValue.INSTANCE;
        }
        return UnsupportedValue.INSTANCE;
    }

    private Map<String, PersistentValue> readLPCObjectState(Path path) throws IOException {
        String text = Files.readString(path);
        if (text.trim().startsWith("{")) {
            LPCObjectStateFile state = JSON.readValue(text, LPCObjectStateFile.class);
            return state.fields() != null ? state.fields() : Map.of();
        }
        return readLegacyPropertiesObjectState(path);
    }

    private Map<String, PersistentValue> readLegacyPropertiesObjectState(Path path) throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }
        Map<String, PersistentValue> fields = new LinkedHashMap<>();
        for (String name : properties.stringPropertyNames()) {
            PersistentValue value = decodeLegacyPersistentValue(properties.getProperty(name));
            if (value != null) {
                fields.put(name, value);
            }
        }
        return fields;
    }

    private PersistentValue decodeLegacyPersistentValue(String encoded) {
        int separator = encoded.indexOf(':');
        if (separator < 0) {
            return null;
        }
        String kind = encoded.substring(0, separator);
        String value = encoded.substring(separator + 1);
        return switch (kind) {
            case "null" -> new PersistentValue("null", null);
            case "string" -> new PersistentValue("string", value);
            case "int" -> {
                try {
                    yield new PersistentValue("int", Integer.valueOf(value));
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            case "boolean" -> new PersistentValue("boolean", Boolean.valueOf(value));
            default -> null;
        };
    }

    private record LPCObjectStateFile(String format, int version, Map<String, PersistentValue> fields) {}

    private record PersistentValue(String type, Object value) {}

    private enum UnsupportedValue {
        INSTANCE
    }
}
