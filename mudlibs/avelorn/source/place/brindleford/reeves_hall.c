void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
}

string short() {
  return "Brindleford Reeve's Hall";
}

void describe(object viewer) {
  write("Brindleford Reeve's Hall\n");
  write("A slate-roofed hall houses the village rolls, weighing standards, ");
  write("and a small public chamber. Petitions are heard each market day, ");
  write("with a Crown circuit-justice expected every new moon. Nothing here ");
  write("suggests fear of the law; it is treated as a familiar public tool.\n\n");
  write("The village green is east.\n");
}

int east(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "east",
      "place/brindleford/village_green");
}
