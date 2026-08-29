void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
}
string short() { return "Blackstone Upland Trail"; }
void describe(object viewer) {
  write("Blackstone Upland Trail\n");
  write("A flagged trail climbs through heather and dark basalt. Boundary cairns ");
  write("bear both village grazing marks and royal ward sigils, overlapping rights ");
  write("kept legible by generations of cooperative survey.\n\n");
  write("Merewatch is south, standing stones north, a shepherd hut east, and reed shrine west.\n");
}
int south(mixed ignored) { return travel("south", "place/merewatch/upland_gate"); }
int north(mixed ignored) { return travel("north", "place/blackstone/standing_stones"); }
int east(mixed ignored) { return travel("east", "place/blackstone/shepherd_hut"); }
int west(mixed ignored) { return travel("west", "place/merewatch/reed_shrine"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
