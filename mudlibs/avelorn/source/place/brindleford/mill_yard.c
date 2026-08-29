void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("south", "south");
  jvmud_add_action("south", "s");
  jvmud_add_action("down", "down");
  jvmud_add_action("down", "d");
}

string short() {
  return "Halward Mill Yard";
}

void describe(object viewer) {
  jvmud_write("Halward Mill Yard\n");
  jvmud_write("A broad waterwheel turns beside a well-kept granary and millhouse. ");
  jvmud_write("Chalked delivery tallies show grain due to village households, the ");
  jvmud_write("Crown reserve, and the winter poor-box. A cellar door stands open.\n\n");
  jvmud_write("Mill Road is south. Stone steps lead down into the mill cellar.\n");
}

int south(mixed ignored) {
  return travel("south", "place/brindleford/mill_road");
}

int down(mixed ignored) {
  return travel("down", "place/brindleford/cellar_landing");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
