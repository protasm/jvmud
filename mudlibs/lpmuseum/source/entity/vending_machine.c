object *vended;
int serial;

void reset(mixed first_load) {
  if (!vended) {
    vended = ({ });
  }
  bind_alias(this_object(), "entity", "machine");
  bind_alias(this_object(), "entity", "vending machine");
  bind_alias(this_object(), "entity", "entity vending machine");
}

void init() {
  add_action("vend", "vend");
  add_action("vend", "press");
}

string short() {
  return "entity vending machine";
}

int id(mixed value) {
  return value == "machine" || value == "vending machine"
      || value == "entity vending machine";
}

void describe(object viewer) {
  write("The entity vending machine is a waist-high brass-and-glass cabinet with a simple button.\n");
  write("It vends temporary inspectable Entities onto the floor. Each one expires after two minutes.\n");
  write("It will keep at most ten vended Entities in existence at a time.\n");
  write("Try: vend entity\n");
}

int can_take(object actor) {
  return 0;
}

int vend(mixed topic) {
  object item;
  object place;

  if (topic && topic != "entity" && topic != "curio" && topic != "machine"
      && topic != "button") {
    return 0;
  }

  prune_vended();
  if (sizeof(vended) >= 10) {
    write("The entity vending machine clicks, but a small counter reads: MAX 10 LIVE ENTITIES.\n");
    return 1;
  }

  place = environment(this_object());
  serial = serial + 1;
  item = clone_object("entity/vended_curio");
  call_other(item, "configure", serial);
  move_object(item, place);
  vended += ({ item });

  write("The entity vending machine vends " + call_other(item, "short") + " onto the floor.\n");
  tell_place_except(place,
      "The entity vending machine vends " + call_other(item, "short") + " onto the floor.\n",
      this_player());
  return 1;
}

int live_count() {
  prune_vended();
  return sizeof(vended);
}

void prune_vended() {
  object *live;
  object item;
  int index;

  live = ({ });
  index = 0;
  while (index < sizeof(vended)) {
    item = vended[index];
    if (item && environment(item)) {
      live += ({ item });
    }
    index = index + 1;
  }
  vended = live;
}
