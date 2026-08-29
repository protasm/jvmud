void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("south", "south");
  jvmud_add_action("south", "s");
  jvmud_add_action("training_drill", "train");
}

string short() {
  return "Brindleford Lantern House";
}

void describe(object viewer) {
  jvmud_write("Brindleford Lantern House\n");
  jvmud_write("Oak beams and pale riverstone frame this modest royal chapter house. ");
  jvmud_write("A polished lantern burns above a map of western Avelorn, marking the ");
  jvmud_write("roads to Greyhaven, Stonebridge, Merewatch, and distant Aldwyn. Here, ");
  jvmud_write("licensed Companions receive work too dangerous for an ordinary patrol ");
  jvmud_write("but too local to demand an army.\n\n");
  jvmud_write("The village green is south.\n");
  jvmud_write("A practice yard is ready; type train to complete the introductory drill.\n");
}

int training_drill(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "complete_introductory_drill");
}

int south(mixed ignored) {
  object actor;

  actor = jvmud_current_actor();
  return jvmud_invoke_lpc_object(
      actor,
      "travel_to",
      "south",
      "place/brindleford/village_green");
}
