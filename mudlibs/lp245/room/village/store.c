#define MAX_LIST  30
object name_of_item;
int item_value;

status buy(mixed item) {
  name_of_item = present(item);

  if (!name_of_item) {
    write("No such item in the store.\n");

    return 0;
  }

  item_value = call_other(name_of_item, "query_value", 0);

  if (!item_value) {
    write("Item has no value.\n");

    return 0;
  }

  item_value *= 2;

  if (call_other(this_player(), "query_money", 0) < item_value) {
    write("It would cost you ");
    write(item_value); write(" gold coins, which you don't have.\n");

    return 0;
  }

  if (!call_other(this_player(), "add_weight",
    call_other(name_of_item, "query_weight"))) {

    write("You can't carry that much.\n");

    return 0;
  }
  call_other(this_player(), "add_money", 0 - item_value);
  move_object(name_of_item, this_player());
  write("Ok.\n");
  say(call_other(this_player(), "query_name") + " buys " + item + ".\n");

  return 1;
}

void heart_beat() {
  object ob, next_ob;

  ob = first_inventory(this_object());

  while(ob) {
    next_ob = next_inventory(ob);

    destruct(ob);

    ob = next_ob;
  }
}

void init() {
  add_action("south", "south");
}

void inventory(mixed str) {
  object ob;
  int max;

  if (!str)
    str = "all";

  max = MAX_LIST;
  ob = first_inventory(this_object());

  while(ob && max > 0) {
    if (str == "all") {
      list(ob);

      max -= 1;
    }

    if (str == "weapons" && call_other(ob, "weapon_class", 0)) {
      list(ob);

      max -= 1;
    }

    if (str == "armours" && call_other(ob, "armour_class", 0)) {
      list(ob);

      max -= 1;
    }

    ob = next_inventory(ob);
  }
}

void list(object ob) {
  int item_value;

  item_value = call_other(ob, "query_value", 0);

  if (item_value) {
    write(item_value*2 + ":\t" + call_other(ob, "short") + ".\n");
  }
}

void long() {
  write("All things from the shop are stored here.\n");
}

void reset(mixed arg) {
  if (!arg)
    set_light(1);

  if (!present("torch")) {
    object torch;

    torch = clone_object("obj/torch");

    call_other(torch, "set_name", "torch");
    call_other(torch, "set_fuel", 2000);
    call_other(torch, "set_weight", 1);
    move_object(torch, this_object());
  }
}

string short() {
  return "store room for the shop";
}

status south() {
  call_other(this_player(), "move_player", "leaves#room/village/shop");

  return 1;
}

void store(object item) {
  string short_desc;
  object ob;

  short_desc = call_other(item, "short");
  ob = first_inventory(this_object());

  while(ob) {
    if (call_other(ob, "short") == short_desc) {
      /* Move it before destruct, because the weight
      has already been compensated for. */

      move_object(item, this_object());
      destruct(item);

      return;
    }

    ob = next_inventory(ob);
  }

  move_object(item, this_object());
}

status value(mixed item) {
  name_of_item = present(item);

  if (!name_of_item) {
    return 0;
  }

  item_value = call_other(name_of_item, "query_value", 0);

  if (!item_value) {
    return 0;
  }

  write("The "); write(item); write(" would cost you ");
  write(item_value*2); write(" gold coins.\n");

  return 1;
}
