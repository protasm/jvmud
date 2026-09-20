/**
 * JVMud application lifetime, configuration, and worker supervision.
 *
 * <p>{@link io.github.protasm.jvmud.execution.application.JVMud} owns the public endpoints,
 * administrator authority, and sandboxed worker processes. It remains available with zero
 * mudlibs. The {@code worker} package supplies process supervision and private control
 * messages; {@code update} maintains an extracted installation.</p>
 *
 * <p>Game concepts belong to {@link io.github.protasm.jvmud.execution.model}; assembling
 * and executing one mudlib belongs to {@link io.github.protasm.jvmud.execution.instance}.</p>
 */
package io.github.protasm.jvmud.execution.application;
