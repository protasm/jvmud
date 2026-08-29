void initialize(mixed first_load) {
}
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
}
string short() { return "North Road Patrol Crossing"; }
void describe(object viewer) {
  jvmud_write("North Road Patrol Crossing\n");
  jvmud_write("A paved military spur meets the Merewatch road beside a roofed muster ");
  jvmud_write("stone. Shepherds leave chalk weather reports for roadwardens, who answer ");
  jvmud_write("with patrol times and notices of safe crossings.\n\n");
  jvmud_write("Greyhaven is south, the abandoned patrol post north, and shepherd fields east.\n");
}
int south(mixed ignored) { return travel("south", "place/greyhaven/north_gate"); }
int north(mixed ignored) { return travel("north", "place/north_road/abandoned_post"); }
int east(mixed ignored) { return travel("east", "place/north_road/shepherd_fields"); }
int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
