string info;
string name, alias, short_desc, long_desc;
mixed value, weight;
object next;
string type;
int worn, ac;
object worn_by;
/*
* This file defines a general purpose armour. See below for configuration
* functions: set_xx.
*/

/*
* If you are going to copy this file, in the purpose of changing
* it a little to your own need, beware:
*
* First try one of the following:
*
* 1. Do clone_object(), and then configure it. This object is specially
*    prepared for configuration.
*
* 2. If you still is not pleased with that, create a new empty
*    object, and make an inheritance of this objet on the first line.
*    This will automatically copy all variables and functions from the
*    original object. Then, add the functions you want to change. The
*    original function can still be accessed with '::' prepended on the name.
*
* The maintainer of this LPmud might become sad with you if you fail
* to do any of the above. Ask other wizards if you are doubtful.
*
* The reason of this, is that the above saves a lot of memory.
*/
status get() { return 1; }
status drop(mixed silently) {
  if (worn) {
    call_other(worn_by, "stop_wearing",name);

    worn = 0;
    worn_by = 0;

    if (!silently)
      tell_object(environment(this_object()),"You drop your worn armour.\n");
  }

  return 0;
}

status id(mixed str) {
  return str == name || str == alias || str == type;
}

void init() {
  add_action("wear", "wear");
  add_action("remove", "remove");
}

void link(object ob) {
  next = ob;
}

void long(mixed str) {
  write(long_desc);
}

string query_info() {
  return info;
}

string rec_short() {
  if(next)
    return name + ", " + call_other(next, "rec_short");

  return name;
}

status remove(mixed str) {
  if (!id(str))
    return 0;

  if (!worn) {
    return 0;
  }

  call_other(worn_by, "stop_wearing",name);

  worn_by = 0;
  worn = 0;
  return 1;
}

object remove_link(mixed str) {
  object ob;

  if (str == name) {
    ob = next;
    next = 0;
    return ob;
  }

  if (next)
    next = call_other(next, "remove_link", str);

  return this_object();
}

void reset(mixed arg) {
  if(arg)
    return;

  type = "armour";
}
void set_arm_light(int l) { set_light(l); }
void set_info(mixed i) {
  info = i;
}
mixed query_weight() { return weight; }

void set_id(mixed n) { name = n; }
void set_name(mixed n) { name = n; }
void set_short(mixed s) { short_desc = s; long_desc = s + ".\n"; }
void set_value(mixed v) { value = v; }
void set_weight(mixed w) { weight = w; }
void set_ac(int a) { ac = a; }
void set_alias(mixed a) { alias = a; }
void set_long(mixed l) { long_desc = l; }
void set_type(mixed t) {
  type = t;
}

mixed short() {
  if (!short_desc)
    return 0;

  if (worn)
    return short_desc + " (worn)";

  return short_desc;
}

object test_type(mixed str) {
  if(str == type)
    return this_object();

  if(next)
    return call_other(next, "test_type", str);

  return 0;
}

int tot_ac() {
  if(next)
    return ac + call_other(next, "tot_ac");

  return ac;
}
string query_type() { return type; }

mixed query_value() { return value; }

status query_worn() { return worn; }

string query_name() { return name; }

int armour_class() { return ac; }

status wear(mixed str) {
  object ob;

  if (!id(str))
    return 0;

  if (environment() != this_player()) {
    write("You must get it first!\n");

    return 1;
  }

  if (worn) {
    write("You already wear it!\n");

    return 1;
  }

  next = 0;
  ob = call_other(this_player(), "wear", this_object());

  if(!ob) {
    worn_by = this_player();
    worn = 1;
    return 1;
  }

  write("You already have an armour of class " + type + ".\n");
  write("Worn armour " + call_other(ob,"short") + ".\n");

  return 1;
}
