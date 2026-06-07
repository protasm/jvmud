mixed rope;

void reset(mixed arg) {
  if (arg)
    return;

  set_light(0);
}

string short() {
  if (set_light(0))
    return "Hole";

  return "dark room";
}

void init() {
  add_action("move1", "south");
  add_action("move2", "north");
  add_action("down"); add_verb("down");
  add_action("down"); add_verb("climb");
}

status move1() {
  call_other(this_player(), "move_player", "south#room/mine/tunnel2");

  return 1;
}

status move2() {
  call_other(this_player(), "move_player", "north#room/mine/tunnel4");

  return 1;
}

void long(mixed str) {
  if (set_light(0) == 0) {
    write("It is dark.\n");

    return;
  }

  if (str == "ring" || str == "rings") {
    write("A sturdy iron ring, fastened to the wall.\n");

    return;
  }

  write("There is a big hole here, and some kind of iron rings in the wall.\n" +
  "It is should be possible to pass the hole.\n");
  write("There are two obvious exits, south and north.\n");
}

status down() {
  if (!rope) {
    write("You would fall down the hole and possible hurt yourself.\n");

    return 1;
  }

  call_other(this_player(), "move_player", "down#room/mine/tunnel8");

  return 1;
}

status id(mixed str) {
  return str == "ring" || str == "rings";
}

mixed query_rope() {
  return rope;
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
