void initialize(mixed first_load) {
}
void offer_interactions() { jvmud_add_action("south", "south"); jvmud_add_action("south", "s"); }
string short() { return "Greyhaven Lantern Tower"; }
void describe(object viewer) {
  write("Greyhaven Lantern Tower\n");
  write("A many-windowed tower holds the western district's great ward flame. ");
  write("Its keepers chart the spreading soot that has begun to dim lesser ");
  write("lanterns, treating the mystery as a shared problem rather than a panic.\n\n");
  write("Temple Court is south.\n");
}
int south(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "south", "place/greyhaven/temple_court");
}
