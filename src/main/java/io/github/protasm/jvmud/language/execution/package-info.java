/**
 * Host-neutral execution contracts and loading primitives for compiled LPC programs.
 *
 * <p>{@link io.github.protasm.jvmud.language.execution.LPCExecution} binds an object's invocation to
 * its owning host. Object handles, load results, inspections and class-loading options describe LPC
 * execution without depending on game worlds, sockets or storage adapters. The engine composes these
 * primitives with mudlib lifecycle and world services in its instance layer. A
 * {@link io.github.protasm.jvmud.language.execution.LPCProgramRevision} identifies linked code;
 * a handle identifies a live object that may keep its identity across behavior revisions.</p>
 *
 * <p>The neighboring {@code language.runtime} package defines the operations consumed by generated
 * bytecode. The {@code language.compiler} package produces that bytecode.</p>
 */
package io.github.protasm.jvmud.language.execution;
