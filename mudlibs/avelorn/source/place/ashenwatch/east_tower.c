void initialize(mixed first_load) { }
void offer_interactions() { jvmud_add_action("west", "west"); jvmud_add_action("west", "w"); jvmud_add_action("align_mirror", "align"); }
string short() { return "Ashenwatch East Tower"; }
void describe(object viewer) {
  jvmud_write("Ashenwatch East Tower\n");
  jvmud_write("A silvered signaling mirror faces the Blackstone wardworks and distant ");
  jvmud_write("Greyhaven Lantern Tower. Its calibrated frame is sound, though soot has ");
  jvmud_write("twisted the mirror a few crucial degrees.\n\n");
  jvmud_write("The great hall is west. Type align to set the ward mirror.\n");
}
int west(mixed ignored) { return jvmud_invoke_lpc_object(jvmud_current_actor(), "travel_to", "west", "place/ashenwatch/great_hall"); }
int align_mirror(mixed ignored) {
  jvmud_write("You clean the silver face and align it with Blackstone's renewed blue pulse.\n");
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "record_quest_action", "ashenwatch-mirror");
}
