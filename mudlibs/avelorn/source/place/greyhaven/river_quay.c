void initialize(mixed first_load) {
}
void offer_interactions() { jvmud_add_action("north", "north"); jvmud_add_action("north", "n"); }
string short() { return "Greyhaven River Quay"; }
void describe(object viewer) {
  write("Greyhaven River Quay\n");
  write("Stone stairs descend to barges carrying grain, wool, slate, and lamp ");
  write("oil. Quay crews use numbered cranes and witnessed cargo tallies, keeping ");
  write("trade brisk without leaving honest disputes to shouted claims.\n\n");
  write("Market Cross is north.\n");
}
int north(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "north", "place/greyhaven/market_cross");
}
