/**
 * Typed intermediate representation produced after semantic analysis.
 *
 * <p>The IR models runtime-level types, control flow, calls, field access, local access, collection
 * operations, and coercions explicitly. It is the stable bridge between the checked semantic model
 * and backend bytecode generation.</p>
 *
 * <p>IR nodes are immutable records that carry source line numbers, runtime types, and JVM-oriented
 * call details. This lets the bytecode backend reason about truthiness, dispatch, descriptors, and
 * helper calls without depending on parser internals.</p>
 */
package io.github.protasm.jvmud.compiler.ir;
