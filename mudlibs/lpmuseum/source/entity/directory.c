void reset(mixed first_load) {
  bind_alias(this_object(), "entity", "directory");
  bind_alias(this_object(), "entity", "map");
}

string short() {
  return "museum directory";
}

int id(mixed value) {
  return value == "directory" || value == "map" || value == "museum directory";
}

void describe(object viewer) {
  write("The directory shows four native JVMud Places: concourse, origins, workshop, and archive.\n");
  write("A separate portal hall is marked as an optional exhibit boundary.\n");
}
