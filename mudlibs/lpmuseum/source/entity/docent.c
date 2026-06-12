void reset(mixed first_load) {
  bind_alias(this_object(), "entity", "docent");
  bind_alias(this_object(), "entity", "guide");
}

void init() {
  add_action("ask", "ask");
}

string short() {
  return "docent";
}

int id(mixed value) {
  return value == "docent" || value == "guide";
}

void describe(object viewer) {
  write("The docent is a small interpretive Entity maintained by LPMuseum itself.\n");
  write("It is here to explain JVMud concepts, not to borrow a legacy login flow.\n");
}

int ask(mixed topic) {
  if (topic != "docent" && topic != "guide") {
    return 0;
  }

  write("The docent says: Player, Session, Persona, Place, and Entity are JVMud words.\n");
  write("The museum uses those words even when an exhibit preserves older vocabulary.\n");
  return 1;
}
