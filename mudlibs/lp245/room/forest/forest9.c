inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "Deep forest";
  long_desc = "You are in the deep forest.\n";
  dest_dir = ({
    "room/forest/forest8", "north",
    "room/forest/forest10", "east",
    "room/forest/forest11", "west"
  });
}
