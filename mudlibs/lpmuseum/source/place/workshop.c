void reset(mixed first_load) {
  object machine;

  if (!present("vending machine", this_object())) {
    machine = clone_object("entity/vending_machine");
    move_object(machine, this_object());
  }
}

void init() {
  add_action("west", "west");
  add_action("west", "w");
  add_action("go", "go");
  add_action("demo", "demo");
}

void describe(object viewer) {
  write("Creator Workshop\n");
  write("Benches here show how native JVMud mudlib authors can keep policy in LPC objects.\n");
  write("The Persona owns general commands; Places own exits; Entities own local affordances.\n");
  write("Try demo time, demo users, demo inventory, demo dispatch, or demo signal.\n");
  write("The concourse is west.\n");
  if (present("vending machine", this_object())) {
    write("Entity Vending Machine\n");
  }
  if (present("staffer", this_object())) {
    write("Museum Security Staffer\n");
  }
  call_other(viewer, "list_vended_entities", viewer);
  call_other(viewer, "list_present_personas", viewer);
}

void long(mixed str) {
  describe(this_player());
}

string short() {
  return "Creator Workshop";
}

int go(mixed destination) {
  if (destination == "west" || destination == "concourse") {
    return west(0);
  }

  write("You can't go that way.\n");
  return 1;
}

int west(mixed str) {
  return call_other(this_player(), "move_player", "west#place/concourse");
}

int demo(mixed topic) {
  object *connected;
  object actor;

  actor = this_player();
  if (topic == "time") {
    write("time() -> " + time() + "\n");
    write("ctime(time()) -> " + ctime(time()) + "\n");
    return 1;
  }
  if (topic == "users") {
    connected = users();
    write("users() -> " + sizeof(connected) + " connected Persona(s)\n");
    write("query_ip_number(this_player()) -> " + query_ip_number(actor) + "\n");
    write("query_idle(this_player()) -> " + query_idle(actor) + "\n");
    return 1;
  }
  if (topic == "inventory") {
    write("first_inventory(this_player()) -> " + first_inventory(actor) + "\n");
    return 1;
  }
  if (topic == "dispatch") {
    write("Dispatching look through the command router.\n");
    command("look");
    return 1;
  }
  if (topic == "signal") {
    tell_object(actor, "Targeted output reached only your Persona.\n");
    tell_place(environment(actor), "Ambient output ripples through the workshop.\n");
    return 1;
  }

  write("Try demo time, demo users, demo inventory, demo dispatch, or demo signal.\n");
  return 1;
}
