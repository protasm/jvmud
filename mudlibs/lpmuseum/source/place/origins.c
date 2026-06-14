void reset(mixed first_load) {
  object model;

  model = clone_object("entity/engine_model");
  move_object(model, this_object());
}

void init() {
  add_action("south", "south");
  add_action("south", "s");
  add_action("east", "east");
  add_action("east", "e");
  add_action("go", "go");
}

void describe(object viewer) {
  int occupant_count;

  write("Origins Gallery\n");
  write("This gallery names JVMud concepts before any exhibit vocabulary appears.\n");
  write("\n");
  write("The concourse is south; the portal hall is east.\n");
  occupant_count = 0;
  if (present("staffer", this_object())) {
    write("\n");
    write("Museum Security Staffer\n");
    occupant_count = occupant_count + 1;
  }
  occupant_count = occupant_count + call_other(viewer, "list_present_personas", viewer);
  write("\n");
  write("An engine model rests in the center of the Place.\n");
  call_other(viewer, "list_vended_entities", viewer);
}

void long(mixed str) {
  describe(this_player());
}

string short() {
  return "Origins Gallery";
}

int go(mixed destination) {
  if (destination == "south" || destination == "concourse") {
    return south(0);
  }
  if (destination == "east" || destination == "portal" || destination == "portal hall") {
    return east(0);
  }

  write("You can't go that way.\n");
  return 1;
}

int south(mixed str) {
  return call_other(this_player(), "move_player", "south#place/concourse");
}

int east(mixed str) {
  return call_other(this_player(), "move_player", "east#place/portal_hall");
}
