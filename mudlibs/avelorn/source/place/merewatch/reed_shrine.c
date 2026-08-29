void initialize(mixed first_load) { }
void offer_interactions() { jvmud_add_action("east", "east"); jvmud_add_action("east", "e"); jvmud_add_action("rest", "rest"); }
string short() { return "Merewatch Reed Shrine"; }
void describe(object viewer) {
  jvmud_write("Merewatch Reed Shrine\n");
  jvmud_write("Woven reed screens surround a lamp reflected in a shallow pool. The ");
  jvmud_write("shrine keeps rescue ropes, fever medicines, and dry blankets alongside ");
  jvmud_write("its offerings, making devotion inseparable from readiness.\n\n");
  jvmud_write("The upland trail is east. You may rest here.\n");
}
int east(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "east", "place/blackstone/upland_trail"); }
int rest(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "rest_at_shrine"); }
