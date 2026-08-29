string display_name;
string gender;
string details;
string *identities;
object *contributors;
int level;
int max_health;
int health;
int minimum_damage;
int maximum_damage;
int experience_reward;
int copper_reward;
string quest_defeat_tag;

void initialize(mixed first_load) {
  if (!display_name) {
    display_name = "hostile creature";
  }
  if (!gender) {
    gender = "non-binary";
  }
  if (!details) {
    details = "It watches you with open hostility.";
  }
  if (!identities) {
    identities = ({ "creature" });
  }
  if (!contributors) {
    contributors = ({ });
  }
  if (!quest_defeat_tag) {
    quest_defeat_tag = "";
  }
  jvmud_bind_entity_alias(jvmud_current_lpc_object(), "entity", "creature");
}

void configure(
    string name,
    string selected_gender,
    string description,
    int selected_level,
    int hit_points,
    int low_damage,
    int high_damage,
    int xp,
    int coins) {
  display_name = name;
  gender = selected_gender;
  details = description;
  level = selected_level;
  max_health = hit_points;
  health = hit_points;
  minimum_damage = low_damage;
  maximum_damage = high_damage;
  experience_reward = xp;
  copper_reward = coins;
  jvmud_bind_entity_alias(
      jvmud_current_lpc_object(),
      "entity",
      jvmud_lowercase_text(display_name));
}

void add_identity(string identity) {
  identities += ({ identity });
  jvmud_bind_entity_alias(jvmud_current_lpc_object(), "entity", identity);
}

void set_quest_defeat_tag(string tag) {
  quest_defeat_tag = tag;
}

int query_hostile() {
  return 1;
}

string query_name() {
  return display_name;
}

string query_gender() {
  return gender;
}

int query_level() {
  return level;
}

int id(mixed value) {
  int index;
  string normalized;

  if (!value) {
    return 0;
  }
  normalized = jvmud_lowercase_text(value);
  if (normalized == jvmud_lowercase_text(display_name)) {
    return 1;
  }
  index = 0;
  while (index < jvmud_size(identities)) {
    if (normalized == identities[index]) {
      return 1;
    }
    index += 1;
  }
  return 0;
}

string short() {
  return display_name;
}

void describe(object viewer) {
  string subject_word;
  string be_word;

  subject_word = jvmud_invoke_lpc_object("system/pronouns", "subject", gender);
  be_word = jvmud_invoke_lpc_object("system/pronouns", "be_present", gender);
  jvmud_write(display_name + "\n");
  jvmud_write(details + "\n");
  jvmud_write(jvmud_capitalize_text(subject_word) + " " + be_word + " a level " + level);
  jvmud_write(" combatant with " + health + "/" + max_health + " health.\n");
}

int receive_attack(object attacker, int damage) {
  object place;
  int counter_damage;

  place = jvmud_entity_location(jvmud_current_lpc_object());
  if (!attacker || jvmud_entity_location(attacker) != place) {
    return 0;
  }
  remember_contributor(attacker);
  health -= damage;
  jvmud_emit_perceivable_at(
      place,
      jvmud_invoke_lpc_object(attacker, "query_name") + " strikes "
          + display_name + " for " + damage + " damage.\n");
  if (health <= 0) {
    defeat(place);
    return 1;
  }
  counter_damage = minimum_damage;
  if (maximum_damage > minimum_damage) {
    counter_damage += jvmud_random(maximum_damage - minimum_damage + 1);
  }
  jvmud_invoke_lpc_object(attacker, "receive_damage", counter_damage, display_name);
  return 1;
}

void remember_contributor(object attacker) {
  int index;

  index = 0;
  while (index < jvmud_size(contributors)) {
    if (contributors[index] == attacker) {
      return;
    }
    index += 1;
  }
  contributors += ({ attacker });
}

void defeat(object place) {
  int index;
  object contributor;

  jvmud_emit_perceivable_at(place, display_name + " is defeated.\n");
  index = 0;
  while (index < jvmud_size(contributors)) {
    contributor = contributors[index];
    if (contributor && jvmud_entity_location(contributor) == place) {
      jvmud_invoke_lpc_object(
          contributor,
          "award_victory",
          experience_reward,
          copper_reward,
          display_name);
      jvmud_invoke_lpc_object(
          contributor,
          "record_quest_defeat",
          quest_defeat_tag);
    }
    index += 1;
  }
  jvmud_destroy_lpc_object(jvmud_current_lpc_object());
}
