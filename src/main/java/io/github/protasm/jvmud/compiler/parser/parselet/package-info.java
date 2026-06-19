/**
 * Pratt parser building blocks for LPC expressions.
 *
 * <p>Defines prefix and infix parselet interfaces alongside concrete implementations for operators,
 * literals, and grouping. Each parselet understands its precedence/associativity and produces the
 * appropriate expression AST nodes.</p>
 *
 * <p>Assumes the owning {@link io.github.protasm.jvmud.compiler.parser.PrattParser} controls token traversal
 * and assignment context; parselets themselves remain stateless and focused on node construction.</p>
 *
 * <p>Parselets do not perform type analysis. They preserve expression structure for semantic
 * analysis and IR lowering.</p>
 */
package io.github.protasm.jvmud.compiler.parser.parselet;
