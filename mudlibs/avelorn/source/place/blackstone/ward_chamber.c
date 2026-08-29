void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("restore_ward", "restore");
}
string short() { return "Blackstone Ward Chamber"; }
void describe(object viewer) {
  jvmud_write("Blackstone Ward Chamber\n");
  jvmud_write("A waist-high basalt heartstone stands within rings of copper and silver. ");
  jvmud_write("Its western channel points toward Ashenwatch Keep; cleaning and aligning ");
  jvmud_write("it may restore one link in the failing Lantern Crown.\n\n");
  jvmud_write("The threshold is south. Type restore to renew the wardstone.\n");
}
int south(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "south", "place/blackstone/wardwork_threshold"); }
int restore_ward(mixed ignored) {
  jvmud_write("You clear the channels, align the silver ring, and kindle a steady blue pulse.\n");
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "record_quest_action", "blackstone-wardstone");
}
