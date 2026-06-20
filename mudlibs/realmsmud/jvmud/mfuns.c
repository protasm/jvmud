void call_out(string method, int delay) {
    jvmud_schedule_deferred_callback(method, delay);
}

void call_out(string method, int delay, mixed arg) {
    jvmud_schedule_deferred_callback(method, delay, arg);
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

object getService(string service) {
    return load_object("/secure/simul_efun.c")->getService(service);
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

string RealmsDatabase() {
    return "RealmsLib";
}

int sizeof(mixed value) {
    return jvmud_size(value);
}

object this_object() {
    return jvmud_current_lpc_object();
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
