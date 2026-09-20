/**
 * LPC compilation, language runtime support, and standalone compilation diagnostics.
 *
 * <p>The compiler module is organized as an educational pipeline: preprocessing, scanning, token
 * modeling, parsing, AST construction, semantic analysis, IR lowering, and bytecode generation.
 * The {@link io.github.protasm.jvmud.language.JVMudCompiler} class provides a command-line facade
 * for compiling one LPC source file into a JVM class file.</p>
 *
 * <p>Most embedders use {@link io.github.protasm.jvmud.language.pipeline.CompilationPipeline} or
 * {@link io.github.protasm.jvmud.language.exec.LPCRuntime} rather than calling stage classes
 * directly.</p>
 *
 * <p>The {@code transpiler} subpackage applies opt-in legacy LPC compatibility transformations
 * before compilation, preserving original source files and source locations.</p>
 *
 * <p>The compiler module does not own JVMud engine ontology. Engine concepts live under {@code
 * io.github.protasm.jvmud.execution.model}; mudlib source lives under repository mudlib trees.</p>
 */
package io.github.protasm.jvmud.language;
