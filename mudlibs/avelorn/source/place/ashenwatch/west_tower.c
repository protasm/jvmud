void initialize(mixed first_load) { spawn_knight(); jvmud_schedule_recurring_tick(1, 150); }
void scheduled_update() { spawn_knight(); }
void spawn_knight() {
  object knight;
  if (!jvmud_find_entity("knight", jvmud_current_lpc_object())) {
    knight = jvmud_clone_lpc_object("npc/hostile");
    jvmud_invoke_lpc_object(knight, "configure", "ember-bound tower knight", "male", "A spectral warden in blackened plate holds a sword of smoldering ward-fire.", 8, 130, 12, 18, 400, 75);
    jvmud_invoke_lpc_object(knight, "add_identity", "knight");
    jvmud_invoke_lpc_object(knight, "add_identity", "tower knight");
    jvmud_invoke_lpc_object(knight, "set_quest_defeat_tag", "ashenwatch-knight");
    jvmud_move_entity(knight, jvmud_current_lpc_object());
  }
}
void offer_interactions() { jvmud_add_action("east", "east"); jvmud_add_action("east", "e"); }
string short() { return "Ashenwatch West Tower"; }
void describe(object viewer) {
  write("Ashenwatch West Tower\n");
  write("Arrow loops command the western ridge above a copper signal floor. ");
  write("The tower's last knight remains bound to a corrupted order to guard a ");
  write("flame that can no longer distinguish friend from foe.\n\n");
  if (jvmud_find_entity("knight", jvmud_current_lpc_object())) { write("An ember-bound tower knight bars the signal stair.\n"); }
  write("The great hall is east.\n");
}
int east(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "east", "place/ashenwatch/great_hall"); }
