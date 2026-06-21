void call_out(string method, int delay) {
    jvmud_schedule_deferred_callback(method, delay);
}

void call_out(string method, int delay, mixed arg) {
    jvmud_schedule_deferred_callback(method, delay, arg);
}

void call_out(string method, int delay, mixed arg1, mixed arg2) {
    jvmud_schedule_deferred_callback(method, delay, arg1, arg2);
}

void call_out(string method, int delay, mixed arg1, mixed arg2, mixed arg3) {
    jvmud_schedule_deferred_callback(method, delay, arg1, arg2, arg3);
}

void call_out(string method, int delay, mixed arg1, mixed arg2, mixed arg3, mixed arg4) {
    jvmud_schedule_deferred_callback(method, delay, arg1, arg2, arg3, arg4);
}

object *all_inventory() {
    return all_inventory(this_object());
}

object *all_inventory(mixed container) {
    object *ret = ({ });
    object item = jvmud_first_entity_at(container);

    while (item) {
        ret += ({ item });
        item = jvmud_next_entity_at(item);
    }
    return ret;
}

object *deep_inventory(mixed container) {
    object *ret = ({ });
    object *items = all_inventory(container);

    foreach(object item in items) {
        ret += ({ item }) + deep_inventory(item);
    }
    return ret;
}

void add_action(string method) {
    jvmud_add_action(method);
}

void add_action(string method, string verb) {
    jvmud_add_action(method, verb);
}

void add_action(string method, string verb, int prefix) {
    jvmud_add_action(method, verb, prefix);
}

void enable_commands() {
    jvmud_enable_commands();
}

void disable_commands() {
    jvmud_configure_lpc_object(jvmud_current_lpc_object(), 0, 0);
}

int exec(object newObject, object oldObject) {
    int ret = jvmud_rebind_session_lpc_object(newObject, oldObject);
    return ret;
}

void addUser(object user) {
    load_object("/secure/simul_efun.c")->addUser(user);
}

mixed call_direct(mixed target, string method) {
    if (pointerp(target)) {
        mixed ret = 0;
        foreach(mixed ob in target) {
            ret = jvmud_invoke_lpc_object(ob, method);
        }
        return ret;
    }
    return jvmud_invoke_lpc_object(target, method);
}

mixed call_direct(mixed target, string method, mixed arg1) {
    if (pointerp(target)) {
        mixed ret = 0;
        foreach(mixed ob in target) {
            ret = jvmud_invoke_lpc_object(ob, method, arg1);
        }
        return ret;
    }
    return jvmud_invoke_lpc_object(target, method, arg1);
}

mixed call_direct(mixed target, string method, mixed arg1, mixed arg2) {
    if (pointerp(target)) {
        mixed ret = 0;
        foreach(mixed ob in target) {
            ret = jvmud_invoke_lpc_object(ob, method, arg1, arg2);
        }
        return ret;
    }
    return jvmud_invoke_lpc_object(target, method, arg1, arg2);
}

mixed call_direct(mixed target, string method, mixed arg1, mixed arg2, mixed arg3) {
    if (pointerp(target)) {
        mixed ret = 0;
        foreach(mixed ob in target) {
            ret = jvmud_invoke_lpc_object(ob, method, arg1, arg2, arg3);
        }
        return ret;
    }
    return jvmud_invoke_lpc_object(target, method, arg1, arg2, arg3);
}

mixed call_direct(mixed target, string method, mixed arg1, mixed arg2, mixed arg3,
    mixed arg4) {
    if (pointerp(target)) {
        mixed ret = 0;
        foreach(mixed ob in target) {
            ret = jvmud_invoke_lpc_object(ob, method, arg1, arg2, arg3, arg4);
        }
        return ret;
    }
    return jvmud_invoke_lpc_object(target, method, arg1, arg2, arg3, arg4);
}

mixed call_other(mixed target, string method) {
    return jvmud_invoke_lpc_object(target, method);
}

mixed call_other(mixed target, string method, mixed arg1) {
    return jvmud_invoke_lpc_object(target, method, arg1);
}

