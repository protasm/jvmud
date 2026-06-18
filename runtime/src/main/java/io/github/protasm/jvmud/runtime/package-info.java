/**
 * Engine-owned JVMud world primitives.
 *
 * <p>This package models the runtime concepts that belong to the engine itself: players, sessions,
 * personas, worlds, places, links, entities, containment, capabilities, deterministic scheduling,
 * and mudlib boundary metadata. Compiler and LPC compatibility layers adapt into these concepts
 * instead of replacing them with legacy driver vocabulary.</p>
 *
 * <p>Mudlib lifecycle hooks are represented by {@link io.github.protasm.jvmud.runtime.MudlibLifecycleEvent}
 * and registered through {@link io.github.protasm.jvmud.runtime.MudlibBoundary}. The hook event
 * names are JVMud-native; mudlibs map those events to their own LPC method names through boundary
 * configuration.</p>
 */
package io.github.protasm.jvmud.runtime;
