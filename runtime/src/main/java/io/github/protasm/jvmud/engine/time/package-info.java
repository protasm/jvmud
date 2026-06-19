/**
 * Deterministic world time and wall-clock driving for hosted JVMud worlds.
 *
 * <p>This package supports the temporal pillar. {@link
 * io.github.protasm.jvmud.engine.time.WorldScheduler} owns scheduled work in engine ticks, while
 * {@link io.github.protasm.jvmud.engine.time.WorldClock} adapts wall-clock time into scheduler
 * advancement when a server hosts the world.</p>
 */
package io.github.protasm.jvmud.engine.time;
