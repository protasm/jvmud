inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain. There are some kind of building to the east.\n";
  dest_dir = ({
    "room/planes/plane2", "south",
    "room/planes/plane6", "north",
    "room/planes/ruin", "east",
    "room/planes/plane7", "west"
  });
}
