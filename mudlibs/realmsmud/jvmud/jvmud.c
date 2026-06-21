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

mixed call_direct(mixed target, string method) {
    return jvmud_invoke_lpc_object(target, method);
}

mixed call_direct(mixed target, string method, mixed arg1) {
    return jvmud_invoke_lpc_object(target, method, arg1);
}

mixed call_direct(mixed target, string method, mixed arg1, mixed arg2) {
    return jvmud_invoke_lpc_object(target, method, arg1, arg2);
}

mixed call_direct(mixed target, string method, mixed arg1, mixed arg2, mixed arg3) {
    return jvmud_invoke_lpc_object(target, method, arg1, arg2, arg3);
}

mixed call_direct(mixed target, string method, mixed arg1, mixed arg2, mixed arg3,
    mixed arg4) {
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

string ctime(int epochSeconds) {
    return jvmud_format_time(epochSeconds);
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

string format(mixed text) {
    return jvmud_wrap_text(text);
}

string format(mixed text, int width) {
    return jvmud_wrap_text(text, width);
}

mixed *functionlist(mixed ob) {
    return ({ });
}

mixed *functionlist(mixed ob, int flags) {
    return ({ });
}

object getService(string service) {
    return load_object("/secure/simul_efun.c")->getService(service);
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

int notify_fail(mixed message) {
    return 0;
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

string regreplace(string input, string pattern, string replacement) {
    return jvmud_regex_replace(input, pattern, replacement, 0);
}

string regreplace(string input, string pattern, string replacement, int flags) {
    return jvmud_regex_replace(input, pattern, replacement, flags);
}

int sizeof(mixed value) {
    return jvmud_size(value);
}

object this_object() {
    return jvmud_current_lpc_object();
}

int time() {
    return jvmud_time();
}

string version() {
    return "JVMud RealmsMUD LDMud compatibility";
}

void write(mixed message) {
    jvmud_write(message);
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
