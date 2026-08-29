void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("down", "down"); jvmud_add_action("down", "d");
}
string short() { return "Ashenwatch Underkeep"; }
void describe(object viewer) {
  write("Ashenwatch Underkeep\n");
  write("Broad stairs descend beside reliefs showing Crown officers receiving ");
  write("lanterns from guild, temple, village, and city delegates. The old magic ");
  write("was built as a covenant of service, not a sovereign's private weapon.\n\n");
  write("The great hall is south, and the ward vault lies below.\n");
}
int south(mixed ignored) { return travel("south", "place/ashenwatch/great_hall"); }
int down(mixed ignored) { return travel("down", "place/ashenwatch/ward_antechamber"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
