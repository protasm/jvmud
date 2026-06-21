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

mixed *allocate(int size) {
  return jvmud_allocate(size);
}

mixed call_other(mixed target, string method) {
  return jvmud_invoke_lpc_object(target, method);
}

mixed call_other(mixed target, string method, mixed arg) {
  return jvmud_invoke_lpc_object(target, method, arg);
}

mixed call_other(mixed target, string method, mixed arg1, mixed arg2) {
  return jvmud_invoke_lpc_object(target, method, arg1, arg2);
}

mixed call_other(mixed target, string method, mixed arg1, mixed arg2, mixed arg3) {
  return jvmud_invoke_lpc_object(target, method, arg1, arg2, arg3);
}

mixed call_other(mixed target, string method, mixed arg1, mixed arg2, mixed arg3, mixed arg4) {
  return jvmud_invoke_lpc_object(target, method, arg1, arg2, arg3, arg4);
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
  return jvmud_clone_lpc_object(path);
}

string capitalize(mixed value) {
  return jvmud_capitalize_text(value);
}

void add_worth(mixed value) {
}

string clear_bit(string flags, int bit) {
  return flags ? flags : "";
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
  jvmud_destroy_lpc_object(ob);
}

int ed() {
  return 0;
}

int ed(string path) {
  return 0;
}

int ed(string path, string callback) {
  return 0;
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
  return jvmud_lpc_object_id(ob);
}

int file_size(string path) {
  return 1;
}

mixed *filter_objects(mixed *values, string method) {
  return values;
}

mixed *filter_objects(mixed *values, string method, mixed arg) {
  return values;
}

string object_name(mixed ob) {
  return jvmud_lpc_object_id(ob);
}

object first_inventory(mixed container) {
  return jvmud_first_entity_at(container);
}

mixed find_object(string path) {
  return jvmud_find_object(path);
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

status intp(mixed value) {
  return jvmud_is_int(value);
}

string lower_case(mixed value) {
  return jvmud_lowercase_text(value);
}

void log_file(mixed file, mixed text) {
  jvmud_append_mudlib_text("/log/" + file, text);
}

int localcmd() {
  return 0;
}

object load_object(string path) {
  return jvmud_load_lpc_object(path);
}

void ls(string path) {
  mixed *entries;
  int i;

  entries = jvmud_list_mudlib_paths(path);

  for (i = 0; i < sizeof(entries); i++)
    write(entries[i] + "\n");
}

int mkdir(string path) {
  return 0;
}

void move_object(mixed ob, mixed destination) {
  jvmud_move_entity(ob, destination);
}

object next_inventory(mixed ob) {
  return jvmud_next_entity_at(ob);
}

status objectp(mixed value) {
  return jvmud_is_object(value);
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

object previous_object() {
  return jvmud_previous_lpc_object();
}

string query_verb() {
  return jvmud_current_verb();
}

int query_idle(mixed player) {
  return jvmud_query_idle(player);
}

mixed query_ip_name(mixed player) {
  return jvmud_query_ip_number(player);
}

mixed query_ip_number(mixed player) {
  return jvmud_query_ip_number(player);
}

mixed query_ip_number() {
  return jvmud_query_ip_number(jvmud_current_actor());
}

mixed query_snoop(mixed player) {
  return 0;
}

string query_load_average() {
  return "";
}

int random(int max) {
  return jvmud_random(max);
}

int remove_call_out(string method) {
  return jvmud_cancel_deferred_callback(method);
}

int rm(string path) {
  return 0;
}

int rmdir(string path) {
  return 0;
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

string set_bit(string flags, int bit) {
  return flags ? flags : "";
}

int set_light(int delta) {
  return jvmud_set_light(delta);
}

void set_living_name(mixed name) {
  jvmud_bind_entity_alias(jvmud_current_lpc_object(), "living", name);
}

int sizeof(mixed value) {
  return jvmud_size(value);
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

int strlen(mixed value) {
  return jvmud_size(value);
}

void shout(mixed value) {
  jvmud_write(value);
}

void shutdown() {
  jvmud_shutdown();
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

mixed snoop() {
  return 0;
}

mixed snoop(mixed target) {
  return 0;
}

mixed snoop(mixed snooper, mixed target) {
  return 0;
}

void tail(string path) {
  cat(path);
}

int test_bit(string flags, int bit) {
  return 0;
}

void tell_object(object target, mixed value) {
  jvmud_write_to_lpc_object(target, value);
}

void tell_room(mixed room, mixed value) {
  jvmud_emit_perceivable_at(room, value);
}

int transfer(mixed ob, mixed destination) {
  jvmud_move_entity(ob, destination);

  return 0;
}

void wizlist() {
}

void wizlist(string name) {
}

object this_object() {
  return jvmud_current_lpc_object();
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
