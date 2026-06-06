package io.github.protasm.jvmud.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Reads JVMud-native mudlib boundary declarations from a simple manifest file. */
public final class MudlibBoundaryConfigReader {
    private MudlibBoundaryConfigReader() {}

    public static MudlibBoundary read(Path mudlibRoot, String configPath) throws IOException {
        Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        Objects.requireNonNull(configPath, "configPath");

        Map<String, List<String>> values = readConfigValues(mudlibRoot.resolve(configPath));
        MudlibBoundary.Builder builder = MudlibBoundary.builder()
                .boundaryObjectPath(configPath);

        addString(builder::gameId, firstValue(values, "game_id"));
        addString(builder::gameName, firstValue(values, "game_name"));
        addString(builder::mfunObjectPath, firstValue(values, "mfun_object"));
        addString(builder::initialPlacePath, firstValue(values, "initial_place"));
        String initialPresence = firstValue(values, "initial_presence_id");
        if (initialPresence == null) {
            initialPresence = firstValue(values, "initial_presence");
        }
        addString(builder::initialPresenceId, initialPresence);
        addString(builder::preloadFilePath, firstValue(values, "preload_file"));
        addPreloadObjects(builder, allValues(values, "preload_objects"));
        addLifecycleEvents(builder, allValues(values, "handled_lifecycle_events"));
        addLifecycleMethods(builder, values);
        addString(builder::temporalTickMethod, firstValue(values, "temporal_tick_method"));
        addPositiveInt(builder::temporalTickIntervalSeconds, firstValue(values, "temporal_tick_interval"));

        return builder.build();
    }

    public static MudlibLifecycleEvent lifecycleEvent(String name) {
        String normalized = normalizeLifecycleEventName(name);
        return switch (normalized) {
            case "OBJECT_INITIALIZED" -> MudlibLifecycleEvent.OBJECT_LOADED;
            case "OBJECT_REACTIVATED" -> MudlibLifecycleEvent.OBJECT_ACTIVATED;
            case "INTERACTION_SCOPE_ENTERED" -> MudlibLifecycleEvent.INTERACTION_SCOPE_STARTED;
            case "PLAYER_CONNECTED" -> MudlibLifecycleEvent.PLAYER_SESSION_CONNECTED;
            case "PLAYER_RESOLVED", "PERSONA_RESOLVED" -> MudlibLifecycleEvent.PLAYER_PERSONA_RESOLVED;
            case "PLAYER_BOUND" -> MudlibLifecycleEvent.PLAYER_OBJECT_BOUND;
            case "PLAYER_ENTERED" -> MudlibLifecycleEvent.PLAYER_ENTERED_WORLD;
            case "PLAYER_DISCONNECTED" -> MudlibLifecycleEvent.PLAYER_SESSION_DISCONNECTED;
            default -> MudlibLifecycleEvent.valueOf(normalized);
        };
    }

    private static Map<String, List<String>> readConfigValues(Path configFile) throws IOException {
        Map<String, List<String>> values = new LinkedHashMap<>();
        for (String line : Files.readAllLines(configFile)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            int separator = trimmed.indexOf('=');
            if (separator == -1) {
                separator = trimmed.indexOf(':');
            }
            if (separator == -1) {
                throw new IllegalArgumentException("Invalid mudlib config line: " + line);
            }

            String key = trimmed.substring(0, separator).trim();
            String rawValue = stripInlineComment(trimmed.substring(separator + 1)).trim();
            if (key.isEmpty() || rawValue.isEmpty()) {
                continue;
            }

            for (String value : splitValues(rawValue)) {
                values.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
            }
        }
        return values;
    }

    private static String stripInlineComment(String value) {
        int comment = value.indexOf('#');
        return comment == -1 ? value : value.substring(0, comment);
    }

    private static List<String> splitValues(String rawValue) {
        List<String> values = new ArrayList<>();
        for (String part : rawValue.split(",")) {
            String value = unquote(part.trim());
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private static String unquote(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static String firstValue(Map<String, List<String>> values, String key) {
        List<String> matches = values.get(key);
        return matches == null || matches.isEmpty() ? null : matches.get(0);
    }

    private static List<String> allValues(Map<String, List<String>> values, String key) {
        return values.getOrDefault(key, List.of());
    }

    private static void addString(java.util.function.Consumer<String> setter, String value) {
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }

    private static void addPositiveInt(java.util.function.IntConsumer setter, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        int parsed = Integer.parseInt(value.trim());
        if (parsed > 0) {
            setter.accept(parsed);
        }
    }

    private static void addPreloadObjects(MudlibBoundary.Builder builder, List<String> declaredObjects) {
        for (String object : declaredObjects) {
            if (!object.isBlank()) {
                builder.preloadObjectPath(object);
            }
        }
    }

    private static void addLifecycleEvents(MudlibBoundary.Builder builder, List<String> declaredEvents) {
        for (String event : declaredEvents) {
            if (!event.isBlank()) {
                builder.handle(lifecycleEvent(event));
            }
        }
    }

    private static void addLifecycleMethods(MudlibBoundary.Builder builder, Map<String, List<String>> values) {
        for (Map.Entry<String, List<String>> entry : values.entrySet()) {
            String key = entry.getKey().trim();
            if (!key.startsWith("lifecycle.")) {
                continue;
            }

            String eventName = key.substring("lifecycle.".length());
            if (eventName.isBlank() || entry.getValue().isEmpty()) {
                continue;
            }
            String methodName = entry.getValue().get(0);
            if (!methodName.isBlank()) {
                builder.lifecycleMethod(lifecycleEvent(eventName), methodName);
            }
        }
    }

    private static String normalizeLifecycleEventName(String name) {
        return name.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();
    }
}
