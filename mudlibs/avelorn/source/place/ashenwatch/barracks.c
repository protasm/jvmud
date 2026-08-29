void initialize(mixed first_load) { spawn_hound(); jvmud_schedule_recurring_tick(1, 120); }
void scheduled_update() { spawn_hound(); }
void spawn_hound() {
  object hound;
  if (!jvmud_find_entity("hound", jvmud_current_lpc_object())) {
    hound = jvmud_clone_lpc_object("npc/hostile");
    jvmud_invoke_lpc_object(hound, "configure", "Lantern-ash hound", "female", "A wolf-shaped knot of ash and blue sparks prowls between the intact bunks.", 7, 100, 10, 15, 300, 50);
    jvmud_invoke_lpc_object(hound, "add_identity", "hound");
    jvmud_invoke_lpc_object(hound, "add_identity", "ash hound");
    jvmud_invoke_lpc_object(hound, "set_quest_defeat_tag", "ashenwatch-hound");
    jvmud_move_entity(hound, jvmud_current_lpc_object());
  }
}
void offer_interactions() { jvmud_add_action("east", "east"); jvmud_add_action("east", "e"); }
string short() { return "Ashenwatch Barracks"; }
void describe(object viewer) {
  write("Ashenwatch Barracks\n");
  write("Bunks, shield pegs, and duty tablets remain in regulation order beneath ");
  write("a century of dust. The ward's corruption animates the ash here, but ");
  write("nothing suggests the keep's final garrison abandoned its discipline.\n\n");
  if (jvmud_find_entity("hound", jvmud_current_lpc_object())) { write("A Lantern-ash hound stalks the center aisle.\n"); }
  write("The outer court is east.\n");
}
int east(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "east", "place/ashenwatch/outer_court"); }
