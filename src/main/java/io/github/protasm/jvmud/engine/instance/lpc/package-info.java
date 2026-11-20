/**
 * Engine-owned LPC object lifecycle and its integration with a running mudlib.
 *
 * <p>{@link io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntime} materializes and invokes mudlib
 * objects. {@link io.github.protasm.jvmud.engine.instance.lpc.LPCObjectLifecycle} preserves their
 * identities and live fields while installing prepared program revisions.
 * {@code LPCInvocationStack} owns lexical current/previous-object scopes and the active-object
 * reload check. {@code LPCMethodInvoker} makes required, optional, lifecycle and caller-preserving
 * invocation policies explicit, including method selection, argument adaptation and diagnostics.
 * {@code LPCCommands} owns actor scopes, action registration, enablement, aliases, dispatch and
 * command failure state. Its narrow host bridge leaves containment, session activity and output
 * with their existing owners through the context's adapters.
 * {@code LPCWorldInteractions} coordinates movement lifecycle, nearby-object refresh and world
 * perception; it also owns LPC presence, aliases and light metadata. Its projection adapts to the
 * authoritative native world graph, while the context retains sessions, private output and the
 * overall destruction sequence.
 * {@code LPCScheduledCallbacks} owns recurring and deferred registrations, cancellation and
 * delivery policy; {@link io.github.protasm.jvmud.engine.model.time.WorldScheduler} retains
 * deterministic time and generic task ordering. The context supplies runtime binding and timed
 * error reporting, and coordinates cancellation at object destruction.
 * {@link io.github.protasm.jvmud.engine.instance.lpc.MudlibRuntimeContext} implements the neutral
 * generated-code contract and adapts authored behavior to world projection, commands, scheduling
 * and configured storage. Session state belongs to
 * {@link io.github.protasm.jvmud.engine.instance.session.InstanceSessions}. Source interpretation
 * uses a separate compiler context, keeping engine state out of language compilation and value
 * helpers.</p>
 */
package io.github.protasm.jvmud.engine.instance.lpc;
