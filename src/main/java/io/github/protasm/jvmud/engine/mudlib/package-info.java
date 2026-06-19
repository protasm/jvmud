/**
 * Engine-to-mudlib boundary declarations, lifecycle hooks, and mudlib projections.
 *
 * <p>This package keeps JVMud's engine model bridge-light: the engine owns general world services,
 * while a mudlib owns authored meaning and compatibility policy. {@link
 * io.github.protasm.jvmud.engine.mudlib.MudlibBoundary} records configured boundary objects,
 * lifecycle mappings, temporal hooks, and output preferences. {@link
 * io.github.protasm.jvmud.engine.mudlib.MudlibLifecycleEvent} names engine events that a mudlib may
 * translate into its own methods.</p>
 *
 * <p>The classes here are engine contracts. Generated LPC support helpers remain under {@code
 * io.github.protasm.jvmud.compiler.runtime}.</p>
 */
package io.github.protasm.jvmud.engine.mudlib;
