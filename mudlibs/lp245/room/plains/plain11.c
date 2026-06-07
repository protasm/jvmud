inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain, There is a mountain to the north.\n";
  dest_dir = ({
    "room/plains/plain6", "south",
    "room/mountain/mount_pass", "north",
    "room/plains/plain13", "east",
    "room/plains/plain12", "west"
  });
}
