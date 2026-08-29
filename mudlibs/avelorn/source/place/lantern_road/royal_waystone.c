void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
  jvmud_add_action("south", "south");
  jvmud_add_action("south", "s");
}

string short() {
  return "Royal Waystone";
}

void describe(object viewer) {
  write("Royal Waystone\n");
  write("A granite pillar gives honest distances to Brindleford, Greyhaven, ");
  write("Stonebridge, and Aldwyn. Fresh whitewash fills its carved letters, ");
  write("renewed by roadwardens and the nearest parish in alternating years.\n\n");
  write("The toll meadow is west, the Crown shelter east, and a riverside path south.\n");
}

int west(mixed ignored) {
  return travel("west", "place/lantern_road/toll_meadow");
}

int east(mixed ignored) {
  return travel("east", "place/lantern_road/crown_shelter");
}

int south(mixed ignored) {
  return travel("south", "place/lantern_road/riverside_path");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
