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
  return jvmud_invoke_entity(target, method);
}

mixed call_other(mixed target, string method, mixed arg) {
  return jvmud_invoke_entity(target, method, arg);
}

void enable_commands() {
  jvmud_enable_commands();
}

object this_player() {
  return jvmud_current_actor();
}

int transfer_player_to_game(string game_id) {
  return jvmud_transfer_player_to_game(game_id);
}

void write(mixed value) {
  jvmud_write(value);
}
