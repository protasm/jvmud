status church() {
  call_other(this_player(), "move_player", "away#room/village/church");

  return 1;
}

void init() {
  add_action("church", "church");
}

void long() {
  write(short() + ".\n");
  write("You come to the void if you fall out of a room, and have nowhere to go.\n");
  write("Give the command 'church', and you will come back to village church.\n");
  write("\nYou are transfered to the church...\n");
  call_other(this_player(), "move_player", "X#room/village/church");
}

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
}

string short() {
  return "The void";
}
status id(string str) { return str == "void"; }
