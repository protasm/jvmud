void reset(mixed first_load) {
  bind_alias(this_object(), "entity", "portal");
  bind_alias(this_object(), "entity", "exhibit portal");
}

void init() {
  add_action("enter", "enter");
}

string short() {
  return "quiet exhibit portal";
}

int id(mixed value) {
  return value == "portal" || value == "exhibit portal";
}

void describe(object viewer) {
  write("The portal is a museum boundary marker. It opens into the Vanilla LPMUD 2.4.5 exhibit.\n");
  write("Crossing it connects you to the exhibit mudlib under your LPMuseum user ID.\n");
}

int enter(mixed target) {
  if (target != "portal" && target != "exhibit portal") {
    return 0;
  }

  write("The portal hums and points toward the Vanilla LPMUD 2.4.5 exhibit.\n");
  if (!jvmud_transfer_player_to_game("vanilla-lpmud-245")) {
    write("The portal is quiet. No exhibit is mounted here yet.\n");
  }
  return 1;
}
