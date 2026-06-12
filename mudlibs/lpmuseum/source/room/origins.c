void reset(mixed first_load) {
}

void init() {
  add_action("south", "south");
  add_action("south", "s");
  add_action("east", "east");
  add_action("east", "e");
}

void long(mixed str) {
  write("Origins Wing\n");
  write("Cases of early LPC craft line the walls. To the east, a lit placard reads: LP245.\n");
  write("The grand concourse waits back to the south.\n");
}

string short() {
  return "Origins Wing";
}

int south(mixed str) {
  return call_other(this_player(), "move_player", "south#room/concourse");
}

int east(mixed str) {
  return call_other(this_player(), "move_player", "east#room/lp245");
}
