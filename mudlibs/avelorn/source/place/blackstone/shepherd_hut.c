void initialize(mixed first_load) { }
void offer_interactions() { jvmud_add_action("west", "west"); jvmud_add_action("west", "w"); }
string short() { return "Blackstone Shepherd Hut"; }
void describe(object viewer) {
  jvmud_write("Blackstone Shepherd Hut\n");
  jvmud_write("A dry-stone hut contains peat, oatcakes, and a slate message board. ");
  jvmud_write("Upland families replenish it by rota, while roadwardens inspect its ");
  jvmud_write("storm shutters before each winter.\n\n");
  jvmud_write("The upland trail is west.\n");
}
int west(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "west", "place/blackstone/upland_trail"); }
