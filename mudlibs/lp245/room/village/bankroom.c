void init() {
  add_action("west", "west");
  add_action("open", "open");
}

void long(mixed str) {
  if (str == "door") {
    if (call_other("room/village/bank", "query_door"))
      write("The door is closed.\n");

    else
      write("The door is open.\n");

    return;
  }

  write("You are in the backroom of the bank.\n");
}

status open(mixed str) {
  if (!str) return 0;
  if (!call_other("room/village/bank", "query_door"))
    return 0;

  call_other("room/village/bank", "open_door_inside");

  say(call_other(this_player(), "query_name") +
  " opens the door.\n");

  write("Ok.\n");

  return 1;
}

void reset(mixed arg) {
  if (!arg) {
    set_light(1);
    move_object(clone_object("obj/safe"), this_object());
  }
}

string short() {
  return "backroom of bank";
}

status west() {
  if (call_other("room/village/bank", "query_door")) {
    write("The door is closed.\n");

    return 1;
  }

  call_other(this_player(), "move_player", "west#room/village/bank");

  return 1;
}
