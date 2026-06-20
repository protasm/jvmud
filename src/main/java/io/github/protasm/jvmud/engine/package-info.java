/**
 * Engine-owned JVMud runtime model.
 *
 * <p>This package contains the concepts that belong to JVMud itself rather than to LPC, a specific
 * mudlib, Telnet hosting, or compiler internals. The subpackages are organized around concrete
 * engine responsibilities, while the Eight Pillars of MUD describe the properties those
 * responsibilities must support: interactive, text-only, multiplayer, world-based, persistent,
 * temporal, player-present games.</p>
 *
 * <p>{@link io.github.protasm.jvmud.engine.world} owns the world-based model: {@link
 * io.github.protasm.jvmud.engine.world.World}, {@link
 * io.github.protasm.jvmud.engine.world.WorldRuntime}, {@link
 * io.github.protasm.jvmud.engine.world.Place}, {@link
 * io.github.protasm.jvmud.engine.world.Entity}, {@link
 * io.github.protasm.jvmud.engine.world.Link}, and {@link
 * io.github.protasm.jvmud.engine.world.Location}. Places are linked locations; entities are
 * contained by exactly one immediate location and may themselves contain other entities.</p>
 *
 * <p>{@link io.github.protasm.jvmud.engine.identity} supports the multiplayer and player-present
 * pillars. {@link io.github.protasm.jvmud.engine.identity.PlayerRecord} represents the human or
 * account-like controller, {@link io.github.protasm.jvmud.engine.identity.SessionRecord}
 * represents a connection context, and {@link
 * io.github.protasm.jvmud.engine.identity.PersonaRecord} represents the player's in-world
 * manifestation.</p>
 *
 * <p>Mudlib lifecycle hooks are represented by {@link io.github.protasm.jvmud.engine.mudlib.MudlibLifecycleEvent}
 * and registered through {@link io.github.protasm.jvmud.engine.mudlib.MudlibBoundary}. The hook event
 * names are JVMud-native; mudlibs map those events to their own LPC method names through boundary
 * configuration. {@link io.github.protasm.jvmud.engine.mudlib.MudlibBoundaryConfigReader} reads that
 * boundary metadata from simple mudlib manifests. Mudlib behavior can be attached as an opaque
 * {@link io.github.protasm.jvmud.engine.mudlib.MudlibProjection}, but that projection does not
 * define the engine ontology.</p>
 *
 * <p>World time is deterministic by default. {@link io.github.protasm.jvmud.engine.time.WorldScheduler}
 * owns scheduled work in ticks, while {@link io.github.protasm.jvmud.engine.time.WorldClock} is the
 * wall-clock adapter that can advance a scheduler for a hosted world.</p>
 *
 * <p>{@link io.github.protasm.jvmud.engine.output} keeps engine-owned text presentation separate
 * from server transport code and mudlib-authored prose. Persistence is currently represented by the
 * stable records in the world and identity packages; repository or storage-specific code can grow
 * into its own package when the engine needs it.</p>
 *
 * <p>Compiler and LPC compatibility layers adapt into these engine concepts instead of replacing
 * them with legacy driver vocabulary. Generated-code support lives under {@code
 * io.github.protasm.jvmud.compiler.runtime}; Telnet hosting lives under {@code
 * io.github.protasm.jvmud.instance} and {@code io.github.protasm.jvmud.transport}; authored mudlib content lives in the repository's mudlib
 * source trees.</p>
 */
package io.github.protasm.jvmud.engine;
