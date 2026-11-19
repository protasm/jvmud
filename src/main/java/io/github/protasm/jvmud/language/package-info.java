/**
 * LPC compilation, execution contracts, value operations, built-in functions and source formatting.
 *
 * <p>{@code compiler} follows LPC source through preprocessing, scanning, parsing, semantic
 * analysis, typed IR, and bytecode generation. Start with {@link
 * io.github.protasm.jvmud.language.compiler.pipeline.CompilationPipeline} or the command-line
 * {@link io.github.protasm.jvmud.language.compiler.JVMudCompiler}. Explicit compatibility
 * transformations live under {@code compiler.transpiler}.</p>
 *
 * <p>{@code execution} provides compiled-class and object invocation contracts. {@code runtime}
 * defines the context consumed by generated code and implements LPC value operations. The
 * {@code efun} package defines callable signatures and language-only built-ins, while
 * {@code formatter} handles LPC source layout.</p>
 *
 * <p>These language facilities do not own worlds, player connections, manifest files, or durable
 * storage. The engine supplies those services when it assembles a running mudlib. Package boundaries
 * keep that collaboration visible to readers of the source.</p>
 */
package io.github.protasm.jvmud.language;
