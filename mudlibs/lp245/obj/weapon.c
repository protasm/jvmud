string alias_name;
string alt_name;
string cap_name;
int class_of_weapon;
object hit_func;
string info;
int local_weight;
string long_desc;
string name_of_weapon;
string read_msg;
string short_desc;
int value;
object wield_func;
string wielded_by;
/*
* This file defines a general purpose weapon. See below for configuration
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

status wielded;

string query_name() { return name_of_weapon; }
status drop(mixed silently) {
  if (wielded) {
    call_other(wielded_by, "stop_wielding");

    wielded = 0;

    if (!silently)
      write("You drop your wielded weapon.\n");
  }

  return 0;
}

int hit(object attacker) {
  if (hit_func)
    return call_other(hit_func,"weapon_hit",attacker);

  return 0;
}

status id(mixed str) {
  return str == name_of_weapon || str == alt_name || str == alias_name;
}

void init() {
  if (read_msg) {
    add_action("read", "read");
  }

  add_action("wield", "wield");
}

void long() {
  write(long_desc);
}

string query_info() {
  return info;
}

int query_value() {
  return value;
}

status read(mixed str) {
  if (!id(str))
    return 0;

  write(read_msg);

  return 1;
}

void reset(mixed arg) {
  if (arg)
    return;

  wielded = 0; value = 0;
}

void set_id(mixed n) {
  name_of_weapon = n;
  cap_name = capitalize(n);
  short_desc = cap_name;
  long_desc = "You see nothing special.\n";
}
status get() { return 1; }

int query_weight() { return local_weight; }

void set_class(int c) { class_of_weapon = c; }

void set_weight(int w) { local_weight = w; }

void set_value(int v) { value = v; }

void set_alt_name(mixed n) { alt_name = n; }

void set_hit_func(object ob) { hit_func = ob; }

void set_wield_func(object ob) { wield_func = ob; }

void set_alias(mixed n) { alias_name = n; }

void set_short(mixed sh) { short_desc = sh; long_desc = short_desc + "\n";}

void set_long(mixed long) { long_desc = long; }

void set_read(mixed str) { read_msg = str; }

void set_info(mixed i) {
  info = i;
}

void set_name(mixed n) {
  name_of_weapon = n;
  cap_name = capitalize(n);
  short_desc = cap_name;
  long_desc = "You see nothing special.\n";
}

string short() {
  if (wielded)
    if(short_desc)
    return short_desc + " (wielded)";

  return short_desc;
}

void un_wield() {
  if (wielded)
    wielded = 0;
}

int weapon_class() {
  return class_of_weapon;
}

status wield(mixed str) {
  if (!id(str))
    return 0;

  if (environment() != this_player()) {
    /* write("You must get it first!\n"); */

    return 0;
  }

  if (wielded) {
    write("You already wield it!\n");

    return 1;
  }

  if(wield_func)
    if(!call_other(wield_func,"wield",this_object()))
    return 1;

  wielded_by = this_player();

  call_other(this_player(), "wield", this_object());

  wielded = 1;
  return 1;
}
