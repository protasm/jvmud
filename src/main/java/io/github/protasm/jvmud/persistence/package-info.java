/**
 * Durable storage adapters used by JVMud.
 *
 * <p>Persistence packages hold storage-specific code for filesystems, databases, and future durable
 * backends. Engine and instance code should ask these adapters for persistence services instead of
 * embedding file or database formats directly.</p>
 */
package io.github.protasm.jvmud.persistence;
