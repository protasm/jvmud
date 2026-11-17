/**
 * JVMud application operation, its game model, and the assembly of running mudlibs.
 *
 * <p>{@code application} starts public services and supervises worker processes. {@code model}
 * defines world, identity, and time invariants. {@code configuration} reads manifest policy.
 * {@code instance} composes those contracts with LPC execution inside a worker JVM.</p>
 *
 * <p>The engine uses language, communication, and storage services. Those services do not define
 * the engine's game concepts. Packages describe responsibility; worker processes provide fault
 * isolation and retain the host account's operating-system permissions.</p>
 */
package io.github.protasm.jvmud.engine;
