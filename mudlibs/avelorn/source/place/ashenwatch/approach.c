void initialize(mixed first_load) { }
void offer_interactions() {
  jvmud_add_action("east", "east"); jvmud_add_action("east", "e");
  jvmud_add_action("west", "west"); jvmud_add_action("west", "w");
}
string short() { return "Ashenwatch Approach"; }
void describe(object viewer) {
  jvmud_write("Ashenwatch Approach\n");
  jvmud_write("A military road follows the ridge toward a square keep veiled in slow ");
  jvmud_write("black sparks. Crown engineers have shored every culvert and marked a safe ");
  jvmud_write("route for the joint watch, Temple, Collegium, and Company expedition.\n\n");
  jvmud_write("The Blackstone standing stones are east, and the expedition camp is west.\n");
}
int east(mixed ignored) { return travel("east", "place/blackstone/standing_stones"); }
int west(mixed ignored) { return travel("west", "place/ashenwatch/expedition_camp"); }
int travel(string direction, string destination) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", direction, destination); }
