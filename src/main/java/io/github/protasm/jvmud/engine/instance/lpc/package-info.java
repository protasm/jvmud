/**
 * Engine-owned LPC object lifecycle and its integration with a running mudlib.
 *
 * <p>{@link io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntime} materializes and invokes mudlib
 * objects. {@link io.github.protasm.jvmud.engine.instance.lpc.LPCObjectLifecycle} preserves their
 * identities and live fields while installing prepared program revisions.
 * {@code LPCInvocationStack} owns lexical current/previous-object scopes and the active-object
 * reload check. {@code LPCMethodInvoker} makes required, optional, lifecycle and caller-preserving
 * invocation policies explicit, including method selection, argument adaptation and diagnostics.
 * {@link io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntimeContext} implements the neutral
 * generated-code contract and adapts authored behavior to world projection, commands, scheduling
 * and configured storage. Session state belongs to
 * {@link io.github.protasm.jvmud.engine.instance.session.InstanceSessions}. Source interpretation
 * uses a separate compiler context, keeping engine state out of language compilation and value
 * helpers.</p>
 */
package io.github.protasm.jvmud.engine.instance.lpc;
