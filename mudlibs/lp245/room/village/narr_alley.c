inherit "room/room";

reset(arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "Narrow alley";
  long_desc = "A narrow alley. There is a well in the middle.\n";
  dest_dir = ({
    "room/village/vill_road1", "north",
    "room/village/bank", "east",
    "room/village/post", "south"
  });
}

void init() {
  add_action("move", "north");
  add_action("move", "east");
  add_action("move", "south");
  add_action("go_down", "down");
}

void long(mixed str) {
  if (set_light(0) == 0) {
    write("It is dark.\n");

    return;
  }

  if (str == "well") {
    write("You look down the well, but see only darkness.\n");
    write("There are some iron handles on the inside.\n");

    return;
  }

  write(long_desc);
  write("There are three obvious exits, north, east and south.\n");
}

go_down() {
  call_other(this_player(), "move_player", "down#room/well");

  return 1;
}

status id(mixed str) {
  if (str == "well")
    return 1;
}
