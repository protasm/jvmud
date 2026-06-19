/**
 * Public entry point for the JVMud LPC compiler module.
 *
 * <p>The compiler module is organized as an educational pipeline: preprocessing, scanning, token
 * modeling, parsing, AST construction, semantic analysis, IR lowering, and bytecode generation.
 * The {@link io.github.protasm.jvmud.compiler.JVMudCompiler} class provides a command-line facade
 * for compiling one LPC source file into a JVM class file.</p>
 *
 * <p>Most embedders use {@link io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline} or
 * {@link io.github.protasm.jvmud.compiler.exec.LPCRuntime} rather than calling stage classes
 * directly.</p>
 *
 * <p>The compiler module does not own JVMud engine ontology. Engine concepts live under {@code
 * io.github.protasm.jvmud.engine}; mudlib source lives under repository mudlib trees.</p>
 */
package io.github.protasm.jvmud.compiler;
