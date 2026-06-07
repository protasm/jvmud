inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain. There is a mountain to the north,\nbut it is to steep to climb.\n";
  dest_dir = ({
    "room/planes/plane11", "west",
    "room/planes/plane8", "south"
  });
}
