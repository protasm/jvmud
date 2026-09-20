/**
 * Player connections: engine-owned menu/direct relays and worker-side Telnet protocol sessions.
 *
 * <p>The engine publishes only ready mudlibs. A selected connection stays with its worker
 * until disconnection; it never returns to the engine menu.</p>
 */
package io.github.protasm.jvmud.transport.telnet;
