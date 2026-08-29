void initialize(mixed first_load) {
  object miller;

  if (!jvmud_find_entity("enid", jvmud_current_lpc_object())) {
    miller = jvmud_clone_lpc_object("npc/quest_giver");
    jvmud_invoke_lpc_object(
        miller,
        "configure",
        "Miller Enid Halward",
        "female",
        "holder of Brindleford's chartered mill",
        "millers-unwelcome-guests");
    jvmud_invoke_lpc_object(miller, "add_identity", "enid");
    jvmud_invoke_lpc_object(miller, "add_identity", "miller");
    jvmud_move_entity(miller, jvmud_current_lpc_object());
  }
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
  write("Halward Mill Yard\n");
  write("A broad waterwheel turns beside a well-kept granary and millhouse. ");
  write("Chalked delivery tallies show grain due to village households, the ");
  write("Crown reserve, and the winter poor-box. A cellar door stands open.\n\n");
  write("Miller Enid Halward waits beside the delivery ledger.\n");
  write("Mill Road is south. Stone steps lead down into the mill cellar.\n");
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
