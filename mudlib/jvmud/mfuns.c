/*
 * Mudlib functions made globally available through JVMud's mfun boundary.
 *
 * This object keeps LP-style compatibility spellings in the mudlib instead of
 * promoting them to JVMud engine functions.
 */

string jvmud_mfun_status() {
    return "ok";
}

void write(mixed value) {
    jvmud_write(value);
}

void say(mixed value) {
    jvmud_say(value);
}

void tell_object(object target, mixed value) {
    jvmud_tell_object(target, value);
}

void tell_room(mixed room, mixed value) {
    jvmud_tell_room(room, value);
}

object this_object() {
    return jvmud_current_object();
}

object this_player() {
    return jvmud_current_actor();
}

string query_verb() {
    return jvmud_current_verb();
}

int time() {
    return jvmud_time();
}

string file_name(mixed ob) {
    return jvmud_object_name(ob);
}

int sizeof(mixed value) {
    return jvmud_size(value);
}

status stringp(mixed value) {
    return jvmud_is_string(value);
}

status pointerp(mixed value) {
    return jvmud_is_array(value);
}

mixed call_other(mixed target, string method) {
    return jvmud_invoke_object(target, method);
}

mixed call_other(mixed target, string method, mixed arg) {
    return jvmud_invoke_object(target, method, arg);
}

object clone_object(string path) {
    return jvmud_clone_object(path);
}

void move_object(mixed ob, mixed destination) {
    jvmud_move_object(ob, destination);
}

object present(string id) {
    return jvmud_present(id);
}

object present(mixed id, mixed container) {
    return jvmud_present(id, container);
}

object first_inventory(mixed container) {
    return jvmud_first_inventory(container);
}

object next_inventory(mixed ob) {
    return jvmud_next_inventory(ob);
}

int set_light(int delta) {
    return jvmud_set_light(delta);
}

void set_heart_beat(int enabled) {
    jvmud_set_heart_beat(enabled);
}

void call_out(string method, int delay) {
    jvmud_call_out(method, delay);
}

void call_out(string method, int delay, mixed arg) {
    jvmud_call_out(method, delay, arg);
}

int remove_call_out(string method) {
    return jvmud_remove_call_out(method);
}

void enable_commands() {
    jvmud_enable_commands();
}

void add_action(string method) {
    jvmud_add_action(method);
}

void add_action(string method, string verb) {
    jvmud_add_action(method, verb);
}

void add_verb(string verb) {
    jvmud_add_verb(verb);
}

object environment() {
    return jvmud_environment();
}

object environment(mixed ob) {
    return jvmud_environment(ob);
}

void destruct(object ob) {
    jvmud_destruct(ob);
}

status living(mixed ob) {
    return 0;
}

mixed *slice_array(mixed *arr, int from, int to) {
    mixed *result;

    result = {};
    while (from <= to) {
        result += { arr[from] };
        from += 1;
    }

    return result;
}

string convert_number(int n) {
    if (n == 0)
        return "no";
    if (n == 1)
        return "one";
    if (n == 2)
        return "two";
    if (n == 3)
        return "three";
    if (n == 4)
        return "four";
    if (n == 5)
        return "five";
    if (n == 6)
        return "six";
    if (n == 7)
        return "seven";
    if (n == 8)
        return "eight";
    if (n == 9)
        return "nine";
    return "lot of";
}
