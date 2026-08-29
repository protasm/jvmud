void initialize(mixed first_load) {
  object marshal;
  if (!jvmud_find_entity("serin", jvmud_current_lpc_object())) {
    marshal = jvmud_clone_lpc_object("npc/quest_giver");
    jvmud_invoke_lpc_object(marshal, "configure", "Marshal Serin Vale", "male", "Crown marshal of the Ashenwatch expedition", "rekindle-western-lantern");
    jvmud_invoke_lpc_object(marshal, "add_identity", "serin");
    jvmud_invoke_lpc_object(marshal, "add_identity", "marshal");
    jvmud_move_entity(marshal, jvmud_current_lpc_object());
  }
}
void offer_interactions() {
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
  jvmud_add_action("rest", "rest");
}
string short() { return "Ashenwatch Expedition Camp"; }
void describe(object viewer) {
  jvmud_write("Ashenwatch Expedition Camp\n");
  jvmud_write("Orderly pavilions surround a field shrine and command table. Royal ");
  jvmud_write("watch officers, village guides, Temple healers, Collegium adepts, and ");
  jvmud_write("Company adventurers share one roster under Marshal Serin Vale.\n\n");
  jvmud_write("The ridge approach is east, and Ashenwatch's lower gate is west. You may rest here.\n");
}
int east(mixed ignored) { return travel("east", "place/ashenwatch/approach"); }
int west(mixed ignored) { return travel("west", "place/ashenwatch/lower_gate"); }
int rest(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "rest_at_shrine"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
