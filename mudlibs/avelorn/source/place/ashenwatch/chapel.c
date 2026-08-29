void initialize(mixed first_load) { }
void offer_interactions() { jvmud_add_action("north", "north"); jvmud_add_action("north", "n"); jvmud_add_action("rest", "rest"); }
string short() { return "Ashenwatch Garrison Chapel"; }
void describe(object viewer) {
  jvmud_write("Ashenwatch Garrison Chapel\n");
  jvmud_write("Seven stone lamps surround a plain altar. Expedition priests have ");
  jvmud_write("rekindled six from flames carried by different western communities, ");
  jvmud_write("leaving the seventh ready for the keep's restored Crown Lantern.\n\n");
  jvmud_write("The outer court is north. You may rest here.\n");
}
int north(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "north", "place/ashenwatch/outer_court"); }
int rest(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "rest_at_shrine"); }
