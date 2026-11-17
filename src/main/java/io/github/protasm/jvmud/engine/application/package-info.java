/**
 * JVMud application lifetime, configuration, and worker supervision.
 *
 * <p>{@link io.github.protasm.jvmud.engine.application.JVMud} owns the public endpoints,
 * administrator authority, and separate worker processes. It remains available with zero
 * mudlibs. The {@code worker} package supplies process supervision; shared control messages
 * live under {@code communication.protocol.worker}. The {@code update} package maintains an
 * extracted installation.</p>
 *
 * <p>Game concepts belong to {@link io.github.protasm.jvmud.engine.model}; assembling
 * and executing one mudlib belongs to {@link io.github.protasm.jvmud.engine.instance}.</p>
 */
package io.github.protasm.jvmud.engine.application;
