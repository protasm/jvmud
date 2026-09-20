/**
 * Mudlib assembly and execution inside a worker JVM.
 *
 * <p>The instance layer assembles compiler output, engine runtime state, mudlib boundary metadata,
 * lifecycle hooks, player/persona attachment, and per-mudlib execution into one running mudlib. It
 * should know how a mudlib boots and lives, but it should not own Telnet protocol mechanics or durable
 * storage formats.</p>
 */
package io.github.protasm.jvmud.execution.instance;
