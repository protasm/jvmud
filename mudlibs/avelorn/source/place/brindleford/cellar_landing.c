void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("up", "up");
  jvmud_add_action("up", "u");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
}

string short() {
  return "Mill Cellar Landing";
}

void describe(object viewer) {
  jvmud_write("Mill Cellar Landing\n");
  jvmud_write("Cool air rises from vaulted riverstone chambers beneath the mill. ");
  jvmud_write("Fresh gnaw marks mar the grain-room door, while the pump room smells ");
  jvmud_write("of clean water and lamp oil.\n\n");
  jvmud_write("The mill yard is up, the grain cellar east, and the pump room west.\n");
}

int up(mixed ignored) {
  return travel("up", "place/brindleford/mill_yard");
}

int east(mixed ignored) {
  return travel("east", "place/brindleford/grain_cellar");
}

int west(mixed ignored) {
  return travel("west", "place/brindleford/pump_room");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
