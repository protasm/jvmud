void reset(mixed first_load) {
}

void show(mixed topic) {
  write("LPMuseum commands:\n");
  write("  look              describe your current Place\n");
  write("  look <entity>     inspect an Entity in the Place\n");
  write("  go <path>         move to a connected Place\n");
  write("  north/east/south/west  use local Place exits\n");
  write("  say <text>        speak to everyone in your Place\n");
  write("  say to <entity> <text>  address an Entity while remaining perceivable\n");
  write("  smile [entity]    use one of 100 native social emotes\n");
  write("  inventory         list carried Entities\n");
  write("  who               list connected Personas\n");
  write("  whoami            show the Player, Session, Persona idea\n");
  write("  quit              leave LPMuseum through the mudlib Persona\n");
  write("  help              show this text\n");
}
