void initialize(mixed first_load) {
}
void offer_interactions() { jvmud_add_action("east", "east"); jvmud_add_action("east", "e"); }
string short() { return "Greyhaven Archive Court"; }
void describe(object viewer) {
  jvmud_write("Greyhaven Archive Court\n");
  jvmud_write("Fireproof record rooms surround a gravel court. Copyists preserve ");
  jvmud_write("charters, judgments, maps, and wardens' reports so that obligations ");
  jvmud_write("remain knowable and authority cannot depend on one person's memory.\n\n");
  jvmud_write("Heron Fountain is east.\n");
}
int east(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "east", "place/greyhaven/heron_fountain");
}
