int lamp_is_lit;
object leo;

status close(mixed str) {
  if (str != "door")
    return 0;

  call_other("room/village/elevator", "close_door", "door");

  return 1;
}

void elevator_arrives() {
  say("The lamp on the button beside the elevator goes out.\n");

  lamp_is_lit = 0;
}

void init() {
  add_action("west", "west");
  add_action("open", "open");
  add_action("close", "close");
  add_action("push", "push");
  add_action("north", "north");
  add_action("south", "south");
}

void long() {
  write("You are in the hall of the wizards.\n" +
  "There is a door to the west and a shimmering field to the north.\n");

  if (lamp_is_lit)
    write("There is a lit lamp beside the elevator.\n");
}

status north() {
  if (call_other(this_player(),"query_level") < 21) {
    write("A strong magic force stops you.\n");

    return 1;
  }

  write("You wriggle through the force field...\n");
  call_other(this_player(), "move_player", "north#room/village/quest_room");

  return 1;
}

status open(mixed str) {
  if (str != "door")
    return 0;

  if (call_other("room/village/elevator", "query_level", 0) != 1) {
    write("You can't when the elevator isn't here.\n");

    return 1;
  }

  call_other("room/village/elevator", "open_door", "door");

  return 1;
}

status push(mixed str) {
  if (str && str != "button")
    return 0;

  if (call_other("room/village/elevator", "call_elevator", 1))
    lamp_is_lit = 1;

  return 1;
}

void reset(mixed arg) {
  if (!arg)
    set_light(1);

  if (!leo) {
    leo = clone_object("obj/leo");

    move_object(leo, this_object());
  }
}

string short() {
  return "wizards hall";
}

status west() {
  if (call_other("room/village/elevator", "query_door", 0) ||
    call_other("room/village/elevator", "query_level", 0) != 1) {

    write("The door is closed.\n");

    return 1;
  }
  call_other(this_player(), "move_player", "west#room/village/elevator");

  return 1;
}
