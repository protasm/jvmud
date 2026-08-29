void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("up", "up"); jvmud_add_action("up", "u");
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
}
string short() { return "Ashenwatch Ward Antechamber"; }
void describe(object viewer) {
  write("Ashenwatch Ward Antechamber\n");
  write("Copper lines from every tower converge beneath a mosaic map of western ");
  write("Avelorn. Brindleford, Greyhaven, Merewatch, and Blackstone now glow blue; ");
  write("only the great channel from the lantern crypt remains dark.\n\n");
  write("The underkeep is up, and the lantern crypt is east.\n");
}
int up(mixed ignored) { return travel("up", "place/ashenwatch/underkeep"); }
int east(mixed ignored) { return travel("east", "place/ashenwatch/lantern_crypt"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
