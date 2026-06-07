inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
  short_desc = "Tunnel";
  long_desc = "The tunnel slopes steeply down a hole here.\n";
  dest_dir = ({
    "room/mine/tunnel23", "west",
    "room/mine/tunnel25", "down"
  });
}
