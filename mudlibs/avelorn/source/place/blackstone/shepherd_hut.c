void initialize(mixed first_load) { }
void offer_interactions() { jvmud_add_action("west", "west"); jvmud_add_action("west", "w"); }
string short() { return "Blackstone Shepherd Hut"; }
void describe(object viewer) {
  write("Blackstone Shepherd Hut\n");
  write("A dry-stone hut contains peat, oatcakes, and a slate message board. ");
  write("Upland families replenish it by rota, while roadwardens inspect its ");
  write("storm shutters before each winter.\n\n");
  write("The upland trail is west.\n");
}
int west(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "west", "place/blackstone/upland_trail"); }
