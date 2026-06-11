inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
  short_desc = "Down the well";
  long_desc = "You are down the well. It is wet and slippery.\n" +
  "There is a lever beside a door to the west.\n";
  dest_dir = ({
    "room/village/narr_alley", "up",
    "room/maze1/maze1", "north"
  });
}

void init() {
  add_action("move", "up");
  add_action("move", "north");
  add_action("west", "west");
  add_action("open", "open");
  add_action("close", "close");
  add_action("pull", "pull");
  add_action("pull", "turn");
  add_action("pull", "move");
}

void long(mixed str) {
  if (set_light(0) == 0) {
    write("It is dark.\n");

    return;
  }

  if (str == "lever") {
    write("The lever can be moved between two positions.\n");

    return;
  }

  if (str == "door") {
    if (call_other("room/sub/door_trap", "query_west_door"))
      write("The door is closed.\n");
    else
      write("The door is open\n");

    return;
  }

  write(long_desc);
  write("There are two obvious exits, up and north.\n");
}

status close(string str) {
  if (!str && str != "door")
    return 0;

  write("You can't.\n");

  return 1;
}

status id(mixed str) {
  return str == "lever" || str == "door";
}

status open(string str) {
  if (!str && str != "door")
    return 0;

  write("You can't.\n");

  return 1;
}

status pull(string str) {
  if (!str || str != "lever")
    return 0;

  call_other("room/sub/door_trap", "toggle_door");

  return 1;
}

status west() {
  if (call_other("room/sub/door_trap", "query_west_door") == 0) {
    call_other(this_player(), "move_player", "west#room/sub/door_trap");

    return 1;
  }

  write("The door is closed.\n");

  return 1;
}
