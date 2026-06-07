inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "Fields";
  long_desc = "You are in the middle of the fields where the city grows all its crops.\n"+
  "A road runs north of here.\n";
  dest_dir = ({
    "room/shore/vill_shore", "north"
  });
}
