/**
 * Engine-owned LPC object lifecycle and its integration with a running mudlib.
 *
 * <p>{@link io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntime} materializes and invokes mudlib
 * objects. {@link io.github.protasm.jvmud.engine.instance.lpc.LPCObjectLifecycle} preserves their
 * identities and live fields while installing prepared program revisions.
 * {@link io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntimeContext} implements the neutral
 * generated-code contract and adapts authored behavior to world projection, commands, scheduling
 * and configured storage. Session state belongs to
 * {@link io.github.protasm.jvmud.engine.instance.session.InstanceSessions}. Source interpretation
 * uses a separate compiler context, keeping engine state out of language compilation and value
 * helpers.</p>
 */
package io.github.protasm.jvmud.engine.instance.lpc;
