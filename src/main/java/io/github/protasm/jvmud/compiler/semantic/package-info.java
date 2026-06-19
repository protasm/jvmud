/**
 * Semantic analysis for parsed LPC objects.
 *
 * <p>This package turns parser output into a checked semantic model. It builds scopes, resolves
 * fields, locals, methods, inherited members, parent calls, dynamic invocations, and LPC-facing
 * efun calls, then reports source-level problems through the compiler pipeline.</p>
 *
 * <p>Semantic analysis is also where JVMud enforces language contracts that require more than token
 * or grammar knowledge, including explicit LPC method and parameter types, assignment validity,
 * argument compatibility, return checking, and collection/indexing rules. Successful analysis
 * produces a {@link io.github.protasm.jvmud.compiler.semantic.SemanticModel} for IR lowering.</p>
 */
package io.github.protasm.jvmud.compiler.semantic;
