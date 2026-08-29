void initialize(mixed first_load) { }
void offer_interactions() { jvmud_add_action("east", "east"); jvmud_add_action("east", "e"); }
string short() { return "Merewatch Lakeside"; }
void describe(object viewer) {
  write("Merewatch Lakeside\n");
  write("Long reed boats rest at numbered moorings beside communal smokehouses. ");
  write("Colored poles mark safe channels and nesting reserves agreed by the ");
  write("fishing guild, Temple naturalists, and the Crown water office.\n\n");
  write("Merewatch Square is east.\n");
}
int east(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "east", "place/merewatch/mere_square"); }
