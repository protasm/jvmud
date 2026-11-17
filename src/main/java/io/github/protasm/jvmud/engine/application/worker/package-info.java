/**
 * Engine-side worker process launch, supervision and lifecycle control.
 *
 * <p>The supervisor starts Java, waits for readiness, opens private channels and observes worker
 * exit. Shared message contracts belong to
 * {@link io.github.protasm.jvmud.communication.protocol.worker}; the worker's mudlib assembly and
 * execution belong to {@link io.github.protasm.jvmud.engine.instance}.</p>
 */
package io.github.protasm.jvmud.engine.application.worker;
