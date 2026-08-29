void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
}
string short() { return "Merewatch South Gate"; }
void describe(object viewer) {
  write("Merewatch South Gate\n");
  write("A low gatehouse of lake-grey stone opens onto raised causeways. Reed ");
  write("wardens exchange reports with Crown riders here, coordinating flood, ");
  write("ferry, and upland patrol duties across village boundaries.\n\n");
  write("The Greyhaven road is south, and Merewatch Square is north.\n");
}
int south(mixed ignored) { return travel("south", "place/north_road/merewatch_road"); }
int north(mixed ignored) { return travel("north", "place/merewatch/mere_square"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
