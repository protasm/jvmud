#include "living.h"
int a_chat_chance;
string *a_chat_head;  /* Vector with all a_chat strings. */
int busy_catch_tell;
int chat_chance;
string *chat_head;  /* Vector with all chat strings. */
object create_room;
object dead_ob;
int have_text;
object init_ob;
object kill_ob;
object me;
int move_at_reset, aggressive;
int random_pick;
string short_desc, long_desc, alias, alt_name, race;
int spell_chance, spell_dam;
string spell_mess1, spell_mess2;
string *talk_func;  /* Vector of functions. */
string *talk_match;  /* Vector of strings. */
object talk_ob;
string *talk_type;  /* Vector of types. */
string the_text;
/*
* A general purpose monster. Clone this object,
* and call local functions to configure it.
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

/*
* The heart beat is always started in living.h when we are attacked.
*/

status healing;    /* True if this monster is healing itself. */
status can_put_and_get(mixed str) {
  if (!str)
    return 0;

  return 1;
}

void catch_tell(mixed str) {
  if (busy_catch_tell)
    return;

  busy_catch_tell = 1;

  if(talk_ob) {
    if(have_text) {
      test_match(the_text);

      the_text = str;
    } else {
      the_text = str;
      have_text = 1;
    }
  }

  busy_catch_tell = 0;
}

/*
* The monster will heal itself slowly.
*/
void heal_slowly() {
  hit_point += 120 / (INTERVAL_BETWEEN_HEALING * 2);

  if (hit_point > max_hp)
    hit_point = max_hp;

  spell_points += 120 / (INTERVAL_BETWEEN_HEALING * 2);

  if (spell_points > max_hp)
    spell_points = max_hp;

  healing = 1;

  if (hit_point < max_hp || spell_points < max_hp)
    call_out("heal_slowly", 120);

  else
    healing = 0;
}
status id(mixed str) { return str == name || str == alias || str == race || str == alt_name; }

void heart_beat() {
  int c;

  age += 1;

  if(!test_if_any_here()) {
    if(have_text && talk_ob) {
      have_text = 0;

      test_match(the_text);
    } else {
      set_heart_beat(0);

      if (!healing)
        heal_slowly();

      return;
    }
  }

  if (kill_ob && present(kill_ob, environment(this_object()))) {
    if (random(2) == 1)
      return;    /* Delay attack some */

    attack_object(kill_ob);

    kill_ob = 0;
    return;
  }

  if (attacker_ob && present(attacker_ob, environment(this_object())) &&
    spell_chance > random(100)) {

    say(spell_mess1 + "\n", attacker_ob);
    tell_object(attacker_ob, spell_mess2 + "\n");
    call_other(attacker_ob, "hit_player", random(spell_dam));
  }
  attack();

  if (attacker_ob && whimpy && hit_point < max_hp/5)
    run_away();

  if(chat_chance || a_chat_chance){
    c = random(100);

    if(attacker_ob && a_chat_head) {
      if(c < a_chat_chance){
        c = random(sizeof(a_chat_head));

        tell_room(environment(), a_chat_head[c]);
      }
    } else {
      if(c < chat_chance && chat_chance){
        c = random(sizeof(chat_head));

        tell_room(environment(), chat_head[c]);
      }
    }
  }

  if(random_pick) {
    c = random(100);

    if(c < random_pick)
      pick_any_obj();
  }

  if(have_text && talk_ob) {
    have_text = 0;

    test_match(the_text);
  }
}

void init() {
  create_room = environment(me);

  if(this_player() == me)
    return;

  if(init_ob)
    if(call_other(init_ob,"monster_init",this_object()))
    return;

  if (attacker_ob) {
    set_heart_beat(1); /* Turn on heart beat */
  }

  if(this_player() && !call_other(this_player(),"query_npc")) {
    set_heart_beat(1);

    if (aggressive == 1)
      kill_ob = this_player();
  }
}

/*
* Force the monster to do a command. The force_us() function isn't
* always good, because it checks the level of the caller, and this function
* can be called by a room.
*/
void init_command(mixed cmd) {
  command(cmd);
}

/* Load attack chat */

void load_a_chat(int chance, mixed strs) {
  sizeof(strs);    /* Just ensure that it is an array. */

  a_chat_head = strs;
  a_chat_chance = chance;
}

void load_chat(int chance, mixed strs) {
  sizeof(strs);    /* Just ensure that it is an array. */

  chat_head = strs;
  chat_chance = chance;
}

void long() {
  write (long_desc);
}

