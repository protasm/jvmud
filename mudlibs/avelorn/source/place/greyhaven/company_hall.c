void initialize(mixed first_load) {
}
void offer_interactions() {
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
}
string short() { return "Greyhaven Company Hall"; }
void describe(object viewer) {
  write("Greyhaven Company Hall\n");
  write("The regional Company chapter occupies a sturdy hall of riverstone and ");
  write("oak. Contract boards separate Crown commissions, town requests, temple ");
  write("relief work, and private charters under one published code of conduct.\n\n");
  write("Heron Fountain is west, the watch barracks north, and Smith Lane east.\n");
}
int west(mixed ignored) { return travel("west", "place/greyhaven/heron_fountain"); }
int north(mixed ignored) { return travel("north", "place/greyhaven/watch_barracks"); }
int east(mixed ignored) { return travel("east", "place/greyhaven/smith_lane"); }
int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
