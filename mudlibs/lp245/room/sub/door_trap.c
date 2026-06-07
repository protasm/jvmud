int west_door_open;

void reset(mixed arg) {
  west_door_open = 1;

  if (arg)
    return;

  set_light(0);
}

string short() {
  if (set_light(0))
    return "Room with black walls";

  return "dark room";
}

void init() {
  add_action("move1", "east");
  add_action("move2", "west");
  add_action("open"); add_verb("close");
  add_action("close"); add_verb("close");
}

status move1() {
  if (west_door_open == 1) {
    write("The door is closed.\n");

    return 1;
  }

  call_other(this_player(), "move_player", "east#room/well");

  return 1;
}

status move2() {
  if (west_door_open == 0) {
    write("The door is closed.\n");

    return 1;
  }

  call_other(this_player(), "move_player", "west#room/sub/after_trap");

  return 1;
}

void long(mixed str) {
  if (set_light(0) == 0) {
    write("It is dark.\n");

    return;
  }

  write("A room with black walls. There is a door to the east,\n" +
  "and a door to the west.\n");
  write("There are two obvious exits, east and west.\n");
}

status close(mixed str) {
  if (str != "door" && str != "west door" && str != "east door")
    return 0;

  write("There is no handle, and you can't push it closed.\n");

  return 1;
}

status open(mixed str) {
  if (str != "door" && str != "west door" && str != "east door")
    return 0;

  write("There is no handle, and you can't push it up.\n");

  return 1;
}

int query_west_door() {
  return west_door_open;
}

void toggle_door() {
  write("You move the lever.\n");
  say(call_other(this_player(), "query_name") + " pulled the lever.\n");

  if (west_door_open) {
    tell_room(this_object(), "The west door closed.\n" +
    "The east door opened.\n");

    tell_room(environment(this_player()), "The west door opened.\n");

    west_door_open = 0;
  } else {
    tell_room(this_object(), "The west door opens.\n" +
    "The east door closed.\n");

    tell_room(environment(this_player()), "The west door closed.\n");

    west_door_open = 1;
  }
}
