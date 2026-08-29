void initialize(mixed first_load) {
}
void offer_interactions() { jvmud_add_action("east", "east"); jvmud_add_action("east", "e"); }
string short() { return "Greyhaven Archive Court"; }
void describe(object viewer) {
  write("Greyhaven Archive Court\n");
  write("Fireproof record rooms surround a gravel court. Copyists preserve ");
  write("charters, judgments, maps, and wardens' reports so that obligations ");
  write("remain knowable and authority cannot depend on one person's memory.\n\n");
  write("Heron Fountain is east.\n");
}
int east(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "east", "place/greyhaven/heron_fountain");
}
