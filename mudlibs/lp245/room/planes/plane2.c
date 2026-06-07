inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain, extending in all directions.\n";
  dest_dir = ({
    "room/planes/plane1", "south",
    "room/planes/plane3", "north",
    "room/planes/plane4", "east",
    "room/planes/plane5", "west"
  });
}
