inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A large open plain";
  long_desc = "A large open plain. There is a forest to the west\n";
  dest_dir = ({
    "room/forest/deep_forest1", "west",
    "room/plains/plain11", "east",
    "room/plains/plain10", "south"
  });
}
