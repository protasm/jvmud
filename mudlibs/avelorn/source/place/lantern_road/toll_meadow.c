void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
}

string short() {
  return "Lantern Road Toll Meadow";
}

void describe(object viewer) {
  jvmud_write("Lantern Road Toll Meadow\n");
  jvmud_write("A mown meadow opens beyond Old Brindle Bridge. The toll house is ");
  jvmud_write("unbarred for licensed Companions, village carts, and pilgrims; commercial ");
  jvmud_write("caravans pay a posted rate that funds bridges, shelters, and patrols.\n\n");
  jvmud_write("Old Brindle Bridge is west, and a royal waystone stands east.\n");
}

int west(mixed ignored) {
  return travel("west", "place/brindleford/old_bridge");
}

int east(mixed ignored) {
  return travel("east", "place/lantern_road/royal_waystone");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
