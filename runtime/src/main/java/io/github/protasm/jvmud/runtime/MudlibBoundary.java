package io.github.protasm.jvmud.runtime;

import java.util.Collections;
import java.util.EnumSet;
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

    private final String boundaryObjectPath;
    private final String mfunObjectPath;
    private final Set<MudlibLifecycleEvent> lifecycleEvents;

    private MudlibBoundary(Builder builder) {
        this.boundaryObjectPath = normalizeOptionalPath(builder.boundaryObjectPath);
        this.mfunObjectPath = normalizeOptionalPath(builder.mfunObjectPath);
        this.lifecycleEvents = immutableCopy(builder.lifecycleEvents);
    }

    public static MudlibBoundary empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<String> boundaryObjectPath() {
        return Optional.ofNullable(boundaryObjectPath);
    }

    public Optional<String> mfunObjectPath() {
        return Optional.ofNullable(mfunObjectPath);
    }

    public Set<MudlibLifecycleEvent> lifecycleEvents() {
        return lifecycleEvents;
    }

    public boolean handles(MudlibLifecycleEvent event) {
        return lifecycleEvents.contains(Objects.requireNonNull(event, "event"));
    }

    public boolean declared() {
        return boundaryObjectPath != null
                || mfunObjectPath != null
                || !lifecycleEvents.isEmpty();
    }

    private static Set<MudlibLifecycleEvent> immutableCopy(EnumSet<MudlibLifecycleEvent> events) {
        if (events.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(events));
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

    public static final class Builder {
        private String boundaryObjectPath;
        private String mfunObjectPath;
        private final EnumSet<MudlibLifecycleEvent> lifecycleEvents =
                EnumSet.noneOf(MudlibLifecycleEvent.class);

        private Builder() {}

        public Builder boundaryObjectPath(String boundaryObjectPath) {
            this.boundaryObjectPath = boundaryObjectPath;
            return this;
        }

        public Builder mfunObjectPath(String mfunObjectPath) {
            this.mfunObjectPath = mfunObjectPath;
            return this;
        }

        public Builder handle(MudlibLifecycleEvent event) {
            lifecycleEvents.add(Objects.requireNonNull(event, "event"));
            return this;
        }

        public MudlibBoundary build() {
            return new MudlibBoundary(this);
        }
    }
}
