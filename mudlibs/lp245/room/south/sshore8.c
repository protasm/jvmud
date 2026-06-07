status east() {
  call_other(this_player(),"move_player", "east#room/south/sforst19");

  return 1;
}

void init() {
  add_action("northeast"); add_verb("northeast");
  add_action("south");     add_verb("south");
  add_action("east");      add_verb("east");
  add_action("southwest"); add_verb("southwest");
}

void long() {
  write("You are standing on the shore of Crescent Lake, a beautiful and\n" +
  "clear lake. Out in the centre of the lake stands the Isle\n" +
  "of the Magi.\n" +
  "Trails lead into the forest to the south and east.\n" +
  "The shore of Crescent Lake continues northeast and southwest\n");
}

status northeast() {
  call_other(this_player(),"move_player", "northeast#room/south/sshore7");

  return 1;
}

void reset(mixed started) {
  if (!started)
    set_light(1);
}

string short() {
  return "The shore of Crescent Lake";
}

status south() {
  call_other(this_player(), "move_player", "south#room/south/sforst20");

  return 1;
}

status southwest() {
  call_other(this_player(),"move_player", "southwest#room/south/sshore9");

  return 1;
}
