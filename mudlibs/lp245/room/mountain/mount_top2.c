inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "Plateau";
  long_desc = "You are on a large, open plateau on top of the mountain.\n"+
  "The view is fantastic in all directions and the clouds\n"+
  "that rush past above feels so close you could almost\n"+
  "touch them. The air here is fresh and clean.\n";
  dest_dir = ({
    "room/mountain/mount_top", "west"
  });
}
