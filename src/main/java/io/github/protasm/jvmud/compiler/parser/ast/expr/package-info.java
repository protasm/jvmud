/**
 * Expression-level AST nodes.
 *
 * <p>Represents literals, identifiers, assignment forms, calls, function references, unary and
 * binary operations, indexing, slicing, collection literals, and other LPC expression forms. Some
 * nodes are deliberately unresolved when produced by the parser and are rewritten or checked during
 * semantic analysis.</p>
 *
 * <p>Assumes surrounding statement or object nodes manage scope and symbol resolution; expression
 * classes themselves are largely immutable apart from inferred type fields.</p>
 *
 * <p>No evaluation happens here; runtime behavior is handled by emitted bytecode, compiled-LPC
 * support helpers, and efun implementations.</p>
 */
package io.github.protasm.jvmud.compiler.parser.ast.expr;
