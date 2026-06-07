inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "East road";
  long_desc = "East road runs north-south.\n"+
  "Sun alley is to the west.\n";
  dest_dir = ({
    "room/shore/eastroad4", "north",
    "room/shore/eastroad2", "south",
    "room/village/sunalley1", "west"
  });
}
