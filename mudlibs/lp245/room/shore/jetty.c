inherit "room/room";

void reset(mixed arg) {
  if (!arg) {
    set_light(1);
    short_desc = "Road";
    long_desc = "You are on a road going out of the village. To the east the road widens out\n"+
    "as it leads down to the shore. To the west lies the city.\n";
    dest_dir = ({
      "room/shore/vill_shore", "west",
      "room/shore/vill_shore2", "east"
    });
  }

  no_castle_flag=1;
}
