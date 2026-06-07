mixed rope;

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
}

string short() {
  if (set_light(0))
    return "Hole in ceiling";

  return "dark room";
}

void init() {
  add_action("move1", "west");
  add_action("move2", "east");
  add_action("go_up"); add_verb("up");
}

status move1() {
  call_other(this_player(), "move_player", "west#room/mine/tunnel10");

  return 1;
}

status move2() {
  call_other(this_player(), "move_player", "east#room/mine/tunnel14");

  return 1;
}

void long(mixed str) {
  if (set_light(0) == 0) {
    write("It is dark.\n");

    return;
  }

  if (call_other("room/mine/tunnel3", "query_rope"))
    write("There is a rope hanging down through the hole.\n");

  write("There is a big hole in the ceiling.\n");
  write("There are two obvious exits, west and east.\n");
}

status go_up() {
  if (!call_other("room/mine/tunnel3","query_rope")) {
    write("You can't go stright up with some kind of support.\n");

    return 1;
  }

  call_other(this_player(), "move_player", "up#room/mine/tunnel8");

  return 1;
}

status id(mixed str) {
  return str == "ring" || str == "rings";
}

status tie(mixed str) {
  if (str != "ring" && str != "rings")
    return 0;

  rope = 1;
  return 1;
}

status untie(mixed str) {
  rope = 0;
  return 1;
}
