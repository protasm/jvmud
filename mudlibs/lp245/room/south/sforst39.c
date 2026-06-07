status east() {
  call_other(this_player(), "move_player", "east#room/south/sforst40");

  return 1;
}

void init() {
  add_action("north"); add_verb("north");
  add_action("south"); add_verb("south");
  add_action("east");  add_verb("east");
  add_action("west");  add_verb("west");
}

void long() {
  write("You are in part of a dimly lit forest.\n" +
  "Trails lead north, south, east and west\n");
}

status north() {
  call_other(this_player(), "move_player", "north#room/south/sforst34");

  return 1;
}

void reset(mixed started) {
  if (!started)
    set_light(1);
}

string short() {
  return "A dimly lit forest";
}

status south() {
  call_other(this_player(), "move_player", "south#room/south/sforst43");

  return 1;
}

status west() {
  call_other(this_player(), "move_player", "west#room/south/sforst38");

  return 1;
}
