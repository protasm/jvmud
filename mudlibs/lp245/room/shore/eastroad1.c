inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "East road";
  long_desc = "East road runs north-south.\n";
  dest_dir = ({
    "room/shore/eastroad2", "north",
    "room/shore/vill_shore", "south"
  });
}
