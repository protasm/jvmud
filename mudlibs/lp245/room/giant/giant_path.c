inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A path";
  long_desc = "You are on a path going in east/west direction. There are some\n" +
"VERY big footsteps here.\n";
  dest_dir = ({
    "room/forest/big_tree", "east",
    "room/giant/giant_lair", "west"
  });
}
