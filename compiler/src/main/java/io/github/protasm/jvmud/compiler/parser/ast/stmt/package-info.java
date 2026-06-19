/**
 * Statement-level AST nodes.
 *
 * <p>Encapsulates control flow and sequencing constructs such as blocks, returns, conditionals,
 * loops, switch statements, break/continue statements, foreach statements, and expression
 * statements. Statement nodes coordinate child expressions and maintain source lines for
 * diagnostics and later lowering.</p>
 *
 * <p>Statements do not independently decide semantic validity. Scope construction, control-flow
 * checks, expression typing, and lowering are handled by later compiler stages.</p>
 */
package io.github.protasm.jvmud.compiler.parser.ast.stmt;
