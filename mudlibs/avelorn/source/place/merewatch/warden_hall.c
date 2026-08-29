void initialize(mixed first_load) {
  object surveyor;
  if (!jvmud_find_entity("maelin", jvmud_current_lpc_object())) {
    surveyor = jvmud_clone_lpc_object("npc/quest_giver");
    jvmud_invoke_lpc_object(surveyor, "configure", "Royal Surveyor Maelin Dorr", "female", "surveyor of the western wardworks", "beneath-blackstone");
    jvmud_invoke_lpc_object(surveyor, "add_identity", "maelin");
    jvmud_invoke_lpc_object(surveyor, "add_identity", "surveyor");
    jvmud_move_entity(surveyor, jvmud_current_lpc_object());
  }
}
void offer_interactions() { jvmud_add_action("west", "west"); jvmud_add_action("west", "w"); }
string short() { return "Merewatch Warden Hall"; }
void describe(object viewer) {
  jvmud_write("Merewatch Warden Hall\n");
  jvmud_write("Maps of lake levels, beacon lines, and ancient ward tunnels cover a ");
  jvmud_write("long table. Local reed wardens work beside Crown surveyors, joining ");
  jvmud_write("practical memory to the kingdom's instruments and archives.\n\n");
  jvmud_write("Royal Surveyor Maelin Dorr is here. Merewatch Square is west.\n");
}
int west(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "west", "place/merewatch/mere_square"); }
