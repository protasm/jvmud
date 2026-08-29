void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("north", "north");
  jvmud_add_action("north", "n");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
  jvmud_add_action("south", "south");
  jvmud_add_action("south", "s");
}

string short() {
  return "Greyhaven Market Cross";
}

void describe(object viewer) {
  jvmud_write("Greyhaven Market Cross\n");
  jvmud_write("Four paved streets meet beneath a roofed market cross. Guild wardens ");
  jvmud_write("settle stall positions from a public slate while town clerks collect ");
  jvmud_write("modest dues for paving, fire cisterns, and night watch wages.\n\n");
  jvmud_write("Gate Square is west, Heron Fountain north, Guild Row east, and the quay south.\n");
}

int west(mixed ignored) { return travel("west", "place/greyhaven/gate_square"); }
int north(mixed ignored) { return travel("north", "place/greyhaven/heron_fountain"); }
int east(mixed ignored) { return travel("east", "place/greyhaven/guild_row"); }
int south(mixed ignored) { return travel("south", "place/greyhaven/river_quay"); }

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
