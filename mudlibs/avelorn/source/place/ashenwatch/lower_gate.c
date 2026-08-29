void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
}
string short() { return "Ashenwatch Lower Gate"; }
void describe(object viewer) {
  write("Ashenwatch Lower Gate\n");
  write("The portcullis stands safely braced above an arch engraved with the ");
  write("oaths of the first western wardens. Expedition carpenters have secured ");
  write("the mechanism while preserving every historic stone.\n\n");
  write("The expedition camp is east, and the outer court is west.\n");
}
int east(mixed ignored) { return travel("east", "place/ashenwatch/expedition_camp"); }
int west(mixed ignored) { return travel("west", "place/ashenwatch/outer_court"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
