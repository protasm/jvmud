package io.github.protasm.jvmud.runtime;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/** Something that exists within a JVMud world and may itself contain entities. */
public final class Entity implements Location {
    private final String id;
    private final String displayName;
    private final EnumSet<Capability> capabilities;

    Entity(String id, String displayName, Set<Capability> capabilities) {
        this.id = requireIdentifier(id, "id");
        this.displayName = requireIdentifier(displayName, "displayName");
        this.capabilities = capabilities.isEmpty()
                ? EnumSet.noneOf(Capability.class)
                : EnumSet.copyOf(capabilities);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    public boolean hasCapability(Capability capability) {
        return capabilities.contains(Objects.requireNonNull(capability, "capability"));
    }

    public Set<Capability> capabilities() {
        return Set.copyOf(capabilities);
    }

    private static String requireIdentifier(String value, String name) {
        Objects.requireNonNull(value, name);
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }
        return trimmed;
    }
}
