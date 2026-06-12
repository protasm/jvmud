void reset(mixed first_load) {
  bind_alias(this_object(), "entity", "portal");
  bind_alias(this_object(), "entity", "exhibit portal");
}

void init() {
  add_action("enter", "enter");
}

string short() {
  return "quiet exhibit portal";
}

int id(mixed value) {
  return value == "portal" || value == "exhibit portal";
}

void describe(object viewer) {
  write("The portal is a museum boundary marker. It can point to an exhibit when one is mounted.\n");
  write("No exhibit mudlib is required for LPMuseum to run.\n");
}

int enter(mixed target) {
  if (target != "portal" && target != "exhibit portal") {
    return 0;
  }

  write("The portal is quiet. No exhibit is mounted here yet.\n");
  return 1;
}
