/*
* Mudlib functions made globally available through JVMud's mfun boundary.
*
* This object keeps LP-style compatibility spellings in the mudlib instead of
* promoting them to JVMud engine functions.
*/
void add_action(string method) {
  jvmud_add_action(method);
}

void add_action(string method, string verb) {
  jvmud_add_action(method, verb);
}

void add_verb(string verb) {
  jvmud_add_verb(verb);
}

mixed call_other(mixed target, string method) {
  return jvmud_invoke_object(target, method);
}

mixed call_other(mixed target, string method, mixed arg) {
  return jvmud_invoke_object(target, method, arg);
}

void call_out(string method, int delay) {
  jvmud_call_out(method, delay);
}

void call_out(string method, int delay, mixed arg) {
  jvmud_call_out(method, delay, arg);
}

int cat(string path) {
  mixed text;

  text = jvmud_read_mudlib_text(path);
  if (!stringp(text))
    return 0;

  write(text);
  return 1;
}

int cat(string path, int line, int count) {
  return cat(path);
}

object clone_object(string path) {
  return jvmud_clone_object(path);
}

string capitalize(mixed value) {
  return jvmud_capitalize_text(value);
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

mixed creator(mixed ob) {
  return 0;
}

void destruct(object ob) {
  jvmud_destruct(ob);
}

void enable_commands() {
  jvmud_enable_commands();
}

void input_to(string method) {
  jvmud_capture_session_input(method, 0);
}

void input_to(string method, int noecho) {
  jvmud_capture_session_input(method, noecho);
}

object environment() {
  return jvmud_environment();
}

object environment(mixed ob) {
  return jvmud_environment(ob);
}

string file_name(mixed ob) {
  return jvmud_object_name(ob);
}

object first_inventory(mixed container) {
  return jvmud_first_inventory(container);
}

string jvmud_mfun_status() {
  return "ok";
}

status living(mixed ob) {
  return 0;
}

string lower_case(mixed value) {
  return jvmud_lowercase_text(value);
}

void move_object(mixed ob, mixed destination) {
  jvmud_move_object(ob, destination);
}

object next_inventory(mixed ob) {
  return jvmud_next_inventory(ob);
}

status pointerp(mixed value) {
  return jvmud_is_array(value);
}

object present(string id) {
  return jvmud_present(id);
}

object present(mixed id, mixed container) {
  return jvmud_present(id, container);
}

string query_verb() {
  return jvmud_current_verb();
}

int query_idle(mixed player) {
  return jvmud_query_idle(player);
}

mixed query_ip_number(mixed player) {
  return jvmud_query_ip_number(player);
}

int remove_call_out(string method) {
  return jvmud_remove_call_out(method);
}

void say(mixed value) {
  jvmud_say(value);
}

void set_heart_beat(int enabled) {
  jvmud_set_heart_beat(enabled);
}

int set_light(int delta) {
  return jvmud_set_light(delta);
}

int sizeof(mixed value) {
  return jvmud_size(value);
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

status stringp(mixed value) {
  return jvmud_is_string(value);
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

int time() {
  return jvmud_time();
}

object *users() {
  return jvmud_users();
}

void write(mixed value) {
  jvmud_write(value);
}
