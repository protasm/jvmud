inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain.\n";
  dest_dir = ({
    "room/planes/plane13", "north",
    "room/planes/plane6", "west",
    "room/planes/ruin", "south"
  });
}
