#define DECAY_TIME  120
int decay;
string name;
/*
* This is a decaying corpse. It is created automatically
* when a player or monster die.
*/
void decay() {
  decay -= 1;

  if (decay > 0) {
    call_out("decay", 20);

    return;
  }

  destruct(this_object());
}

status get() {
  return 1;
}

status id(string str) {
  return str == "corpse" || str == "corpse of " + name ||

  str == "remains";
}

void init() {
  add_action("search", "search");
}

void long() {
  write("This is the dead body of " + capitalize(name) + ".\n");
}

status prevent_insert() {
  write("The corpse is too big.\n");

  return 1;
}

int query_weight() {
  return 5;
}

void reset(string arg) {
  if (arg)
    return;

  name = "noone";
  decay = 2;
}
status can_put_and_get() { return 1; }

status search(string str) {
  object ob;

  if (!str || !id(str))
    return 0;

  ob = present(str, environment(this_player()));

  if (!ob)
    ob = present(str, this_player());

  if (!ob)
    return 0;

  write("You search " + str + ", and find:\n");
  say(call_other(this_player(), "query_name") + " searches " + str + ".\n");

  if (!search_obj(ob))
    write("\tNothing.\n");

  else
    write("\n");

  return 1;
}

status search_obj(object cont) {
  object ob;
  int total;

  if (!call_other(cont, "can_put_and_get"))
    return 0;

  ob = first_inventory(cont);

  while(ob) {
    total += 1;

    write(call_other(ob, "short") + ", ");

    ob = next_inventory(ob);
  }

  return total;
}

void set_name(string n) {
  name = n;

  call_out("decay", DECAY_TIME);
}

string short() {
  if (decay < 2)
    return "The somewhat decayed remains of " + capitalize(name);

  return "Corpse of " + capitalize(name);
}
