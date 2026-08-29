package io.github.protasm.jvmud.instance;

import io.github.protasm.jvmud.engine.mudlib.MudlibProjection;
import java.util.Objects;

/**
 * Adapter for mudlib player objects that collapse several JVMud concepts into one object.
 *
 * <p>LP245's {@code /obj/player.c} is the motivating case: the same LPC object carries mudlib
 * account/profile fields, login prompts and policy, Persona behavior, and session-oriented glue.
 * JVMud keeps Session, Player, and Persona engine-owned; this adapter records the combined object as a
 * combined mudlib projection attached to that relationship.</p>
 */
public final class CombinedPlayerPersonaAdapter {
    private final String playerObjectPath;

    /** Creates an adapter for combined objects cloned from the supplied mudlib path. */
    public CombinedPlayerPersonaAdapter(String playerObjectPath) {
        this.playerObjectPath = Objects.requireNonNull(playerObjectPath, "playerObjectPath");
    }

    /** Returns the combined Player-profile and Persona-behavior projection for an LPC object. */
    public MudlibProjection combinedProjection(Object playerObject) {
        return MudlibProjection.combinedPlayerPersona(playerObjectPath, playerObject);
    }
}
