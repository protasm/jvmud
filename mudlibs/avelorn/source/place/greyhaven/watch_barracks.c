void initialize(mixed first_load) {
  object captain;

  if (!jvmud_find_entity("ilyra", jvmud_current_lpc_object())) {
    captain = jvmud_clone_lpc_object("npc/quest_giver");
    jvmud_invoke_lpc_object(
        captain,
        "configure",
        "Watch-Captain Ilyra Venn",
        "non-binary",
        "captain of Greyhaven's Crown watch",
        "silent-patrol-bell");
    jvmud_invoke_lpc_object(captain, "add_identity", "ilyra");
    jvmud_invoke_lpc_object(captain, "add_identity", "captain");
    jvmud_move_entity(captain, jvmud_current_lpc_object());
  }
}
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
}
string short() { return "Greyhaven Watch Barracks"; }
void describe(object viewer) {
  jvmud_write("Greyhaven Watch Barracks\n");
  jvmud_write("A clean drill yard adjoins offices for patrol rosters, lost property, ");
  jvmud_write("and public complaints. Town constables and Crown roadwardens train ");
  jvmud_write("together here, preserving clear duties without rivalry.\n\n");
  jvmud_write("Watch-Captain Ilyra Venn studies a silent patrol roster.\n");
  jvmud_write("Company Hall is south, and the north gate is north.\n");
}
int south(mixed ignored) { return travel("south", "place/greyhaven/company_hall"); }
int north(mixed ignored) { return travel("north", "place/greyhaven/north_gate"); }
int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
