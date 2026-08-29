void initialize(mixed first_load) { spawn_guardian(); jvmud_schedule_recurring_tick(1, 90); }
void scheduled_update() { spawn_guardian(); }
void spawn_guardian() {
  object guardian;
  if (!jvmud_find_entity("guardian", jvmud_current_lpc_object())) {
    guardian = jvmud_clone_lpc_object("npc/hostile");
    jvmud_invoke_lpc_object(guardian, "configure", "drowned stone guardian", "non-binary", "Water streams through the joints of an armored basalt sentinel.", 5, 95, 8, 14, 275, 40);
    jvmud_invoke_lpc_object(guardian, "add_identity", "guardian");
    jvmud_invoke_lpc_object(guardian, "add_identity", "drowned guardian");
    jvmud_invoke_lpc_object(guardian, "set_quest_defeat_tag", "blackstone-water-guardian");
    jvmud_move_entity(guardian, jvmud_current_lpc_object());
  }
}
void offer_interactions() { jvmud_add_action("west", "west"); jvmud_add_action("west", "w"); }
string short() { return "Blackstone Flooded Gallery"; }
void describe(object viewer) {
  write("Blackstone Flooded Gallery\n");
  write("Spring water covers a mosaic of crowned lanterns. A broken sluice has ");
  write("turned the chamber's guardian rite inward, binding old stone to the ");
  write("ward-soot gathering beneath the surface.\n\n");
  if (jvmud_find_entity("guardian", jvmud_current_lpc_object())) { write("A drowned stone guardian blocks the sluice.\n"); }
  write("The wardwork threshold is west.\n");
}
int west(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "west", "place/blackstone/wardwork_threshold"); }
