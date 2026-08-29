void initialize(mixed first_load) {
}
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
}
string short() { return "Greyhaven North Gate"; }
void describe(object viewer) {
  jvmud_write("Greyhaven North Gate\n");
  jvmud_write("The northern arch faces Merewatch and the Blackstone uplands. A bell ");
  jvmud_write("board lists each patrol safely returned—except one road post whose brass ");
  jvmud_write("marker remains troublingly silent.\n\n");
  jvmud_write("The watch barracks is south, and the north road begins north.\n");
}
int south(mixed ignored) { return travel("south", "place/greyhaven/watch_barracks"); }
int north(mixed ignored) { return travel("north", "place/north_road/patrol_crossing"); }
int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
