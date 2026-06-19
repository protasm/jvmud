/**
 * Orchestration layer for the compiler pipeline.
 *
 * <p>{@link io.github.protasm.jvmud.compiler.pipeline.CompilationPipeline} runs scan, parse,
 * inherited-source resolution, semantic analysis, IR lowering, and optional bytecode generation,
 * collecting source-level failures as {@link
 * io.github.protasm.jvmud.compiler.pipeline.CompilationProblem} instances in a {@link
 * io.github.protasm.jvmud.compiler.pipeline.CompilationResult}.</p>
 *
 * <p>Assumes the caller selects a parent class internal name up front; the pipeline does not retry or
 * backtrack once a stage fails.</p>
 *
 * <p>Not concerned with interactive presentation or file management—callers handle supplying source
 * text and reporting diagnostics.</p>
 */
package io.github.protasm.jvmud.compiler.pipeline;
