void reset(mixed first_load) {
  object archive_case;

  archive_case = clone_object("entity/archive_case");
  move_object(archive_case, this_object());
}

void init() {
  add_action("east", "east");
  add_action("east", "e");
  add_action("go", "go");
}

void describe(object viewer) {
  int occupant_count;

  write("Archive\n");
  write("The Archive is deliberately independent of any exhibit mudlib content.\n");
  write("It demonstrates that LPMuseum can host its own Entities and player experience first.\n");
  write("\n");
  write("The concourse is east.\n");
  occupant_count = 0;
  if (present("staffer", this_object())) {
    write("\n");
    write("Museum Security Staffer\n");
    occupant_count = occupant_count + 1;
  }
  occupant_count = occupant_count + call_other(viewer, "list_present_personas", viewer);
  write("\n");
  call_other(viewer, "list_vended_entities", viewer);
}

void long(mixed str) {
  describe(this_player());
}

string short() {
  return "Archive";
}

int go(mixed destination) {
  if (destination == "east" || destination == "concourse") {
    return east(0);
  }

  write("You can't go that way.\n");
  return 1;
}

int east(mixed str) {
  return call_other(this_player(), "move_player", "east#place/concourse");
}
