void initialize(mixed first_load) { }
void offer_interactions() { jvmud_add_action("south", "south"); jvmud_add_action("south", "s"); jvmud_add_action("rekindle_lantern", "rekindle"); }
string short() { return "The Western Crown Lantern"; }
void describe(object viewer) {
  write("The Western Crown Lantern\n");
  write("A crystal lantern as tall as a person stands above seven silver channels. ");
  write("Each bears the offered mark of a different order of Avelorn's society; ");
  write("together they can carry one protective flame across the western realm.\n\n");
  write("The lantern crypt is south. Type rekindle to restore the Crown Lantern.\n");
}
int south(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "south", "place/ashenwatch/lantern_crypt"); }
int rekindle_lantern(mixed ignored) {
  write("You join the seven silver channels and kindle a clear blue flame.\n");
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "record_quest_action", "ashenwatch-crown-lantern");
}
