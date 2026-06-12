void reset(mixed first_load) {
  object portal;

  portal = clone_object("entity/portal");
  move_object(portal, this_object());
}

void init() {
  add_action("west", "west");
  add_action("west", "w");
  add_action("go", "go");
}

void describe(object viewer) {
  write("Portal Hall\n");
  write("Mount points for exhibit mudlibs line the walls, but the hall belongs to LPMuseum.\n");
  write("A quiet exhibit portal waits here as an Entity. Origins is west.\n");
  call_other(viewer, "list_vended_entities", viewer);
  call_other(viewer, "list_present_personas", viewer);
}

void long(mixed str) {
  describe(this_player());
}

string short() {
  return "Portal Hall";
}

int go(mixed destination) {
  if (destination == "west" || destination == "origins") {
    return west(0);
  }

  write("You can't go that way.\n");
  return 1;
}

int west(mixed str) {
  return call_other(this_player(), "move_player", "west#place/origins");
}
