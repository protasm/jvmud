void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
}
string short() { return "Ashenwatch Outer Court"; }
void describe(object viewer) {
  jvmud_write("Ashenwatch Outer Court\n");
  jvmud_write("Ash lies across a court built for wagon musters and public refuge. ");
  jvmud_write("Beneath it, painted lines still assign water, grain, medical, and guard ");
  jvmud_write("stations—an old plan the modern expedition follows exactly.\n\n");
  jvmud_write("The lower gate is east, barracks west, great hall north, and chapel south.\n");
}
int east(mixed ignored) { return travel("east", "place/ashenwatch/lower_gate"); }
int west(mixed ignored) { return travel("west", "place/ashenwatch/barracks"); }
int north(mixed ignored) { return travel("north", "place/ashenwatch/great_hall"); }
int south(mixed ignored) { return travel("south", "place/ashenwatch/chapel"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
