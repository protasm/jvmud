inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "Ravine";
  long_desc = "You are in a ravine between mountains. It seems to be possible\n"+
  "to go up from here.\n";
  dest_dir = ({
    "room/mountain/mount_pass", "down",
    "room/mountain/mount_top", "up"
  });
}
