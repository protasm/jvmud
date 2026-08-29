void initialize(mixed first_load) { spawn_warden(); jvmud_schedule_recurring_tick(1, 180); }
void scheduled_update() { spawn_warden(); }
void spawn_warden() {
  object warden;
  if (!jvmud_find_entity("warden", jvmud_current_lpc_object())) {
    warden = jvmud_clone_lpc_object("npc/hostile");
    jvmud_invoke_lpc_object(warden, "configure", "soot-crowned Lantern warden", "female", "A towering royal warden of blue glass and black fire guards the final stair.", 9, 165, 13, 17, 600, 125);
    jvmud_invoke_lpc_object(warden, "add_identity", "warden");
    jvmud_invoke_lpc_object(warden, "add_identity", "lantern warden");
    jvmud_invoke_lpc_object(warden, "set_quest_defeat_tag", "ashenwatch-warden");
    jvmud_move_entity(warden, jvmud_current_lpc_object());
  }
}
void offer_interactions() {
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
}
string short() { return "Ashenwatch Lantern Crypt"; }
void describe(object viewer) {
  write("Ashenwatch Lantern Crypt\n");
  write("Names of wardens, masons, lamp keepers, and village delegates cover ");
  write("the crypt walls without rank distinction. A final corrupted sentinel ");
  write("stands between their memorial and the Crown Lantern chamber.\n\n");
  if (jvmud_find_entity("warden", jvmud_current_lpc_object())) { write("The soot-crowned Lantern warden waits before the northern stair.\n"); }
  write("The antechamber is west, and the Crown Lantern is north.\n");
}
int west(mixed ignored) { return travel("west", "place/ashenwatch/ward_antechamber"); }
int north(mixed ignored) { return travel("north", "place/ashenwatch/crown_lantern"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
