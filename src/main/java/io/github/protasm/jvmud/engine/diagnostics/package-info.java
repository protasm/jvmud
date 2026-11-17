/**
 * Compilation diagnostics for the startup content of a configured mudlib.
 *
 * <p>{@link io.github.protasm.jvmud.engine.diagnostics.MudlibPreloadCompileScan} reads the selected
 * manifest, applies its compiler policy and efun signatures, and reports compilation problems for
 * preloaded source. It stops before object construction and gameplay, so a successful scan is
 * evidence about compilation rather than login, movement, persistence, or database availability.</p>
 *
 * <p>This package belongs to the engine because it interprets mudlib manifests and host capability
 * policy. Reusable compiler diagnostics remain part of the language compilation pipeline.</p>
 */
package io.github.protasm.jvmud.engine.diagnostics;
