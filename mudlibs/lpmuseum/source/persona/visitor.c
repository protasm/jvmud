string display_name;
string persona_name;

void reset(mixed first_load) {
  if (!display_name) {
    display_name = "visitor";
  }
  if (!persona_name) {
    persona_name = "visitor";
  }
}

void connect() {
  enable_commands();
  write("What is your name today? ");
  input_to("choose_name");
}

void choose_name(mixed name) {
  if (!name || strlen(name) == 0) {
    persona_name = "visitor";
  } else {
    persona_name = lower_case(name);
  }
  display_name = capitalize(persona_name);

  write("Hi, " + display_name + "! Welcome to LPMuseum.\n");
  tell_place(environment(this_object()), display_name + " enters LPMuseum through the museum doors.\n");
  write("This is a native JVMud mudlib. Type help for museum commands.\n\n");
  look(0);
}

void disconnect() {
}

void init() {
  add_action("look", "look");
  add_action("look", "l");
  add_action("go", "go");
  add_action("direction", "north");
  add_action("direction", "n");
  add_action("direction", "east");
  add_action("direction", "e");
  add_action("direction", "south");
  add_action("direction", "s");
  add_action("direction", "west");
  add_action("direction", "w");
  add_action("direction", "up");
  add_action("direction", "u");
  add_action("direction", "down");
  add_action("direction", "d");
  add_action("examine", "examine");
  add_action("examine", "exa");
  add_action("take", "take");
  add_action("take", "get");
  add_action("drop", "drop");
  add_action("inventory", "inventory");
  add_action("inventory", "i");
  add_action("help", "help");
  add_action("who", "who");
  add_action("whoami", "whoami");
  add_action("quit", "quit");
  add_action("quit", "exit");
  add_action("say_command", "say");
  call_other("system/socials", "register", 0);
}

string query_name() {
  return display_name;
}

string query_real_name() {
  return display_name;
}

string short() {
  return display_name;
}

void describe(object viewer) {
  if (viewer == this_object()) {
    write("You are " + display_name + ", a visiting Persona exploring LPMuseum.\n");
  } else {
    write(display_name + " is a visiting Persona exploring LPMuseum.\n");
  }
}

int id(mixed value) {
  if (!value) {
    return 0;
  }
  value = lower_case(value);
  return value == persona_name || value == lower_case(display_name)
      || value == "visitor" || value == "persona" || value == "me";
}

int look(mixed target) {
  object place;
  object item;

  place = environment(this_object());
  if (!target || target == "around") {
    call_other(place, "describe", this_object());
    return 1;
  }

  item = present(target, place);
  if (!item) {
    item = present(target, this_object());
  }
  if (!item) {
    write("You do not see that here.\n");
    return 1;
  }

  call_other(item, "describe", this_object());
  return 1;
}

int examine(mixed target) {
  if (!target) {
    write("Examine what?\n");
    return 1;
  }

  return look(target);
}

int take(mixed target) {
  object place;
  object item;

  if (!target) {
    write("Take what?\n");
    return 1;
  }

  place = environment(this_object());
  item = present(target, place);
  if (!item) {
    write("You do not see that here.\n");
    return 1;
  }
  if (!call_other(item, "can_take", this_object())) {
    write("That is part of the museum display.\n");
    return 1;
  }

  move_object(item, this_object());
  write("You take " + call_other(item, "short") + ".\n");
  tell_place_except(place, display_name + " takes " + call_other(item, "short") + ".\n", this_object());
  return 1;
}

int drop(mixed target) {
  object place;
  object item;

  if (!target) {
    write("Drop what?\n");
    return 1;
  }

  item = present(target, this_object());
  if (!item) {
    write("You are not carrying that.\n");
    return 1;
  }

  place = environment(this_object());
  move_object(item, place);
  write("You drop " + call_other(item, "short") + ".\n");
  tell_place_except(place, display_name + " drops " + call_other(item, "short") + ".\n", this_object());
  return 1;
}

