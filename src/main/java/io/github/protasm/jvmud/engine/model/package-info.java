/**
 * The engine's world, identity, time, and mudlib contracts.
 *
 * <p>These types describe game state and enforce its invariants. They do not parse manifests,
 * compile LPC, open connections, or select storage adapters. The instance layer connects this
 * model to authored behavior and applies configuration.</p>
 *
 * <p>{@code world} owns locations, containment, links, and perception events. {@code identity}
 * distinguishes the human-facing Player, the active Session, and the Persona controlled in the
 * world. {@code time} owns deterministic scheduling. {@code mudlib} names lifecycle events,
 * capabilities, and opaque behavior projections without choosing their LPC implementations.</p>
 */
package io.github.protasm.jvmud.engine.model;
