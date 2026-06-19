/**
 * Visitor-style utilities that operate over the LPC AST.
 *
 * <p>The current visitor surface provides shared traversal dispatch and debug-oriented tree
 * printing. Core compiler behavior now lives in named pipeline stages such as semantic analysis and
 * IR lowering rather than in broad visitor passes.</p>
 *
 * <p>Visitors should remain small support utilities. Language validation belongs to {@code
 * semantic}; backend translation belongs to {@code ir} and {@code bytecode}.</p>
 */
package io.github.protasm.jvmud.compiler.parser.ast.visitor;
