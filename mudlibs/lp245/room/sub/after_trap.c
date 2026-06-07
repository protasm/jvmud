object rat;

void reset(mixed arg) {
  extra_reset();

  if (arg)
    return;

  set_light(0);
}

string short() {
  if (set_light(0))
    return "Black room";

  return "dark room";
}

void init() {
  add_action("move", "east");
}

status move() {
  if (call_other("room/sub/door_trap", "query_west_door") == 0) {
    write("The door is closed.\n");

    return 1;
  }

  call_other(this_player(), "move_player", "east#room/sub/door_trap");

  return 1;
}

void long(mixed str) {
  if (set_light(0) == 0) {
    write("It is dark.\n");

    return;
  }

  write("This is the black room.\n");
  write("    The only obvious exit is east.\n");
}

void extra_reset() {
  object black_stone;

  if (!rat || !living(rat)) {
    rat = clone_object("obj/monster");

    call_other(rat, "set_name", "rat");
    call_other(rat, "set_alias", "black rat");
    call_other(rat, "set_level", 3);
    call_other(rat, "set_short", "An ugly black rat");
    call_other(rat, "set_wc", 5);
    call_other(rat, "set_agressive", 1);
    move_object(rat, this_object());

    black_stone = clone_object("obj/treasure");

    call_other(black_stone, "set_id", "stone");
    call_other(black_stone, "set_alias", "black stone");
    call_other(black_stone, "set_short", "A black stone");
    call_other(black_stone, "set_value", 60);
    move_object(black_stone, rat);
  }
}
