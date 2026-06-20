/**
 * Human-facing connection transports.
 *
 * <p>Transport packages adapt external protocols and sessions into a hosted JVMud instance. They
 * own socket/protocol details, line I/O, echo behavior, and connection lifecycle mechanics, while
 * instance packages own the running world those connections enter.</p>
 */
package io.github.protasm.jvmud.transport;
