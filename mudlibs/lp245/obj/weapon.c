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

query_name() { return name_of_weapon; }
drop(silently) {
  if (wielded) {
    call_other(wielded_by, "stop_wielding");

    wielded = 0;

    if (!silently)
      write("You drop your wielded weapon.\n");
  }

  return 0;
}

hit(attacker) {
  if (hit_func)
    return call_other(hit_func,"weapon_hit",attacker);

  return 0;
}

id(str) {
  return str == name_of_weapon || str == alt_name || str == alias_name;
}

init() {
  if (read_msg) {
    add_action("read", "read");
  }

  add_action("wield", "wield");
}

long() {
  write(long_desc);
}

query_info() {
  return info;
}

query_value() {
  return value;
}

read(str) {
  if (!id(str))
    return 0;

  write(read_msg);

  return 1;
}

reset(arg) {
  if (arg)
    return;

  wielded = 0; value = 0;
}

set_id(n) {
  name_of_weapon = n;
  cap_name = capitalize(n);
  short_desc = cap_name;
  long_desc = "You see nothing special.\n";
}
get() { return 1; }

query_weight() { return local_weight; }

set_class(c) { class_of_weapon = c; }

set_weight(w) { local_weight = w; }

set_value(v) { value = v; }

set_alt_name(n) { alt_name = n; }

set_hit_func(ob) { hit_func = ob; }

set_wield_func(ob) { wield_func = ob; }

set_alias(n) { alias_name = n; }

set_short(sh) { short_desc = sh; long_desc = short_desc + "\n";}

set_long(long) { long_desc = long; }

set_read(str) { read_msg = str; }

set_info(i) {
  info = i;
}

set_name(n) {
  name_of_weapon = n;
  cap_name = capitalize(n);
  short_desc = cap_name;
  long_desc = "You see nothing special.\n";
}

short() {
  if (wielded)
    if(short_desc)
    return short_desc + " (wielded)";

  return short_desc;
}

un_wield() {
  if (wielded)
    wielded = 0;
}

weapon_class() {
  return class_of_weapon;
}

wield(str) {
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
