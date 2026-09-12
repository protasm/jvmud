/*
// weapon.c
//
// This is the base weapon object.  All weapons should just inherit this
// object and set the proper attributes.
*/

#include <config.h>
#include <attributes.h>
#include <attrs/position.h>

inherit BASE;

create() {
  base::create();
// Default to a very slow weapon that does almost no damage.
  base::set(a_damage_die, 1);
  base::set(a_damage_num_dice, 1);
  base::set(a_damage_modifier, 0);
  base::set(a_speed_factor, 15); // Default to worst listed in PHB.
  base::set(a_weapon_type, 4);
  base::set(a_magical_bonus, 0);
  base::set(a_eshort, "A weapon.");
  base::set(a_elong, "A weapon.\n");
  base::set(a_ids, ({ "weapon" }) );
  base::set(a_poundage, 20);
  base::set(a_worn_at, wear_hand);
  base::set(a_damage_string, (: this_object, "query_damage_string" :) );
}

void
set(mixed key, mixed value) {
  int die, num, mod;
  string temp;

  if (key != a_damage_string) {
    base::set(key, value);
    return;
  }
  if (!stringp(value))
    return;
  if (sscanf(value, "%s+%d", temp, mod) == 2)
    value = temp;
  else if (sscanf(value, "%s-%d", temp, mod) == 2)
    value = temp;
  else
    mod = 0;
  if ((sscanf(value, "%dd%d", num, die) != 2) || (sscanf(value, "%dD%d",
    num, die) != 2)) {
    num = 1;
    if (!sscanf(value, "d%d", die) || !sscanf(value, "D%d", die))
      return;
  }
  base::set(a_damage_modifier, mod);
  base::set(a_damage_num_dice, num);
  base::set(a_damage_die, die);
}

string
query_damage_string(mixed key) {
  string str;
  int mod;

  if (key != a_damage_string)
    return base::query(key);
  if (base::query(a_damage_num_dice) > 1)
    str = (base::query(a_damage_num_dice)) + "d";
  else
    str = "d";
  str += base::query(a_damage_die);
  mod = base::query(a_damage_modifier);
  if (mod > 0)
    str += "+" + mod;
  else if (mod < 0)
    str += "-" + mod;
  return str;
}
