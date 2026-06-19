/**
 * Abstract syntax tree structures representing LPC objects.
 *
 * <p>Contains node definitions for objects, inherits, fields, methods, parameters, arguments,
 * locals, symbols, and common list/map node shapes. Expression and statement specializations live
 * in subpackages.</p>
 *
 * <p>AST nodes preserve source line information and parser structure without owning final language
 * meaning. Semantic analysis resolves names, validates types, and prepares the checked model used
 * by IR lowering.</p>
 *
 * <p>This package focuses on structure rather than parsing mechanics (see {@code parser}) or
 * expression/statement specializations (see subpackages).</p>
 */
package io.github.protasm.jvmud.compiler.parser.ast;
