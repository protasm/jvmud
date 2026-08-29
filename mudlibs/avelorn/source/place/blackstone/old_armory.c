void initialize(mixed first_load) { spawn_guardian(); jvmud_schedule_recurring_tick(1, 90); }
void scheduled_update() { spawn_guardian(); }
void spawn_guardian() {
  object guardian;
  if (!jvmud_find_entity("guardian", jvmud_current_lpc_object())) {
    guardian = jvmud_clone_lpc_object("npc/hostile");
    jvmud_invoke_lpc_object(guardian, "configure", "ashbound iron guardian", "male", "Ancient plate encloses a shape of emberless ash beside the sealed arms racks.", 6, 105, 9, 14, 325, 50);
    jvmud_invoke_lpc_object(guardian, "add_identity", "guardian");
    jvmud_invoke_lpc_object(guardian, "add_identity", "ashbound guardian");
    jvmud_invoke_lpc_object(guardian, "set_quest_defeat_tag", "blackstone-ash-guardian");
    jvmud_move_entity(guardian, jvmud_current_lpc_object());
  }
}
void offer_interactions() { jvmud_add_action("east", "east"); jvmud_add_action("east", "e"); }
string short() { return "Blackstone Old Armory"; }
void describe(object viewer) {
  write("Blackstone Old Armory\n");
  write("Sealed racks preserve spears and lantern shields from Avelorn's first ");
  write("western watch. Black soot crawls over inventory tablets whose orderly ");
  write("marks still agree with every weapon in place.\n\n");
  if (jvmud_find_entity("guardian", jvmud_current_lpc_object())) { write("An ashbound iron guardian stands before the racks.\n"); }
  write("The wardwork threshold is east.\n");
}
int east(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "east", "place/blackstone/wardwork_threshold"); }
