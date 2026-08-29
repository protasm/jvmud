void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("south", "south");
  jvmud_add_action("south", "s");
}

string short() {
  return "Roadside Birch Copse";
}

void describe(object viewer) {
  jvmud_write("Roadside Birch Copse\n");
  jvmud_write("White trunks encircle a spring dedicated to travelers of every temple. ");
  jvmud_write("Ribbons name safe arrivals, reconciled families, and apprenticeships ");
  jvmud_write("completed in distant towns of the same kingdom.\n\n");
  jvmud_write("The orchard lane is south.\n");
}

int south(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "south",
      "place/lantern_road/orchard_lane");
}
