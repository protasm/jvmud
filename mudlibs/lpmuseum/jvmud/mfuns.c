void add_action(string method) {
  jvmud_add_action(method);
}

void add_action(string method, string verb) {
  jvmud_add_action(method, verb);
}

void add_verb(string verb) {
  jvmud_add_verb(verb);
}

void bind_alias(object ob, string namespace, mixed alias) {
  jvmud_bind_entity_alias(ob, namespace, alias);
}

mixed call_other(mixed target, string method) {
  return jvmud_invoke_entity(target, method);
}

mixed call_other(mixed target, string method, mixed arg) {
  return jvmud_invoke_entity(target, method, arg);
}

void call_out(string method, int delay) {
  jvmud_call_out(method, delay);
}

void call_out(string method, int delay, mixed arg) {
  jvmud_call_out(method, delay, arg);
}

mixed command(string command_line) {
  return jvmud_dispatch_entity_command(jvmud_current_actor(), command_line);
}

object clone_object(string path) {
  return jvmud_spawn_entity(path);
}

string ctime(int timestamp) {
  return jvmud_format_time(timestamp);
}

void destruct(object ob) {
  jvmud_destroy_entity(ob);
}

void enable_commands() {
  jvmud_enable_commands();
}

object environment() {
  return jvmud_entity_location();
}

object environment(mixed ob) {
  return jvmud_entity_location(ob);
}

object first_inventory(mixed container) {
  return jvmud_first_entity_at(container);
}

object find_living(mixed name) {
  return jvmud_find_entity_alias("living", name);
}

int has_alias(mixed ob, string namespace) {
  return jvmud_entity_has_alias(ob, namespace);
}

void input_to(string method) {
  jvmud_capture_session_input(method, 0);
}

void input_to(string method, int noecho) {
  jvmud_capture_session_input(method, noecho);
}

int living(mixed ob) {
  return jvmud_entity_commands_enabled(ob);
}

string lower_case(mixed value) {
  return jvmud_lowercase_text(value);
}

string capitalize(mixed value) {
  return jvmud_capitalize_text(value);
}

void move_object(mixed ob, mixed destination) {
  jvmud_move_entity(ob, destination);
}

object next_inventory(mixed ob) {
  return jvmud_next_entity_at(ob);
}

string object_name(mixed ob) {
  return jvmud_entity_id(ob);
}

int pointerp(mixed value) {
  return jvmud_is_array(value);
}

object present(mixed id) {
  return jvmud_find_entity(id);
}

object present(mixed id, mixed container) {
  return jvmud_find_entity(id, container);
}

mixed query_ip_number(mixed player) {
  return jvmud_query_ip_number(player);
}

int query_idle(mixed player) {
  return jvmud_query_idle(player);
}

string query_verb() {
  return jvmud_current_verb();
}

int random(int max) {
  return jvmud_random(max);
}

mixed read_file(string path) {
  return jvmud_read_mudlib_text(path);
}

void remove_call_out(string method) {
  jvmud_remove_call_out(method);
}

void say(mixed value) {
  jvmud_emit_perceivable(jvmud_current_actor(), value);
}

void set_heart_beat(int enabled) {
  jvmud_schedule_recurring_tick(enabled, 0);
}

int set_light(int amount) {
  return jvmud_set_light(amount);
}

int sizeof(mixed value) {
  return jvmud_size(value);
}

int stringp(mixed value) {
  return jvmud_is_string(value);
}

int strlen(mixed value) {
  return jvmud_size(value);
}

void tell_object(object target, mixed value) {
  jvmud_send_to_entity(target, value);
}

void tell_place(mixed place, mixed value) {
  jvmud_emit_perceivable_at(place, value);
}

object this_object() {
  return jvmud_current_entity();
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
