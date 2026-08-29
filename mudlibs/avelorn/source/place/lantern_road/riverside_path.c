void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("north", "north");
  jvmud_add_action("north", "n");
}

string short() {
  return "Brindle Riverside Path";
}

void describe(object viewer) {
  jvmud_write("Brindle Riverside Path\n");
  jvmud_write("Willow roots hold the bank above clear, quick water. Children have ");
  jvmud_write("hung painted wooden fish from a boundary rope, marking the village ");
  jvmud_write("spawning pool where netting is forbidden until midsummer.\n\n");
  jvmud_write("The royal waystone is north.\n");
}

int north(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "north",
      "place/lantern_road/royal_waystone");
}
