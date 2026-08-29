void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
}

string short() {
  return "Lamplighter Post";
}

void describe(object viewer) {
  jvmud_write("Lamplighter Post\n");
  jvmud_write("A compact brick depot stores lamp oil, blue glass, ladder hooks, and ");
  jvmud_write("weather cloaks. Its duty board assigns each road lantern to a named ");
  jvmud_write("keeper, with the Company covering gaps during storms and emergencies.\n\n");
  jvmud_write("The orchard lane is west, and the road climbs east.\n");
}

int west(mixed ignored) {
  return travel("west", "place/lantern_road/orchard_lane");
}

int east(mixed ignored) {
  return travel("east", "place/lantern_road/westward_rise");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
