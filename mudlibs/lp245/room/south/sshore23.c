status east() {
  call_other(this_player(),"move_player", "east#room/south/sshore24");

  return 1;
}

void init() {
  add_action("east");      add_verb("east");
  add_action("west");      add_verb("west");
  add_action("southwest"); add_verb("southwest");
}

void long() {
  write("You are standing on the shore of Crescent Lake, a beautiful and\n" +
  "clear lake. Out in the centre of the lake stands the Isle\n" +
  "of the Magi.\n" +
  "A trail leads into the forest to the west.\n" +
  "The shore of Crescent Lake continues southwest and east\n");
}

void reset(mixed started) {
  if (!started)
    set_light(1);
}

string short() {
  return "The shore of Crescent Lake";
}

status southwest() {
  call_other(this_player(),"move_player", "southwest#room/south/sshore22");

  return 1;
}

status west() {
  call_other(this_player(), "move_player", "west#room/south/sforst21");

  return 1;
}
