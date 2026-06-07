inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "Deep forest";
  long_desc = "In the deep forest. The wood lights up to the east.\n";
  dest_dir = ({
    "room/planes/plane12", "east"
  });
}
