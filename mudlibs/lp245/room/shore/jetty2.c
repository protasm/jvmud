inherit "room/room";

void reset(mixed arg) {
  if (!arg) {
    set_light(1);
    short_desc = "Jetty";
    long_desc = "You are at a jetty. The waves rolls in from east.\nA small path leads back to west.\n";
    dest_dir = ({
      "room/shore/vill_shore2", "west",
      "room/shore/sea", "east"
    });
  }

  /* no castle drop here... its a jetty, how can anything be placed north &
  south of here... there is nothing but water around, place it in sea */
  no_castle_flag=1;

  if (!present("bag"))
    move_object(clone_object("obj/bag"), this_object());
}
