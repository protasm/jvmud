void init() {
  add_action("north"); add_verb("north");
  add_action("south"); add_verb("south");
  add_action("west");  add_verb("west");
}

void long() {
  write("You are in part of a dimly lit forest.\n" +
  "Trails lead north, south and west\n");
}

status north() {
  call_other(this_player(), "move_player", "north#room/south/sforst2");

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
  call_other(this_player(), "move_player", "south#room/south/sforst4");

  return 1;
}

status west() {
  call_other(this_player(), "move_player", "west#room/south/sforst7");

  return 1;
}
