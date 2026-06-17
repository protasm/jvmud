void reset(int arg) {
}

void init() {
  add_action("enter", "enter");
  add_action("enter", "return");
}

string short() {
  return "museum return portal";
}

int id(string str) {
  return str == "portal" || str == "museum portal" || str == "return portal"
      || str == "museum return portal";
}

void long(string str) {
  write("The portal leads back to LPMuseum.\n");
}

int enter(string target) {
  if (!id(target)) {
    return 0;
  }

  write("The museum return portal hums.\n");
  if (!jvmud_transfer_player_to_game("lpmuseum")) {
    write("The return portal flickers, but LPMuseum does not answer.\n");
  }
  return 1;
}
