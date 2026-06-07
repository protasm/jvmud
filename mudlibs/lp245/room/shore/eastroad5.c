inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "East road";
  long_desc = "East road runs south from here.\n"+
  "To the west lies the Eastroad Inn.\n";
  dest_dir = ({
    "room/shore/eastroad4", "south",
    "room/village/inn", "west"
  });
}
