inherit "room/room";
void reset(mixed arg) {
  if (!arg) {
    set_light(1);

    short_desc = "Humpbacked bridge";
    long_desc = "An old humpbacked bridge.\n";

    dest_dir = ({
      "room/village/vill_green", "east",
      "room/forest/wild1", "west"
    });
  }

  no_castle_flag = 1;

  if (!present("stick")) {
    object stick;

    stick = clone_object("obj/torch");

    move_object(stick, "room/mountain/hump");
    call_other(stick, "set_name", "stick");
    call_other(stick, "set_fuel", 500);
    call_other(stick, "set_weight", 1);
  }

  if (!present("money")) {
    object money;

    money = clone_object("obj/money");

    move_object(money, "room/mountain/hump");
    call_other(money, "set_money", 10);
  }
}
