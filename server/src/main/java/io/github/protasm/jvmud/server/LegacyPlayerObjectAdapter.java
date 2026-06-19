package io.github.protasm.jvmud.server;

import io.github.protasm.jvmud.engine.MudlibProjection;
import java.util.Objects;

/**
 * Adapter for legacy mudlib player objects that collapse several JVMud concepts into one object.
 *
 * <p>LP245's {@code /obj/player.c} is the motivating case: the same LPC object carries mudlib
 * account/profile fields, login prompts and policy, Persona behavior, and session-oriented glue.
 * JVMud keeps Session, Player, and Persona engine-owned; this adapter records the legacy object as a
 * combined mudlib projection attached to that relationship.</p>
 */
final class LegacyPlayerObjectAdapter {
    private final String playerObjectPath;

    LegacyPlayerObjectAdapter(String playerObjectPath) {
        this.playerObjectPath = Objects.requireNonNull(playerObjectPath, "playerObjectPath");
    }

    MudlibProjection combinedProjection(Object playerObject) {
        return MudlibProjection.combinedPlayerPersona(playerObjectPath, playerObject);
    }
}
