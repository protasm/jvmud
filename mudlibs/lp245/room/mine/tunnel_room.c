inherit "room/room";

object hobgoblin;

void reset(mixed arg) {
  if (!hobgoblin || !living(hobgoblin)) {
    object money;

    hobgoblin = clone_object("obj/monster");

    call_other(hobgoblin, "set_name", "hobgoblin");
    call_other(hobgoblin, "set_level", 5);
    call_other(hobgoblin, "set_wc", 9);
    call_other(hobgoblin, "set_short", "a hobgoblin");

    call_other(hobgoblin, "set_long",
    "This hobgoblin looks really nasty.\n");

    move_object(hobgoblin, this_object());

    money = clone_object("obj/money");

    call_other(money, "set_money", random(50));
    move_object(money, hobgoblin);
  }

  if (arg)
    return;

  set_light(0);
  short_desc = "small room";
  long_desc = "A small room with rough cut walls.\n";
  dest_dir = ({
    "room/mine/tunnel5", "south"
  });
}
