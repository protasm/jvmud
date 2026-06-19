package io.github.protasm.jvmud.engine.mudlib;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reads JVMud-native mudlib boundary declarations from a simple manifest file.
 *
 * <p>The manifest is intentionally small: each non-comment line is a {@code key = value} or
 * {@code key: value} declaration, with comma-separated values accepted where a key supports
 * multiple entries. Lifecycle hooks are declared either as bare handled events:</p>
 *
 * <pre>{@code
 * handled_lifecycle_events = scheduled_tick
 * }</pre>
 *
 * <p>or as event-to-method mappings:</p>
 *
 * <pre>{@code
 * lifecycle.object_loaded = reset
 * lifecycle.interaction_scope_started = init
 * lifecycle.player_session_connected = logon
 * }</pre>
 *
 * <p>The {@code lifecycle.} key suffix is parsed with {@link #lifecycleEvent(String)}, so manifest
 * authors may use lower-case names, hyphens, or spaces. The resulting event stays JVMud-native;
 * the value remains the mudlib's method name.</p>
 */
public final class MudlibBoundaryConfigReader {
    private MudlibBoundaryConfigReader() {}

    /**
     * Reads and normalizes a mudlib boundary manifest.
     *
     * <p>{@code mudlibRoot} is the fallback filesystem root for mudlib-absolute LPC paths. If the
     * manifest declares {@code mudlib_root}, relative values are resolved against the manifest
     * file's directory.</p>
     *
     * @param mudlibRoot fallback mudlib root directory
     * @param configPath manifest path relative to {@code mudlibRoot}
     * @return immutable boundary metadata
     * @throws IOException if the manifest cannot be read
     * @throws IllegalArgumentException if a line, enum name, boolean, duration, or bounded integer
     *         value is invalid
     * @throws NullPointerException if either argument is {@code null}
     */
    public static MudlibBoundary read(Path mudlibRoot, String configPath) throws IOException {
        Objects.requireNonNull(mudlibRoot, "mudlibRoot");
        Objects.requireNonNull(configPath, "configPath");

        Path configFile = mudlibRoot.resolve(configPath).toAbsolutePath().normalize();
        Map<String, List<String>> values = readConfigValues(configFile);
        MudlibBoundary.Builder builder = MudlibBoundary.builder();

        addString(builder::gameId, firstValue(values, "game_id"));
        addString(builder::gameName, firstValue(values, "game_name"));
        builder.mudlibRootPath(resolveMudlibRootPath(configFile, mudlibRoot, firstValue(values, "mudlib_root")));
        addString(builder::boundaryObjectPath, firstValue(values, "mudlib_object"));
        addString(builder::mfunObjectPath, firstValue(values, "mfun_object"));
        addString(builder::playerObjectPath, firstValue(values, "persona_object", "player_object"));
        String playerPrompt = firstValue(values, "player_prompt");
        if (playerPrompt == null) {
            playerPrompt = firstValue(values, "command_prompt");
        }
        addString(builder::playerPrompt, playerPrompt);
        addBoundedInt(builder::maxLineLength, firstValue(values, "max_line_length"));
        addBoolean(builder::showRuler, firstValue(values, "show_ruler"));
        addString(builder::initialPlacePath, firstValue(values, "initial_place"));
        addString(builder::preloadFilePath, firstValue(values, "preload_file"));
        addPreloadObjects(builder, allValues(values, "preload_objects"));
        addDirectEfunAliases(builder, values);
        addLdmudCompatibilityPredefines(builder, values);
        addLifecycleEvents(builder, allValues(values, "handled_lifecycle_events"));
        addLifecycleMethods(builder, values);
        addString(builder::temporalTickMethod, firstValue(values, "temporal_tick_method"));
        addPositiveDuration(builder::temporalTickInterval, firstValue(values, "temporal_tick_interval"));

        return builder.build();
    }

    /**
     * Parses a lifecycle event name from mudlib configuration.
     *
     * <p>Names are trimmed, hyphens and spaces become underscores, and matching is case-insensitive.
     * A few older boundary names are accepted as aliases so existing manifests keep working while
     * the public contract uses the current {@link MudlibLifecycleEvent} names.</p>
     *
     * @param name manifest event name, such as {@code object_loaded} or {@code interaction scope
     *        started}
     * @return matching lifecycle event
     * @throws IllegalArgumentException if the normalized name is not a known event or alias
     * @throws NullPointerException if {@code name} is {@code null}
     */
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

            for (String value : splitValues(rawValue, shouldPreserveReplacementText(key))) {
                values.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
            }
        }
        return values;
    }

    private static String stripInlineComment(String value) {
        int comment = value.indexOf('#');
        return comment == -1 ? value : value.substring(0, comment);
    }

    private static List<String> splitValues(String rawValue, boolean preserveReplacementText) {
        if (preserveReplacementText) {
            return rawValue.isBlank() ? List.of() : List.of(rawValue.trim());
        }

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

    private static boolean shouldPreserveReplacementText(String key) {
        return key.trim().startsWith("ldmud_compat_predefine.");
    }

    private static String firstValue(Map<String, List<String>> values, String... keys) {
        for (String key : keys) {
            List<String> matches = values.get(key);
            if (matches != null && !matches.isEmpty()) {
                return matches.get(0);
            }
        }
        return null;
    }

    private static List<String> allValues(Map<String, List<String>> values, String key) {
        return values.getOrDefault(key, List.of());
    }

    private static Path resolveMudlibRootPath(Path configFile, Path fallbackRoot, String declaredRoot) {
        if (declaredRoot == null || declaredRoot.isBlank()) {
            return fallbackRoot.toAbsolutePath().normalize();
        }

        Path root = Path.of(declaredRoot.trim());
        if (!root.isAbsolute()) {
            Path configDir = configFile.getParent();
            if (configDir != null) {
                root = configDir.resolve(root);
            }
        }
        return root.toAbsolutePath().normalize();
    }

    private static void addString(java.util.function.Consumer<String> setter, String value) {
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }

    private static void addBoundedInt(java.util.function.IntConsumer setter, String value) {
        if (value != null && !value.isBlank()) {
            setter.accept(Integer.parseInt(value.trim()));
        }
    }

    private static void addBoolean(java.util.function.Consumer<Boolean> setter, String value) {
        if (value != null && !value.isBlank()) {
            setter.accept(parseBoolean(value.trim()));
        }
    }

    private static boolean parseBoolean(String value) {
        return switch (value.toLowerCase()) {
            case "true", "yes", "on", "1" -> true;
            case "false", "no", "off", "0" -> false;
            default -> throw new IllegalArgumentException("Invalid boolean config value: " + value);
        };
    }

    private static void addPositiveDuration(java.util.function.Consumer<Duration> setter, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        Duration parsed = parseDuration(value.trim());
        if (!parsed.isZero()) {
            setter.accept(parsed);
        }
    }

    private static Duration parseDuration(String value) {
        String normalized = value.toLowerCase();
        BigDecimal multiplier;
        String amount;
        if (normalized.endsWith("ms")) {
            amount = normalized.substring(0, normalized.length() - 2).trim();
            multiplier = BigDecimal.valueOf(1_000_000L);
        } else if (normalized.endsWith("millisecond") || normalized.endsWith("milliseconds")) {
            amount = normalized.replaceFirst("milliseconds?$", "").trim();
            multiplier = BigDecimal.valueOf(1_000_000L);
        } else if (normalized.endsWith("s")) {
            amount = normalized.substring(0, normalized.length() - 1).trim();
            multiplier = BigDecimal.valueOf(1_000_000_000L);
        } else if (normalized.endsWith("second") || normalized.endsWith("seconds")) {
            amount = normalized.replaceFirst("seconds?$", "").trim();
            multiplier = BigDecimal.valueOf(1_000_000_000L);
        } else {
            amount = normalized;
            multiplier = BigDecimal.valueOf(1_000_000_000L);
        }

        BigDecimal numeric = new BigDecimal(amount);
        if (numeric.signum() < 0) {
            throw new IllegalArgumentException("Temporal tick interval cannot be negative.");
        }
        long nanos = numeric.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).longValueExact();
        return Duration.ofNanos(nanos);
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

    private static void addDirectEfunAliases(MudlibBoundary.Builder builder, Map<String, List<String>> values) {
        for (Map.Entry<String, List<String>> entry : values.entrySet()) {
            String key = entry.getKey().trim();
            if (!key.startsWith("direct_efun.")) {
                continue;
            }

            String mudlibName = key.substring("direct_efun.".length());
            if (mudlibName.isBlank() || entry.getValue().isEmpty()) {
                continue;
            }

            String engineName = entry.getValue().get(0);
            if (!engineName.isBlank()) {
                builder.directEfunAlias(mudlibName, engineName);
            }
        }
    }

    private static void addLdmudCompatibilityPredefines(
            MudlibBoundary.Builder builder, Map<String, List<String>> values) {
        for (Map.Entry<String, List<String>> entry : values.entrySet()) {
            String key = entry.getKey().trim();
            if (!key.startsWith("ldmud_compat_predefine.")) {
                continue;
            }

            String macroName = key.substring("ldmud_compat_predefine.".length());
            if (macroName.isBlank() || entry.getValue().isEmpty()) {
                continue;
            }

            String replacementText = entry.getValue().get(0);
            if (!replacementText.isBlank()) {
                builder.compatibilityPredefine(macroName, replacementText);
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
