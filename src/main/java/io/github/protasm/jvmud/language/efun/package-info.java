/**
 * LPC callable contracts, registry declarations and language-only built-in implementations.
 *
 * <p>{@link io.github.protasm.jvmud.language.efun.LanguageEfuns} composes text, regular expression,
 * collection and value catalogs. They operate on LPC values and use the neutral runtime contract
 * for callbacks and lexical method receivers. They do not access worlds, connections, host clocks,
 * entropy, files or databases. Hosted service adapters are composed separately by the engine.</p>
 *
 * <h2>Language function vocabulary</h2>
 * <ul>
 *   <li>{@code jvmud_raise_error(mixed message) : void} raises a JVMud runtime exception with the
 *       supplied LPC message.</li>
 *   <li>{@code jvmud_parse_json(string document) : mixed} converts ordinary JSON objects, arrays,
 *       and scalars to LPC-compatible values.</li>
 *   <li>{@code jvmud_format_json(mixed value) : string} converts LPC-compatible values to ordinary
 *       JSON, requiring string mapping keys.</li>
 *   <li>{@code jvmud_size(mixed value) : int} returns the size of a string, collection, mapping, or
 *       array.</li>
 *   <li>{@code jvmud_sqrt(mixed value) : float} returns the square root of a numeric value.</li>
 *   <li>{@code jvmud_lowercase_text(mixed value) : string} lowercases text.</li>
 *   <li>{@code jvmud_uppercase_text(mixed value) : string} uppercases text.</li>
 *   <li>{@code jvmud_split_text(string text, string delimiter) : array} splits text on a literal
 *       delimiter while preserving empty fields; empty text yields a single empty field.</li>
 *   <li>{@code jvmud_regex_match(array values, string pattern[, int flags]) : mixed} returns the
 *       values whose string forms match a regular expression, or LPC false when no value
 *       matches. JVMud normalizes supported compatibility-regexp idioms before running the pattern on Java's
 *       regex engine.</li>
 *   <li>{@code jvmud_regex_replace(string input, string pattern, mixed replacement, int flags) :
 *       string} performs a compatibility regex replacement for mudlib text helpers, including
 *       multiline matches and function callbacks that receive each matched string.</li>
 *   <li>{@code jvmud_regex_explode(string input, string pattern) : array} splits text around
 *       regular-expression matches while preserving matched delimiters.</li>
 *   <li>{@code jvmud_to_int(mixed value) : int} converts numeric values and base-10 text to an
 *       integer, returning LPC false-style zero for non-numeric input.</li>
 *   <li>{@code jvmud_to_string(mixed value) : string} converts a mixed LPC value to its JVMud text
 *       form for compatibility helpers that need explicit stringification.</li>
 *   <li>{@code jvmud_capitalize_text(mixed value) : string} capitalizes the first character of
 *       text.</li>
 *   <li>{@code jvmud_wrap_text(mixed text[, int width]) : string} wraps description text at a
 *       requested line width, defaulting to 78 columns for compatibility text helpers.</li>
 *   <li>{@code jvmud_serialize_lpc_value(mixed value) : string} serializes LPC data values to a
 *       JVMud-owned tagged JSON payload.</li>
 *   <li>{@code jvmud_deserialize_lpc_value(string data) : mixed} restores LPC data values produced
 *       by {@code jvmud_serialize_lpc_value}.</li>
 *   <li>{@code jvmud_format_text(string format, mixed ...args) : string} formats text using the
 *       host formatter with LPC {@code %O} object placeholders treated as string placeholders and
 *       legacy-compatible string column mode handled directly.</li>
 *   <li>{@code jvmud_extract_text(mixed value, int from) : string} extracts text from an inclusive
 *       start index through the end.</li>
 *   <li>{@code jvmud_extract_text(mixed value, int from, int to) : string} extracts text using
 *       inclusive start and end indexes.</li>
 *   <li>{@code jvmud_member(mixed value, mixed needle) : int} checks mapping key membership or
 *       returns the index of a value in an array or string.</li>
 *   <li>{@code jvmud_sort_array(array values, mixed comparator[, mixed ...args]) : array} sorts a
 *       copy of an array using a callable comparator or current-object method name. Mudlib
 *       manifests can expose this under legacy names such as {@code sort_array}.</li>
 *   <li>{@code jvmud_filter(mixed values, function callback, mixed ...args) : mixed} filters
 *       arrays, strings, and mappings with a callable predicate. Mudlib manifests can expose this
 *       under legacy names such as {@code filter}.</li>
 *   <li>{@code jvmud_map(mixed values, function callback, mixed ...args) : mixed} maps arrays,
 *       strings, and mappings with a callable transformer. Mudlib manifests can expose this under
 *       legacy names such as {@code map}.</li>
 *   <li>{@code jvmud_mapping_keys(mapping value) : array} returns the mapping's keys in runtime
 *       iteration order.</li>
 *   <li>{@code jvmud_mapping_values(mapping value) : array} returns the mapping's values in
 *       runtime iteration order.</li>
 *   <li>{@code jvmud_mapping_values(mapping value, int index) : array} returns one value slot from
 *       each entry in a multi-value mapping.</li>
 *   <li>{@code jvmud_mapping_from_keys(array keys) : mapping} builds a mapping whose keys come from
 *       the supplied array and whose values are LPC true.</li>
 *   <li>{@code jvmud_mapping_delete(mapping value, mixed key) : mapping} removes a key from a
 *       mutable mapping and returns the same mapping.</li>
 *   <li>{@code jvmud_is_string(mixed value) : status} reports whether a value is Java-backed LPC
 *       string data.</li>
 *   <li>{@code jvmud_is_int(mixed value) : status} reports whether a value is Java-backed LPC
 *       integer data.</li>
 *   <li>{@code jvmud_is_float(mixed value) : status} reports whether a value is Java-backed LPC
 *       floating point data.</li>
 *   <li>{@code jvmud_is_array(mixed value) : status} reports whether a value is Java-backed LPC
 *       array data.</li>
 *   <li>{@code jvmud_is_mapping(mixed value) : status} reports whether a value is Java-backed LPC
 *       mapping data.</li>
 *   <li>{@code jvmud_filter_indices(mapping values, function callback, mixed ...args) : mapping} returns
 *       a mapping containing the entries whose keys satisfy a callable predicate.</li>
 *   <li>{@code jvmud_allocate(int size) : array} returns a zero-filled LPC array of non-negative
 *       size.</li>
 *   <li>{@code jvmud_sscanf(mixed input, mixed format, mixed capture, mixed ...captures) : int}
 *       captures formatted text into one or more assignable LPC values.</li>
 *   <li>{@code jvmud_apply_callable(function callback, mixed ...args) : mixed} invokes a callable
 *       and expands a final array argument for compatibility {@code apply} calls.</li>
 * </ul>
 */
package io.github.protasm.jvmud.language.efun;
