/**
 * Entry point utilities that tie together scanning, parsing, and compilation for LPC source.
 *
 * <p>The {@link io.github.protasm.jvmud.compiler.JVMudCompiler} facade offers simple static helpers for running the
 * compiler pipeline against strings, handling diagnostic printing, and applying a default Java
 * superclass when emitting bytecode.</p>
 *
 * <p>Assumes downstream components ({@code scanner}, {@code parser}, {@code bytecode}) enforce their
 * own validation rules; this layer simply orchestrates them and returns {@code null} on failure.</p>
 *
 * <p>Not responsible for broader runtime/server concerns; callers handle file management,
 * diagnostics, and process integration.</p>
 */
package io.github.protasm.jvmud.compiler;
