void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
}

string short() { return "Greyhaven Heron Fountain"; }

void describe(object viewer) {
  write("Greyhaven Heron Fountain\n");
  write("Clear water spills from a silver-grey heron into a broad public basin. ");
  write("The fountain commemorates the town's first aqueduct, jointly endowed ");
  write("by the Crown, the masons, and six surrounding farming hundreds.\n\n");
  write("Market Cross is south, Temple Court north, Archive Court west, and Company Hall east.\n");
}

int south(mixed ignored) { return travel("south", "place/greyhaven/market_cross"); }
int north(mixed ignored) { return travel("north", "place/greyhaven/temple_court"); }
int west(mixed ignored) { return travel("west", "place/greyhaven/archive_court"); }
int east(mixed ignored) { return travel("east", "place/greyhaven/company_hall"); }
int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
