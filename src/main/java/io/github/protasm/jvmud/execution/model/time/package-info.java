/**
 * Deterministic world time and wall-clock driving for hosted JVMud worlds.
 *
 * <p>This package supports the temporal pillar. {@link
 * io.github.protasm.jvmud.execution.model.time.WorldScheduler} owns scheduled work in engine ticks, while
 * {@link io.github.protasm.jvmud.execution.model.time.WorldClock} adapts wall-clock time into scheduler
 * advancement when a server hosts the world.</p>
 */
package io.github.protasm.jvmud.execution.model.time;
