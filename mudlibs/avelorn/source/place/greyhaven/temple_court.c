void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
  jvmud_add_action("rest", "rest");
}

string short() { return "Greyhaven Temple Court"; }

void describe(object viewer) {
  write("Greyhaven Temple Court\n");
  write("Hospice, school, shrine, and almshouse face a quiet cloister planted ");
  write("with rosemary. Several orders share the court under the Concord of Seven ");
  write("Lamps, each keeping its rites while pooling public duties.\n\n");
  write("Heron Fountain is south, and the Lantern Tower is north. You may rest here.\n");
}

int south(mixed ignored) { return travel("south", "place/greyhaven/heron_fountain"); }
int north(mixed ignored) { return travel("north", "place/greyhaven/lantern_tower"); }
int rest(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "rest_at_shrine"); }
int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
