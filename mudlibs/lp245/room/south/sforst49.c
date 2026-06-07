status east() {
  call_other(this_player(), "move_player", "east#room/south/sforst48");

  return 1;
}

void init() {
  add_action("south"); add_verb("south");
  add_action("east");  add_verb("east");
  add_action("west");  add_verb("west");
}

void long() {
  write("You are in part of a dimly lit forest.\n" +
  "Trails lead south, east and west\n");
}

void reset(mixed started) {
  if (!started)
    set_light(1);
}

string short() {
  return "A dimly lit forest";
}

status south() {
  call_other(this_player(), "move_player", "south#room/south/sshore25");

  return 1;
}

status west() {
  call_other(this_player(), "move_player", "west#room/south/sshore24");

  return 1;
}
