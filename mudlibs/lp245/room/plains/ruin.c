status id(mixed str) {
  if (str == "ruin")
    return 1;

  else
    return 0;
}

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
}

string short() {
  if (set_light(0))
    return "Ruin";

  return "dark room";
}

void init() {
  add_action("move1", "south");
  add_action("move2", "north");
  add_action("move3", "east");
  add_action("move4", "west");
}

status move1() {
  call_other(this_player(), "move_player", "south#room/plains/plain4");

  return 1;
}

status move2() {
  call_other(this_player(), "move_player", "north#room/plains/plain8");

  return 1;
}

status move3() {
  call_other(this_player(), "move_player", "east#room/plains/plain9");

  return 1;
}

status move4() {
  call_other(this_player(), "move_player", "west#room/plains/plain3");

  return 1;
}

void long(mixed str) {
  if (set_light(0) == 0) {
    write("It is dark.\n");

    return;
  }

  write("A very old looking ruin. There is no roof, and no door.\n");
  write("There are four obvious exits, south, north, east and west.\n");
}
