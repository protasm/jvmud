status east() {
  call_other(this_player(),"move_player", "east#room/south/sforst20");

  return 1;
}

void init() {
  add_action("northeast"); add_verb("northeast");
  add_action("west");      add_verb("west");
  add_action("east");      add_verb("east");
}

void long() {
  write("You are standing on the shore of Crescent Lake, a beautiful and\n" +
  "clear lake. Out in the centre of the lake stands the Isle\n" +
  "of the Magi.\n" +
  "A trail leads into the forest to the east.\n" +
  "The shore of Crescent Lake continues northeast and west\n");
}

status northeast() {
  call_other(this_player(),"move_player", "northeast#room/south/sshore8");

  return 1;
}

void reset(mixed started) {
  if (!started)
    set_light(1);
}

string short() {
  return "The shore of Crescent Lake";
}

status west() {
  call_other(this_player(), "move_player", "west#room/south/sshore10");

  return 1;
}
