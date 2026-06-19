/**
 * LPC source formatting utilities.
 *
 * <p>The formatter is a lexical, source-preserving maintenance tool for LPC files. It normalizes
 * indentation, selected code spacing, control-flow brace placement, and trailing whitespace without
 * reordering declarations or interpreting mudlib semantics.</p>
 *
 * <p>This package is separate from the compiler pipeline: formatting prepares source text for
 * humans and repository consistency, while preprocessing, scanning, parsing, semantic analysis, IR
 * lowering, and bytecode generation determine compiled behavior.</p>
 */
package io.github.protasm.jvmud.compiler.formatter;
