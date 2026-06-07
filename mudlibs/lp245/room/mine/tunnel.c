void reset(mixed arg) {
  if (arg)
    return;

  set_light(1);
}

string short() {
  if (set_light(0))
    return "Mine entrance";

  return "dark room";
}

void init() {
  add_action("move1", "south");
  add_action("move2", "north");
}

status move1() {
  call_other(this_player(), "move_player", "south#room/mountain/mount_pass");

  return 1;
}

status move2() {
  call_other(this_player(), "move_player", "north#room/mine/tunnel2");

  return 1;
}

void long(mixed str) {
  if (set_light(0) == 0) {
    write("It is dark.\n");

    return;
  }

  if (id(str)) {
    write("WARNING !!\n\n"+
    "The mines are closed due to risk of falling rock.\n");

    return;
  }

  write("This is the entrance to the mines.\nThere is a sign on a pole.\n");
  write("There are two obvious exits, south and north.\n");
}

status id(mixed str) {
  return str == "sign" || str == "pole";
}
