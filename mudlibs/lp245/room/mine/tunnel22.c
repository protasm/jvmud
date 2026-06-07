inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
  short_desc = "Tunnel";
  long_desc = "Tunnel fork.\n";
  dest_dir = ({
    "room/mine/tunnel19", "south",
    "room/mine/tunnel21", "west",
    "room/mine/tunnel23", "east"
  });
}
