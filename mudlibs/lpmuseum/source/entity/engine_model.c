void reset(mixed first_load) {
  bind_alias(this_object(), "entity", "model");
  bind_alias(this_object(), "entity", "engine");
}

string short() {
  return "engine model";
}

int id(mixed value) {
  return value == "model" || value == "engine" || value == "engine model";
}

void describe(object viewer) {
  write("The engine model has five labels: Player, Session, Persona, Place, Entity.\n");
  write("Lines from Persona to Place and Entity show how command routing enters the World.\n");
}