mixed call_other(mixed target, string method, mixed arg1, mixed arg2) {
    return jvmud_invoke_lpc_object(target, method, arg1, arg2);
}

mixed call_other(mixed target, string method, mixed arg1, mixed arg2, mixed arg3) {
    return jvmud_invoke_lpc_object(target, method, arg1, arg2, arg3);
}

mixed call_other(mixed target, string method, mixed arg1, mixed arg2, mixed arg3,
    mixed arg4) {
    return jvmud_invoke_lpc_object(target, method, arg1, arg2, arg3, arg4);
}

mixed command(string command_line) {
    return jvmud_dispatch_entity_command(jvmud_current_agent(), command_line);
}

mixed command(string command_line, mixed actor) {
    return jvmud_dispatch_entity_command(actor, command_line);
}

mixed *caller_stack() {
    return ({ });
}

string capitalizeAllWords(string stringToCapitalize) {
    string *words = explode(stringToCapitalize || "", " ");
    int size = sizeof(words);
    int i;

    for (i = 0; i < size; i++) {
        words[i] = capitalize(words[i]);
    }
    return implode(words, " ");
}

string ctime(int epochSeconds) {
    return jvmud_format_time(epochSeconds);
}

string ctime() {
    return ctime(time());
}

int clonep(mixed ob) {
    return objectp(ob) && (strstr(object_name(ob), "#") > -1);
}

void configureCharset(object player, string charset) {
}

int configure_interactive(object player, int key, mixed value) {
    return 1;
}

int canAccessDatabase(mixed ob) {
    return 1;
}

int db_close(int handle) {
    return load_object("/secure/simul_efun.c")->db_close(handle);
}

int db_connect(string database) {
    return load_object("/secure/simul_efun.c")->db_connect(database);
}

int db_connect(string database, string user, string password) {
    return load_object("/secure/simul_efun.c")->db_connect(database, user, password);
}

string db_conv_string(mixed value) {
    return jvmud_db_escape(value);
}

mixed db_error(int handle) {
    return jvmud_db_error(handle);
}

int db_exec(int handle, string sql) {
    return load_object("/secure/simul_efun.c")->db_exec(handle, sql);
}

void debug_message(mixed message) {
}

void debug_message(mixed message, int flags) {
}

mixed db_fetch(int handle) {
    return jvmud_db_fetch(handle);
}

int *db_handles() {
    return load_object("/secure/simul_efun.c")->db_handles();
}

int file_size(string path) {
    mixed text = jvmud_read_mudlib_text(path);
    return jvmud_is_string(text) ? jvmud_size(text) : -1;
}

object *filter_objects(object *objects, string method) {
    object *ret = ({ });

    foreach(object ob in objects) {
        if (jvmud_invoke_lpc_object(ob, method)) {
            ret += ({ ob });
        }
    }
    return ret;
}

object *filter_objects(object *objects, string method, mixed arg1) {
    object *ret = ({ });

    foreach(object ob in objects) {
        if (jvmud_invoke_lpc_object(ob, method, arg1)) {
            ret += ({ ob });
        }
    }
    return ret;
}

object *filter_objects(object *objects, string method, mixed arg1, mixed arg2) {
    object *ret = ({ });

    foreach(object ob in objects) {
        if (jvmud_invoke_lpc_object(ob, method, arg1, arg2)) {
            ret += ({ ob });
        }
    }
    return ret;
}

string format(mixed text) {
    return jvmud_wrap_text(text);
}

string format(mixed text, int width) {
    return jvmud_wrap_text(text, width);
}

mixed *functionlist(mixed ob) {
    return jvmud_lpc_object_methods(ob);
}

mixed *functionlist(mixed ob, int flags) {
    return jvmud_lpc_object_methods(ob);
}

object getService(string service) {
    return load_object("/secure/simul_efun.c")->getService(service);
}

object getModule(string service) {
    return getService(service);
}

string getuid() {
    return getuid(this_object());
}

string getuid(mixed ob) {
    string ret = object_name(ob);
    return ret || "jvmud";
}

