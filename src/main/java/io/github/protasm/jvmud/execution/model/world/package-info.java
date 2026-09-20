/**
 * Engine-owned world model: worlds, places, entities, links, locations, and containment.
 *
 * <p>This package is the home of JVMud's world-based pillar. It names the things the engine owns
 * directly: a {@link io.github.protasm.jvmud.execution.model.world.World} has linked {@link
 * io.github.protasm.jvmud.execution.model.world.Place Places}; {@link
 * io.github.protasm.jvmud.execution.model.world.Entity Entities} occupy one immediate {@link
 * io.github.protasm.jvmud.execution.model.world.Location}; and {@link
 * io.github.protasm.jvmud.execution.model.world.WorldRuntime} enforces movement, containment, and link
 * registration.</p>
 *
 * <p>Mudlib objects can project into this model, but LPC object vocabulary is not the source of
 * truth for the engine's ontology.</p>
 */
package io.github.protasm.jvmud.execution.model.world;
