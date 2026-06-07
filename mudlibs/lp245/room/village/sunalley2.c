inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "Sun alley";
  long_desc = "Sun alley runs east from here.\n";
  dest_dir = ({
    "room/village/sunalley1", "east"
  });
}
