status east() {
  call_other(this_player(), "move_player", "east#room/south/sforst38");

  return 1;
}

void init() {
  add_action("north");  add_verb("north");
  add_action("south"); add_verb("south");
  add_action("east");  add_verb("east");
}

void long() {
  write("You are in part of a dimly lit forest.\n" +
  "Trails lead north, south and east\n");
}

status north() {
  call_other(this_player(), "move_player", "north#room/south/sforst36");

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
  call_other(this_player(), "move_player", "south#room/south/sforst45");

  return 1;
}
