/**
 * Engine-owned JVMud runtime model.
 *
 * <p>This package contains the concepts that belong to JVMud itself rather than to LPC, a specific
 * mudlib, Telnet hosting, or compiler internals. A running world is represented in JVMud terms:
 * {@link io.github.protasm.jvmud.engine.World}, {@link
 * io.github.protasm.jvmud.engine.WorldRuntime}, {@link io.github.protasm.jvmud.engine.Place},
 * {@link io.github.protasm.jvmud.engine.Entity}, {@link io.github.protasm.jvmud.engine.Link}, and
 * {@link io.github.protasm.jvmud.engine.Location}. Places are linked locations; entities are
 * contained by exactly one immediate location and may themselves contain other entities.</p>
 *
 * <p>Identity and interaction records are engine-owned as well. {@link
 * io.github.protasm.jvmud.engine.PlayerRecord} represents the human or account-like controller,
 * {@link io.github.protasm.jvmud.engine.SessionRecord} represents a connection context, and {@link
 * io.github.protasm.jvmud.engine.PersonaRecord} represents the player's in-world manifestation.
 * Mudlib behavior can be attached as an opaque {@link
 * io.github.protasm.jvmud.engine.MudlibProjection}, but that projection does not define the engine
 * ontology.</p>
 *
 * <p>Mudlib lifecycle hooks are represented by {@link io.github.protasm.jvmud.engine.MudlibLifecycleEvent}
 * and registered through {@link io.github.protasm.jvmud.engine.MudlibBoundary}. The hook event
 * names are JVMud-native; mudlibs map those events to their own LPC method names through boundary
 * configuration. {@link io.github.protasm.jvmud.engine.MudlibBoundaryConfigReader} reads that
 * boundary metadata from simple mudlib manifests.</p>
 *
 * <p>World time is deterministic by default. {@link io.github.protasm.jvmud.engine.WorldScheduler}
 * owns scheduled work in ticks, while {@link io.github.protasm.jvmud.engine.WorldClock} is the
 * wall-clock adapter that can advance a scheduler for a hosted world.</p>
 *
 * <p>Compiler and LPC compatibility layers adapt into these engine concepts instead of replacing
 * them with legacy driver vocabulary. Generated-code support lives under {@code
 * io.github.protasm.jvmud.compiler.runtime}; Telnet hosting lives under {@code
 * io.github.protasm.jvmud.server}; authored mudlib content lives in the repository's mudlib
 * source trees.</p>
 */
package io.github.protasm.jvmud.engine;
