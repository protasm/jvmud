inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "Top of mountain";
  long_desc = "You are on top of a mountain. There is a small plateau to the\n"+
  "east.\n";
  dest_dir = ({
    "room/mountain/ravine", "down",
    "room/mountain/mount_top2", "east"
  });
}
