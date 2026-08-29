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
  return "Greyhaven Western Approach";
}

void describe(object viewer) {
  write("Greyhaven Western Approach\n");
  write("Market gardens give way to a broad, paved approach beneath the town ");
  write("walls. A tall ward lantern guides late caravans toward the open gate, ");
  write("its blue glass clouded by the same strange soot seen near Brindleford.\n\n");
  write("The Westward Rise is west, and Greyhaven's west gate is east.\n");
}

int west(mixed ignored) {
  return travel("west", "place/lantern_road/westward_rise");
}

int east(mixed ignored) {
  return travel("east", "place/greyhaven/west_gate");
}

int tend_lantern(mixed ignored) {
  write("You clear the soot, renew the wick, and test the approach lantern's ward.\n");
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "record_quest_action",
      "greyhaven-west-lantern");
}

int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
