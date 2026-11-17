/**
 * Private engine-to-worker message contracts, shared by the supervisor and mudlib entry point.
 *
 * <p>These values describe boot, readiness, and administration requests. They carry no process
 * supervision or game state; each endpoint owns its interpretation and lifecycle.</p>
 */
package io.github.protasm.jvmud.communication.protocol.worker;
