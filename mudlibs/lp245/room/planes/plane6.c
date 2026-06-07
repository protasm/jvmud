inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain.\n";
  dest_dir = ({
    "room/planes/plane3", "south",
    "room/planes/plane11", "north",
    "room/planes/plane8", "east",
    "room/planes/plane10", "west"
  });
}
