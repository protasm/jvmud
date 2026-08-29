void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
}

string short() {
  return "Greyhaven West Gate";
}

void describe(object viewer) {
  write("Greyhaven West Gate\n");
  write("Two round towers guard an open arch painted with the Crown lantern and ");
  write("Greyhaven's silver heron. Watch officers greet known farmers by name, ");
  write("check caravan seals, and direct newcomers without needless ceremony.\n\n");
  write("The western approach is west, and Gate Square lies east.\n");
}

int west(mixed ignored) {
  return travel("west", "place/lantern_road/greyhaven_approach");
}

int east(mixed ignored) {
  return travel("east", "place/greyhaven/gate_square");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
