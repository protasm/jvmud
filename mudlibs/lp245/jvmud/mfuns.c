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

void add_action(string method, string verb, int flag) {
  jvmud_add_action(method, verb, flag);
}

void add_verb(string verb) {
  jvmud_add_verb(verb);
}

mixed call_other(mixed target, string method) {
  return jvmud_invoke_entity(target, method);
}

mixed call_other(mixed target, string method, mixed arg) {
  return jvmud_invoke_entity(target, method, arg);
}

mixed call_other(mixed target, string method, mixed arg1, mixed arg2) {
  return jvmud_invoke_entity(target, method, arg1, arg2);
}

mixed call_other(mixed target, string method, mixed arg1, mixed arg2, mixed arg3) {
  return jvmud_invoke_entity(target, method, arg1, arg2, arg3);
}

mixed call_other(mixed target, string method, mixed arg1, mixed arg2, mixed arg3, mixed arg4) {
  return jvmud_invoke_entity(target, method, arg1, arg2, arg3, arg4);
}

void call_out(string method, int delay) {
  jvmud_schedule_deferred_callback(method, delay);
}

void call_out(string method, int delay, mixed arg) {
  jvmud_schedule_deferred_callback(method, delay, arg);
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
  return jvmud_spawn_entity(path);
}

string capitalize(mixed value) {
  return jvmud_capitalize_text(value);
}

void add_worth(mixed value) {
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

string crypt(mixed value, mixed salt) {
  return "" + value;
}

string ctime(int timestamp) {
  return jvmud_format_time(timestamp);
}

mixed creator(mixed ob) {
  return 0;
}

mixed command(string command_line) {
  return jvmud_dispatch_entity_command(jvmud_current_actor(), command_line);
}

mixed command(string command_line, mixed actor) {
  return jvmud_dispatch_entity_command(actor, command_line);
}

void destruct(object ob) {
  jvmud_destroy_entity(ob);
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

void input_to(string method, int flags, string prompt) {
  jvmud_capture_session_input(method, flags);
}

object environment() {
  return jvmud_entity_location();
}

object environment(mixed ob) {
  return jvmud_entity_location(ob);
}

string extract(mixed value, int from) {
  return jvmud_extract_text(value, from);
}

string extract(mixed value, int from, int to) {
  return jvmud_extract_text(value, from, to);
}

string file_name(mixed ob) {
  return jvmud_entity_id(ob);
}

string object_name(mixed ob) {
  return jvmud_entity_id(ob);
}

object first_inventory(mixed container) {
  return jvmud_first_entity_at(container);
}

object find_player(mixed name) {
  return 0;
}

object find_living(mixed name) {
  return jvmud_find_entity_alias("living", name);
}

string jvmud_mfun_status() {
  return "ok";
}

status living(mixed ob) {
  return jvmud_entity_commands_enabled(ob);
}

string lower_case(mixed value) {
  return jvmud_lowercase_text(value);
}

void log_file(mixed file, mixed text) {
}

void move_object(mixed ob, mixed destination) {
  jvmud_move_entity(ob, destination);
}

object next_inventory(mixed ob) {
  return jvmud_next_entity_at(ob);
}

status pointerp(mixed value) {
  return jvmud_is_array(value);
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

int query_idle(mixed player) {
  return jvmud_query_idle(player);
}

mixed query_ip_number(mixed player) {
  return jvmud_query_ip_number(player);
}

mixed query_ip_number() {
  return jvmud_query_ip_number(jvmud_current_actor());
}

int random(int max) {
  return jvmud_random(max);
}

int remove_call_out(string method) {
  return jvmud_cancel_deferred_callback(method);
}

int restore_object(string path) {
  return jvmud_restore_lpc_object_state(path);
}

int save_object(string path) {
  return jvmud_save_lpc_object_state(path);
}

void say(mixed value) {
  jvmud_emit_perceivable(jvmud_current_actor(), value);
}

void say(mixed value, object excluded) {
  jvmud_emit_perceivable_except(jvmud_current_actor(), value, excluded);
}

void set_heart_beat(int enabled) {
  jvmud_schedule_recurring_tick(enabled, 0);
}

void set_heart_beat(int enabled, int interval_seconds) {
  jvmud_schedule_recurring_tick(enabled, interval_seconds);
}

int set_light(int delta) {
  return jvmud_set_light(delta);
}

void set_living_name(mixed name) {
  jvmud_bind_entity_alias(jvmud_current_entity(), "living", name);
}

int sizeof(mixed value) {
  return jvmud_size(value);
}

int strlen(mixed value) {
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
  jvmud_send_to_entity(target, value);
}

void tell_room(mixed room, mixed value) {
  jvmud_emit_perceivable_at(room, value);
}

int transfer(mixed ob, mixed destination) {
  jvmud_move_entity(ob, destination);
  return 0;
}

object this_object() {
  return jvmud_current_entity();
}

object this_player() {
  return jvmud_current_actor();
}

string version() {
  return "JVMud";
}

int valid_name(mixed name) {
  string lowered;

  if (!stringp(name))
    return 0;

  lowered = lower_case(name);
  if (strlen(lowered) < 2)
    return 0;

  return lowered == name;
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
