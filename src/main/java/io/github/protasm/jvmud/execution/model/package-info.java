/**
 * World, identity, time, and mudlib contracts shared by running instances.
 *
 * <p>The model defines game state and its rules independently of application startup,
 * worker process supervision, and connection mechanics. Instances assemble these
 * concepts with executable mudlib behavior.</p>
 *
 * <p>{@link io.github.protasm.jvmud.execution.model.world} owns the world-based model: {@link
 * io.github.protasm.jvmud.execution.model.world.World}, {@link
 * io.github.protasm.jvmud.execution.model.world.WorldRuntime}, {@link
 * io.github.protasm.jvmud.execution.model.world.Place}, {@link
 * io.github.protasm.jvmud.execution.model.world.Entity}, {@link
 * io.github.protasm.jvmud.execution.model.world.Link}, and {@link
 * io.github.protasm.jvmud.execution.model.world.Location}. Places are linked locations; entities are
 * contained by exactly one immediate location and may themselves contain other entities.</p>
 *
 * <p>{@link io.github.protasm.jvmud.execution.model.identity} supports the multiplayer and player-present
 * pillars. {@link io.github.protasm.jvmud.execution.model.identity.PlayerRecord} represents the human or
 * account-like controller, {@link io.github.protasm.jvmud.execution.model.identity.SessionRecord}
 * represents a connection context, and {@link
 * io.github.protasm.jvmud.execution.model.identity.PersonaRecord} represents the player's in-world
 * manifestation.</p>
 *
 * <p>Mudlib lifecycle hooks are represented by {@link io.github.protasm.jvmud.execution.model.mudlib.MudlibLifecycleEvent}
 * and registered through {@link io.github.protasm.jvmud.execution.model.mudlib.MudlibBoundary}. The hook event
 * names are JVMud-native; mudlibs map those events to their own LPC method names through boundary
 * configuration. {@link io.github.protasm.jvmud.execution.model.mudlib.MudlibBoundaryConfigReader} reads that
 * boundary metadata from simple mudlib manifests. Mudlib behavior can be attached as an opaque
 * {@link io.github.protasm.jvmud.execution.model.mudlib.MudlibProjection}, but that projection does not
 * define the engine ontology.</p>
 *
 * <p>World time is deterministic by default. {@link io.github.protasm.jvmud.execution.model.time.WorldScheduler}
 * owns scheduled work in ticks, while {@link io.github.protasm.jvmud.execution.model.time.WorldClock} is the
 * standalone wall-clock adapter. Hosted mudlibs own their clock through their instance
 * execution queue so ticks and commands cannot run concurrently.</p>
 *
 */
package io.github.protasm.jvmud.execution.model;
