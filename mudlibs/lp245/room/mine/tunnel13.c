inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
  short_desc = "Tunnel";
  long_desc = "End of the tunnel.\n";
  dest_dir = ({
    "room/mine/tunnel12", "south"
  });
}
