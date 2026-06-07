east() {
  call_other(this_player(), "move_player", "east#room/south/sforst3");

  return 1;
}

init() {
  add_action("north"); add_verb("north");
  add_action("south"); add_verb("south");
  add_action("east");  add_verb("east");
}

long() {
  write("You are in part of a dimly lit forest.\n" +
  "Trails lead north, south and east\n");
}

north() {
  call_other(this_player(), "move_player", "north#room/south/sforst6");

  return 1;
}

reset(started) {
  if (!started)
    set_light(1);
}

short() {
  return "A dimly lit forest";
}

south() {
  call_other(this_player(), "move_player", "south#room/south/sforst8");

  return 1;
}
