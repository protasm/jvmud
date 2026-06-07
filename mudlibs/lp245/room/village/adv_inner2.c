inherit "room/room";

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
  short_desc = "The LPC board";
  long_desc = "This is the LPC discussion room.\n" +
  "Only wizards can access this room.\n";
  dest_dir = ({
    "room/village/adv_inner", "north"
  });
  move_object(clone_object("obj/wiz_bull_board2"), this_object());
}
