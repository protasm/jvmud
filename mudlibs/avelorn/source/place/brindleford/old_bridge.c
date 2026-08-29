void initialize(mixed first_load) {
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
}

string short() {
  return "Old Brindle Bridge";
}

void describe(object viewer) {
  jvmud_write("Old Brindle Bridge\n");
  jvmud_write("Three low arches carry the royal road over the bright River Brindle. ");
  jvmud_write("The masons' date-stone honors both Queen Meriel and the local hundred ");
  jvmud_write("whose levy rebuilt the bridge, a compact between realm and community ");
  jvmud_write("that has endured for eighty peaceful years.\n\n");
  jvmud_write("The road returns west. The farther eastern road is not yet open.\n");
}

int west(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "west",
      "place/brindleford/east_road");
}
