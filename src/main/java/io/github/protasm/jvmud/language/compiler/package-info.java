/**
 * Source-to-bytecode LPC compilation, independent of a running game.
 *
 * <p>{@link io.github.protasm.jvmud.language.compiler.pipeline.CompilationPipeline} coordinates
 * preprocessing, scanning, parsing, semantic analysis, typed IR lowering and JVM bytecode emission.
 * Each stage exposes its own representations and diagnostics so readers can inspect how authored
 * source becomes executable code.</p>
 *
 * <p>The compiler consumes a read-only {@link
 * io.github.protasm.jvmud.language.compiler.LPCCompilationEnvironment}. Its default implementation,
 * {@link io.github.protasm.jvmud.language.compiler.LPCCompilerContext}, resolves sources and callable
 * declarations without creating worlds, sessions, storage adapters or live LPC objects. Hosts select
 * language features and compatibility transformations explicitly; original source remains intact.</p>
 */
package io.github.protasm.jvmud.language.compiler;
