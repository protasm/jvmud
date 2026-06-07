string bag;

void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
}

string short() {
  if (set_light(0))
    return "All at sea";

  return "dark room";
}

void init() {
  add_action("move", "west");
  add_action("dive", "dive");
}

status move() {
  call_other(this_player(), "move_player", "west#room/shore/jetty2");

  return 1;
}

void long(mixed str) {
  if (set_light(0) == 0) {
    write("It is dark.\n");

    return;
  }

  write("You are swimming out at the sea.\n");
  write("    The only obvious exit is west.\n");
}

status dive() {
  object ob;

  ob = first_inventory(this_player());

  while(ob) {
    if (call_other(ob, "can_put_and_get", 0)) {
      call_other(this_player(), "move_player", "down#room/shore/sea_bottom");

      return 1;
    }

    ob = next_inventory(ob);
  }

  write("You can't breathe under water !\n");
  write("You should try to get some portable air supply!\n");

  return 1;
}
