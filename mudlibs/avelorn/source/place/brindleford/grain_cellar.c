void initialize(mixed first_load) {
  spawn_rat();
  jvmud_schedule_recurring_tick(1, 20);
}

void scheduled_update() {
  spawn_rat();
}

void spawn_rat() {
  object rat;

  if (!jvmud_find_entity("rat", jvmud_current_lpc_object())) {
    rat = jvmud_clone_lpc_object("npc/hostile");
    jvmud_invoke_lpc_object(
        rat,
        "configure",
        "scarred granary rat",
        "female",
        "An unnaturally large brown rat guards a split sack of winter rye.",
        1,
        34,
        2,
        5,
        35,
        8);
    jvmud_invoke_lpc_object(rat, "add_identity", "rat");
    jvmud_invoke_lpc_object(rat, "add_identity", "granary rat");
    jvmud_invoke_lpc_object(rat, "set_quest_defeat_tag", "granary-rat");
    jvmud_move_entity(rat, jvmud_current_lpc_object());
  }
}

void offer_interactions() {
  jvmud_add_action("west", "west");
  jvmud_add_action("west", "w");
}

string short() {
  return "Mill Grain Cellar";
}

void describe(object viewer) {
  object rat;
  string possessive_word;

  jvmud_write("Mill Grain Cellar\n");
  jvmud_write("Rye and barley sacks rest on raised oak slats beneath a dry brick ");
  jvmud_write("vault. One storage bin has been clawed open from an old river culvert, ");
  jvmud_write("turning ordinary mill maintenance into work for a Companion.\n\n");
  rat = jvmud_find_entity("rat", jvmud_current_lpc_object());
  if (rat) {
    possessive_word = jvmud_invoke_lpc_object(
        "system/pronouns",
        "possessive_adjective",
        jvmud_invoke_lpc_object(rat, "query_gender"));
    jvmud_write("A scarred granary rat bares " + possessive_word);
    jvmud_write(" teeth beside the damaged grain.\n");
  }
  jvmud_write("The cellar landing is west.\n");
}

int west(mixed ignored) {
  return jvmud_invoke_lpc_object(
      jvmud_current_actor(),
      "travel_to",
      "west",
      "place/brindleford/cellar_landing");
}
