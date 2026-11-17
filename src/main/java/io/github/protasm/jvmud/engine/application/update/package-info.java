/**
 * Updates extracted JVMud installations and records the servers that use them.
 *
 * <p>{@link io.github.protasm.jvmud.engine.application.update.InstallationServers} records launch,
 * readiness, and shutdown metadata for an installed engine. {@link
 * io.github.protasm.jvmud.engine.application.update.DistributionUpdater} verifies a release,
 * coordinates downtime, backs up the installation, replaces owned files, and attempts restart or
 * rollback. It preserves mudlib content outside the explicitly updatable adapters.</p>
 *
 * <p>Installation maintenance belongs to the application. This package does not define world
 * state, implement player save formats, or supply an in-world LPC reload operation. External
 * state directories and databases retain their own backup responsibilities.</p>
 */
package io.github.protasm.jvmud.engine.application.update;
