void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
}
string short() { return "Blackstone Standing Stones"; }
void describe(object viewer) {
  jvmud_write("Blackstone Standing Stones\n");
  jvmud_write("Seven basalt pillars frame a view toward Ashenwatch. New silver survey ");
  jvmud_write("pins sit beside ancient lantern runes, evidence that modern craft and ");
  jvmud_write("old magic are being studied together rather than set at odds.\n\n");
  jvmud_write("The upland trail is south, the wardwork entrance north, and Ashenwatch west.\n");
}
int south(mixed ignored) { return travel("south", "place/blackstone/upland_trail"); }
int north(mixed ignored) { return travel("north", "place/blackstone/wardwork_entrance"); }
int west(mixed ignored) { return travel("west", "place/ashenwatch/approach"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
