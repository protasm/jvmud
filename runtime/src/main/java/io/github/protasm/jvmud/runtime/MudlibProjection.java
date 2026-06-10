package io.github.protasm.jvmud.runtime;

import java.util.Objects;
import java.util.Set;

/**
 * Opaque mudlib-side projection attached to JVMud-native Player or Persona records.
 *
 * <p>This records the adapter meaning of a mudlib object without making that object define JVMud's
 * engine ontology. LP245's {@code /obj/player.c}, for example, is a combined projection that carries
 * login/profile policy and Persona behavior in one LPC object.</p>
 */
public record MudlibProjection(String sourcePath, Object object, Set<MudlibProjectionRole> roles) {
    public MudlibProjection {
        sourcePath = RuntimeModel.requireIdentifier(sourcePath, "sourcePath");
        object = Objects.requireNonNull(object, "object");
        roles = Set.copyOf(Objects.requireNonNull(roles, "roles"));
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("roles must not be empty.");
        }
    }

    public static MudlibProjection combinedPlayerPersona(String sourcePath, Object object) {
        return new MudlibProjection(
                sourcePath,
                object,
                Set.of(MudlibProjectionRole.PLAYER_PROFILE,
                        MudlibProjectionRole.PERSONA_BEHAVIOR,
                        MudlibProjectionRole.COMBINED_PLAYER_PERSONA));
    }

    public static MudlibProjection personaBehavior(String sourcePath, Object object) {
        return new MudlibProjection(sourcePath, object, Set.of(MudlibProjectionRole.PERSONA_BEHAVIOR));
    }

    public boolean hasRole(MudlibProjectionRole role) {
        return roles.contains(Objects.requireNonNull(role, "role"));
    }
}
