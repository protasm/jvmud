void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
}

string short() {
  return "Westward Rise";
}

void describe(object viewer) {
  jvmud_write("Westward Rise\n");
  jvmud_write("The road climbs between old hawthorns. Greyhaven's slate roofs and ");
  jvmud_write("round western towers appear ahead, while the Brindle valley spreads ");
  jvmud_write("behind in ordered fields, mills, shrines, and wooded commons.\n\n");
  jvmud_write("The lamplighter post is west, and Greyhaven's approach is east.\n");
}

int west(mixed ignored) {
  return travel("west", "place/lantern_road/lamplighter_post");
}

int east(mixed ignored) {
  return travel("east", "place/lantern_road/greyhaven_approach");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
