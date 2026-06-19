package io.github.protasm.jvmud.engine;

/** Engine-visible role played by an opaque mudlib-side compatibility projection. */
public enum MudlibProjectionRole {
    /** Mudlib-owned profile, account, login, or policy state associated with the Player endpoint. */
    PLAYER_PROFILE,

    /** Mudlib-owned behavior/state for the Persona's in-World manifestation. */
    PERSONA_BEHAVIOR,

    /** Legacy projection that combines Player-profile/policy and Persona-behavior responsibilities. */
    COMBINED_PLAYER_PERSONA
}
