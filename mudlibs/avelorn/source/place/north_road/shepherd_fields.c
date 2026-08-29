void initialize(mixed first_load) {
}
void offer_interactions() { jvmud_add_action("west", "west"); jvmud_add_action("west", "w"); }
string short() { return "Greyhaven Shepherd Fields"; }
void describe(object viewer) {
  jvmud_write("Greyhaven Shepherd Fields\n");
  jvmud_write("Low walls divide summer grazing without blocking the old drove paths. ");
  jvmud_write("Flocks from several villages mingle under agreed marks, watched by ");
  jvmud_write("families who rotate the upland duty.\n\n");
  jvmud_write("The patrol crossing is west.\n");
}
int west(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "west", "place/north_road/patrol_crossing");
}
