inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "A slope";
  long_desc = "The forest gets light here, and slopes down to the west.\n";
  dest_dir = ({
    "room/orc/orc_vall", "west",
    "room/forest/forest2", "east",
    "room/forest/forest3", "south"
  });
}
