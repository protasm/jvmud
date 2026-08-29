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
}

string short() {
  return "Brindleford Market";
}

void describe(object viewer) {
  jvmud_write("Brindleford Market\n");
  jvmud_write("Canvas awnings shade orderly rows of farm produce, woolens, lamp oil, ");
  jvmud_write("and road supplies. A clerk checks the Crown stamps on weights while ");
  jvmud_write("neighbors exchange news beside the outfitter's permanent oak counter.\n\n");
  jvmud_write("The village green is west. The outfitter invites you to type list, buy, or sell.\n");
}

int west(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "west",
      "place/brindleford/village_green");
}
