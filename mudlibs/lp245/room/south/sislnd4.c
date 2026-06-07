void init() {
  add_action("north");  add_verb("north");
  add_action("south"); add_verb("south");
  add_action("west");  add_verb("west");
}

void long() {
  write("You are standing on the shore of the Isle of the Magi\n" +
  "The shore of the island continues north and south from here\n" +
  "To the west, a hill rises up to the ancient ruins of the Tower\n" +
  "of Arcanarton, the archmage who used to live on this island\n");
}

status north() {
  call_other(this_player(), "move_player", "north#room/south/sislnd3");

  return 1;
}

void reset(mixed started) {
  if (!started)
    set_light(1);
}

string short() {
  return "The shore of the Isle of the Magi";
}

status south() {
  call_other(this_player(), "move_player", "south#room/south/sislnd5");

  return 1;
}

status west() {
  call_other(this_player(), "move_player", "west#room/south/sislnd14");

  return 1;
}
