void init() {
  add_action("north");     add_verb("north");
  add_action("west");      add_verb("west");
  add_action("northeast"); add_verb("northeast");
  add_action("southwest"); add_verb("southwest");
}

void long() {
  write("You are standing on the shore of Crescent Lake, a beautiful and\n" +
  "clear lake. Out in the centre of the lake stands the Isle\n" +
  "of the Magi.\n" +
  "Trails lead into the forest to the north and west.\n" +
  "The shore of Crescent Lake continues northeast and southwest\n");
}

status north() {
  call_other(this_player(), "move_player", "north#room/south/sforst27");

  return 1;
}

status northeast() {
  call_other(this_player(),"move_player", "northeast#room/south/sshore22");

  return 1;
}

void reset(mixed started) {
  if (!started)
    set_light(1);
}

string short() {
  return "The shore of Crescent Lake";
}

status southwest() {
  call_other(this_player(),"move_player", "southwest#room/south/sshore20");

  return 1;
}

status west() {
  call_other(this_player(),"move_player", "west#room/south/sforst28");

  return 1;
}
