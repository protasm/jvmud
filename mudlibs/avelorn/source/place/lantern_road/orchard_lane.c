void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
  jvmud_add_action("north", "north");
  jvmud_add_action("north", "n");
}

string short() {
  return "Greyhaven Orchard Lane";
}

void describe(object viewer) {
  jvmud_write("Greyhaven Orchard Lane\n");
  jvmud_write("Pear and damson trees cover the southward slope in chartered strips. ");
  jvmud_write("Small brass plaques name the families who tend each row and the guild ");
  jvmud_write("press entitled to buy its first harvest share.\n\n");
  jvmud_write("The Crown shelter is west, the lamplighter post east, and a birch copse north.\n");
}

int west(mixed ignored) {
  return travel("west", "place/lantern_road/crown_shelter");
}

int east(mixed ignored) {
  return travel("east", "place/lantern_road/lamplighter_post");
}

int north(mixed ignored) {
  return travel("north", "place/lantern_road/birch_copse");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
