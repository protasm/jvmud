int serial;

void reset(mixed first_load) {
  bind_alias(this_object(), "entity", "curio");
  bind_alias(this_object(), "entity", "vended curio");
  bind_alias(this_object(), "entity", "temporary entity");
  call_out("expire", 120);
}

void configure(int value) {
  serial = value;
}

string short() {
  if (serial) {
    return "vended curio #" + serial;
  }
  return "vended curio";
}

int id(mixed value) {
  return value == "curio" || value == "vended curio" || value == "temporary entity"
      || value == short();
}

void describe(object viewer) {
  write("This is a palm-sized temporary Entity from the workshop vending machine.\n");
  write("Its glassy surface shows its JVMud identity: " + object_name(this_object()) + ".\n");
  write("It will self-destruct two minutes after being vended.\n");
}

int can_take(object actor) {
  return 1;
}

int is_vended_entity() {
  return 1;
}

void expire() {
  object holder;

  holder = environment(this_object());
  if (holder) {
    if (living(holder)) {
      tell_object(holder, short() + " fades out of your inventory.\n");
    } else {
      tell_place(holder, short() + " fades out of existence.\n");
    }
  }
  destruct(this_object());
}
