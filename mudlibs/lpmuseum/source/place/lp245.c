void reset(mixed first_load) {
}

void init() {
  add_action("west", "west");
  add_action("west", "w");
  add_action("enter", "enter");
}

void long(mixed str) {
  write("LP245 Exhibit\n");
  write("A careful reconstruction of Vanilla LPMUD 2.4.5 stands beyond a shimmering portal.\n");
  write("The Origins wing is west. You can enter portal to cross into the exhibit.\n");
}

string short() {
  return "LP245 Exhibit";
}

int west(mixed str) {
  return call_other(this_player(), "move_player", "west#place/origins");
}

int enter(mixed str) {
  if (str != "portal") {
    write("Enter what?\n");
    return 1;
  }

  write("You step into the LP245 exhibit portal.\n");
  return transfer_player_to_game("vanilla-lpmud-245");
}
