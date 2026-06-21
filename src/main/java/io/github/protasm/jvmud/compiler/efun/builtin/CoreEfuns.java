package io.github.protasm.jvmud.compiler.efun.builtin;

import io.github.protasm.jvmud.compiler.efun.Efun;
import io.github.protasm.jvmud.compiler.efun.EfunSignature;
import io.github.protasm.jvmud.compiler.exec.LPCRuntime;
import io.github.protasm.jvmud.compiler.parser.ast.Symbol;
import io.github.protasm.jvmud.compiler.parser.type.LPCType;
import io.github.protasm.jvmud.compiler.runtime.RuntimeCallable;
import io.github.protasm.jvmud.compiler.runtime.RuntimeContext;
import io.github.protasm.jvmud.compiler.runtime.RuntimeScanf;
import io.github.protasm.jvmud.compiler.runtime.RuntimeValueCodec;
import io.github.protasm.jvmud.compiler.runtime.Truth;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Defines and registers JVMud's built-in LPC-facing engine functions.
 *
 * <p>These functions are the lowest-level operations that compiled mudlib code can ask the JVMud
 * engine to perform. They intentionally use JVMud engine vocabulary: {@code Entity} identity,
 * location containment, command dispatch, session input capture, scheduler callbacks, and
 * perceivable output. Legacy LP driver names belong in mudlib-side compatibility objects, where
 * they can delegate to these engine operations without turning LPC compatibility terms into the
 * engine's own model.</p>
 *
 * <p>Most users should treat this class as the authoritative catalog of core efuns. Each entry
 * created by the internal catalog builder has a name, LPC return type, LPC parameter types, and a Java
 * implementation that receives the active {@link RuntimeContext}. The same catalog can be
 * installed into a raw generated-code context with {@link #registerCore(RuntimeContext)} or into a
 * host-facing {@link LPCRuntime} with {@link #registerCore(LPCRuntime)}.</p>
 *
 * <h2>Output and perception</h2>
 * <ul>
 *   <li>{@code jvmud_write(mixed message) : void} writes text to the current execution
 *       recipient.</li>
 *   <li>{@code jvmud_write_to_lpc_object(object target, mixed message) : void} writes text to the
 *       target object's bound session, when it has one.</li>
 *   <li>{@code jvmud_rebind_session_lpc_object(object newObject, object oldObject) : status}
 *       moves an interactive session binding from one LPC object to another.</li>
 *   <li>{@code jvmud_emit_perceivable(mixed emitter, mixed message) : void} emits near an entity
 *       or path-resolved object.</li>
 *   <li>{@code jvmud_emit_perceivable_except(mixed emitter, mixed message, mixed excluded) : void}
 *       emits near an entity while suppressing one recipient.</li>
 *   <li>{@code jvmud_emit_perceivable_at(mixed location, mixed message) : void} emits at a
 *       location or path-resolved object.</li>
 * </ul>
 *
 * <h2>Current execution context</h2>
 * <ul>
 *   <li>{@code jvmud_current_lpc_object() : object} returns the currently executing LPC object.</li>
 *   <li>{@code jvmud_previous_lpc_object() : object} returns the previous LPC object in the current
 *       call chain, where one is available.</li>
 *   <li>{@code jvmud_current_actor() : object} returns the active command actor, falling back to
 *       the current object outside command dispatch.</li>
 *   <li>{@code jvmud_current_agent() : object} returns the active world-present agent responsible
 *       for the current action scope, or LPC false when no agent is active.</li>
 *   <li>{@code jvmud_current_verb() : string} returns the verb being dispatched for the current
 *       command.</li>
 * </ul>
 *
 * <h2>Command dispatch and interactions</h2>
 * <ul>
 *   <li>{@code jvmud_dispatch_entity_command(mixed actor, string commandLine) : mixed} dispatches
 *       a command as an entity or path-resolved object.</li>
 *   <li>{@code jvmud_enable_commands() : void} enables command handling for the current object.</li>
 *   <li>{@code jvmud_add_action(string methodName) : void} remembers a method as a command
 *       action without registering a verb.</li>
 *   <li>{@code jvmud_add_action(string methodName, string verb) : void} remembers an action method
 *       and registers a verb.</li>
 *   <li>{@code jvmud_add_action(string methodName, string verb, status prefix) : void} registers
 *       a verb, optionally as a prefix verb.</li>
 *   <li>{@code jvmud_add_verb(string verb) : void} registers a verb for the current interaction
 *       scope.</li>
 * </ul>
 *
 * <h2>Time, scheduling, and randomness</h2>
 * <ul>
 *   <li>{@code jvmud_time() : int} returns Unix epoch seconds.</li>
 *   <li>{@code jvmud_format_time(int epochSeconds) : string} formats epoch seconds using the
 *       host's default time zone.</li>
 *   <li>{@code jvmud_random(int max) : int} returns a value in {@code [0, max)}, or {@code 0} when
 *       {@code max <= 0}.</li>
 *   <li>{@code jvmud_schedule_recurring_tick(int enabled, int intervalSeconds) : void} schedules
 *       or disables recurring ticks for the current object.</li>
 *   <li>{@code jvmud_schedule_deferred_callback(string methodName, int delaySeconds) : void}
 *       schedules a one-shot callback on the current object.</li>
 *   <li>{@code jvmud_schedule_deferred_callback(string methodName, int delaySeconds,
 *       mixed... args) : void} schedules a one-shot callback with mudlib-supplied arguments.</li>
 *   <li>{@code jvmud_cancel_deferred_callback(string methodName) : int} cancels matching deferred
 *       callbacks and returns the runtime's cancellation count/status.</li>
 * </ul>
 *
 * <h2>LPC object identity plus entity lookup, containment, and lifecycle</h2>
 * <ul>
 *   <li>{@code jvmud_lpc_object_id(mixed object) : string} returns the runtime object identifier
 *       for a loaded LPC object.</li>
 *   <li>{@code jvmud_method_exists(string name) : status} returns whether the current LPC object
 *       exposes a public mudlib method with that name.</li>
 *   <li>{@code jvmud_method_exists(string name, mixed object) : status} checks another loaded LPC
 *       object for a public mudlib method.</li>
 *   <li>{@code jvmud_lpc_object_methods(mixed object) : array} returns the public mudlib method
 *       names exposed by a loaded LPC object.</li>
 *   <li>{@code jvmud_inherited_programs(mixed object) : array} returns the transitive LPC program
 *       paths inherited by a generated object.</li>
 *   <li>{@code jvmud_load_lpc_object(string path) : object} loads or returns the shared LPC
 *       runtime object for a mudlib path.</li>
 *   <li>{@code jvmud_clone_lpc_object(string path) : object} clones an LPC object and returns the
 *       new runtime instance.</li>
 *   <li>{@code jvmud_set_entity_location(mixed entity, mixed location) : void} sets a
 *       world-present entity's runtime location/containment without using a mudlib movement wrapper.</li>
 *   <li>{@code jvmud_move_entity(mixed entity, mixed destination) : void} moves an entity or
 *       path-resolved object.</li>
 *   <li>{@code jvmud_find_entity(string id) : object} searches for an entity in the default
 *       location scope.</li>
 *   <li>{@code jvmud_find_entity(mixed id, mixed location) : object} searches for an entity inside
 *       a location or container.</li>
 *   <li>{@code jvmud_entity_location() : object} returns the current object's location.</li>
 *   <li>{@code jvmud_entity_location(mixed entity) : object} returns another entity's location.</li>
 *   <li>{@code jvmud_first_entity_at(mixed location) : object} returns the first entity in a
 *       location or container.</li>
 *   <li>{@code jvmud_next_entity_at(mixed entity) : object} returns the next entity in the same
 *       inventory walk.</li>
 *   <li>{@code jvmud_destroy_lpc_object(object object) : void} destroys an LPC object and removes
 *       its runtime state.</li>
 * </ul>
 *
 * <h2>Aliases and command capability</h2>
 * <ul>
 *   <li>{@code jvmud_bind_entity_alias(object entity, string alias, mixed location) : void} binds a
 *       lookup alias in a location scope.</li>
 *   <li>{@code jvmud_find_entity_alias(string alias, mixed location) : object} resolves an alias in
 *       a location scope.</li>
 *   <li>{@code jvmud_entity_has_alias(mixed entity, string alias) : status} reports whether an
 *       entity has an alias.</li>
 *   <li>{@code jvmud_entity_commands_enabled(mixed entity) : status} reports whether command
 *       handling is enabled for an entity.</li>
 *   <li>{@code jvmud_set_entity_translucent(mixed entity, status translucent) : void} controls
 *       whether ambient perception such as light passes through an entity container. Entities are
 *       translucent by default.</li>
 *   <li>{@code jvmud_entity_translucent(mixed entity) : status} reports whether an entity is
 *       currently translucent.</li>
 * </ul>
 *
 * <h2>Session, users, persistence, and security helpers</h2>
 * <ul>
 *   <li>{@code jvmud_users() : array} returns the active user/player objects known to the runtime.</li>
 *   <li>{@code jvmud_previous_object() : object} returns the previous LPC object on the runtime call
 *       stack.</li>
 *   <li>{@code jvmud_interactive(mixed user) : status} reports whether an object is bound to an active
 *       session.</li>
 *   <li>{@code jvmud_interactive_info(mixed user, int key) : mixed} returns LPC false for
 *       driver-specific interactive metadata JVMud does not expose.</li>
 *   <li>{@code jvmud_lpc_object_info(mixed object, int key) : mixed} returns LPC false for
 *       driver-specific object metadata JVMud does not expose.</li>
 *   <li>{@code jvmud_configure_lpc_object(mixed object, int key, mixed value) : status} bridges
 *       supported driver object configuration requests such as command enablement.</li>
 *   <li>{@code jvmud_find_lpc_object(string path) : mixed} returns an already loaded LPC object, or
 *       LPC false when the path is not loaded.</li>
 *   <li>{@code jvmud_to_lpc_object(mixed pathOrObject) : mixed} resolves an LPC object value or
 *       returns LPC false when no loaded object matches.</li>
 *   <li>{@code jvmud_shutdown() : void} accepts legacy mudlib shutdown requests.</li>
 *   <li>{@code jvmud_set_this_player(mixed player) : void} accepts legacy current-player mutation
 *       requests while JVMud keeps session ownership on the engine side.</li>
 *   <li>{@code jvmud_raise_error(mixed message) : void} raises a JVMud runtime exception with the
 *       supplied LPC message.</li>
 *   <li>{@code jvmud_query_idle(mixed user) : int} returns idle time for a user/player object.</li>
 *   <li>{@code jvmud_query_ip_number(mixed user) : mixed} returns the user's remote IP address, or
 *       the runtime's false/null value when unavailable.</li>
 *   <li>{@code jvmud_query_ip_name(mixed user) : mixed} returns the user's remote host address, or
 *       the runtime's false/null value when unavailable.</li>
 *   <li>{@code jvmud_driver_info(int key) : mixed} returns LPC false for unsupported driver-info
 *       queries.</li>
 *   <li>{@code jvmud_capture_session_input(string methodName, int flags[, mixed ...args]) : void}
 *       routes the next session input line to a method on the current object, followed by any
 *       captured compatibility arguments.</li>
 *   <li>{@code jvmud_transfer_player_to_game(string gameId) : status} asks the host to transfer
 *       the current player to another registered game.</li>
 *   <li>{@code jvmud_save_lpc_object_state(string path) : status} persists the current object's LPC
 *       fields.</li>
 *   <li>{@code jvmud_restore_lpc_object_state(string path) : status} restores the current object's
 *       LPC fields.</li>
 *   <li>{@code jvmud_db_connect(string database) : int} opens a configured JDBC database handle.</li>
 *   <li>{@code jvmud_db_connect(string database, string user, string password) : int} opens a JDBC
 *       database handle with mudlib-supplied credentials.</li>
 *   <li>{@code jvmud_db_exec(int handle, string sql) : int} executes SQL and retains any result
 *       cursor for later fetches.</li>
 *   <li>{@code jvmud_db_fetch(int handle) : mixed} returns the next result row as an LPC array, or
 *       LPC false when no row remains.</li>
 *   <li>{@code jvmud_db_error(int handle) : mixed} returns the last SQL error for a handle.</li>
 *   <li>{@code jvmud_db_close(int handle) : status} closes a database handle.</li>
 *   <li>{@code jvmud_db_handles() : array} returns currently open database handles.</li>
 *   <li>{@code jvmud_db_escape(mixed value) : string} escapes text for mudlib-generated SQL.</li>
 *   <li>{@code jvmud_hash_password(string password) : string} returns a PBKDF2-SHA256 password
 *       hash string.</li>
 *   <li>{@code jvmud_verify_password(string password, string encodedHash) : status} verifies a
 *       password against a hash created by {@code jvmud_hash_password}.</li>
 * </ul>
 *
 * <h2>Text, collections, invocation, and compatibility helpers</h2>
 * <ul>
 *   <li>{@code jvmud_read_mudlib_text(string path[, int startLine, int lineCount]) : mixed} reads a
 *       mudlib-relative text file, optionally slicing lines for legacy {@code read_file} calls.</li>
 *   <li>{@code jvmud_list_mudlib_paths(string path[, int flags]) : array} lists mudlib-relative
 *       file names, including simple glob support for compatibility file discovery.</li>
 *   <li>{@code jvmud_append_mudlib_text(string path, mixed text) : status} appends text to a
 *       mudlib-relative file.</li>
 *   <li>{@code jvmud_remove_mudlib_text(string path) : status} removes a mudlib-relative file.</li>
 *   <li>{@code jvmud_copy_mudlib_text(string source, string destination) : int} copies a
 *       mudlib-relative file, returning LDMud-style zero for success.</li>
 *   <li>{@code jvmud_rename_mudlib_text(string source, string destination) : int} renames a
 *       mudlib-relative path, returning LDMud-style zero for success.</li>
 *   <li>{@code jvmud_create_mudlib_directory(string path) : status} creates a mudlib-relative
 *       directory.</li>
 *   <li>{@code jvmud_remove_mudlib_directory(string path) : status} removes an empty
 *       mudlib-relative directory.</li>
 *   <li>{@code jvmud_size(mixed value) : int} returns the size of a string, collection, mapping, or
 *       array.</li>
 *   <li>{@code jvmud_sqrt(mixed value) : float} returns the square root of a numeric value.</li>
 *   <li>{@code jvmud_lowercase_text(mixed value) : string} lowercases text.</li>
 *   <li>{@code jvmud_uppercase_text(mixed value) : string} uppercases text.</li>
 *   <li>{@code jvmud_split_text(string text, string delimiter) : array} splits text on a literal
 *       delimiter while preserving empty trailing fields.</li>
 *   <li>{@code jvmud_regex_match(array values, string pattern[, int flags]) : mixed} returns the
 *       values whose string forms match a regular expression, or LPC false when no value
 *       matches. JVMud accepts common LDMud regexp idioms before running the pattern on Java's
 *       regex engine.</li>
 *   <li>{@code jvmud_regex_replace(string input, string pattern, mixed replacement, int flags) :
 *       string} performs a compatibility regex replacement for legacy mudlib text helpers,
 *       including function callbacks that receive each matched string.</li>
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
 *       host formatter with LPC {@code %O} object placeholders treated as string placeholders; this
 *       overload is registered for arities 1 through 24.</li>
 *   <li>{@code jvmud_extract_text(mixed value, int from) : string} extracts text from an inclusive
 *       start index through the end.</li>
 *   <li>{@code jvmud_extract_text(mixed value, int from, int to) : string} extracts text using
 *       inclusive start and end indexes.</li>
 *   <li>{@code jvmud_member(mixed value, mixed needle) : int} checks mapping key membership or
 *       returns the index of a value in an array or string.</li>
 *   <li>{@code jvmud_mapping_keys(mapping value) : array} returns the mapping's keys in runtime
 *       iteration order.</li>
 *   <li>{@code jvmud_mapping_values(mapping value) : array} returns the mapping's values in
 *       runtime iteration order.</li>
 *   <li>{@code jvmud_mapping_from_keys(array keys) : mapping} builds a mapping whose keys come from
 *       the supplied array and whose values are LPC true.</li>
 *   <li>{@code jvmud_mapping_delete(mapping value, mixed key) : mapping} removes a key from a
 *       mutable mapping and returns the same mapping.</li>
 *   <li>{@code jvmud_is_string(mixed value) : status} reports whether a value is Java-backed LPC
 *       string data.</li>
 *   <li>{@code jvmud_is_int(mixed value) : status} reports whether a value is Java-backed LPC
 *       integer data.</li>
 *   <li>{@code jvmud_is_object(mixed value) : status} reports whether a value is a live runtime
 *       object.</li>
 *   <li>{@code jvmud_is_array(mixed value) : status} reports whether a value is Java-backed LPC
 *       array data.</li>
 *   <li>{@code jvmud_is_mapping(mixed value) : status} reports whether a value is Java-backed LPC
 *       mapping data.</li>
 *   <li>{@code jvmud_filter_indices(mapping values, function callback, mixed ...args) : mapping} returns
 *       a mapping containing the entries whose keys satisfy a callable predicate; this overload is
 *       registered for arities 2 through 8.</li>
 *   <li>{@code jvmud_allocate(int size) : array} returns a zero-filled LPC array of non-negative
 *       size.</li>
 *   <li>{@code jvmud_sscanf(mixed input, mixed format, mixed ...captures) : int} is registered for
 *       arities 3 through 8.</li>
 *   <li>{@code jvmud_apply_callable(function callback, mixed ...args) : mixed} invokes a callable
 *       and expands a final array argument for LDMud-style {@code apply} calls.</li>
 *   <li>{@code jvmud_invoke_lpc_object(mixed target, string methodName, mixed ...args) : mixed}
 *       invokes an optional method on an LPC object or path-resolved shared object; this overload is
 *       registered for arities 2 through 6.</li>
 *   <li>{@code jvmud_set_light(int delta) : int} adjusts the current runtime light level and
 *       returns the resulting value.</li>
 * </ul>
 */
public final class CoreEfuns {
    private static final int PASSWORD_ITERATIONS = 210_000;
    private static final int PASSWORD_SALT_BYTES = 16;
    private static final int PASSWORD_HASH_BITS = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CoreEfuns() {}

    /**
     * Registers the complete core efun set directly into a generated-code runtime context.
     *
     * <p>Use this when tests or lower-level runtime code already have the {@link RuntimeContext}
     * that generated bytecode will use. The supplied context receives fresh {@link Efun}
     * instances, including all overloads listed in this class-level catalog.</p>
     *
     * @param context runtime context that should resolve built-in efuns
     * @throws NullPointerException if {@code context} is {@code null}
     */
    public static void registerCore(RuntimeContext context) {
        Objects.requireNonNull(context, "context");

        for (Efun efun : coreEfuns()) {
            context.registerEfun(efun);
        }
    }

    /**
     * Registers the complete core efun set into a host-facing LPC runtime.
     *
     * <p>This is the usual entry point for embedding JVMud through {@link LPCRuntime}; the runtime
     * forwards registration to its active {@link RuntimeContext}.</p>
     *
     * @param runtime host-facing runtime whose generated LPC objects should see the core efuns
     * @throws NullPointerException if {@code runtime} is {@code null}
     */
    public static void registerCore(LPCRuntime runtime) {
        Objects.requireNonNull(runtime, "runtime");

        for (Efun efun : coreEfuns()) {
            runtime.registerEfun(efun);
        }
    }

    private static List<Efun> coreEfuns() {
        List<Efun> efuns = new ArrayList<>();
        efuns.add(efun("jvmud_write", LPCType.LPCVOID, List.of(LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.write(args[0]);
                    return null;
                }));
        efuns.add(efun("jvmud_write_to_lpc_object", LPCType.LPCVOID, List.of(LPCType.LPCOBJECT, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.writeToLpcObject(args[0], args[1]);
                    return null;
                }));
        efuns.add(efun("jvmud_rebind_session_lpc_object", LPCType.LPCSTATUS,
                List.of(LPCType.LPCOBJECT, LPCType.LPCOBJECT),
                (runtime, args) -> runtime.rebindSessionLpcObject(args[0], args[1]) ? 1 : 0));
        efuns.add(efun("jvmud_emit_perceivable", LPCType.LPCVOID, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> emitPerceivable(runtime, args[0], args[1])));
        efuns.add(efun("jvmud_emit_perceivable_except", LPCType.LPCVOID,
                List.of(LPCType.LPCMIXED, LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> emitPerceivableExcept(runtime, args[0], args[1], args[2])));
        efuns.add(efun("jvmud_emit_perceivable_at", LPCType.LPCVOID, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> emitPerceivableAt(runtime, args[0], args[1])));
        efuns.add(efun("jvmud_current_lpc_object", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.currentObject()));
        efuns.add(efun("jvmud_previous_lpc_object", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.previousObject()));
        efuns.add(efun("jvmud_current_actor", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.currentCommandActor() != null
                        ? runtime.currentCommandActor()
                        : runtime.currentObject()));
        efuns.add(efun("jvmud_current_agent", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> currentAgent(runtime)));
        efuns.add(efun("jvmud_current_verb", LPCType.LPCSTRING, List.of(),
                (runtime, args) -> runtime.currentCommandVerb()));
        efuns.add(efun("jvmud_dispatch_entity_command", LPCType.LPCMIXED,
                List.of(LPCType.LPCMIXED, LPCType.LPCSTRING),
                (runtime, args) -> dispatchEntityCommand(runtime, args[0], String.valueOf(args[1]))));
        efuns.add(efun("jvmud_time", LPCType.LPCINT, List.of(),
                (runtime, args) -> (int) (System.currentTimeMillis() / 1000L)));
        efuns.add(efun("jvmud_format_time", LPCType.LPCSTRING, List.of(LPCType.LPCINT),
                (runtime, args) -> formatTime(((Number) args[0]).longValue())));
        efuns.add(efun("jvmud_lpc_object_id", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.objectId(args[0])));
        efuns.add(efun("jvmud_method_exists", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> methodExists(runtime.currentObject(), String.valueOf(args[0])) ? 1 : 0));
        efuns.add(efun("jvmud_method_exists", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING, LPCType.LPCMIXED),
                (runtime, args) -> methodExists(args[1], String.valueOf(args[0])) ? 1 : 0));
        efuns.add(efun("jvmud_lpc_object_methods", LPCType.LPCARRAY, List.of(LPCType.LPCMIXED),
                (runtime, args) -> lpcObjectMethods(args[0])));
        efuns.add(efun("jvmud_inherited_programs", LPCType.LPCARRAY, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.inheritedPrograms(args[0])));
        efuns.add(efun("jvmud_size", LPCType.LPCINT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> sizeOf(args[0])));
        efuns.add(efun("jvmud_random", LPCType.LPCINT, List.of(LPCType.LPCINT),
                (runtime, args) -> random(((Number) args[0]).intValue())));
        efuns.add(efun("jvmud_sqrt", LPCType.LPCFLOAT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> Math.sqrt(toDouble(args[0]))));
        efuns.add(efun("jvmud_users", LPCType.LPCARRAY, List.of(),
                (runtime, args) -> runtime.users()));
        efuns.add(efun("jvmud_previous_object", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.previousObject()));
        efuns.add(efun("jvmud_interactive", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.isInteractive(args[0]) ? 1 : 0));
        efuns.add(efun("jvmud_interactive_info", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> 0));
        efuns.add(efun("jvmud_lpc_object_info", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> 0));
        efuns.add(efun("jvmud_object_info", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> 0));
        efuns.add(efun("jvmud_configure_lpc_object", LPCType.LPCSTATUS,
                List.of(LPCType.LPCMIXED, LPCType.LPCINT, LPCType.LPCMIXED),
                (runtime, args) -> configureLpcObject(runtime, args[0], ((Number) args[1]).intValue(), args[2])));
        efuns.add(efun("jvmud_configure_object", LPCType.LPCSTATUS,
                List.of(LPCType.LPCMIXED, LPCType.LPCINT, LPCType.LPCMIXED),
                (runtime, args) -> configureLpcObject(runtime, args[0], ((Number) args[1]).intValue(), args[2])));
        efuns.add(efun("jvmud_find_lpc_object", LPCType.LPCMIXED, List.of(LPCType.LPCSTRING),
                (runtime, args) -> findLpcObject(runtime, String.valueOf(args[0]))));
        efuns.add(efun("jvmud_find_object", LPCType.LPCMIXED, List.of(LPCType.LPCSTRING),
                (runtime, args) -> findLpcObject(runtime, String.valueOf(args[0]))));
        efuns.add(efun("jvmud_to_lpc_object", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED),
                (runtime, args) -> toLpcObject(runtime, args[0])));
        efuns.add(efun("jvmud_shutdown", LPCType.LPCVOID, List.of(),
                (runtime, args) -> null));
        efuns.add(efun("jvmud_set_this_player", LPCType.LPCVOID, List.of(LPCType.LPCMIXED),
                (runtime, args) -> null));
        efuns.add(efun("jvmud_raise_error", LPCType.LPCVOID, List.of(LPCType.LPCMIXED),
                (runtime, args) -> {
                    throw new IllegalStateException(String.valueOf(args[0]));
                }));
        efuns.add(efun("jvmud_query_idle", LPCType.LPCINT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.queryIdle(args[0])));
        efuns.add(efun("jvmud_query_ip_number", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.queryIpNumber(args[0])));
        efuns.add(efun("jvmud_query_ip_name", LPCType.LPCMIXED, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.queryIpNumber(args[0])));
        efuns.add(efun("jvmud_driver_info", LPCType.LPCMIXED, List.of(LPCType.LPCINT),
                (runtime, args) -> 0));
        efuns.add(efun("jvmud_read_mudlib_text", LPCType.LPCMIXED, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.readMudlibText(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_read_mudlib_text", LPCType.LPCMIXED,
                List.of(LPCType.LPCSTRING, LPCType.LPCINT, LPCType.LPCINT),
                (runtime, args) -> readMudlibText(
                        runtime,
                        String.valueOf(args[0]),
                        ((Number) args[1]).intValue(),
                        ((Number) args[2]).intValue())));
        efuns.add(efun("jvmud_list_mudlib_paths", LPCType.LPCARRAY, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.listMudlibPaths(String.valueOf(args[0]), 1)));
        efuns.add(efun("jvmud_list_mudlib_paths", LPCType.LPCARRAY,
                List.of(LPCType.LPCSTRING, LPCType.LPCINT),
                (runtime, args) -> runtime.listMudlibPaths(
                        String.valueOf(args[0]),
                        ((Number) args[1]).intValue())));
        efuns.add(efun("jvmud_append_mudlib_text", LPCType.LPCSTATUS,
                List.of(LPCType.LPCSTRING, LPCType.LPCMIXED),
                (runtime, args) -> runtime.appendMudlibText(String.valueOf(args[0]), args[1])));
        efuns.add(efun("jvmud_remove_mudlib_text", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.removeMudlibText(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_copy_mudlib_text", LPCType.LPCINT,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> runtime.copyMudlibText(String.valueOf(args[0]), String.valueOf(args[1]))));
        efuns.add(efun("jvmud_rename_mudlib_text", LPCType.LPCINT,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> runtime.renameMudlibText(String.valueOf(args[0]), String.valueOf(args[1]))));
        efuns.add(efun("jvmud_create_mudlib_directory", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.createMudlibDirectory(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_remove_mudlib_directory", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.removeMudlibDirectory(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_transfer_player_to_game", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.transferCurrentPlayerToGame(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_save_lpc_object_state", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.saveCurrentLPCObjectState(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_restore_lpc_object_state", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.restoreCurrentLPCObjectState(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_db_connect", LPCType.LPCINT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.dbConnect(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_db_connect", LPCType.LPCINT,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> runtime.dbConnect(
                        String.valueOf(args[0]),
                        nullableText(args[1]),
                        nullableText(args[2]))));
        efuns.add(efun("jvmud_db_exec", LPCType.LPCINT, List.of(LPCType.LPCINT, LPCType.LPCSTRING),
                (runtime, args) -> runtime.dbExec(((Number) args[0]).intValue(), String.valueOf(args[1]))));
        efuns.add(efun("jvmud_db_fetch", LPCType.LPCMIXED, List.of(LPCType.LPCINT),
                (runtime, args) -> runtime.dbFetch(((Number) args[0]).intValue())));
        efuns.add(efun("jvmud_db_error", LPCType.LPCMIXED, List.of(LPCType.LPCINT),
                (runtime, args) -> runtime.dbError(((Number) args[0]).intValue())));
        efuns.add(efun("jvmud_db_close", LPCType.LPCSTATUS, List.of(LPCType.LPCINT),
                (runtime, args) -> runtime.dbClose(((Number) args[0]).intValue())));
        efuns.add(efun("jvmud_db_handles", LPCType.LPCARRAY, List.of(),
                (runtime, args) -> runtime.dbHandles()));
        efuns.add(efun("jvmud_db_escape", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.dbEscape(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_hash_password", LPCType.LPCSTRING, List.of(LPCType.LPCSTRING),
                (runtime, args) -> hashPassword(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_verify_password", LPCType.LPCSTATUS, List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> verifyPassword(String.valueOf(args[0]), String.valueOf(args[1])) ? 1 : 0));
        efuns.add(efun("jvmud_lowercase_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> String.valueOf(args[0]).toLowerCase()));
        efuns.add(efun("jvmud_uppercase_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> String.valueOf(args[0]).toUpperCase()));
        efuns.add(efun("jvmud_split_text", LPCType.LPCARRAY,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> splitText(String.valueOf(args[0]), String.valueOf(args[1]))));
        efuns.add(efun("jvmud_regex_match", LPCType.LPCMIXED,
                List.of(LPCType.LPCARRAY, LPCType.LPCSTRING),
                (runtime, args) -> regexMatch(args[0], String.valueOf(args[1]), 0)));
        efuns.add(efun("jvmud_regex_match", LPCType.LPCMIXED,
                List.of(LPCType.LPCARRAY, LPCType.LPCSTRING, LPCType.LPCINT),
                (runtime, args) -> regexMatch(args[0], String.valueOf(args[1]), ((Number) args[2]).intValue())));
        efuns.add(efun("jvmud_regex_replace", LPCType.LPCSTRING,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING, LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> regexReplace(
                        runtime,
                        String.valueOf(args[0]),
                        String.valueOf(args[1]),
                        args[2],
                        ((Number) args[3]).intValue())));
        efuns.add(efun("jvmud_regex_explode", LPCType.LPCARRAY,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> regexExplode(String.valueOf(args[0]), String.valueOf(args[1]))));
        efuns.add(efun("jvmud_to_int", LPCType.LPCINT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> toInt(args[0])));
        efuns.add(efun("jvmud_to_string", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> String.valueOf(args[0])));
        efuns.add(efun("jvmud_capitalize_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> capitalizeText(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_wrap_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> wrapText(args[0], 78)));
        efuns.add(efun("jvmud_wrap_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> wrapText(args[0], ((Number) args[1]).intValue())));
        efuns.add(efun("jvmud_serialize_lpc_value", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED),
                (runtime, args) -> RuntimeValueCodec.serialize(args[0])));
        efuns.add(efun("jvmud_deserialize_lpc_value", LPCType.LPCMIXED, List.of(LPCType.LPCSTRING),
                (runtime, args) -> RuntimeValueCodec.deserialize(String.valueOf(args[0]))));
        for (int arity = 1; arity <= 24; arity++) {
            efuns.add(formatTextEfun(arity));
        }
        efuns.add(efun("jvmud_extract_text", LPCType.LPCSTRING, List.of(LPCType.LPCMIXED, LPCType.LPCINT),
                (runtime, args) -> extractText(
                        String.valueOf(args[0]),
                        ((Number) args[1]).intValue(),
                        -1)));
        efuns.add(efun("jvmud_extract_text", LPCType.LPCSTRING,
                List.of(LPCType.LPCMIXED, LPCType.LPCINT, LPCType.LPCINT),
                (runtime, args) -> extractText(
                        String.valueOf(args[0]),
                        ((Number) args[1]).intValue(),
                        ((Number) args[2]).intValue())));
        efuns.add(efun("jvmud_member", LPCType.LPCINT, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> member(args[0], args[1])));
        efuns.add(efun("jvmud_mapping_keys", LPCType.LPCARRAY, List.of(LPCType.LPCMAPPING),
                (runtime, args) -> mappingKeys(args[0])));
        efuns.add(efun("jvmud_mapping_values", LPCType.LPCARRAY, List.of(LPCType.LPCMAPPING),
                (runtime, args) -> mappingValues(args[0])));
        efuns.add(efun("jvmud_mapping_from_keys", LPCType.LPCMAPPING, List.of(LPCType.LPCARRAY),
                (runtime, args) -> mappingFromKeys(args[0])));
        efuns.add(efun("jvmud_mapping_delete", LPCType.LPCMAPPING, List.of(LPCType.LPCMAPPING, LPCType.LPCMIXED),
                (runtime, args) -> mappingDelete(args[0], args[1])));
        for (int arity = 2; arity <= 16; arity++) {
            efuns.add(captureSessionInputEfun(arity));
        }
        efuns.add(efun("jvmud_is_string", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> args[0] instanceof String ? 1 : 0));
        efuns.add(efun("jvmud_is_int", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> args[0] instanceof Number number
                        && !(number instanceof Float)
                        && !(number instanceof Double) ? 1 : 0));
        efuns.add(efun("jvmud_is_object", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.objectId(args[0]) != null ? 1 : 0));
        efuns.add(efun("jvmud_is_array", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> args[0] instanceof List<?> ? 1 : 0));
        efuns.add(efun("jvmud_is_mapping", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> args[0] instanceof Map<?, ?> ? 1 : 0));
        for (int arity = 2; arity <= 8; arity++) {
            efuns.add(filterIndicesEfun(arity));
        }
        for (int arity = 3; arity <= 8; arity++) {
            efuns.add(sscanfEfun(arity));
        }
        efuns.add(efun("jvmud_allocate", LPCType.LPCARRAY, List.of(LPCType.LPCINT),
                (runtime, args) -> new ArrayList<>(
                        Collections.nCopies(Math.max(0, ((Number) args[0]).intValue()), Integer.valueOf(0)))));
        for (int arity = 2; arity <= 6; arity++) {
            efuns.add(invokeLpcObjectEfun(arity));
        }
        for (int arity = 1; arity <= 10; arity++) {
            efuns.add(applyCallableEfun(arity));
        }
        efuns.add(efun("jvmud_load_lpc_object", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.loadOrGetObject(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_clone_lpc_object", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.cloneObject(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_move_entity", LPCType.LPCVOID, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.moveObject(resolveTarget(runtime, args[0]), resolveTarget(runtime, args[1]));
                    return null;
                }));
        efuns.add(efun("jvmud_find_entity", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.present(args[0], null)));
        efuns.add(efun("jvmud_find_entity", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> runtime.present(args[0], args[1])));
        efuns.add(efun("jvmud_bind_entity_alias", LPCType.LPCVOID,
                List.of(LPCType.LPCOBJECT, LPCType.LPCSTRING, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.bindEntityAlias(args[0], args[1], args[2]);
                    return null;
                }));
        efuns.add(efun("jvmud_find_entity_alias", LPCType.LPCOBJECT, List.of(LPCType.LPCSTRING, LPCType.LPCMIXED),
                (runtime, args) -> runtime.findEntityAlias(args[0], args[1])));
        efuns.add(efun("jvmud_entity_has_alias", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED, LPCType.LPCSTRING),
                (runtime, args) -> runtime.entityHasAlias(args[0], args[1])));
        efuns.add(efun("jvmud_entity_commands_enabled", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.entityCommandsEnabled(args[0])));
        efuns.add(efun("jvmud_set_entity_location", LPCType.LPCVOID,
                List.of(LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.moveObject(args[0], args[1]);
                    return null;
                }));
        efuns.add(efun("jvmud_set_entity_translucent", LPCType.LPCVOID,
                List.of(LPCType.LPCMIXED, LPCType.LPCSTATUS),
                (runtime, args) -> {
                    runtime.setEntityTranslucent(args[0], Truth.isTruthy(args[1]));
                    return null;
                }));
        efuns.add(efun("jvmud_entity_translucent", LPCType.LPCSTATUS, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.entityTranslucent(args[0]) ? 1 : 0));
        efuns.add(efun("jvmud_first_entity_at", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.firstInventory(args[0])));
        efuns.add(efun("jvmud_next_entity_at", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.nextInventory(args[0])));
        efuns.add(efun("jvmud_set_light", LPCType.LPCINT, List.of(LPCType.LPCINT),
                (runtime, args) -> runtime.setLight(((Number) args[0]).intValue())));
        efuns.add(efun("jvmud_schedule_recurring_tick", LPCType.LPCVOID,
                List.of(LPCType.LPCINT, LPCType.LPCINT),
                (runtime, args) -> {
                    runtime.scheduleRecurringTick(
                            ((Number) args[0]).intValue(),
                            ((Number) args[1]).intValue());
                    return null;
                }));
        efuns.add(efun("jvmud_schedule_deferred_callback", LPCType.LPCVOID,
                List.of(LPCType.LPCSTRING, LPCType.LPCINT),
                (runtime, args) -> {
                    runtime.scheduleDeferredCallback(String.valueOf(args[0]), ((Number) args[1]).intValue());
                    return null;
                }));
        efuns.add(efun("jvmud_schedule_deferred_callback", LPCType.LPCVOID,
                List.of(LPCType.LPCSTRING, LPCType.LPCINT, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.scheduleDeferredCallback(String.valueOf(args[0]), ((Number) args[1]).intValue(), args[2]);
                    return null;
                }));
        efuns.add(efun("jvmud_schedule_deferred_callback", LPCType.LPCVOID,
                List.of(LPCType.LPCSTRING, LPCType.LPCINT, LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.scheduleDeferredCallback(String.valueOf(args[0]), ((Number) args[1]).intValue(),
                            args[2], args[3]);
                    return null;
                }));
        efuns.add(efun("jvmud_schedule_deferred_callback", LPCType.LPCVOID,
                List.of(LPCType.LPCSTRING, LPCType.LPCINT, LPCType.LPCMIXED, LPCType.LPCMIXED,
                        LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.scheduleDeferredCallback(String.valueOf(args[0]), ((Number) args[1]).intValue(),
                            args[2], args[3], args[4]);
                    return null;
                }));
        efuns.add(efun("jvmud_schedule_deferred_callback", LPCType.LPCVOID,
                List.of(LPCType.LPCSTRING, LPCType.LPCINT, LPCType.LPCMIXED, LPCType.LPCMIXED,
                        LPCType.LPCMIXED, LPCType.LPCMIXED),
                (runtime, args) -> {
                    runtime.scheduleDeferredCallback(String.valueOf(args[0]), ((Number) args[1]).intValue(),
                            args[2], args[3], args[4], args[5]);
                    return null;
                }));
        efuns.add(efun("jvmud_cancel_deferred_callback", LPCType.LPCINT, List.of(LPCType.LPCSTRING),
                (runtime, args) -> runtime.cancelDeferredCallback(String.valueOf(args[0]))));
        efuns.add(efun("jvmud_enable_commands", LPCType.LPCVOID, List.of(),
                (runtime, args) -> {
                    runtime.enableEntityCommands(runtime.currentObject());
                    return null;
                }));
        efuns.add(efun("jvmud_add_action", LPCType.LPCVOID, List.of(LPCType.LPCSTRING),
                (runtime, args) -> {
                    runtime.rememberActionMethod(String.valueOf(args[0]));
                    return null;
                }));
        efuns.add(efun("jvmud_add_action", LPCType.LPCVOID, List.of(LPCType.LPCSTRING, LPCType.LPCSTRING),
                (runtime, args) -> {
                    runtime.rememberActionMethod(String.valueOf(args[0]));
                    runtime.registerVerb(String.valueOf(args[1]));
                    return null;
                }));
        efuns.add(efun("jvmud_add_action", LPCType.LPCVOID,
                List.of(LPCType.LPCSTRING, LPCType.LPCSTRING, LPCType.LPCSTATUS),
                (runtime, args) -> {
                    runtime.rememberActionMethod(String.valueOf(args[0]));
                    runtime.registerVerb(String.valueOf(args[1]), Truth.isTruthy(args[2]));
                    return null;
                }));
        efuns.add(efun("jvmud_add_verb", LPCType.LPCVOID, List.of(LPCType.LPCSTRING),
                (runtime, args) -> {
                    runtime.registerVerb(String.valueOf(args[0]));
                    return null;
                }));
        efuns.add(efun("jvmud_entity_location", LPCType.LPCOBJECT, List.of(),
                (runtime, args) -> runtime.environment(null)));
        efuns.add(efun("jvmud_entity_location", LPCType.LPCOBJECT, List.of(LPCType.LPCMIXED),
                (runtime, args) -> runtime.environment(args[0])));
        efuns.add(efun("jvmud_destroy_lpc_object", LPCType.LPCVOID, List.of(LPCType.LPCOBJECT),
                (runtime, args) -> {
                    runtime.destructObject(args[0]);
                    return null;
                }));
        return efuns;
    }

    private static int sizeOf(Object value) {
        if (value == null) {
            return 0;
        }
        if (Integer.valueOf(0).equals(value)) {
            return 0;
        }
        if (value instanceof CharSequence text) {
            return text.length();
        }
        if (value instanceof Collection<?> collection) {
            return collection.size();
        }
        if (value instanceof Map<?, ?> map) {
            return map.size();
        }
        if (value.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(value);
        }
        throw new IllegalArgumentException("sizeof expects array, mapping, or string value");
    }

    private static int member(Object value, Object needle) {
        if (value instanceof Map<?, ?> map) {
            return map.containsKey(needle) ? 1 : 0;
        }
        if (value instanceof List<?> list) {
            return list.indexOf(needle);
        }
        if (value instanceof CharSequence text) {
            String haystack = text.toString();
            if (needle instanceof Number number) {
                return haystack.indexOf((char) number.intValue());
            }
            return haystack.indexOf(String.valueOf(needle));
        }
        return -1;
    }

    private static List<Object> mappingKeys(Object value) {
        if (value instanceof Map<?, ?> map) {
            return new ArrayList<>(map.keySet());
        }
        throw new IllegalArgumentException("jvmud_mapping_keys expects a mapping value");
    }

    private static List<Object> mappingValues(Object value) {
        if (value instanceof Map<?, ?> map) {
            return new ArrayList<>(map.values());
        }
        throw new IllegalArgumentException("jvmud_mapping_values expects a mapping value");
    }

    private static Map<Object, Object> mappingFromKeys(Object value) {
        if (value instanceof List<?> keys) {
            Map<Object, Object> mapping = new LinkedHashMap<>();
            for (Object key : keys) {
                mapping.put(key, 1);
            }
            return mapping;
        }
        throw new IllegalArgumentException("jvmud_mapping_from_keys expects an array value");
    }

    private static Map<?, ?> mappingDelete(Object value, Object key) {
        if (value instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> mutable = (Map<Object, Object>) map;
            mutable.remove(key);
            return map;
        }
        throw new IllegalArgumentException("jvmud_mapping_delete expects a mapping value");
    }

    private static boolean methodExists(Object target, String methodName) {
        if (target == null || (target instanceof Number number && number.intValue() == 0) || methodName == null) {
            return false;
        }
        for (Method method : target.getClass().getMethods()) {
            if (!isPublicMudlibMethod(method)) {
                continue;
            }
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> lpcObjectMethods(Object target) {
        if (target == null || (target instanceof Number number && number.intValue() == 0)) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (Method method : target.getClass().getMethods()) {
            if (!isPublicMudlibMethod(method) || names.contains(method.getName())) {
                continue;
            }
            names.add(method.getName());
        }
        Collections.sort(names);
        return names;
    }

    private static boolean isPublicMudlibMethod(Method method) {
        return !method.isSynthetic()
                && !method.isBridge()
                && method.getDeclaringClass() != Object.class
                && Modifier.isPublic(method.getModifiers())
                && !method.getName().startsWith("$");
    }

    private static String capitalizeText(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private static List<String> splitText(String text, String delimiter) {
        if (delimiter.isEmpty()) {
            List<String> characters = new ArrayList<>();
            for (int i = 0; i < text.length(); i++) {
                characters.add(String.valueOf(text.charAt(i)));
            }
            return characters;
        }
        return List.of(text.split(java.util.regex.Pattern.quote(delimiter), -1));
    }

    private static String hashPassword(String password) {
        byte[] salt = new byte[PASSWORD_SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, PASSWORD_ITERATIONS);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return "pbkdf2-sha256$" + PASSWORD_ITERATIONS + "$"
                + encoder.encodeToString(salt) + "$"
                + encoder.encodeToString(hash);
    }

    private static boolean verifyPassword(String password, String encodedHash) {
        String[] parts = encodedHash.split("\\$");
        if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expected = Base64.getUrlDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, PASSWORD_HASH_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash password", e);
        }
    }

    private static String extractText(String value, int from, int to) {
        int length = value.length();
        int start = Math.max(0, from);
        int end = to < 0 ? length - 1 : Math.min(to, length - 1);
        if (start >= length || end < start) {
            return "";
        }
        return value.substring(start, end + 1);
    }

    private static String formatTime(long epochSeconds) {
        return DateTimeFormatter
                .ofPattern("EEE MMM dd HH:mm:ss yyyy", Locale.US)
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochSecond(epochSeconds));
    }

    private static Object emitPerceivable(RuntimeContext runtime, Object emitter, Object message) {
        runtime.emitPerceivable(resolveTarget(runtime, emitter), message);
        return null;
    }

    private static Object emitPerceivableExcept(
            RuntimeContext runtime, Object emitter, Object message, Object excluded) {
        runtime.emitPerceivableExcept(
                resolveTarget(runtime, emitter),
                message,
                resolveTarget(runtime, excluded));
        return null;
    }

    private static Object emitPerceivableAt(RuntimeContext runtime, Object location, Object message) {
        runtime.emitPerceivableAt(resolveTarget(runtime, location), message);
        return null;
    }

    private static Object dispatchEntityCommand(RuntimeContext runtime, Object actor, String commandLine) {
        Object resolvedActor = resolveTarget(runtime, actor);
        if (resolvedActor == null) {
            return 0;
        }

        return runtime.withCommandActor(resolvedActor, () -> runtime.dispatchCommand(resolvedActor, commandLine));
    }

    /**
     * Returns the active world-present agent for legacy command-giver semantics.
     *
     * <p>This deliberately does not fall back to the currently executing LPC object: service objects,
     * daemons, and other non-agent code can execute during an action without becoming the action's
     * agent.</p>
     */
    private static Object currentAgent(RuntimeContext runtime) {
        Object agent = runtime.currentCommandActor();
        return agent != null ? agent : 0;
    }

    private static int random(int max) {
        if (max <= 0) {
            return 0;
        }

        return ThreadLocalRandom.current().nextInt(max);
    }

    private static Efun formatTextEfun(int arity) {
        List<LPCType> parameters = new ArrayList<>();
        parameters.add(LPCType.LPCSTRING);
        for (int i = 1; i < arity; i++) {
            parameters.add(LPCType.LPCMIXED);
        }
        return efun("jvmud_format_text", LPCType.LPCSTRING, parameters,
                (runtime, args) -> formatText(args));
    }

    private static String formatText(Object[] args) {
        String format = String.valueOf(args[0]).replace("%O", "%s");
        Object[] values = new Object[args.length - 1];
        if (values.length > 0) {
            System.arraycopy(args, 1, values, 0, values.length);
        }
        return String.format(Locale.ROOT, format, values);
    }

    /**
     * Wraps mudlib-owned description text while preserving LPC's false/empty text convention.
     *
     * <p>This helper intentionally lives beside the efuns instead of using the engine's outgoing
     * presentation formatter: mudlibs may store or compare the result, and legacy compatibility
     * expects a trailing newline for non-empty formatted text.</p>
     */
    private static String wrapText(Object value, int requestedWidth) {
        String text = String.valueOf(value);
        if (text.isEmpty()) {
            return "";
        }

        int width = requestedWidth > 0 ? requestedWidth : 78;
        StringBuilder wrapped = new StringBuilder(text.length() + 1);
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                wrapped.append('\n');
            }
            appendWrappedPlainLine(wrapped, lines[i], width);
        }
        if (!wrapped.isEmpty() && wrapped.charAt(wrapped.length() - 1) != '\n') {
            wrapped.append('\n');
        }
        return wrapped.toString();
    }

    private static void appendWrappedPlainLine(StringBuilder output, String line, int width) {
        String trimmed = line.strip();
        if (trimmed.isEmpty()) {
            return;
        }

        StringBuilder current = new StringBuilder();
        for (String word : trimmed.split("\\s+")) {
            if (current.isEmpty()) {
                current.append(word);
            } else if (current.length() + 1 + word.length() <= width) {
                current.append(' ').append(word);
            } else {
                output.append(current).append('\n');
                current.setLength(0);
                current.append(word);
            }
        }
        output.append(current);
    }

    private static String regexReplace(RuntimeContext runtime, String input, String pattern, Object replacement, int flags) {
        String javaPattern = javaRegexPattern(pattern);
        if (replacement instanceof RuntimeCallable callback) {
            return regexReplaceWithCallback(runtime, input, javaPattern, callback, flags);
        }
        String javaReplacement = javaRegexReplacement(String.valueOf(replacement));
        return flags == 0
                ? input.replaceFirst(javaPattern, javaReplacement)
                : input.replaceAll(javaPattern, javaReplacement);
    }

    private static String regexReplaceWithCallback(
            RuntimeContext runtime, String input, String javaPattern, RuntimeCallable callback, int flags) {
        Matcher matcher = Pattern.compile(javaPattern).matcher(input);
        StringBuffer output = new StringBuffer();
        int replacements = 0;
        while (matcher.find()) {
            if (flags == 0 && replacements > 0) {
                break;
            }
            Object replacement = callback.call(runtime, matcher.group());
            matcher.appendReplacement(output, Matcher.quoteReplacement(String.valueOf(replacement)));
            replacements++;
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private static Object regexMatch(Object values, String pattern, int flags) {
        if (!(values instanceof List<?> source)) {
            throw new IllegalArgumentException("jvmud_regex_match expects an array of values");
        }
        int javaFlags = flags == 0 ? 0 : java.util.regex.Pattern.DOTALL;
        java.util.regex.Pattern compiled = java.util.regex.Pattern.compile(javaRegexPattern(pattern), javaFlags);
        List<Object> matches = new ArrayList<>();
        for (Object value : source) {
            String text = String.valueOf(value);
            if (compiled.matcher(text).find()) {
                matches.add(value);
            }
        }
        return matches.isEmpty() ? 0 : matches;
    }

    private static List<String> regexExplode(String input, String pattern) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(javaRegexPattern(pattern)).matcher(input);
        List<String> parts = new ArrayList<>();
        int lastEnd = 0;
        while (matcher.find()) {
            parts.add(input.substring(lastEnd, matcher.start()));
            parts.add(matcher.group());
            lastEnd = matcher.end();
        }
        parts.add(input.substring(lastEnd));
        return parts;
    }

    private static String javaRegexPattern(String pattern) {
        return escapeAnchorQuestionMark(escapeLiteralLeftBracketsInCharacterClasses(pattern));
    }

    /** Escapes LDMud-style literal {@code [} characters embedded inside regex character classes. */
    private static String escapeLiteralLeftBracketsInCharacterClasses(String pattern) {
        String literalLeftBracket = "\\" + "u005B";
        StringBuilder translated = new StringBuilder(pattern.length());
        boolean inCharacterClass = false;
        boolean escaped = false;
        for (int index = 0; index < pattern.length(); index++) {
            char current = pattern.charAt(index);
            if (escaped) {
                translated.append(current);
                escaped = false;
                continue;
            }
            if (current == '\\') {
                translated.append(current);
                escaped = true;
                continue;
            }
            if (current == '[') {
                if (inCharacterClass) {
                    translated.append(literalLeftBracket);
                } else {
                    translated.append(current);
                    inCharacterClass = true;
                }
                continue;
            }
            if (current == ']') {
                inCharacterClass = false;
            }
            translated.append(current);
        }
        return translated.toString();
    }

    /**
     * Keeps LDMud command aliases such as {@code ? [-v]} literal when the mudlib turns them into
     * an anchored pattern like {@code ^?( -v)*$}. Java treats {@code ?} after {@code ^} as a
     * quantifier; LDMud mudlibs commonly intend it as command text in that position.
     */
    private static String escapeAnchorQuestionMark(String pattern) {
        StringBuilder translated = new StringBuilder(pattern.length());
        boolean inCharacterClass = false;
        boolean escaped = false;
        char previous = 0;
        for (int index = 0; index < pattern.length(); index++) {
            char current = pattern.charAt(index);
            if (current == '?' && previous == '^' && !inCharacterClass && !escaped) {
                translated.append('\\');
            }
            translated.append(current);

            if (escaped) {
                escaped = false;
            } else if (current == '\\') {
                escaped = true;
            } else if (current == '[') {
                inCharacterClass = true;
            } else if (current == ']') {
                inCharacterClass = false;
            }
            previous = current;
        }
        return translated.toString();
    }

    private static Object readMudlibText(RuntimeContext runtime, String path, int startLine, int lineCount) {
        Object text = runtime.readMudlibText(path);
        if (!(text instanceof String content) || lineCount <= 0) {
            return text;
        }

        String[] lines = content.split("(?<=\\n)", -1);
        int start = Math.max(0, startLine - 1);
        if (start >= lines.length) {
            return "";
        }
        int end = Math.min(lines.length, start + lineCount);
        StringBuilder selected = new StringBuilder();
        for (int i = start; i < end; i++) {
            selected.append(lines[i]);
        }
        return selected.toString();
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return 0.0d;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0d;
        }
    }

    private static String javaRegexReplacement(String replacement) {
        StringBuilder converted = new StringBuilder();
        for (int i = 0; i < replacement.length(); i++) {
            char current = replacement.charAt(i);
            if (current == '\\' && i + 1 < replacement.length() && Character.isDigit(replacement.charAt(i + 1))) {
                converted.append('$').append(replacement.charAt(++i));
            } else if (current == '\\') {
                converted.append("\\\\");
            } else if (current == '$') {
                converted.append("\\$");
            } else {
                converted.append(current);
            }
        }
        return converted.toString();
    }

    private static Efun filterIndicesEfun(int arity) {
        List<LPCType> parameters = new ArrayList<>();
        parameters.add(LPCType.LPCMAPPING);
        parameters.add(LPCType.LPCFUNCTION);
        for (int i = 2; i < arity; i++) {
            parameters.add(LPCType.LPCMIXED);
        }
        return efun("jvmud_filter_indices", LPCType.LPCMAPPING, parameters,
                (runtime, args) -> filterIndices(runtime, args));
    }

    private static Object filterIndices(RuntimeContext runtime, Object[] args) {
        if (!(args[0] instanceof Map<?, ?> source)) {
            throw new IllegalArgumentException("jvmud_filter_indices expects a mapping as its first argument");
        }
        if (!(args[1] instanceof RuntimeCallable callback)) {
            throw new IllegalArgumentException("jvmud_filter_indices expects a callable as its second argument");
        }

        Map<Object, Object> result = new LinkedHashMap<>();
        Object[] callbackArgs = new Object[args.length - 1];
        if (args.length > 2) {
            System.arraycopy(args, 2, callbackArgs, 1, args.length - 2);
        }

        for (Map.Entry<?, ?> entry : source.entrySet()) {
            callbackArgs[0] = entry.getKey();
            if (Truth.isTruthy(callback.call(runtime, callbackArgs))) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static Object findLpcObject(RuntimeContext runtime, String path) {
        Object object = runtime.getObject(path);
        if (object == null) {
            object = runtime.getObject(stripLeadingSlash(path));
        }
        if (object == null) {
            object = runtime.getObject(stripExtension(stripLeadingSlash(path)));
        }
        return object != null ? object : 0;
    }

    private static Object toLpcObject(RuntimeContext runtime, Object value) {
        if (value == null || Integer.valueOf(0).equals(value)) {
            return 0;
        }
        if (runtime.objectId(value) != null) {
            return value;
        }
        return findLpcObject(runtime, String.valueOf(value));
    }

    private static int configureLpcObject(RuntimeContext runtime, Object object, int key, Object value) {
        if (key == 0) {
            if (Truth.isTruthy(value)) {
                runtime.enableEntityCommands(object);
            } else {
                runtime.disableEntityCommands(object);
            }
        }
        return 1;
    }

    private static Efun captureSessionInputEfun(int arity) {
        List<LPCType> parameters = new ArrayList<>();
        parameters.add(LPCType.LPCSTRING);
        parameters.add(LPCType.LPCINT);
        for (int i = 2; i < arity; i++) {
            parameters.add(LPCType.LPCMIXED);
        }
        return efun("jvmud_capture_session_input", LPCType.LPCVOID, parameters,
                (runtime, args) -> {
                    Object[] extraArgs = new Object[Math.max(0, args.length - 2)];
                    if (extraArgs.length > 0) {
                        System.arraycopy(args, 2, extraArgs, 0, extraArgs.length);
                    }
                    runtime.captureSessionInput(
                            String.valueOf(args[0]),
                            (((Number) args[1]).intValue() & 1) != 0,
                            extraArgs);
                    return null;
                });
    }

    private static Efun invokeLpcObjectEfun(int arity) {
        List<LPCType> parameters = new ArrayList<>();
        parameters.add(LPCType.LPCMIXED);
        parameters.add(LPCType.LPCSTRING);
        for (int i = 2; i < arity; i++) {
            parameters.add(LPCType.LPCMIXED);
        }
        return efun("jvmud_invoke_lpc_object", LPCType.LPCMIXED, parameters,
                (runtime, args) -> invokeLpcObject(runtime, args));
    }

    private static Efun applyCallableEfun(int arity) {
        return efun("jvmud_apply_callable", LPCType.LPCMIXED, Collections.nCopies(arity, LPCType.LPCMIXED),
                (runtime, args) -> applyCallable(runtime, args));
    }

    private static Object applyCallable(RuntimeContext runtime, Object[] args) {
        if (args.length == 0 || !(args[0] instanceof RuntimeCallable callback)) {
            return 0;
        }
        List<Object> invocationArgs = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            if (i == args.length - 1 && args[i] instanceof List<?> finalArray) {
                invocationArgs.addAll(finalArray);
            } else {
                invocationArgs.add(args[i]);
            }
        }
        return callback.call(runtime, invocationArgs.toArray());
    }

    private static Object invokeLpcObject(RuntimeContext runtime, Object[] args) {
        Object[] invocationArgs = new Object[Math.max(0, args.length - 2)];
        System.arraycopy(args, 2, invocationArgs, 0, invocationArgs.length);
        return invokeLpcObject(runtime, args[0], String.valueOf(args[1]), invocationArgs);
    }

    private static Object invokeLpcObject(RuntimeContext runtime, Object target, String methodName, Object... arguments) {
        Object resolvedTarget = resolveTarget(runtime, target);
        if (resolvedTarget == null) {
            return 0;
        }

        if (arguments.length == 0) {
            return runtime.invokeOptionalObject(resolvedTarget, methodName);
        }

        if (arguments.length == 1 && isNoArgumentSentinel(arguments[0])) {
            if (hasMethod(resolvedTarget, methodName, 0) && !hasMethod(resolvedTarget, methodName, 1)) {
                return runtime.invokeOptionalObject(resolvedTarget, methodName);
            }
        }
        return runtime.invokeOptionalObject(resolvedTarget, methodName, arguments);
    }

    private static boolean hasMethod(Object target, String methodName, int arity) {
        for (var method : target.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == arity) {
                return true;
            }
        }
        return false;
    }

    private static Object resolveTarget(RuntimeContext runtime, Object target) {
        if (target instanceof String path) {
            try {
                return runtime.loadOrGetObject(stripLeadingSlash(path));
            } catch (RuntimeException e) {
                return null;
            }
        }
        return target;
    }

    private static boolean isNoArgumentSentinel(Object argument) {
        return argument == null || Integer.valueOf(0).equals(argument);
    }

    private static Efun sscanfEfun(int arity) {
        return efun("jvmud_sscanf", LPCType.LPCINT, Collections.nCopies(arity, LPCType.LPCMIXED),
                (runtime, args) -> RuntimeScanf.scan(args[0], args[1], args.length - 2)[0]);
    }

    private static String stripLeadingSlash(String path) {
        String stripped = path;
        while (stripped.startsWith("/")) {
            stripped = stripped.substring(1);
        }
        return stripped;
    }

    private static String stripExtension(String path) {
        if (path.endsWith(".c")) {
            return path.substring(0, path.length() - 2);
        }
        return path;
    }

    /** Converts an LPC value to text while preserving Java {@code null} for omitted varargs. */
    private static String nullableText(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Efun efun(String name, LPCType returnType, List<LPCType> parameters, EfunBody body) {
        return new Efun() {
            @Override
            public EfunSignature signature() {
                return new EfunSignature(new Symbol(returnType, name), parameters);
            }

            @Override
            public Object call(RuntimeContext context, Object[] args) {
                return body.call(context, args);
            }
        };
    }

    @FunctionalInterface
    private interface EfunBody {
        Object call(RuntimeContext context, Object[] args);
    }
}
