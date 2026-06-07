inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain.\n";
  dest_dir = ({
    "room/plains/plain7", "north",
    "room/plains/plain2", "east"
  });
}
