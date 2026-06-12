void reset(mixed first_load) {
}

void init() {
  add_action("north", "north");
  add_action("north", "n");
}

void long(mixed str) {
  write("Grand Concourse of LPMuseum\n");
  write("Marble floors carry the hush of a vast museum for LPMud worlds.\n");
  write("A broad archway to the north leads into the Origins wing.\n");
}

string short() {
  return "Grand Concourse of LPMuseum";
}

int north(mixed str) {
  return call_other(this_player(), "move_player", "north#place/origins");
}
