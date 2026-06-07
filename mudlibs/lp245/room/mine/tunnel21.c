inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
  short_desc = "Tunnel";
  long_desc = "Tunnel into the mines.\n";
  dest_dir = ({
    "room/mine/tunnel20", "west",
    "room/mine/tunnel22", "east"
  });
}
