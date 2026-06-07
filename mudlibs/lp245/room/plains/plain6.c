inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain.\n";
  dest_dir = ({
    "room/plains/plain3", "south",
    "room/plains/plain11", "north",
    "room/plains/plain8", "east",
    "room/plains/plain10", "west"
  });
}
