inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain. There is a big tree to the west.\n";
  dest_dir = ({
    "room/planes/plane5", "south",
    "room/planes/plane10", "north",
    "room/planes/plane3", "east",
    "room/forest/big_tree", "west"
  });
}