int go(mixed destination) {
  object place;

  if (!destination) {
    write("Go where?\n");
    return 1;
  }

  place = environment(this_object());
  return call_other(place, "go", destination);
}

int direction(mixed ignored) {
  return go(query_verb());
}

int move_player(mixed movement) {
  string destination;
  string direction;
  int separator;
  object old_place;
  object new_place;

  if (!movement) {
    return 0;
  }

  destination = movement;
  direction = movement;
  separator = 0;
  while (separator < strlen(destination) && destination[separator] != '#') {
    separator = separator + 1;
  }
  if (separator < strlen(destination)) {
    direction = destination[0..separator - 1];
    destination = destination[separator + 1..];
  }

  old_place = environment(this_object());
  tell_place_except(old_place, display_name + " leaves " + direction + ".\n", this_object());
  move_object(this_object(), destination);
  new_place = environment(this_object());
  tell_place_except(new_place, display_name + " arrives.\n", this_object());
  call_other(new_place, "describe", this_object());
  return 1;
}

int quit(mixed ignored) {
  write("You step away from LPMuseum.\n");
  destruct(this_object());
  return 1;
}

int say_command(mixed text) {
  string target_name;
  string message;
  int separator;
  object place;
  object target;

  if (!text) {
    write("Say what?\n");
    return 1;
  }

  place = environment(this_object());
  message = text;
  target = 0;

  if (strlen(text) > 3 && text[0..2] == "to ") {
    separator = 3;
    while (separator < strlen(text) && text[separator] != ' ') {
      separator = separator + 1;
    }
    if (separator >= strlen(text) - 1) {
      write("Say what to whom?\n");
      return 1;
    }

    target_name = text[3..separator - 1];
    message = text[separator + 1..];
    target = present(target_name, place);
    if (!target) {
      write("You do not see " + target_name + " here.\n");
      return 1;
    }
  }

  if (target) {
    tell_place(place, query_name() + " says to " + call_other(target, "short") + ": " + message + "\n");
  } else {
    tell_place(place, query_name() + " says: " + message + "\n");
  }
  return 1;
}

int inventory(mixed ignored) {
  object item;

  item = first_inventory(this_object());
  if (!item) {
    write("You are carrying nothing.\n");
    return 1;
  }

  write("You are carrying:\n");
  while (item) {
    write("  " + call_other(item, "short") + "\n");
    item = next_inventory(item);
  }
  return 1;
}

int help(mixed topic) {
  call_other("system/help", "show", topic);
  return 1;
}

int who(mixed ignored) {
  object *connected;
  object persona;
  int index;
  int count;

  connected = users();
  count = sizeof(connected);
  write("Connected Personas in LPMuseum: " + count + "\n");
  index = 0;
  while (index < count) {
    persona = connected[index];
    write("  " + call_other(persona, "query_name") + "  " + object_name(persona));
    write("  from " + query_ip_number(persona));
    write("  idle " + query_idle(persona) + "s\n");
    index = index + 1;
  }
  return 1;
}

int whoami(mixed ignored) {
  write("You are " + display_name + ", a JVMud Player with this Telnet Session bound to a museum Persona.\n");
  return 1;
}

void list_present_personas(object viewer) {
  object *connected;
  object persona;
  int index;

  connected = users();
  index = 0;
  while (index < sizeof(connected)) {
    persona = connected[index];
    if (persona != viewer && environment(persona) == environment(viewer)) {
      write(call_other(persona, "query_name") + " is here.\n");
    }
    index = index + 1;
  }
}

void list_vended_entities(object viewer) {
  object place;
  object item;

  place = environment(viewer);
  item = first_inventory(place);
  while (item) {
    if (item != viewer && call_other(item, "is_vended_entity")) {
      write(call_other(item, "short") + "\n");
    }
    item = next_inventory(item);
  }
}
