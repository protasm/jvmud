inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
  short_desc = "Tunnel";
  long_desc = "The tunnel splits up in a fork forward.\n";
  dest_dir = ({
    "room/mine/tunnel17", "south",
    "room/mine/tunnel22", "north"
  });
}
