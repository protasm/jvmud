/**
 * Lexical analysis for LPC source text.
 *
 * <p>Converts raw or preprocessed source strings into {@link io.github.protasm.jvmud.language.token.Token}
 * streams while preserving {@link io.github.protasm.jvmud.language.sourcepos.SourcePos} information via
 * {@link io.github.protasm.jvmud.language.scanner.ScannableSource}.</p>
 *
 * <p>Responsible for coordinating preprocessing, recognizing reserved words and LPC types, and
 * rejecting malformed input with {@link io.github.protasm.jvmud.language.scanner.ScanException}.</p>
 *
 * <p>Assumes higher layers will interpret token sequences grammatically; it does not perform parsing
 * or semantic validation beyond token boundaries.</p>
 */
package io.github.protasm.jvmud.language.scanner;
