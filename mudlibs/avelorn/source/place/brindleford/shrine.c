void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("north", "north");
  jvmud_add_action("north", "n");
  jvmud_add_action("rest", "rest");
}

string short() {
  return "Shrine of the Seven Lamps";
}

void describe(object viewer) {
  write("Shrine of the Seven Lamps\n");
  write("Seven bronze lamps burn beneath a simple timber canopy. The shrine ");
  write("serves equally as sanctuary, schoolroom, and sickroom, maintained by ");
  write("the village and the royal Temple concord. Clean cots stand ready for ");
  write("travelers whom the road has treated poorly.\n\n");
  write("The village green is north. You may rest here in safety.\n");
}

int north(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "north",
      "place/brindleford/village_green");
}

int rest(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "rest_at_shrine");
}
