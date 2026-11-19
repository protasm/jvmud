/**
 * Engine service adapters behind the hosted LPC function vocabulary.
 *
 * <p>{@link io.github.protasm.jvmud.engine.instance.efun.EngineEfuns} composes language functions
 * with these services. Object, world, session, command, scheduling, file, database and host catalogs
 * keep their implementation and capability declarations together. Value-only operations belong to
 * {@link io.github.protasm.jvmud.language.efun.LanguageEfuns}; a mudlib controls legacy aliases.</p>
 *
 * <h2>LPCObjectEfuns</h2>
 * <ul>
 *   <li>{@code jvmud_current_lpc_object() : object} returns the currently executing LPC object.</li>
 *   <li>{@code jvmud_previous_lpc_object() : object} returns the previous LPC object in the current
 *       call chain, where one is available.</li>
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
 *   <li>{@code jvmud_destroy_lpc_object(object object) : void} destroys an LPC object and removes
 *       its runtime state.</li>
 *   <li>{@code jvmud_previous_object() : object} returns the previous LPC object on the runtime call
 *       stack.</li>
 *   <li>{@code jvmud_lpc_object_info(mixed object, int key) : mixed} returns LPC false for
 *       driver-specific object metadata JVMud does not expose.</li>
 *   <li>{@code jvmud_configure_lpc_object(mixed object, int key, mixed value) : status} bridges
 *       supported driver object configuration requests such as command enablement.</li>
 *   <li>{@code jvmud_find_lpc_object(string path) : mixed} returns an already loaded LPC object, or
 *       LPC false when the path is not loaded.</li>
 *   <li>{@code jvmud_to_lpc_object(mixed pathOrObject) : mixed} resolves an LPC object value or
 *       returns LPC false when no loaded object matches.</li>
 *   <li>{@code jvmud_is_object(mixed value) : status} reports whether a value is a live runtime
 *       object.</li>
 *   <li>{@code jvmud_invoke_lpc_object(mixed target, string methodName, mixed ...args) : mixed}
 *       invokes an optional method on an LPC object, a path-resolved shared object, or each object
 *       in an LPC array.</li>
 * </ul>
 *
 * <h2>WorldEfuns</h2>
 * <ul>
 *   <li>{@code jvmud_emit_world_event(mixed source, string kind, mixed content, string text) : void}
 *       delivers a structured observation to the source's location and neighboring entities.</li>
 *   <li>{@code jvmud_deliver_world_event(mixed target, string kind, mixed content, string text) : void}
 *       delivers a directed world observation, separate from private session output.</li>
 *   <li>{@code jvmud_emit_perceivable(mixed emitter, mixed message) : void} emits near an entity
 *       or path-resolved object.</li>
 *   <li>{@code jvmud_emit_perceivable_except(mixed emitter, mixed message, mixed excluded) : void}
 *       emits near an entity while suppressing one recipient.</li>
 *   <li>{@code jvmud_emit_perceivable_at(mixed location, mixed message) : void} emits at a
 *       location or path-resolved object.</li>
 *   <li>{@code jvmud_set_entity_location(mixed entity, mixed location) : void} sets a
 *       world-present entity's runtime location/containment without using a mudlib movement wrapper.</li>
 *   <li>{@code jvmud_move_entity(mixed entity, mixed destination) : void} moves an entity or
 *       path-resolved object.</li>
 *   <li>{@code jvmud_find_entity(mixed id) : object} searches for an entity in the default
 *       location scope.</li>
 *   <li>{@code jvmud_find_entity(mixed id, mixed location) : object} searches for an entity inside
 *       a location or container.</li>
 *   <li>{@code jvmud_entity_location() : object} returns the current object's location.</li>
 *   <li>{@code jvmud_entity_location(mixed entity) : object} returns another entity's location.</li>
 *   <li>{@code jvmud_first_entity_at(mixed location) : object} returns the first entity in a
 *       location or container.</li>
 *   <li>{@code jvmud_next_entity_at(mixed entity) : object} returns the next entity in the same
 *       inventory walk.</li>
 *   <li>{@code jvmud_bind_entity_alias(object entity, string alias, mixed location) : void} binds a
 *       lookup alias in a location scope.</li>
 *   <li>{@code jvmud_find_entity_alias(string alias, mixed location) : object} resolves an alias in
 *       a location scope.</li>
 *   <li>{@code jvmud_entity_has_alias(mixed entity, string alias) : status} reports whether an
 *       entity has an alias.</li>
 *   <li>{@code jvmud_set_entity_translucent(mixed entity, status translucent) : void} controls
 *       whether ambient perception such as light passes through an entity container. Entities are
 *       translucent by default.</li>
 *   <li>{@code jvmud_entity_translucent(mixed entity) : status} reports whether an entity is
 *       currently translucent.</li>
 *   <li>{@code jvmud_set_light(int delta) : int} adjusts the current runtime light level and
 *       returns the resulting value.</li>
 * </ul>
 *
 * <h2>SessionEfuns</h2>
 * <ul>
 *   <li>{@code jvmud_write(mixed message) : void} writes text to the current execution
 *       recipient.</li>
 *   <li>{@code jvmud_write_to_lpc_object(object target, mixed message) : void} writes text to the
 *       target object's bound session, when it has one.</li>
 *   <li>{@code jvmud_rebind_session_lpc_object(object newObject, object oldObject) : status}
 *       moves an interactive session binding from one LPC object to another.</li>
 *   <li>{@code jvmud_users() : array} returns the active user/player objects known to the runtime.</li>
 *   <li>{@code jvmud_find_player(string name) : object} finds an active interactive user/player
 *       by common mudlib name methods.</li>
 *   <li>{@code jvmud_interactive(mixed user) : status} reports whether an object is bound to an active
 *       session.</li>
 *   <li>{@code jvmud_interactive_info(mixed user, int key) : mixed} returns LPC false for
 *       driver-specific interactive metadata JVMud does not expose.</li>
 *   <li>{@code jvmud_query_idle(mixed user) : int} returns idle time for a user/player object.</li>
 *   <li>{@code jvmud_query_ip_number(mixed user) : mixed} returns the user's remote IP address, or
 *       the runtime's false/null value when unavailable.</li>
 *   <li>{@code jvmud_query_ip_name(mixed user) : mixed} returns the user's remote host address, or
 *       the runtime's false/null value when unavailable.</li>
 *   <li>{@code jvmud_capture_session_input(string methodName, int flags[, mixed ...args]) : void}
 *       routes the next session input line to a method on the current object, followed by any
 *       captured compatibility arguments.</li>
 *   <li>{@code jvmud_gmcp_enabled([mixed user]) : status} reports whether GMCP option 201 has
 *       been negotiated for the current or supplied interactive object.</li>
 *   <li>{@code jvmud_send_gmcp(string package[, mixed payload]) : status} JSON-encodes and sends
 *       a GMCP message to the current interactive object.</li>
 * </ul>
 *
 * <h2>CommandEfuns</h2>
 * <ul>
 *   <li>{@code jvmud_current_actor() : object} returns the active command actor, falling back to
 *       the current object outside command dispatch.</li>
 *   <li>{@code jvmud_current_agent() : object} returns the active world-present agent responsible
 *       for the current action scope, or LPC false when no agent is active.</li>
 *   <li>{@code jvmud_current_verb() : string} returns the verb being dispatched for the current
 *       command.</li>
 *   <li>{@code jvmud_configure_command_aliases(mixed aliases) : void} installs command alias
 *       rewrites selected by mudlib policy.</li>
 *   <li>{@code jvmud_notify_fail(mixed message) : int} records failure text for an unhandled
 *       command.</li>
 *   <li>{@code jvmud_dispatch_entity_command(mixed actor, string commandLine) : mixed} dispatches
 *       a command as an entity or path-resolved object.</li>
 *   <li>{@code jvmud_enable_commands() : void} enables command handling for the current object.</li>
 *   <li>{@code jvmud_add_action(string methodName) : void} remembers a method as a command
 *       action without registering a verb.</li>
 *   <li>{@code jvmud_add_action(string methodName, string verb) : void} remembers an action method
 *       and registers a verb.</li>
 *   <li>{@code jvmud_add_action(string methodName, string verb, status prefix) : void} registers
 *       a verb, optionally as a prefix verb.</li>
 *   <li>{@code jvmud_remove_action(int flags) : int} removes current handler actions from the
 *       active command actor.</li>
 *   <li>{@code jvmud_remove_action(int flags, mixed actor) : int} removes current handler actions
 *       from the supplied actor.</li>
 *   <li>{@code jvmud_add_verb(string verb) : void} registers a verb for the current interaction
 *       scope.</li>
 *   <li>{@code jvmud_entity_commands_enabled(mixed entity) : status} reports whether command
 *       handling is enabled for an entity.</li>
 *   <li>{@code jvmud_set_this_player(mixed player) : void} accepts legacy current-player mutation
 *       requests while JVMud keeps session ownership on the engine side.</li>
 * </ul>
 *
 * <h2>SchedulingEfuns</h2>
 * <ul>
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
 * <h2>FileEfuns</h2>
 * <ul>
 *   <li>{@code jvmud_save_lpc_object_state(string path) : status} persists the current object's LPC
 *       fields.</li>
 *   <li>{@code jvmud_restore_lpc_object_state(string path) : status} restores the current object's
 *       LPC fields.</li>
 *   <li>{@code jvmud_read_mudlib_text(string path[, int startLine, int lineCount]) : mixed} reads a
 *       mudlib-relative text file, optionally slicing lines for legacy {@code read_file} calls.</li>
 *   <li>{@code jvmud_read_mudlib_json(string path) : mixed} parses a mudlib-relative JSON file,
 *       transparently accepting gzip compression.</li>
 *   <li>{@code jvmud_read_mudlib_json_array(string path, string pointer, int offset, int count) : mixed}
 *       streams a bounded slice from an array selected by JSON Pointer.</li>
 *   <li>{@code jvmud_list_mudlib_paths(string path[, int flags]) : array} lists mudlib-relative
 *       file names, including simple glob support for compatibility file discovery.</li>
 *   <li>{@code jvmud_append_mudlib_text(string path, mixed text) : status} appends text to a
 *       mudlib-relative file.</li>
 *   <li>{@code jvmud_remove_mudlib_text(string path) : status} removes a mudlib-relative file.</li>
 *   <li>{@code jvmud_copy_mudlib_text(string source, string destination) : int} copies a
 *       mudlib-relative file, returning zero for success.</li>
 *   <li>{@code jvmud_rename_mudlib_text(string source, string destination) : int} renames a
 *       mudlib-relative path, returning zero for success.</li>
 *   <li>{@code jvmud_create_mudlib_directory(string path) : status} creates a mudlib-relative
 *       directory.</li>
 *   <li>{@code jvmud_remove_mudlib_directory(string path) : status} removes an empty
 *       mudlib-relative directory.</li>
 * </ul>
 *
 * <h2>DatabaseEfuns</h2>
 * <ul>
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
 * </ul>
 *
 * <h2>HostEfuns</h2>
 * <ul>
 *   <li>{@code jvmud_time() : int} returns Unix epoch seconds.</li>
 *   <li>{@code jvmud_format_time(int epochSeconds) : string} formats epoch seconds using the
 *       host's default time zone.</li>
 *   <li>{@code jvmud_random(int max) : int} returns a value in {@code [0, max)}, or {@code 0} when
 *       {@code max <= 0}.</li>
 *   <li>{@code jvmud_shutdown() : void} accepts legacy mudlib shutdown requests as a no-op.</li>
 *   <li>{@code jvmud_runtime_info(int key) : mixed} returns LPC false for unsupported runtime-info
 *       queries.</li>
 *   <li>{@code jvmud_hash_password(string password) : string} returns a PBKDF2-SHA256 password
 *       hash string.</li>
 *   <li>{@code jvmud_verify_password(string password, string encodedHash) : status} verifies a
 *       password against a hash created by {@code jvmud_hash_password}.</li>
 * </ul>
 */
package io.github.protasm.jvmud.engine.instance.efun;
