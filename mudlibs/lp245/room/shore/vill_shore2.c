inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "Village shore";
  long_desc = "The village shore. A jetty leads out to the east. To the north some stairs\n"+
  "leads down to the north beach. A road starts to the west\n";
  dest_dir = ({
    "room/shore/jetty", "west",
    "room/shore/jetty2", "east"
  });
}
