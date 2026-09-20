/**
 * Application operation, the game model, and hosted mudlib execution.
 *
 * <p>{@code application} owns startup, endpoints, worker supervision, and installation updates.
 * {@code model} defines worlds, identities, time, and mudlib contracts. {@code instance}
 * assembles those contracts with the language runtime inside one mudlib worker.
 * These are responsibility boundaries; packages alone do not enforce process isolation.</p>
 */
package io.github.protasm.jvmud.execution;
