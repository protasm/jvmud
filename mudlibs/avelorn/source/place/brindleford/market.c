void initialize(mixed first_load) {
  object shop;

  if (!jvmud_find_entity("outfitter", jvmud_current_lpc_object())) {
    shop = jvmud_clone_lpc_object("shop/brindleford_outfitter");
    jvmud_move_entity(shop, jvmud_current_lpc_object());
  }
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
  jvmud_add_action("east", "east");
  jvmud_add_action("east", "e");
}

string short() {
  return "Brindleford Market";
}

void describe(object viewer) {
  write("Brindleford Market\n");
  write("Canvas awnings shade orderly rows of farm produce, woolens, lamp oil, ");
  write("and road supplies. A clerk checks the Crown stamps on weights while ");
  write("neighbors exchange news beside the outfitter's permanent oak counter.\n\n");
  write("The village green is west, and Mill Road runs east. ");
  write("The outfitter invites you to type list, buy, or sell.\n");
}

int west(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "west",
      "place/brindleford/village_green");
}

int east(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "east",
      "place/brindleford/mill_road");
}
