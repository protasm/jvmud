void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
  jvmud_add_action("tend_lantern", "tend");
}

string short() {
  return "Crown Road Shelter";
}

void describe(object viewer) {
  write("Crown Road Shelter\n");
  write("A three-sided stone shelter offers dry benches, a rain cistern, and ");
  write("an emergency grain chest sealed by the reeves of Brindleford and ");
  write("Greyhaven. A ward lantern hangs from its tiled eave.\n\n");
  write("The royal waystone is west, and the orchard lane is east.\n");
}

int west(mixed ignored) {
  return travel("west", "place/lantern_road/royal_waystone");
}

int east(mixed ignored) {
  return travel("east", "place/lantern_road/orchard_lane");
}

int tend_lantern(mixed ignored) {
  write("You refill the shelter lantern and reset its small silver ward-ring.\n");
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "record_quest_action", "shelter-lantern");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