string *inherit_list() {
    return inherit_list(this_object());
}

string *inherit_list(mixed ob) {
    return jvmud_inherited_programs(ob);
}

int intp(mixed value) {
    return jvmud_is_int(value);
}

int floatp(mixed value) {
    return !intp(value) && (to_string(value) == to_string(to_float(value)));
}

float log(float value) {
    return 0.0;
}

string implode(mixed *values, string delimiter) {
    string ret = "";
    int i;

    delimiter = delimiter || "";
    if (values) {
        for (i = 0; i < sizeof(values); i++) {
            if (i) {
                ret += delimiter;
            }
            ret += "" + values[i];
        }
    }
    return ret;
}

int living(mixed ob) {
    mixed *programs;

    if (!objectp(ob)) {
        return 0;
    }

    programs = inherit_list(ob);
    return (member(programs, "/lib/realizations/living.c") > -1) ||
        (member(programs, "/lib/realizations/player.c") > -1) ||
        (member(programs, "/lib/realizations/wizard.c") > -1) ||
        (member(programs, "/lib/realizations/monster.c") > -1) ||
        (member(programs, "/lib/realizations/henchman.c") > -1);
}

void input_to(string method) {
    jvmud_capture_session_input(method, 0);
}

void input_to(string method, int flags) {
    jvmud_capture_session_input(method, flags & 1);
}

void input_to(string method, int flags, mixed arg1) {
    jvmud_capture_session_input(method, flags & 1, arg1);
}

void input_to(string method, int flags, mixed arg1, mixed arg2) {
    jvmud_capture_session_input(method, flags & 1, arg1, arg2);
}

string program_name(mixed ob) {
    string ret = jvmud_lpc_object_id(ob);
    if (ret && sizeof(ret)) {
        if (ret[0] != '/') {
            ret = "/" + ret;
        }
        if ((sizeof(ret) < 2) || (ret[<2..] != ".c")) {
            ret += ".c";
        }
    }
    return ret;
}

object *players() {
    return load_object("/secure/simul_efun.c")->players();
}

void printf(string format) {
    jvmud_write(jvmud_format_text(format));
}

void printf(string format, mixed arg1) {
    jvmud_write(jvmud_format_text(format, arg1));
}

void printf(string format, mixed arg1, mixed arg2) {
    jvmud_write(jvmud_format_text(format, arg1, arg2));
}

void printf(string format, mixed arg1, mixed arg2, mixed arg3) {
    jvmud_write(jvmud_format_text(format, arg1, arg2, arg3));
}

void printf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4) {
    jvmud_write(jvmud_format_text(format, arg1, arg2, arg3, arg4));
}

void printf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5) {
    jvmud_write(jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5));
}

void printf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5, mixed arg6) {
    jvmud_write(jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5, arg6));
}

string RealmsDatabase() {
    return "RealmsLib";
}

int remove_call_out(string method) {
    return jvmud_cancel_deferred_callback(method);
}

void move_object(mixed ob, mixed destination) {
    jvmud_move_entity(ob, destination);
}

int mkdir(string path) {
    return 1;
}

int notify_fail(mixed message) {
    return jvmud_notify_fail(message);
}

string object_name(mixed ob) {
    return jvmud_lpc_object_id(ob);
}

object *wizards() {
    return load_object("/secure/simul_efun.c")->wizards();
}

object present(mixed id) {
    return jvmud_find_entity(id);
}

object present(mixed id, mixed container) {
    return jvmud_find_entity(id, container);
}

string query_verb() {
    return jvmud_current_verb();
}

string query_command() {
    return query_verb();
}

int query_idle(mixed user) {
    return jvmud_query_idle(user);
}

object present_clone(string blueprint) {
    return present_clone(blueprint, this_object());
}

object present_clone(string blueprint, mixed container) {
    string normalized = regreplace(blueprint || "", "^/", "", 1);
    normalized = regreplace(normalized, "\\.c$", "", 1);

