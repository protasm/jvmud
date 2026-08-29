void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("north", "north");
  jvmud_add_action("north", "n");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
}

string short() {
  return "Brindleford Mill Road";
}

void describe(object viewer) {
  write("Brindleford Mill Road\n");
  write("A packed-stone lane follows an irrigation channel between clipped ");
  write("hedges. Crown road crews have set blue distance stones at each furlong, ");
  write("and village tenants share the work of keeping the watercourse clear.\n\n");
  write("The market is west, Halward Mill is north, and the east road continues east.\n");
}

int west(mixed ignored) {
  return travel("west", "place/brindleford/market");
}

int north(mixed ignored) {
  return travel("north", "place/brindleford/mill_yard");
}

int east(mixed ignored) {
  return travel("east", "place/brindleford/east_road");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