void pick_any_obj() {
  object ob;
  int weight;

  ob = first_inventory(environment(this_object()));

  while(ob) {
    if (call_other(ob, "get", 0) && call_other(ob, "short")) {
      weight = call_other(ob, "query_weight", 0);

      if (!add_weight(weight)) {
        say(cap_name + " tries to take " + call_other(ob, "short", 0) +
        " but fails.\n");

        return;
      }

      move_object(ob, this_object());
      say(cap_name + " takes " + call_other(ob, "short", 0) + ".\n");

      if (call_other(ob, "weapon_class", 0))
        call_other(ob, "wield", call_other(ob,"query_name"));

      else if (call_other(ob, "armour_class", 0))
        call_other(ob, "wear", call_other(ob,"query_name"));

      return;
    }

    ob = next_inventory(ob);
  }
}

void random_move() {
  int n;

  n = random(4);

  if (n == 0)
    command("west");

  else if (n == 1)
    command("east");

  else if (n == 2)
    command("south");

  else if (n == 3)
    command("north");
}

void reset(mixed arg) {
  if (arg) {
    if (move_at_reset)
      random_move();

    return;
  }

  is_npc = 1;

  enable_commands();

  me = this_object();
  create_room = environment(me);
}

mixed second_life() {
  if(dead_ob)
    return call_other(dead_ob,"monster_died",this_object());
}

/* Optional */
void set_alias(mixed a) { alias = a; }
/* Optional */
void set_alt_name(mixed a) { alt_name = a; }
/* Optional */
void set_race(mixed r) { race = r; }
/* optional */
void set_hp(int hp) { max_hp = hp; hit_point = hp; }
/* optional. Can only be lowered */
void set_ep(int ep) { if (ep < experience) experience = ep; }
/* optional */
void set_al(int al) { alignment = al; }
/* optional */
void set_short(mixed sh) { short_desc = sh; long_desc = short_desc + "\n";}
/* optional */
void set_long(mixed lo) { long_desc = lo; }
/* optional */
void set_wc(int wc) { if (wc > weapon_class) weapon_class = wc; }
/* optional */
void set_ac(int ac) { if (ac > armour_class) armour_class = ac; }
/* optional */
void set_move_at_reset() { move_at_reset = 1; }
/* optional
* 0: Peaceful.
* 1: Attack on sight.
*/
void set_aggressive(int a) {
  aggressive = a;
}

/*
* Now some functions for setting up spells !
*/
/*
* The percent chance of casting a spell.
*/
void set_chance(int c) {
  spell_chance = c;
}

void set_dead_ob(object ob) {
  dead_ob = ob;
}

/* Set the frog curse. */
void set_frog() {
  frog = 1;
}

void set_init_ob(object ob) {
  init_ob = ob;
}

void set_level(int l) {
  level = l;
  Str = l; Int = l; Con = l; Dex = l;
  weapon_class = level/2 + 3;
  armour_class = level/4;
  hit_point = 50 + (level - 1) * 8;  /* Same as a player */
  max_hp = hit_point;
  spell_points = max_hp;
  experience = call_other("room/village/adv_guild", "query_cost", l-1);
  /* This is for level 1 monsters. */
  if (experience == 0)
    experience = random(500);
}

/* Catch the talk */

void set_match(object ob, mixed func, mixed type, mixed match) {
  object old;

  if (sizeof(func) != sizeof(type) || sizeof(match) != sizeof(type))
    return;

  talk_ob = ob;
  talk_func = func;
  talk_type = type;
  talk_match = match;

  say("talk match length " + sizeof(func) + "\n");
}

/*
* Call the following functions to setup the monster.
* Call them in the order they appear.
*/

void set_name(mixed n) {
  name = n;

  set_living_name(n);

  alignment = 0;    /* Neutral monster */
  cap_name = capitalize(n);
  short_desc = cap_name;
  long_desc = "You see nothing special.\n";
}

void set_random_pick(int r) {
  random_pick = r;
}

void set_spell_dam(int d) {
  spell_dam = d;
}

/* Message to the victim. */
void set_spell_mess1(mixed m) {
  spell_mess1 = m;
}

void set_spell_mess2(mixed m) {
  spell_mess2 = m;
}

/* Set the whimpy mode */
void set_whimpy() {
  whimpy = 1;
}

string short() {
  return short_desc;
}
object query_create_room() { return create_room; }

string query_race() { return race; }

mixed test_match(mixed str) {
  string who, str1, type, match, func;
  int i;

  while(i < sizeof(talk_match)) {
    if (talk_type[i])
      type = talk_type[i];

    match = talk_match[i];

    if (match == 0)
      match = "";

    if (talk_func[i])
      func = talk_func[i];

    if (sscanf(str,"%s " + type + match + " %s\n",who,str1) == 2 ||
      sscanf(str,"%s " + type + match + "\n",who) == 1 ||

    sscanf(str,"%s " + type + match + "%s\n",who,str1) == 2 ||
    sscanf(str,"%s " + type + " " + match + "\n",who) == 1 ||
    sscanf(str,"%s " + type + " " + match + " %s\n",who,str1) == 2) {
      return call_other(talk_ob, func, str);
    }
    i += 1;
  }
}
