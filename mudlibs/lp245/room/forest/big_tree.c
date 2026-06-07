string bag;

void reset(mixed arg) {
  if (!present("rope"))
    move_object(clone_object("obj/rope"), this_object());

  if (arg)
    return;

  set_light(1);
}

string short() {
  if (set_light(0))
    return "Big tree";

  return "dark room";
}

void init() {
  add_action("move1", "east");
  add_action("move2", "west");
  add_action("climb", "climb");
}

status move1() {
  call_other(this_player(), "move_player", "east#room/plains/plain7");

  return 1;
}

status move2() {
  call_other(this_player(), "move_player", "west#room/giant/giant_path");

  return 1;
}

void long(mixed str) {
  if (set_light(0) == 0) {
    write("It is dark.\n");

    return;
  }

  write("A big single tree on the plain.\n");
  write("There are two obvious exits, east and west.\n");
}

status climb(mixed str) {
  if (!id(str))
    return 0;

  write("There are no low branches.\n");

  return 1;
}

status id(mixed str) {
  if (str == "tree" || str == "big tree")
    return 1;

  return 0;
}

status tie(mixed str) {
  if (str == "tree" || str == "big tree") {
    write("The branches are very high up.\n");

    return 0;
  }

  return 0;
}
