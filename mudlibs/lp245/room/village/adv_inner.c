inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "The inner room of adventurers guild";
  long_desc = "This is the inner room of adventures guild. If you want to discuss LPC,\n" +
  "then move to the room south from here.\n" +
  "Only wizards can access this room.\n";
  dest_dir = ({
    "room/village/adv_guild", "north",
    "room/village/adv_inner2", "south"
  });
  move_object(clone_object("obj/wiz_bull_board"), this_object());
}
