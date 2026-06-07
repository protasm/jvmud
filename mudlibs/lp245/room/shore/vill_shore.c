inherit "room/room";

void reset(mixed arg) {
  if (!arg) {
    set_light(1);
    short_desc = "Road";
    long_desc = "You are on a road going out of the village. Eastroad runs north from here,\n"+
    "along the eastern perimeter of the city, and to the south are some fields\n"+
    "planted with all the crops that the city needs. The main road runs towards\n"+
    "the shore to the east, and into the city to the west.\n";
    dest_dir = ({
      "room/village/vill_road2", "west",
      "room/shore/jetty", "east",
      "room/shore/eastroad1", "north",
      "room/shore/crop", "south"
    });
  }

  no_castle_flag = 1;
}
