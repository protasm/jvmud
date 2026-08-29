void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
}
string short() { return "Merewatch South Gate"; }
void describe(object viewer) {
  jvmud_write("Merewatch South Gate\n");
  jvmud_write("A low gatehouse of lake-grey stone opens onto raised causeways. Reed ");
  jvmud_write("wardens exchange reports with Crown riders here, coordinating flood, ");
  jvmud_write("ferry, and upland patrol duties across village boundaries.\n\n");
  jvmud_write("The Greyhaven road is south, and Merewatch Square is north.\n");
}
int south(mixed ignored) { return travel("south", "place/north_road/merewatch_road"); }
int north(mixed ignored) { return travel("north", "place/merewatch/mere_square"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
