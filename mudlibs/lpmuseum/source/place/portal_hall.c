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
  int occupant_count;

  write("Portal Hall\n");
  write("Mount points for exhibit mudlibs line the walls, but the hall belongs to LPMuseum.\n");
  write("\n");
  write("Origins is west.\n");
  occupant_count = 0;
  if (present("staffer", this_object())) {
    write("\n");
    write("Museum Security Staffer\n");
    occupant_count = occupant_count + 1;
  }
  occupant_count = occupant_count + call_other(viewer, "list_present_personas", viewer);
  write("\n");
  write("A quiet exhibit portal waits here as an Entity.\n");
  call_other(viewer, "list_vended_entities", viewer);
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