    foreach(object item in all_inventory(container)) {
        string name = object_name(item);
        name = regreplace(name || "", "^/", "", 1);
        name = regreplace(name, "#[0-9]+$", "", 1);
        name = regreplace(name, "\\.c$", "", 1);

        if (name == normalized) {
            return item;
        }
    }
    return 0;
}

string regreplace(string input, string pattern, mixed replacement) {
    return jvmud_regex_replace(input, pattern, replacement, 0);
}

string regreplace(string input, string pattern, mixed replacement, int flags) {
    return jvmud_regex_replace(input, pattern, replacement, flags);
}

string *regexplode(string input, string pattern) {
    return jvmud_regex_explode(input, pattern);
}

int remove_action(int flags) {
    return jvmud_remove_action(flags);
}

int remove_action(int flags, mixed actor) {
    return jvmud_remove_action(flags, actor);
}

int sizeof(mixed value) {
    return jvmud_size(value);
}

int *rusage() {
    return ({ 0, 0 });
}

void say(mixed message) {
    jvmud_emit_perceivable_except(this_object(), message, this_object());
}

int set_heart_beat(int enabled) {
    jvmud_schedule_recurring_tick(enabled, 1);
    return enabled ? 1 : 0;
}

string StartLocation() {
    return "/areas/eledhel/southern-city/12x2.c";
}

object this_object() {
    return jvmud_current_lpc_object();
}

int time() {
    return jvmud_time();
}

int abs(int value) {
    return value < 0 ? -value : value;
}

int textWidth(mixed text) {
    return sizeof(regreplace(to_string(text), "(\x1b[^m]+m)", "", 1));
}

float to_float(mixed value) {
    return jvmud_to_int(value) * 1.0;
}

int unicodeIsSingleCharacter() {
    return 1;
}

void unshadow() {
}

string convertToTextOfLength(string text, int length) {
    text = text || "";
    return sizeof(text) > length ? text[0..(length - 1)] : text;
}

int isValidPersistenceObject(mixed persistence) {
    return objectp(persistence);
}

string getGuestName(object player) {
    return "guest";
}

object findPlayer(string name) {
    return 0;
}

object findLiving(string name) {
    return findPlayer(name);
}

int createWizard(string wizardName, string level) {
    return 1;
}

int demoteWizardToPlayer(string wizardName) {
    return 1;
}

mapping availableRoles() {
    return ([]);
}

int createRole(string newRole, string type) {
    return 1;
}

string addRoleToPlayer(object character, string newRole) {
    return newRole || "";
}

int removeRoleFromPlayer(object character, string role) {
    return 1;
}

int strstr(string haystack, string needle) {
    return member(haystack || "", needle || "");
}

int strstr(string haystack, string needle, int start) {
    string value = haystack || "";
    int found = member(value[start..], needle || "");
    return found < 0 ? -1 : found + start;
}

string version() {
    return "JVMud RealmsMUD LDMud compatibility";
}

mixed *sort_array(mixed *values, string method) {
    mixed *ret = values + ({ });
    int size = sizeof(ret);
    int i;
    int j;

    for (i = 0; i < size; i++) {
        for (j = i + 1; j < size; j++) {
            if (call_other(this_object(), method, ret[i], ret[j])) {
                mixed swap = ret[i];
                ret[i] = ret[j];
                ret[j] = swap;
            }
        }
    }
    return ret;
}

void set_driver_hook(int hook, mixed callback) {
    jvmud_set_driver_hook(hook, callback);
}

function unbound_lambda(mixed *parameters, mixed body) {
    return (: 0 :);
}

void write(mixed message) {
    jvmud_write(message);
}

void tell_room(mixed location, mixed message) {
    jvmud_emit_perceivable_at(location, message);
}

int write_file(string path, mixed text) {
    return jvmud_append_mudlib_text(path, text);
}

int write_file(string path, mixed text, int flags) {
    return jvmud_append_mudlib_text(path, text);
}

string sprintf(string format) {
    return jvmud_format_text(format);
}

string sprintf(string format, mixed arg1) {
    return jvmud_format_text(format, arg1);
}

