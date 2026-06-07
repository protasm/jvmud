inherit "room/room";
void reset(mixed arg) {
  if (!arg) {
    set_light(1);

    short_desc = "Village track";

    long_desc =
    "A track going into the village. The track opens up to a road to the east\n" +
    "and ends with a green lawn to the west.\n";
    dest_dir = ({
      "room/village/vill_green", "west",
      "room/village/vill_road1", "east"
    });
  }

  no_castle_flag = 1;
}
