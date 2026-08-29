void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("up", "up"); jvmud_add_action("up", "u");
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
  jvmud_add_action("north", "north"); jvmud_add_action("north", "n");
}
string short() { return "Blackstone Wardwork Threshold"; }
void describe(object viewer) {
  jvmud_write("Blackstone Wardwork Threshold\n");
  jvmud_write("Three ancient galleries meet around copper channels that once carried ");
  jvmud_write("blue ward-fire. Water stains the eastern stones, ash drifts west, and a ");
  jvmud_write("faint pulse answers from the chamber north.\n\n");
  jvmud_write("The entrance is up, flooded gallery east, old armory west, and ward chamber north.\n");
}
int up(mixed ignored) { return travel("up", "place/blackstone/wardwork_entrance"); }
int east(mixed ignored) { return travel("east", "place/blackstone/flooded_gallery"); }
int west(mixed ignored) { return travel("west", "place/blackstone/old_armory"); }
int north(mixed ignored) { return travel("north", "place/blackstone/ward_chamber"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
