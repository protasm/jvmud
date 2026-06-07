status east() {
  call_other(this_player(), "move_player", "east#room/south/sforst23");

  return 1;
}

void init() {
  add_action("south"); add_verb("south");
  add_action("east");  add_verb("east");
}

void long() {
  write("You are in part of a dimly lit forest.\n" +
  "Trails lead south and east\n");
}

void reset(mixed started) {
  if (!started)
    set_light(1);
}

string short() {
  return "A dimly lit forest";
}

status south() {
  call_other(this_player(), "move_player", "south#room/south/sforst25");

  return 1;
}
