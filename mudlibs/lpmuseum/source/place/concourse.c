void reset(mixed first_load) {
  object directory;
  object docent;
  object staffer;

  directory = clone_object("entity/directory");
  move_object(directory, this_object());

  docent = clone_object("entity/docent");
  move_object(docent, this_object());

  staffer = clone_object("entity/staffer");
  move_object(staffer, this_object());

  set_light(1);
}

void init() {
  add_action("north", "north");
  add_action("north", "n");
  add_action("east", "east");
  add_action("east", "e");
  add_action("west", "west");
  add_action("west", "w");
  add_action("go", "go");
}

void describe(object viewer) {
  write("Grand Concourse of LPMuseum\n");
  write("Marble floors carry the hush of a museum for native JVMud mudlibs.\n");
  write("This is a complete starting Place: no exhibit mudlib is required.\n");
  write("North leads to Origins, east to the Creator Workshop, and west to the Archive.\n");
  write("A directory and a docent are here.\n");
  if (present("staffer", this_object())) {
    write("Museum Security Staffer\n");
  }
  call_other(viewer, "list_vended_entities", viewer);
  call_other(viewer, "list_present_personas", viewer);
}

void long(mixed str) {
  describe(this_player());
}

string short() {
  return "Grand Concourse of LPMuseum";
}

int go(mixed destination) {
  if (destination == "north" || destination == "origins") {
    return north(0);
  }
  if (destination == "east" || destination == "workshop") {
    return east(0);
  }
  if (destination == "west" || destination == "archive") {
    return west(0);
  }

  write("You can't go that way.\n");
  return 1;
}

int north(mixed str) {
  return call_other(this_player(), "move_player", "north#place/origins");
}

int east(mixed str) {
  return call_other(this_player(), "move_player", "east#place/workshop");
}

int west(mixed str) {
  return call_other(this_player(), "move_player", "west#place/archive");
}
