// Each room owns its prose, exits and residents. No game rules live in Java.
string title;
string description;
string exit_text;
mapping routes;

string short() {
  return title;
}

mapping exits() {
  return routes;
}

string destination(string direction) {
  return routes[direction];
}

void describe(object viewer) {
  object occupant;

  jvmud_write_to_lpc_object(viewer, title + "\n" + description + "\nExits: " + exit_text + ".\n");

  occupant = jvmud_first_entity_at(jvmud_current_lpc_object());

  while (occupant) {
    if (occupant != viewer) jvmud_write_to_lpc_object(viewer, jvmud_invoke_lpc_object(occupant, "short") + " is here.\n");

    occupant = jvmud_next_entity_at(occupant);
  }
}

void long(mixed ignored) {
  describe(jvmud_current_actor());
}

void resident(string name, string prose, string reply, int health, int damage) {
  object npc;

  npc = jvmud_clone_lpc_object("npc/villager");

  jvmud_invoke_lpc_object(npc, "configure", name, prose, reply, health, damage);

  jvmud_move_entity(npc, jvmud_current_lpc_object());
}
