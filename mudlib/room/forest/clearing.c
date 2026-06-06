inherit "room/room";

reset(arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "Clearing";
  long_desc = "A small clearing. There are trees all around you.\n" +
  "However, the trees are sparse to the north.\n";
  dest_dir = ({
    "room/forest/forest1", "east",
    "room/forest/forest2", "west",
    "room/planes/plane1", "north"
  });
}
