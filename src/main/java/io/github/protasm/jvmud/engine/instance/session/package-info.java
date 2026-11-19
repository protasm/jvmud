/**
 * Connection lifetime and authoritative Player/Persona attachment inside one mudlib instance.
 *
 * <p>{@link io.github.protasm.jvmud.engine.instance.session.InstanceSessions} owns connection state,
 * lookups, input capture, protocol negotiation and output routing. A stable
 * {@link io.github.protasm.jvmud.engine.instance.session.InstanceSession} can exist during login
 * without a Persona. {@link io.github.protasm.jvmud.engine.instance.session.PersonaAttachment}
 * explicitly relates the selected Persona to its native Entity and authored behavior.</p>
 *
 * <p>The owning instance confines mutations to its execution queue. Transport and generated-code
 * adapters delegate to this owner instead of maintaining independent attachment copies.</p>
 */
package io.github.protasm.jvmud.engine.instance.session;
