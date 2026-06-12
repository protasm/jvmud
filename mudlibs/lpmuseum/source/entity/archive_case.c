void reset(mixed first_load) {
  bind_alias(this_object(), "entity", "case");
  bind_alias(this_object(), "entity", "archive");
}

string short() {
  return "archive case";
}

int id(mixed value) {
  return value == "case" || value == "archive" || value == "archive case";
}

void describe(object viewer) {
  write("The archive case is empty on purpose: LPMuseum stands without exhibit content.\n");
  write("An exhibit can be mounted later without becoming part of the museum's own mudlib.\n");
}
