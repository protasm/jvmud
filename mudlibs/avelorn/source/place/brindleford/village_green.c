void initialize(mixed first_load) {
  object citizen;

  if (!jvmud_find_entity("oren", jvmud_current_lpc_object())) {
    citizen = jvmud_clone_lpc_object("npc/citizen");
    jvmud_invoke_lpc_object(
        citizen,
        "configure",
        "Oren Halward",
        "male",
        "village reeve",
        "responsibility for the Crown's measures and ordinary village disputes");
    jvmud_invoke_lpc_object(citizen, "add_identity", "oren");
    jvmud_invoke_lpc_object(citizen, "add_identity", "reeve");
    jvmud_move_entity(citizen, jvmud_current_lpc_object());
  }

  if (!jvmud_find_entity("elara", jvmud_current_lpc_object())) {
    citizen = jvmud_clone_lpc_object("npc/citizen");
    jvmud_invoke_lpc_object(
        citizen,
        "configure",
        "Sister Elara",
        "female",
        "keeper of the village shrine",
        "charge of Brindleford's sickroom, schoolroom, and roadside lamps");
    jvmud_invoke_lpc_object(citizen, "add_identity", "elara");
    jvmud_invoke_lpc_object(citizen, "add_identity", "sister");
    jvmud_invoke_lpc_object(citizen, "set_quest", "light-for-the-road");
    jvmud_move_entity(citizen, jvmud_current_lpc_object());
  }

  if (!jvmud_find_entity("rowan", jvmud_current_lpc_object())) {
    citizen = jvmud_clone_lpc_object("npc/citizen");
    jvmud_invoke_lpc_object(
        citizen,
        "configure",
        "Rowan Mere",
        "non-binary",
        "Company quartermaster",
        "prepared the chapter house for a new class of Companions");
    jvmud_invoke_lpc_object(citizen, "add_identity", "rowan");
    jvmud_invoke_lpc_object(citizen, "add_identity", "quartermaster");
    jvmud_move_entity(citizen, jvmud_current_lpc_object());
  }
}

void offer_interactions() {
  jvmud_add_action("north", "north");
  jvmud_add_action("north", "n");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
  jvmud_add_action("south", "south");
  jvmud_add_action("south", "s");
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
}

string short() {
  return "Brindleford Village Green";
}

void describe(object viewer) {
  write("Brindleford Village Green\n");
  write("A broad green lies at the heart of a prosperous farming village. ");
  write("Whitewashed cottages face a weathered stone well, while the Crown's ");
  write("blue-and-gold banner hangs beside the reeve's hall. The people move ");
  write("with the unhurried purpose of a community accustomed to good roads, ");
  write("fair measures, and dependable law.\n\n");
  write("Reeve Oren, Sister Elara, and Quartermaster Rowan are here.\n");
  write("The Company of the Lantern maintains a small chapter house to the north.\n");
  write("The village market is east.\n");
  write("The reeve's hall is west, and the village shrine is south.\n");
}

int north(mixed ignored) {
  object actor;

  actor = jvmud_current_actor();
  return jvmud_invoke_lpc_object(
      actor,
      "travel_to",
      "north",
      "place/brindleford/lantern_house");
}

int east(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "east",
      "place/brindleford/market");
}

int south(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "south",
      "place/brindleford/shrine");
}

int west(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "west",
      "place/brindleford/reeves_hall");
}
