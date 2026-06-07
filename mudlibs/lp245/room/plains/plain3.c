inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain. There are some kind of building to the east.\n";
  dest_dir = ({
    "room/plains/plain2", "south",
    "room/plains/plain6", "north",
    "room/plains/ruin", "east",
    "room/plains/plain7", "west"
  });
}
