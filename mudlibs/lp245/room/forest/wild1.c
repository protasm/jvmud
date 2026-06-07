inherit "room/room";
void reset(mixed arg) {
  if (!arg) {
    set_light(1);

    short_desc = "Wilderness";

    long_desc =
    "You are in the wilderness outside the village.\n" +
    "There is a big forest to the west.\n";
    dest_dir = ({
      "room/mountain/hump", "east",
      "room/forest/forest1", "west"
    });
  }

  no_castle_flag = 1;
}
