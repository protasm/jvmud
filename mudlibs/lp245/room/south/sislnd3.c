void init() {
  add_action("south"); add_verb("south");
  add_action("west");  add_verb("west");
}

void long() {
  write("You are standing on the shore of the Isle of the Magi\n" +
  "The shore of the island continues south and west from here\n" +
  "To the south, a hill rises up to the ancient ruins of the Tower\n" +
  "of Arcanarton, the archmage who used to live on this island\n" +
  "although no track leads directly there from here\n");
}

void reset(mixed started) {
  if (!started)
    set_light(1);
}

string short() {
  return "Shore of the Isle of the Magi";
}

status south() {
  call_other(this_player(), "move_player", "south#room/south/sislnd4");

  return 1;
}

status west() {
  call_other(this_player(), "move_player", "west#room/south/sislnd2");

  return 1;
}
