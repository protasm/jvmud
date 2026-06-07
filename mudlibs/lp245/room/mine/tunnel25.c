inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
  short_desc = "Tunnel";
  long_desc = "The tunnel slopes steeply down a hole here.\n";
  dest_dir = ({
    "room/mine/tunnel24", "up",
    "room/mine/tunnel26", "north"
  });
}
