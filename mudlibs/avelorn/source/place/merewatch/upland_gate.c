void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
}
string short() { return "Merewatch Upland Gate"; }
void describe(object viewer) {
  jvmud_write("Merewatch Upland Gate\n");
  jvmud_write("A stout palisade gate faces the Blackstone hills. The gate remains open ");
  jvmud_write("by day, with posted guidance asking travelers to carry ward lanterns and ");
  jvmud_write("report any soot-touched stone rather than concealing delays.\n\n");
  jvmud_write("Merewatch Square is south, and the upland trail is north.\n");
}
int south(mixed ignored) { return travel("south", "place/merewatch/mere_square"); }
int north(mixed ignored) { return travel("north", "place/blackstone/upland_trail"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
