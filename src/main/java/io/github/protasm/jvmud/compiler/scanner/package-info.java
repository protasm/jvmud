/**
 * Lexical analysis for LPC source text.
 *
 * <p>Converts raw or preprocessed source strings into {@link io.github.protasm.jvmud.compiler.token.Token}
 * streams while preserving {@link io.github.protasm.jvmud.compiler.sourcepos.SourcePos} information via
 * {@link io.github.protasm.jvmud.compiler.scanner.ScannableSource}.</p>
 *
 * <p>Responsible for coordinating preprocessing, recognizing reserved words and LPC types, and
 * rejecting malformed input with {@link io.github.protasm.jvmud.compiler.scanner.ScanException}.</p>
 *
 * <p>Assumes higher layers will interpret token sequences grammatically; it does not perform parsing
 * or semantic validation beyond token boundaries.</p>
 */
package io.github.protasm.jvmud.compiler.scanner;
