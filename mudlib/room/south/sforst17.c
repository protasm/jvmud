init() {
  add_action("north"); add_verb("north");
  add_action("west");  add_verb("west");
}

long() {
  write("You are in part of a dimly lit forest.\n" +
  "Trails lead north and west\n");
}

north() {
  call_other(this_player(), "move_player", "north#room/south/sforst16");

  return 1;
}

reset(started) {
  if (!started)
    set_light(1);
}

short() {
  return "A dimly lit forest";
}

west() {
  call_other(this_player(), "move_player", "west#room/south/sforst18");

  return 1;
}
