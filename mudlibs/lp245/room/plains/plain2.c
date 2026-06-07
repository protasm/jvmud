inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain, extending in all directions.\n";
  dest_dir = ({
    "room/plains/plain1", "south",
    "room/plains/plain3", "north",
    "room/plains/plain4", "east",
    "room/plains/plain5", "west"
  });
}
