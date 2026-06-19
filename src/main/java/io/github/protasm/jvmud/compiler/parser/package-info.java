/**
 * Core parser for LPC, producing AST structures from token streams.
 *
 * <p>Parses object-level declarations and method bodies while delegating expression precedence to a
 * Pratt-style expression parser. The {@link io.github.protasm.jvmud.compiler.parser.Parser}
 * records declared fields, methods, locals, parameters, inherits, and parser options.</p>
 *
 * <p>Assumes tokens have already been validated lexically; raises {@link
 * io.github.protasm.jvmud.compiler.parser.ParseException} when encountering structural issues. Parser behavior
 * is influenced by {@link io.github.protasm.jvmud.compiler.parser.ParserOptions} but leaves name
 * resolution, efun lookup, and type enforcement to semantic analysis.</p>
 *
 * <p>This package defines parsing mechanics rather than AST shape (see {@code parser.ast}) or operator
 * definitions (see {@code parser.parselet} and {@code parser.type}).</p>
 */
package io.github.protasm.jvmud.compiler.parser;
