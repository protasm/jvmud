/**
 * Player, session, and persona identity records for a running JVMud world.
 *
 * <p>This package supports the multiplayer and player-present pillars. A {@link
 * io.github.protasm.jvmud.engine.identity.PlayerRecord} represents the human or account-like
 * controller, a {@link io.github.protasm.jvmud.engine.identity.SessionRecord} represents one
 * connection context, and a {@link io.github.protasm.jvmud.engine.identity.PersonaRecord}
 * represents the player's in-world manifestation.</p>
 *
 * <p>The records here describe identity and attachment. They do not perform telnet I/O and they do
 * not define mudlib-specific login policy.</p>
 */
package io.github.protasm.jvmud.engine.identity;