string sprintf(string format, mixed arg1, mixed arg2) {
    return jvmud_format_text(format, arg1, arg2);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3) {
    return jvmud_format_text(format, arg1, arg2, arg3);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5, mixed arg6) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5, arg6);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5, mixed arg6, mixed arg7) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5, mixed arg6, mixed arg7, mixed arg8) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
        arg8);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5, mixed arg6, mixed arg7, mixed arg8, mixed arg9) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
        arg8, arg9);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5, mixed arg6, mixed arg7, mixed arg8, mixed arg9, mixed arg10) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
        arg8, arg9, arg10);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5, mixed arg6, mixed arg7, mixed arg8, mixed arg9, mixed arg10,
    mixed arg11) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
        arg8, arg9, arg10, arg11);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5, mixed arg6, mixed arg7, mixed arg8, mixed arg9, mixed arg10,
    mixed arg11, mixed arg12) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
        arg8, arg9, arg10, arg11, arg12);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5, mixed arg6, mixed arg7, mixed arg8, mixed arg9, mixed arg10,
    mixed arg11, mixed arg12, mixed arg13) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
        arg8, arg9, arg10, arg11, arg12, arg13);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5, mixed arg6, mixed arg7, mixed arg8, mixed arg9, mixed arg10,
    mixed arg11, mixed arg12, mixed arg13, mixed arg14) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
        arg8, arg9, arg10, arg11, arg12, arg13, arg14);
}

string sprintf(string format, mixed arg1, mixed arg2, mixed arg3, mixed arg4,
    mixed arg5, mixed arg6, mixed arg7, mixed arg8, mixed arg9, mixed arg10,
    mixed arg11, mixed arg12, mixed arg13, mixed arg14, mixed arg15) {
    return jvmud_format_text(format, arg1, arg2, arg3, arg4, arg5, arg6, arg7,
        arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15);
}

mapping filter_indices(mapping values, function callback) {
    return jvmud_filter_indices(values, callback);
}

mapping filter_indices(mapping values, function callback, mixed arg1) {
    return jvmud_filter_indices(values, callback, arg1);
}

mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2) {
    return jvmud_filter_indices(values, callback, arg1, arg2);
}

mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2,
    mixed arg3) {
    return jvmud_filter_indices(values, callback, arg1, arg2, arg3);
}

mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2,
    mixed arg3, mixed arg4) {
    return jvmud_filter_indices(values, callback, arg1, arg2, arg3, arg4);
}

mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2,
    mixed arg3, mixed arg4, mixed arg5) {
    return jvmud_filter_indices(values, callback, arg1, arg2, arg3, arg4, arg5);
}

mapping filter_indices(mapping values, function callback, mixed arg1, mixed arg2,
    mixed arg3, mixed arg4, mixed arg5, mixed arg6) {
    return jvmud_filter_indices(values, callback, arg1, arg2, arg3, arg4, arg5, arg6);
}

int sscanf(mixed input, mixed format, mixed capture1) {
    return jvmud_sscanf(input, format, capture1);
}

int sscanf(mixed input, mixed format, mixed capture1, mixed capture2) {
    return jvmud_sscanf(input, format, capture1, capture2);
}

int sscanf(mixed input, mixed format, mixed capture1, mixed capture2, mixed capture3) {
    return jvmud_sscanf(input, format, capture1, capture2, capture3);
}

int sscanf(mixed input, mixed format, mixed capture1, mixed capture2, mixed capture3,
    mixed capture4) {
    return jvmud_sscanf(input, format, capture1, capture2, capture3, capture4);
}

int sscanf(mixed input, mixed format, mixed capture1, mixed capture2, mixed capture3,
    mixed capture4, mixed capture5) {
    return jvmud_sscanf(input, format, capture1, capture2, capture3, capture4, capture5);
}

int sscanf(mixed input, mixed format, mixed capture1, mixed capture2, mixed capture3,
    mixed capture4, mixed capture5, mixed capture6) {
    return jvmud_sscanf(input, format, capture1, capture2, capture3, capture4, capture5,
        capture6);
}
