inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
  short_desc = "Dead end";
  long_desc = "Dead end.\n";
  dest_dir = ({
    "room/mine/tunnel21", "east"
  });
}
