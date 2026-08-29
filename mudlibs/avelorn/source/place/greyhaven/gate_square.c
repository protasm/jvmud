void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
}

string short() {
  return "Greyhaven Gate Square";
}

void describe(object viewer) {
  write("Greyhaven Gate Square\n");
  write("A paved square receives wagons beneath guild signs and public notices. ");
  write("Porters, watch clerks, temple guides, and licensed brokers keep arrivals ");
  write("moving toward the right market, lodging, hearing, or storehouse.\n\n");
  write("The west gate is west, and Market Cross is east.\n");
}

int east(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "east",
      "place/greyhaven/market_cross");
}

int west(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "west",
      "place/greyhaven/west_gate");
}
