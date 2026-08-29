package io.github.protasm.jvmud.engine.mudlib;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * JVMud-native registry of mudlib-side boundary objects and lifecycle interests.
 *
 * <p>The paths recorded here point to mudlib objects that perform translation. This class does not
 * implement legacy LP engine behavior and does not define engine concepts using legacy names.</p>
 *
 * <p>Lifecycle hooks are registered here as mappings from {@link MudlibLifecycleEvent} values to
 * mudlib method names. The event is the JVMud-native contract; the method name is mudlib policy.
 * For example, an LP245 compatibility boundary can map {@link MudlibLifecycleEvent#OBJECT_LOADED}
 * to {@code reset} and {@link MudlibLifecycleEvent#INTERACTION_SCOPE_STARTED} to {@code init}
 * without making those legacy method names part of the engine ontology.</p>
 *
 * <p>A mapping is optional. When an event occurs and no method is configured, JVMud skips the
 * mudlib call and continues the engine-owned operation. When a method is configured, the runtime
 * invokes it on the event's target object or on the configured boundary object, depending on the
 * event definition.</p>
 */
public final class MudlibBoundary {
    public static final int DEFAULT_MAX_LINE_LENGTH = 80;
    public static final int MIN_MAX_LINE_LENGTH = 20;
    public static final int MAX_MAX_LINE_LENGTH = 140;

    private static final MudlibBoundary EMPTY = builder().build();

    private final String gameId;
    private final String gameName;
    private final Path mudlibRootPath;
    private final String boundaryObjectPath;
    private final String mudlibGlobalObjectPath;
    private final Path mudlibGlobalObjectSourcePath;
    private final String compatibilityGlobalObjectPath;
    private final Path compatibilityGlobalObjectSourcePath;
    private final Set<String> compatibilityGlobalOverrides;
    private final String playerObjectPath;
    private final String sessionPolicy;
    private final String playerPrompt;
    private final String connectedBanner;
    private final String transportControlPrefix;
    private final String locationDiagnosticCommand;
    private final int maxLineLength;
    private final boolean showRuler;
    private final String initialPlacePath;
    private final String preloadFilePath;
    private final Set<String> preloadObjectPaths;
    private final Set<String> includePaths;
    private final String databaseJdbcUrl;
    private final String databaseUser;
    private final String databasePassword;
    private final Set<LanguageFeature> languageFeatures;
    private final Set<EngineCapability> engineCapabilities;
    private final String temporalTickMethod;
    private final Duration temporalTickInterval;
    private final Set<MudlibLifecycleEvent> lifecycleEvents;
    private final Map<MudlibLifecycleEvent, String> lifecycleMethods;
    private final Map<String, String> engineFunctionAliases;
    private final Map<String, String> mountedMudlibConfigs;
    private final Map<String, String> compatibilityPredefines;
    private final Map<String, Map<String, String>> compatibilityFunctionPredefines;

    private MudlibBoundary(Builder builder) {
        this.gameId = normalizeOptionalText(builder.gameId);
        this.gameName = normalizeOptionalText(builder.gameName);
        this.mudlibRootPath = normalizeOptionalFilesystemPath(builder.mudlibRootPath);
        this.boundaryObjectPath = normalizeOptionalPath(builder.boundaryObjectPath);
        this.mudlibGlobalObjectPath = normalizeOptionalPath(builder.mudlibGlobalObjectPath);
        this.mudlibGlobalObjectSourcePath = normalizeOptionalFilesystemPath(builder.mudlibGlobalObjectSourcePath);
        this.compatibilityGlobalObjectPath = normalizeOptionalPath(builder.compatibilityGlobalObjectPath);
        this.compatibilityGlobalObjectSourcePath = normalizeOptionalFilesystemPath(builder.compatibilityGlobalObjectSourcePath);
        this.compatibilityGlobalOverrides = normalizeTextSet(builder.compatibilityGlobalOverrides);
        this.playerObjectPath = normalizeOptionalPath(builder.playerObjectPath);
        this.sessionPolicy = normalizeOptionalText(builder.sessionPolicy);
        this.playerPrompt = normalizeOptionalPrompt(builder.playerPrompt);
        this.connectedBanner = normalizeOptionalPrompt(builder.connectedBanner);
        this.transportControlPrefix = normalizeOptionalPrompt(builder.transportControlPrefix);
        this.locationDiagnosticCommand = normalizeOptionalText(builder.locationDiagnosticCommand);
        this.maxLineLength = normalizeMaxLineLength(builder.maxLineLength);
        this.showRuler = builder.showRuler;
        this.initialPlacePath = normalizeOptionalPath(builder.initialPlacePath);
        this.preloadFilePath = normalizeOptionalPath(builder.preloadFilePath);
        this.preloadObjectPaths = normalizePathSet(builder.preloadObjectPaths);
        this.includePaths = normalizePathSet(builder.includePaths);
        this.databaseJdbcUrl = normalizeOptionalText(builder.databaseJdbcUrl);
        this.databaseUser = normalizeOptionalText(builder.databaseUser);
        this.databasePassword = builder.databasePassword != null ? builder.databasePassword : null;
        this.languageFeatures = Set.copyOf(builder.languageFeatures);
        this.engineCapabilities = Set.copyOf(builder.engineCapabilities);
        this.temporalTickMethod = normalizeOptionalText(builder.temporalTickMethod);
        this.temporalTickInterval = builder.temporalTickInterval;
        this.lifecycleEvents = immutableCopy(builder.lifecycleEvents);
        this.lifecycleMethods = immutableCopy(builder.lifecycleMethods);
        this.engineFunctionAliases = normalizeTextMap(builder.engineFunctionAliases);
        this.mountedMudlibConfigs = normalizeTextMap(builder.mountedMudlibConfigs);
        this.compatibilityPredefines = normalizeTextMap(builder.compatibilityPredefines);
        this.compatibilityFunctionPredefines = normalizeNestedTextMap(builder.compatibilityFunctionPredefines);
    }

    /** Returns a boundary with no declared mudlib integration points. */
    public static MudlibBoundary empty() {
        return EMPTY;
    }

    /** Starts a builder for immutable mudlib boundary metadata. */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the optional stable game id declared by the mudlib. */
    public Optional<String> gameId() {
        return Optional.ofNullable(gameId);
    }

    /** Returns the optional display name declared by the mudlib. */
    public Optional<String> gameName() {
        return Optional.ofNullable(gameName);
    }

    /** Returns the optional filesystem root used to resolve mudlib-absolute LPC paths. */
    public Optional<Path> mudlibRootPath() {
        return Optional.ofNullable(mudlibRootPath);
    }

    /** Returns the optional object path for a general mudlib boundary adapter. */
    public Optional<String> boundaryObjectPath() {
        return Optional.ofNullable(boundaryObjectPath);
    }

    /** Returns the optional mudlib-owned global function object path. */
    public Optional<String> mudlibGlobalObjectPath() {
        return Optional.ofNullable(mudlibGlobalObjectPath);
    }

    /**
     * Returns the optional filesystem source for the mudlib-owned global mfun object.
     *
     * <p>This lets profile-side mfun objects live beside the JVMud manifest even when the mudlib
     * source root points at imported upstream content.</p>
     */
    public Optional<Path> mudlibGlobalObjectSourcePath() {
        return Optional.ofNullable(mudlibGlobalObjectSourcePath);
    }

    /** Returns the optional JVMud compatibility global function object path. */
    public Optional<String> compatibilityGlobalObjectPath() {
        return Optional.ofNullable(compatibilityGlobalObjectPath);
    }

    /**
     * Returns the optional filesystem source for the JVMud compatibility global object.
     *
     * <p>This lets the compiler inspect a profile-side helper object's declarations even when the
     * upstream mudlib source root lives in a different folder.</p>
     */
    public Optional<Path> compatibilityGlobalObjectSourcePath() {
        return Optional.ofNullable(compatibilityGlobalObjectSourcePath);
    }

    /**
     * Returns function names for which the compatibility object takes precedence over the
     * mudlib-owned global object.
     *
     * <p>Overrides are explicit so a profile can adapt a small incompatible surface without
     * replacing the mudlib's remaining global policy.</p>
     */
    public Set<String> compatibilityGlobalOverrides() {
        return compatibilityGlobalOverrides;
    }

    /** Returns the optional mudlib-owned global mfun object path. */
    public Optional<String> mfunObjectPath() {
        return mudlibGlobalObjectPath();
    }

    /** Returns the optional LPC player object path used when binding interactive sessions. */
    public Optional<String> playerObjectPath() {
        return Optional.ofNullable(playerObjectPath);
    }

    /** Returns the optional JVMud host session-policy adapter selected by the manifest. */
    public Optional<String> sessionPolicy() {
        return Optional.ofNullable(sessionPolicy);
    }

    /** Returns the optional prompt text to display when an interactive player can enter commands. */
    public Optional<String> playerPrompt() {
        return Optional.ofNullable(playerPrompt);
    }

    /** Returns optional text emitted when a transport session first attaches. */
    public Optional<String> connectedBanner() {
        return Optional.ofNullable(connectedBanner);
    }

    /** Returns the transport command escape prefix, defaulting to {@code //}. */
    public String transportControlPrefix() {
        return transportControlPrefix != null ? transportControlPrefix : "//";
    }

    /** Returns an optional host diagnostic command that prints the native current location id. */
    public Optional<String> locationDiagnosticCommand() {
        return Optional.ofNullable(locationDiagnosticCommand);
    }

    /** Returns the configured maximum output line length before whitespace wrapping is attempted. */
    public int maxLineLength() {
        return maxLineLength;
    }

    /** Returns whether command prompts should be preceded by a visual line-length ruler. */
    public boolean showRuler() {
        return showRuler;
    }

    /** Returns the optional starting place object path for new local Personas. */
    public Optional<String> initialPlacePath() {
        return Optional.ofNullable(initialPlacePath);
    }

    /** Returns the optional mudlib-relative file that lists startup preload objects. */
    public Optional<String> preloadFilePath() {
        return Optional.ofNullable(preloadFilePath);
    }

    /** Returns additional mudlib object paths to preload at startup. */
    public Set<String> preloadObjectPaths() {
        return preloadObjectPaths;
    }

    /** Returns explicitly configured include search paths relative to the active mudlib root. */
    public Set<String> includePaths() {
        return includePaths;
    }

    /** Returns the optional JDBC URL used by JVMud-native database efuns. */
    public Optional<String> databaseJdbcUrl() {
        return Optional.ofNullable(databaseJdbcUrl);
    }

    /** Returns the optional JDBC user used by JVMud-native database efuns. */
    public Optional<String> databaseUser() {
        return Optional.ofNullable(databaseUser);
    }

    /** Returns the optional JDBC password used by JVMud-native database efuns. */
    public Optional<String> databasePassword() {
        return Optional.ofNullable(databasePassword);
    }

    /** Returns optional LPC syntax families explicitly selected by this profile. */
    public Set<LanguageFeature> languageFeatures() {
        return languageFeatures;
    }

    /** Returns host-resource capabilities explicitly granted to this mudlib. */
    public Set<EngineCapability> engineCapabilities() {
        return engineCapabilities;
    }

    /** Returns the optional mudlib method to invoke for deterministic temporal ticks. */
    public Optional<String> temporalTickMethod() {
        return Optional.ofNullable(temporalTickMethod);
    }

    /** Returns the configured wall-clock interval for one world tick, or {@link Duration#ZERO} when disabled. */
    public Duration temporalTickInterval() {
        return temporalTickInterval;
    }

    /** Returns the configured temporal tick interval in whole seconds, or {@code 0} when disabled. */
    public int temporalTickIntervalSeconds() {
        return Math.toIntExact(temporalTickInterval.getSeconds());
    }

    /**
     * Returns lifecycle events the mudlib has declared interest in handling.
     *
     * <p>This set includes events declared with {@link Builder#handle(MudlibLifecycleEvent)} and
     * events that have explicit method mappings through {@link Builder#lifecycleMethod(
     * MudlibLifecycleEvent, String)}. A declared event without a method mapping is useful as
     * boundary metadata, but JVMud can only invoke hooks that also have a mapped method.</p>
     *
     * @return immutable lifecycle event declarations
     */
    public Set<MudlibLifecycleEvent> lifecycleEvents() {
        return lifecycleEvents;
    }

    /**
     * Returns explicit lifecycle event-to-method mappings declared by the mudlib.
     *
     * <p>Keys are JVMud-native event names. Values are LPC method names to invoke when those events
     * are delivered.</p>
     *
     * @return immutable map from lifecycle event to mudlib method name
     */
    public Map<MudlibLifecycleEvent, String> lifecycleMethods() {
        return lifecycleMethods;
    }

    /**
     * Returns engine function name translations declared by this mudlib boundary.
     *
     * <p>Keys are mudlib-visible function spellings used by mudlib source, and values are
     * JVMud-native engine efun names. This keeps legacy vocabulary at the compatibility boundary
     * instead of adding those names to the engine efun catalog.</p>
     *
     * @return immutable map from mudlib-visible function name to JVMud engine function name
     */
    public Map<String, String> engineFunctionAliases() {
        return engineFunctionAliases;
    }

    /** Returns game-id to config-file declarations for additional worlds mounted by the host. */
    public Map<String, String> mountedMudlibConfigs() {
        return mountedMudlibConfigs;
    }

    /** Returns the JVMud-native engine function name for a mudlib-visible spelling, if one is declared. */
    public Optional<String> engineFunction(String mudlibName) {
        return Optional.ofNullable(engineFunctionAliases.get(normalizeRequiredText(mudlibName, "Engine function name")));
    }

    /**
     * Returns preprocessor predefines supplied by a mudlib compatibility profile.
     *
     * <p>These are compile-time facts exposed to LPC source before parsing. They are intended for
     * driver-compatibility shims, such as LDMud-flavored version macros that imported mudlibs probe
     * with {@code __VERSION_MAJOR__}. JVMud keeps them at the mudlib boundary instead of treating
     * another driver's predefined macro vocabulary as JVMud-native LPC.</p>
     *
     * @return immutable map from predefined macro name to replacement text
     */
    public Map<String, String> compatibilityPredefines() {
        return compatibilityPredefines;
    }

    /**
     * Returns configured function-like preprocessor compatibility probes.
     *
     * <p>The outer key is the macro name exposed to mudlib source. The inner key is the first
     * argument spelling, and the inner value is replacement source text. This supports driver
     * compatibility probes without baking another driver's macro names into JVMud's compiler.</p>
     *
     * @return immutable map from macro name to argument-specific replacement text
     */
    public Map<String, Map<String, String>> compatibilityFunctionPredefines() {
        return compatibilityFunctionPredefines;
    }

    /**
     * Returns the configured mudlib method for an event, if one was declared.
     *
     * @param event lifecycle event to inspect
     * @return configured mudlib method name, or empty when the event has no method mapping
     * @throws NullPointerException if {@code event} is {@code null}
     */
    public Optional<String> lifecycleMethod(MudlibLifecycleEvent event) {
        return Optional.ofNullable(lifecycleMethods.get(Objects.requireNonNull(event, "event")));
    }

    /**
     * Returns whether the mudlib declares any interest in an event.
     *
     * <p>This is broader than {@link #lifecycleMethod(MudlibLifecycleEvent)}: it is true for both a
     * bare event declaration and an explicit event-to-method mapping.</p>
     *
     * @param event lifecycle event to inspect
     * @return true when the event was declared or mapped
     * @throws NullPointerException if {@code event} is {@code null}
     */
    public boolean handles(MudlibLifecycleEvent event) {
        Objects.requireNonNull(event, "event");
        return lifecycleEvents.contains(event) || lifecycleMethods.containsKey(event);
    }

    /** Returns whether any boundary value was declared. */
    public boolean declared() {
        return boundaryObjectPath != null
                || gameId != null
                || gameName != null
                || mudlibRootPath != null
                || mudlibGlobalObjectPath != null
                || mudlibGlobalObjectSourcePath != null
                || compatibilityGlobalObjectPath != null
                || compatibilityGlobalObjectSourcePath != null
                || !compatibilityGlobalOverrides.isEmpty()
                || playerObjectPath != null
                || sessionPolicy != null
                || playerPrompt != null
                || connectedBanner != null
                || transportControlPrefix != null
                || locationDiagnosticCommand != null
                || maxLineLength != DEFAULT_MAX_LINE_LENGTH
                || showRuler
                || initialPlacePath != null
                || preloadFilePath != null
                || !preloadObjectPaths.isEmpty()
                || !includePaths.isEmpty()
                || databaseJdbcUrl != null
                || databaseUser != null
                || databasePassword != null
                || !languageFeatures.isEmpty()
                || !engineCapabilities.isEmpty()
                || temporalTickMethod != null
                || !temporalTickInterval.isZero()
                || !lifecycleEvents.isEmpty()
                || !lifecycleMethods.isEmpty()
                || !engineFunctionAliases.isEmpty()
                || !mountedMudlibConfigs.isEmpty()
                || !compatibilityPredefines.isEmpty()
                || !compatibilityFunctionPredefines.isEmpty();
    }

    private static Set<MudlibLifecycleEvent> immutableCopy(EnumSet<MudlibLifecycleEvent> events) {
        if (events.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(events));
    }

    private static Map<MudlibLifecycleEvent, String> immutableCopy(
            EnumMap<MudlibLifecycleEvent, String> methods) {
        if (methods.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new EnumMap<>(methods));
    }

    private static Map<String, String> normalizeTextMap(Map<String, String> mappings) {
        if (mappings.isEmpty()) {
            return Map.of();
        }
        java.util.LinkedHashMap<String, String> normalized = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            normalized.put(
                    normalizeRequiredText(entry.getKey(), "Mapping key"),
                    normalizeRequiredText(entry.getValue(), "Mapping value"));
        }
        return Collections.unmodifiableMap(normalized);
    }

    private static Map<String, Map<String, String>> normalizeNestedTextMap(
            Map<String, ? extends Map<String, String>> mappings) {
        if (mappings.isEmpty()) {
            return Map.of();
        }
        java.util.LinkedHashMap<String, Map<String, String>> normalized = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, ? extends Map<String, String>> entry : mappings.entrySet()) {
            normalized.put(
                    normalizeRequiredText(entry.getKey(), "Compatibility function predefine name"),
                    normalizeTextMap(entry.getValue()));
        }
        return Collections.unmodifiableMap(normalized);
    }

    private static String normalizeOptionalPath(String path) {
        if (path == null) {
            return null;
        }
        String normalized = path.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith(".c")) {
            normalized = normalized.substring(0, normalized.length() - 2);
        }
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Mudlib object path cannot be blank.");
        }
        return normalized;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeRequiredText(String value, String description) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new IllegalArgumentException(description + " cannot be blank.");
        }
        return normalized;
    }

    private static String normalizeOptionalPrompt(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private static int normalizeMaxLineLength(int maxLineLength) {
        if (maxLineLength < MIN_MAX_LINE_LENGTH || maxLineLength > MAX_MAX_LINE_LENGTH) {
            throw new IllegalArgumentException("max_line_length must be between "
                    + MIN_MAX_LINE_LENGTH + " and " + MAX_MAX_LINE_LENGTH + ".");
        }
        return maxLineLength;
    }

    private static Path normalizeOptionalFilesystemPath(Path path) {
        if (path == null) {
            return null;
        }
        return path.toAbsolutePath().normalize();
    }

    private static Set<String> normalizePathSet(Set<String> paths) {
        if (paths.isEmpty()) {
            return Set.of();
        }
        java.util.LinkedHashSet<String> normalized = new java.util.LinkedHashSet<>();
        for (String path : paths) {
            normalized.add(normalizeOptionalPath(path));
        }
        return Collections.unmodifiableSet(normalized);
    }

    private static Set<String> normalizeTextSet(Set<String> values) {
        if (values.isEmpty()) {
            return Set.of();
        }
        java.util.LinkedHashSet<String> normalized = new java.util.LinkedHashSet<>();
        for (String value : values) {
            normalized.add(normalizeRequiredText(value, "Compatibility override name"));
        }
        return Collections.unmodifiableSet(normalized);
    }

    public static final class Builder {
        private String gameId;
        private String gameName;
        private Path mudlibRootPath;
        private String boundaryObjectPath;
        private String mudlibGlobalObjectPath;
        private Path mudlibGlobalObjectSourcePath;
        private String compatibilityGlobalObjectPath;
        private Path compatibilityGlobalObjectSourcePath;
        private final java.util.LinkedHashSet<String> compatibilityGlobalOverrides =
                new java.util.LinkedHashSet<>();
        private String playerObjectPath;
        private String sessionPolicy;
        private String playerPrompt;
        private String connectedBanner;
        private String transportControlPrefix;
        private String locationDiagnosticCommand;
        private int maxLineLength = DEFAULT_MAX_LINE_LENGTH;
        private boolean showRuler;
        private String initialPlacePath;
        private String preloadFilePath;
        private final java.util.LinkedHashSet<String> preloadObjectPaths =
                new java.util.LinkedHashSet<>();
        private final java.util.LinkedHashSet<String> includePaths = new java.util.LinkedHashSet<>();
        private String databaseJdbcUrl;
        private String databaseUser;
        private String databasePassword;
        private final EnumSet<LanguageFeature> languageFeatures = EnumSet.noneOf(LanguageFeature.class);
        private final EnumSet<EngineCapability> engineCapabilities = EnumSet.noneOf(EngineCapability.class);
        private String temporalTickMethod;
        private Duration temporalTickInterval = Duration.ZERO;
        private final EnumSet<MudlibLifecycleEvent> lifecycleEvents =
                EnumSet.noneOf(MudlibLifecycleEvent.class);
        private final EnumMap<MudlibLifecycleEvent, String> lifecycleMethods =
                new EnumMap<>(MudlibLifecycleEvent.class);
        private final java.util.LinkedHashMap<String, String> engineFunctionAliases =
                new java.util.LinkedHashMap<>();
        private final java.util.LinkedHashMap<String, String> mountedMudlibConfigs =
                new java.util.LinkedHashMap<>();
        private final java.util.LinkedHashMap<String, String> compatibilityPredefines =
                new java.util.LinkedHashMap<>();
        private final java.util.LinkedHashMap<String, java.util.LinkedHashMap<String, String>>
                compatibilityFunctionPredefines = new java.util.LinkedHashMap<>();

        private Builder() {}

        /** Sets a stable game id. Blank values are treated as absent. */
        public Builder gameId(String gameId) {
            this.gameId = gameId;
            return this;
        }

        /** Sets a display name for the game. Blank values are treated as absent. */
        public Builder gameName(String gameName) {
            this.gameName = gameName;
            return this;
        }

        /** Sets the filesystem root used to resolve mudlib-absolute LPC paths. */
        public Builder mudlibRootPath(Path mudlibRootPath) {
            this.mudlibRootPath = mudlibRootPath;
            return this;
        }

        /** Sets a mudlib object path for a general boundary adapter. */
        public Builder boundaryObjectPath(String boundaryObjectPath) {
            this.boundaryObjectPath = boundaryObjectPath;
            return this;
        }

        /** Sets the mudlib-owned global function object path. */
        public Builder mudlibGlobalObjectPath(String mudlibGlobalObjectPath) {
            this.mudlibGlobalObjectPath = mudlibGlobalObjectPath;
            return this;
        }

        /** Sets the filesystem source for the mudlib-owned global mfun object. */
        public Builder mudlibGlobalObjectSourcePath(Path mudlibGlobalObjectSourcePath) {
            this.mudlibGlobalObjectSourcePath = mudlibGlobalObjectSourcePath;
            return this;
        }

        /** Sets the JVMud compatibility global function object path. */
        public Builder compatibilityGlobalObjectPath(String compatibilityGlobalObjectPath) {
            this.compatibilityGlobalObjectPath = compatibilityGlobalObjectPath;
            return this;
        }

        /** Sets the filesystem source for the JVMud compatibility global function object. */
        public Builder compatibilityGlobalObjectSourcePath(Path compatibilityGlobalObjectSourcePath) {
            this.compatibilityGlobalObjectSourcePath = compatibilityGlobalObjectSourcePath;
            return this;
        }

        /** Adds a function for which the compatibility object should override the mudlib global. */
        public Builder compatibilityGlobalOverride(String functionName) {
            this.compatibilityGlobalOverrides.add(functionName);
            return this;
        }

        /** Sets the mudlib-owned global mfun object path. */
        public Builder mfunObjectPath(String mfunObjectPath) {
            return mudlibGlobalObjectPath(mfunObjectPath);
        }

        /** Sets the LPC player object path used when binding interactive sessions. */
        public Builder playerObjectPath(String playerObjectPath) {
            this.playerObjectPath = playerObjectPath;
            return this;
        }

        /** Selects an explicit host session-policy adapter. */
        public Builder sessionPolicy(String sessionPolicy) {
            this.sessionPolicy = sessionPolicy;
            return this;
        }

        /** Sets the prompt text shown when an interactive player can enter commands. */
        public Builder playerPrompt(String playerPrompt) {
            this.playerPrompt = playerPrompt;
            return this;
        }

        /** Sets optional text emitted when a transport session first attaches. */
        public Builder connectedBanner(String connectedBanner) {
            this.connectedBanner = connectedBanner;
            return this;
        }

        /** Sets the escaped prefix reserved for transport-level commands. */
        public Builder transportControlPrefix(String transportControlPrefix) {
            this.transportControlPrefix = transportControlPrefix;
            return this;
        }

        /** Sets an optional host diagnostic command for displaying the current location id. */
        public Builder locationDiagnosticCommand(String locationDiagnosticCommand) {
            this.locationDiagnosticCommand = locationDiagnosticCommand;
            return this;
        }

        /** Sets the maximum output line length before whitespace wrapping is attempted. */
        public Builder maxLineLength(int maxLineLength) {
            this.maxLineLength = normalizeMaxLineLength(maxLineLength);
            return this;
        }

        /** Sets whether command prompts should be preceded by a visual line-length ruler. */
        public Builder showRuler(boolean showRuler) {
            this.showRuler = showRuler;
            return this;
        }

        /** Sets the starting place object path for new Personas. */
        public Builder initialPlacePath(String initialPlacePath) {
            this.initialPlacePath = initialPlacePath;
            return this;
        }

        /** Sets the mudlib-relative file that lists startup preload objects. */
        public Builder preloadFilePath(String preloadFilePath) {
            this.preloadFilePath = preloadFilePath;
            return this;
        }

        /** Adds a startup preload object path. */
        public Builder preloadObjectPath(String preloadObjectPath) {
            this.preloadObjectPaths.add(preloadObjectPath);
            return this;
        }

        /** Adds an include search path relative to the active mudlib root. */
        public Builder includePath(String includePath) {
            includePaths.add(includePath);
            return this;
        }

        /** Sets the JDBC URL used by JVMud-native database efuns. */
        public Builder databaseJdbcUrl(String databaseJdbcUrl) {
            this.databaseJdbcUrl = databaseJdbcUrl;
            return this;
        }

        /** Sets the JDBC user used by JVMud-native database efuns. */
        public Builder databaseUser(String databaseUser) {
            this.databaseUser = databaseUser;
            return this;
        }

        /** Sets the JDBC password used by JVMud-native database efuns. */
        public Builder databasePassword(String databasePassword) {
            this.databasePassword = databasePassword;
            return this;
        }

        /** Enables an optional LPC syntax family for this mudlib profile. */
        public Builder languageFeature(LanguageFeature feature) {
            languageFeatures.add(Objects.requireNonNull(feature, "feature"));
            return this;
        }

        /** Grants a host-resource capability to this mudlib profile. */
        public Builder engineCapability(EngineCapability capability) {
            engineCapabilities.add(Objects.requireNonNull(capability, "capability"));
            return this;
        }

        /** Sets the mudlib method to invoke for deterministic temporal ticks. */
        public Builder temporalTickMethod(String temporalTickMethod) {
            this.temporalTickMethod = temporalTickMethod;
            return this;
        }

        /** Sets the wall-clock interval for one world tick. */
        public Builder temporalTickInterval(Duration temporalTickInterval) {
            Objects.requireNonNull(temporalTickInterval, "temporalTickInterval");
            if (temporalTickInterval.isNegative()) {
                throw new IllegalArgumentException("Temporal tick interval cannot be negative.");
            }
            this.temporalTickInterval = temporalTickInterval;
            return this;
        }

        /** Sets the wall-clock interval for one world tick in whole seconds. */
        public Builder temporalTickIntervalSeconds(int temporalTickIntervalSeconds) {
            if (temporalTickIntervalSeconds < 0) {
                throw new IllegalArgumentException("Temporal tick interval cannot be negative.");
            }
            return temporalTickInterval(Duration.ofSeconds(temporalTickIntervalSeconds));
        }

        /**
         * Declares interest in a lifecycle event without naming a specific method.
         *
         * <p>This records boundary intent and makes {@link MudlibBoundary#handles(
         * MudlibLifecycleEvent)} return true, but it does not by itself give JVMud a method to
         * invoke. Use {@link #lifecycleMethod(MudlibLifecycleEvent, String)} for hooks that should
         * actually call into LPC code.</p>
         *
         * @param event lifecycle event the mudlib understands
         * @return this builder
         * @throws NullPointerException if {@code event} is {@code null}
         */
        public Builder handle(MudlibLifecycleEvent event) {
            lifecycleEvents.add(Objects.requireNonNull(event, "event"));
            return this;
        }

        /**
         * Maps a lifecycle event to a mudlib method name.
         *
         * <p>The event remains JVMud-native while the method name is mudlib-specific. Blank method
         * names remove any existing mapping. Nonblank names also mark the event as handled so
         * {@link MudlibBoundary#handles(MudlibLifecycleEvent)} remains true.</p>
         *
         * @param event lifecycle event to map
         * @param methodName mudlib method name to invoke for the event
         * @return this builder
         * @throws NullPointerException if {@code event} is {@code null}
         */
        public Builder lifecycleMethod(MudlibLifecycleEvent event, String methodName) {
            Objects.requireNonNull(event, "event");
            String normalized = normalizeOptionalText(methodName);
            if (normalized == null) {
                lifecycleMethods.remove(event);
            } else {
                lifecycleMethods.put(event, normalized);
                lifecycleEvents.add(event);
            }
            return this;
        }

        /** Maps a JVMud-native engine function name to a mudlib-visible function spelling. */
        public Builder engineFunction(String engineName, String mudlibName) {
            String normalizedMudlibName = normalizeRequiredText(mudlibName, "Mudlib-visible engine function name");
            String normalizedEngineName = normalizeOptionalText(engineName);
            if (normalizedEngineName == null) {
                engineFunctionAliases.remove(normalizedMudlibName);
            } else {
                engineFunctionAliases.put(normalizedMudlibName, normalizedEngineName);
            }
            return this;
        }

        /** Declares another mudlib configuration to mount under its stable game id. */
        public Builder mountedMudlib(String gameId, String configPath) {
            mountedMudlibConfigs.put(
                    normalizeRequiredText(gameId, "Mounted mudlib game id"),
                    normalizeRequiredText(configPath, "Mounted mudlib config path"));
            return this;
        }

        /**
         * Declares a compile-time compatibility predefine for this mudlib profile.
         *
         * <p>The replacement text is passed to the LPC preprocessor as source text. String-valued
         * macros should therefore include their LPC quotes, for example {@code "\"3.6.3\""}.</p>
         *
         * @param macroName predefined macro name exposed to mudlib source
         * @param replacementText replacement source text for the macro
         * @return this builder
         * @throws IllegalArgumentException if {@code macroName} is blank
         */
        public Builder compatibilityPredefine(String macroName, String replacementText) {
            String normalizedName = normalizeRequiredText(macroName, "Compatibility predefine name");
            String normalizedReplacement = normalizeOptionalText(replacementText);
            if (normalizedReplacement == null) {
                compatibilityPredefines.remove(normalizedName);
            } else {
                compatibilityPredefines.put(normalizedName, normalizedReplacement);
            }
            return this;
        }

        /**
         * Declares an argument-specific function-like preprocessor compatibility replacement.
         *
         * <p>For a configured macro {@code PROBE} and argument {@code feature}, the preprocessor can
         * replace {@code PROBE(feature)} with the supplied replacement source text. JVMud treats the
         * macro name and argument as mudlib-boundary data, not as engine-native LPC vocabulary.</p>
         *
         * @param macroName function-like macro name exposed to mudlib source
         * @param argumentName first argument spelling that should match
         * @param replacementText replacement source text for that call
         * @return this builder
         * @throws IllegalArgumentException if {@code macroName} or {@code argumentName} is blank
         */
        public Builder compatibilityFunctionPredefine(
                String macroName, String argumentName, String replacementText) {
            String normalizedName = normalizeRequiredText(macroName, "Compatibility function predefine name");
            String normalizedArgument =
                    normalizeRequiredText(argumentName, "Compatibility function predefine argument");
            String normalizedReplacement = normalizeOptionalText(replacementText);
            java.util.LinkedHashMap<String, String> replacements =
                    compatibilityFunctionPredefines.computeIfAbsent(normalizedName, ignored -> new java.util.LinkedHashMap<>());
            if (normalizedReplacement == null) {
                replacements.remove(normalizedArgument);
                if (replacements.isEmpty()) {
                    compatibilityFunctionPredefines.remove(normalizedName);
                }
            } else {
                replacements.put(normalizedArgument, normalizedReplacement);
            }
            return this;
        }

        /** Builds an immutable, normalized boundary declaration. */
        public MudlibBoundary build() {
            return new MudlibBoundary(this);
        }
    }
}
