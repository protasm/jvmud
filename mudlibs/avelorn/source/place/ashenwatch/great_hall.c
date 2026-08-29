void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
}
string short() { return "Ashenwatch Great Hall"; }
void describe(object viewer) {
  jvmud_write("Ashenwatch Great Hall\n");
  jvmud_write("A long hall displays the arms of every western hundred beneath the ");
  jvmud_write("Crown lantern. The arrangement gives no settlement pride of place; each ");
  jvmud_write("shield supports one rib of the painted ward-vault overhead.\n\n");
  jvmud_write("The outer court is south, west and east towers flank the hall, and stairs descend north.\n");
}
int south(mixed ignored) { return travel("south", "place/ashenwatch/outer_court"); }
int west(mixed ignored) { return travel("west", "place/ashenwatch/west_tower"); }
int east(mixed ignored) { return travel("east", "place/ashenwatch/east_tower"); }
int north(mixed ignored) { return travel("north", "place/ashenwatch/underkeep"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
