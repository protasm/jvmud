inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
  short_desc = "Tunnel";
  long_desc = "In the tunnel into the mines.\n";
  dest_dir = ({
    "room/mine/tunnel16", "east",
    "room/mine/tunnel14", "west"
  });
}
