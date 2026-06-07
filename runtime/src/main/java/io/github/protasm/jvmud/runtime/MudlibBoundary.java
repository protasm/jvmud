package io.github.protasm.jvmud.runtime;

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
 */
public final class MudlibBoundary {
    private static final MudlibBoundary EMPTY = builder().build();

    private final String gameId;
    private final String gameName;
    private final String boundaryObjectPath;
    private final String mfunObjectPath;
    private final String playerObjectPath;
    private final String initialPlacePath;
    private final String initialPresenceId;
    private final String preloadFilePath;
    private final Set<String> preloadObjectPaths;
    private final String temporalTickMethod;
    private final int temporalTickIntervalSeconds;
    private final Set<MudlibLifecycleEvent> lifecycleEvents;
    private final Map<MudlibLifecycleEvent, String> lifecycleMethods;

    private MudlibBoundary(Builder builder) {
        this.gameId = normalizeOptionalText(builder.gameId);
        this.gameName = normalizeOptionalText(builder.gameName);
        this.boundaryObjectPath = normalizeOptionalPath(builder.boundaryObjectPath);
        this.mfunObjectPath = normalizeOptionalPath(builder.mfunObjectPath);
        this.playerObjectPath = normalizeOptionalPath(builder.playerObjectPath);
        this.initialPlacePath = normalizeOptionalPath(builder.initialPlacePath);
        this.initialPresenceId = normalizeOptionalText(builder.initialPresenceId);
        this.preloadFilePath = normalizeOptionalPath(builder.preloadFilePath);
        this.preloadObjectPaths = normalizePathSet(builder.preloadObjectPaths);
        this.temporalTickMethod = normalizeOptionalText(builder.temporalTickMethod);
        this.temporalTickIntervalSeconds = builder.temporalTickIntervalSeconds;
        this.lifecycleEvents = immutableCopy(builder.lifecycleEvents);
        this.lifecycleMethods = immutableCopy(builder.lifecycleMethods);
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

    /** Returns the optional object path for a general mudlib boundary adapter. */
    public Optional<String> boundaryObjectPath() {
        return Optional.ofNullable(boundaryObjectPath);
    }

    /** Returns the optional object path for legacy mudlib function adapters. */
    public Optional<String> mfunObjectPath() {
        return Optional.ofNullable(mfunObjectPath);
    }

    /** Returns the optional LPC player object path used when binding interactive sessions. */
    public Optional<String> playerObjectPath() {
        return Optional.ofNullable(playerObjectPath);
    }

    /** Returns the optional starting place object path for new local presence. */
    public Optional<String> initialPlacePath() {
        return Optional.ofNullable(initialPlacePath);
    }

    /** Returns the optional initial engine presence id used for host-owned personas. */
    public Optional<String> initialPresenceId() {
        return Optional.ofNullable(initialPresenceId);
    }

    /** Returns the optional mudlib-relative file that lists startup preload objects. */
    public Optional<String> preloadFilePath() {
        return Optional.ofNullable(preloadFilePath);
    }

    /** Returns additional mudlib object paths to preload at startup. */
    public Set<String> preloadObjectPaths() {
        return preloadObjectPaths;
    }

    /** Returns the optional mudlib method to invoke for deterministic temporal ticks. */
    public Optional<String> temporalTickMethod() {
        return Optional.ofNullable(temporalTickMethod);
    }

    /** Returns the configured temporal tick interval in seconds, or {@code 0} when disabled. */
    public int temporalTickIntervalSeconds() {
        return temporalTickIntervalSeconds;
    }

    /** Returns lifecycle events the mudlib has declared interest in handling. */
    public Set<MudlibLifecycleEvent> lifecycleEvents() {
        return lifecycleEvents;
    }

    /** Returns explicit lifecycle event-to-method mappings declared by the mudlib. */
    public Map<MudlibLifecycleEvent, String> lifecycleMethods() {
        return lifecycleMethods;
    }

    /** Returns the configured mudlib method for an event, if one was declared. */
    public Optional<String> lifecycleMethod(MudlibLifecycleEvent event) {
        return Optional.ofNullable(lifecycleMethods.get(Objects.requireNonNull(event, "event")));
    }

    /** Returns whether the mudlib declares any handler for an event. */
    public boolean handles(MudlibLifecycleEvent event) {
        Objects.requireNonNull(event, "event");
        return lifecycleEvents.contains(event) || lifecycleMethods.containsKey(event);
    }

    /** Returns whether any boundary value was declared. */
    public boolean declared() {
        return boundaryObjectPath != null
                || gameId != null
                || gameName != null
                || mfunObjectPath != null
                || playerObjectPath != null
                || initialPlacePath != null
                || initialPresenceId != null
                || preloadFilePath != null
                || !preloadObjectPaths.isEmpty()
                || temporalTickMethod != null
                || temporalTickIntervalSeconds > 0
                || !lifecycleEvents.isEmpty()
                || !lifecycleMethods.isEmpty();
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

    public static final class Builder {
        private String gameId;
        private String gameName;
        private String boundaryObjectPath;
        private String mfunObjectPath;
        private String playerObjectPath;
        private String initialPlacePath;
        private String initialPresenceId;
        private String preloadFilePath;
        private final java.util.LinkedHashSet<String> preloadObjectPaths =
                new java.util.LinkedHashSet<>();
        private String temporalTickMethod;
        private int temporalTickIntervalSeconds;
        private final EnumSet<MudlibLifecycleEvent> lifecycleEvents =
                EnumSet.noneOf(MudlibLifecycleEvent.class);
        private final EnumMap<MudlibLifecycleEvent, String> lifecycleMethods =
                new EnumMap<>(MudlibLifecycleEvent.class);

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

        /** Sets a mudlib object path for a general boundary adapter. */
        public Builder boundaryObjectPath(String boundaryObjectPath) {
            this.boundaryObjectPath = boundaryObjectPath;
            return this;
        }

        /** Sets a mudlib object path for legacy mfun/efun adapter behavior. */
        public Builder mfunObjectPath(String mfunObjectPath) {
            this.mfunObjectPath = mfunObjectPath;
            return this;
        }

        /** Sets the LPC player object path used when binding interactive sessions. */
        public Builder playerObjectPath(String playerObjectPath) {
            this.playerObjectPath = playerObjectPath;
            return this;
        }

        /** Sets the starting place object path for new presence. */
        public Builder initialPlacePath(String initialPlacePath) {
            this.initialPlacePath = initialPlacePath;
            return this;
        }

        /** Sets the initial host-owned presence id. */
        public Builder initialPresenceId(String initialPresenceId) {
            this.initialPresenceId = initialPresenceId;
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

        /** Sets the mudlib method to invoke for deterministic temporal ticks. */
        public Builder temporalTickMethod(String temporalTickMethod) {
            this.temporalTickMethod = temporalTickMethod;
            return this;
        }

        /** Sets the temporal tick interval in seconds. */
        public Builder temporalTickIntervalSeconds(int temporalTickIntervalSeconds) {
            if (temporalTickIntervalSeconds < 0) {
                throw new IllegalArgumentException("Temporal tick interval cannot be negative.");
            }
            this.temporalTickIntervalSeconds = temporalTickIntervalSeconds;
            return this;
        }

        /** Declares interest in a lifecycle event without naming a specific method. */
        public Builder handle(MudlibLifecycleEvent event) {
            lifecycleEvents.add(Objects.requireNonNull(event, "event"));
            return this;
        }

        /** Maps a lifecycle event to a mudlib method name. Blank method names remove the mapping. */
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

        /** Builds an immutable, normalized boundary declaration. */
        public MudlibBoundary build() {
            return new MudlibBoundary(this);
        }
    }
}
