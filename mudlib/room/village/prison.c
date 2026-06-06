init() {
  add_action("quit"); add_verb("quit");
}

long() {
  write("You are in the local prison.\n");
  write("There are no exits.\n");
}

reset(arg) {
  if (arg)
    return;

  set_light(1);
}

short() {
  return "The local prison";
}
quit() { return 1; }
