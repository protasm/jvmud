void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
}

string short() {
  return "Mill Pump Room";
}

void describe(object viewer) {
  write("Mill Pump Room\n");
  write("A brass hand pump feeds a stone cistern used in fire drills and dry ");
  write("summers. The mechanism bears the paired marks of the Millers' Guild ");
  write("and the Crown Water Office, both responsible for its annual inspection.\n\n");
  write("The cellar landing is east.\n");
}

int east(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "east",
      "place/brindleford/cellar_landing");
}
