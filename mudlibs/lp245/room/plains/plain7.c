inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain. There is a big tree to the west.\n";
  dest_dir = ({
    "room/plains/plain5", "south",
    "room/plains/plain10", "north",
    "room/plains/plain3", "east",
    "room/forest/big_tree", "west"
  });
}
