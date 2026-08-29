void initialize(mixed first_load) {
}
void offer_interactions() { jvmud_add_action("west", "west"); jvmud_add_action("west", "w"); }
string short() { return "Greyhaven Smith Lane"; }
void describe(object viewer) {
  jvmud_write("Greyhaven Smith Lane\n");
  jvmud_write("Forge doors stand open beneath chimneys fitted with spark screens. ");
  jvmud_write("The smiths share a quenching cistern and night fire-watch, practical ");
  jvmud_write("guild rules that protect workshops and neighboring homes alike.\n\n");
  jvmud_write("Company Hall is west.\n");
}
int west(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "west", "place/greyhaven/company_hall");
}
