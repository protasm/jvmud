inherit "room/room";
void reset(mixed arg) {
  if (!arg) {
    set_light(1);

    short_desc = "Village road";

    long_desc =
    "A long road going east through the village. The road narrows to a\n" +
    "track to the west. There is an alley to the north and the south.\n";
    dest_dir = ({
      "room/village/vill_track", "west",
      "room/village/yard", "north",
      "room/village/narr_alley", "south",
      "room/village/vill_road2", "east"
    });
  }

  no_castle_flag = 1;
}
