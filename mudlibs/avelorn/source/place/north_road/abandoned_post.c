void initialize(mixed first_load) {
  spawn_wraith();
  jvmud_schedule_recurring_tick(1, 60);
}

void scheduled_update() { spawn_wraith(); }

void spawn_wraith() {
  object wraith;
  if (!jvmud_find_entity("wraith", jvmud_current_lpc_object())) {
    wraith = jvmud_clone_lpc_object("npc/hostile");
    jvmud_invoke_lpc_object(
        wraith,
        "configure",
        "hollow bell wraith",
        "non-binary",
        "Soot-black mail hangs within a translucent figure coiled around the patrol bell's clapper.",
        4,
        72,
        6,
        11,
        140,
        30);
    jvmud_invoke_lpc_object(wraith, "add_identity", "wraith");
    jvmud_invoke_lpc_object(wraith, "add_identity", "bell wraith");
    jvmud_invoke_lpc_object(wraith, "set_quest_defeat_tag", "bell-wraith");
    jvmud_move_entity(wraith, jvmud_current_lpc_object());
  }
}

void offer_interactions() {
  jvmud_add_action("south", "south"); jvmud_add_action("south", "s");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
}
string short() { return "Abandoned Patrol Post"; }
void describe(object viewer) {
  write("Abandoned Patrol Post\n");
  write("The post itself remains sound: shutters barred, roster sealed, beacon ");
  write("wood dry. Yet unnatural soot muffles its alarm bell, evidence that the ");
  write("kingdom's failing western ward—not neglect—drove the patrol away.\n\n");
  if (jvmud_find_entity("wraith", jvmud_current_lpc_object())) {
    write("A hollow bell wraith coils around the silent alarm.\n");
  }
  write("The patrol crossing is south, and the Merewatch road continues north.\n");
}
int south(mixed ignored) { return travel("south", "place/north_road/patrol_crossing"); }
int north(mixed ignored) { return travel("north", "place/north_road/merewatch_road"); }
int travel(string direction, string destination) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination);
}
