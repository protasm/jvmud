/**
 * Hosted JVMud world instances.
 *
 * <p>The instance layer assembles compiler output, engine runtime state, mudlib boundary metadata,
 * lifecycle hooks, player/persona attachment, and cross-mud transfer into one running world. It
 * should know how a mud boots and lives, but it should not own Telnet protocol mechanics or durable
 * storage formats.</p>
 */
package io.github.protasm.jvmud.instance;
