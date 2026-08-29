void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
}

string short() {
  return "Brindleford East Road";
}

void describe(object viewer) {
  jvmud_write("Brindleford East Road\n");
  jvmud_write("The royal road rises through barley fields toward an old stone bridge. ");
  jvmud_write("Drainage ditches, mile markers, and a roadside shelter show the quiet ");
  jvmud_write("coordination of village labor, Crown engineers, and traveling guilds.\n\n");
  jvmud_write("Mill Road is west, and the old bridge is east.\n");
}

int west(mixed ignored) {
  return travel("west", "place/brindleford/mill_road");
}

int east(mixed ignored) {
  return travel("east", "place/brindleford/old_bridge");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
