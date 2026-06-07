status east() {
  call_other(this_player(),"move_player", "east#room/south/sforst49");

  return 1;
}

void init() {
  add_action("west");      add_verb("west");
  add_action("east");      add_verb("east");
  add_action("southeast"); add_verb("southeast");
}

void long() {
  write("You are standing on the shore of Crescent Lake, a beautiful and\n" +
  "clear lake. Out in the centre of the lake stands the Isle\n" +
  "of the Magi.\n" +
  "A trail leads into the forest to the east.\n" +
  "The shore of Crescent Lake continues southeast and west\n");
}

void reset(mixed started) {
  if (!started)
    set_light(1);
}

string short() {
  return "The shore of Crescent Lake";
}

status southeast() {
  call_other(this_player(),"move_player", "southeast#room/south/sshore25");

  return 1;
}

status west() {
  call_other(this_player(), "move_player", "west#room/south/sshore23");

  return 1;
}
