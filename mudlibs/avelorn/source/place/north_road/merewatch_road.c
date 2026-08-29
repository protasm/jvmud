void initialize(mixed first_load) {
}
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
}
string short() { return "Merewatch Road"; }
void describe(object viewer) {
  jvmud_write("Merewatch Road\n");
  jvmud_write("The road bends toward reed-bright lakes and the northern watchtowers. ");
  jvmud_write("Fresh wheel tracks show that trade continues under escorted schedules ");
  jvmud_write("while wardens investigate the western lantern failures.\n\n");
  jvmud_write("The patrol post is south, and Merewatch's south gate is north.\n");
}
int north(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "north", "place/merewatch/south_gate");
}
int south(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "south", "place/north_road/abandoned_post");
}
