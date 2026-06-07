inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "Sun alley";
  long_desc = "Sun alley runs east-west.\n";
  dest_dir = ({
    "room/village/sunalley2", "west",
    "room/shore/eastroad3", "east"
  });
}
