void initialize(mixed first_load) {
}
void offer_interactions() { jvmud_add_action("west", "west"); jvmud_add_action("west", "w"); }
string short() { return "Greyhaven Guild Row"; }
void describe(object viewer) {
  write("Greyhaven Guild Row\n");
  write("Carved signs mark the halls of weavers, chandlers, carters, millers, ");
  write("and apothecaries. Apprentices cross between shared lecture rooms while ");
  write("journeymasters post wages and examination dates in public.\n\n");
  write("Market Cross is west.\n");
}
int west(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "west", "place/greyhaven/market_cross");
}
