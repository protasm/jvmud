status east() {
  call_other(this_player(),"move_player", "east#room/south/sshore11");

  return 1;
}

void init() {
  add_action("east");      add_verb("east");
  add_action("west");      add_verb("west");
}

void long() {
  write("You are standing on the shore of Crescent Lake, a beautiful and\n" +
  "clear lake. Out in the centre of the lake stands the Isle\n" +
  "of the Magi.\n" +
  "The shore of Crescent Lake continues east and west\n");
}

void reset(mixed started) {
  if (!started)
    set_light(1);
}

string short() {
  return "The shore of Crescent Lake";
}

status west() {
  call_other(this_player(), "move_player", "west#room/south/sshore13");

  return 1;
}
