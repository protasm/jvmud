void initialize(mixed first_load) { }
void offer_interactions() { jvmud_add_action("east", "east"); jvmud_add_action("east", "e"); }
string short() { return "Merewatch Lakeside"; }
void describe(object viewer) {
  jvmud_write("Merewatch Lakeside\n");
  jvmud_write("Long reed boats rest at numbered moorings beside communal smokehouses. ");
  jvmud_write("Colored poles mark safe channels and nesting reserves agreed by the ");
  jvmud_write("fishing guild, Temple naturalists, and the Crown water office.\n\n");
  jvmud_write("Merewatch Square is east.\n");
}
int east(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "east", "place/merewatch/mere_square"); }
