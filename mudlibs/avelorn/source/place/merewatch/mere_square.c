void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
}
string short() { return "Merewatch Square"; }
void describe(object viewer) {
  jvmud_write("Merewatch Square\n");
  jvmud_write("Timber galleries overlook a cobbled square built above the spring flood ");
  jvmud_write("line. Fishers, peat cutters, shepherds, and royal wardens share the same ");
  jvmud_write("weather board and emergency stores.\n\n");
  jvmud_write("The south gate is south, upland gate north, lakeside west, and warden hall east.\n");
}
int south(mixed ignored) { return travel("south", "place/merewatch/south_gate"); }
int north(mixed ignored) { return travel("north", "place/merewatch/upland_gate"); }
int west(mixed ignored) { return travel("west", "place/merewatch/lakeside"); }
int east(mixed ignored) { return travel("east", "place/merewatch/warden_hall"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
