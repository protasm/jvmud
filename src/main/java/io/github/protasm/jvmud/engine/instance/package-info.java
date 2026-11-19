/**
 * Mudlib assembly and execution inside a worker JVM.
 *
 * <p>The instance layer assembles compiler output, engine runtime state, mudlib boundary metadata,
 * lifecycle hooks, player/persona attachment, and per-mudlib execution into one running mudlib. It
 * should know how a mudlib boots and lives, but it should not own Telnet protocol mechanics or durable
 * storage formats. The {@code session} subpackage owns connection identity and explicit Persona
 * attachment; its {@code login} subpackage supplies optional policies that return profile data.
 * The {@code efun} subpackage composes language-only functions with engine service adapters,
 * keeping each adapter's resource grants beside its implementation.</p>
 */
package io.github.protasm.jvmud.engine.instance;
