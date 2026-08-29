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
  jvmud_write("Shrine of the Seven Lamps\n");
  jvmud_write("Seven bronze lamps burn beneath a simple timber canopy. The shrine ");
  jvmud_write("serves equally as sanctuary, schoolroom, and sickroom, maintained by ");
  jvmud_write("the village and the royal Temple concord. Clean cots stand ready for ");
  jvmud_write("travelers whom the road has treated poorly.\n\n");
  jvmud_write("The village green is north. You may rest here in safety.\n");
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
