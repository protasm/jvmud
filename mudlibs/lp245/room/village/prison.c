void init() {
  add_action("quit"); add_verb("quit");
}

void long() {
  write("You are in the local prison.\n");
  write("There are no exits.\n");
}

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
}

string short() {
  return "The local prison";
}
status quit() { return 1; }
