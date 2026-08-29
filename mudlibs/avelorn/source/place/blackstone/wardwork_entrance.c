void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("down", "down"); jvmud_add_action("down", "d");
}
string short() { return "Blackstone Wardwork Entrance"; }
void describe(object viewer) {
  jvmud_write("Blackstone Wardwork Entrance\n");
  jvmud_write("A royal survey awning shelters steps descending beneath a split basalt ");
  jvmud_write("arch. Tools are stacked in order and every worker's slate is accounted ");
  jvmud_write("for; the danger below interrupted careful work, not a disorderly flight.\n\n");
  jvmud_write("The standing stones are south, and the wardworks descend below.\n");
}
int south(mixed ignored) { return travel("south", "place/blackstone/standing_stones"); }
int down(mixed ignored) { return travel("down", "place/blackstone/wardwork_threshold"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
