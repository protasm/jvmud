string account_id;
string password_hash;
string pending_password;
string character_name;
string gender;
string character_class;
int account_created;
int save_version;
int level;
int experience;
int strength;
int dexterity;
int constitution;
int intelligence;
int wisdom;
int charisma;
int max_health;
int health;
int max_resource;
int resource;
int copper;
int attribute_points;
int introductory_drill_completed;
string inventory_state;
int inventory_materialized;
string quest_state;
mapping quest_stages;
mapping quest_counts;
mapping quest_flags;
int quests_materialized;
object combat_target;

void initialize(mixed first_load) {
  if (!account_id) {
    account_id = "";
  }
  if (!password_hash) {
    password_hash = "";
  }
  if (!pending_password) {
    pending_password = "";
  }
  if (!character_name) {
    character_name = "unformed companion";
  }
  if (!gender) {
    gender = "non-binary";
  }
  if (!character_class) {
    character_class = "";
  }
  if (!save_version) {
    save_version = 1;
  }
  if (!level) {
    level = 1;
  }
  if (!inventory_state) {
    inventory_state = "";
  }
  if (!quest_state) {
    quest_state = "";
  }
  if (!account_created && !copper) {
    copper = 120;
  }
  jvmud_schedule_recurring_tick(1, 1);
}

void begin_session() {
  write("\nThe western chapter of the Company of the Lantern welcomes you.\n");
  avelorn_prompt("Account ID: ");
  jvmud_capture_session_input("receive_account_id", 0);
}

void receive_account_id(mixed value) {
  string requested;

  requested = normalize_account_id(value);
  if (!valid_account_id(requested)) {
    write("Use 3-24 letters, numbers, underscores, or dashes.\n");
    avelorn_prompt("Account ID: ");
    jvmud_capture_session_input("receive_account_id", 0);
    return;
  }

  account_id = requested;
  if (jvmud_restore_lpc_object_state("accounts/" + requested)) {
    account_id = requested;
    pending_password = "";
    inventory_materialized = 0;
    quests_materialized = 0;
    avelorn_prompt("Password: ");
    jvmud_capture_session_input("receive_login_password", 1);
    return;
  }

  avelorn_prompt("No account named " + requested + " exists. Create it? (yes/no) ");
  jvmud_capture_session_input("confirm_account_creation", 0);
}

void confirm_account_creation(mixed value) {
  string answer;

  answer = jvmud_lowercase_text(value);
  if (answer == "yes" || answer == "y") {
    avelorn_prompt("Choose a password: ");
    jvmud_capture_session_input("receive_new_password", 1);
    return;
  }
  if (answer == "no" || answer == "n") {
    account_id = "";
    write("No account was created.\n");
    avelorn_prompt("Account ID: ");
    jvmud_capture_session_input("receive_account_id", 0);
    return;
  }
  avelorn_prompt("Please answer yes or no: ");
  jvmud_capture_session_input("confirm_account_creation", 0);
}

void receive_new_password(mixed value) {
  mixed problem;

  problem = password_problem(value);
  if (problem) {
    write(problem + "\n");
    avelorn_prompt("Choose a password: ");
    jvmud_capture_session_input("receive_new_password", 1);
    return;
  }

  pending_password = value;
  avelorn_prompt("Password again: ");
  jvmud_capture_session_input("confirm_new_password", 1);
}

void confirm_new_password(mixed value) {
  if (value != pending_password) {
    pending_password = "";
    write("Those passwords did not match.\n");
    avelorn_prompt("Choose a password: ");
    jvmud_capture_session_input("receive_new_password", 1);
    return;
  }

  password_hash = jvmud_hash_password(value);
  pending_password = "";
  avelorn_prompt("Character name: ");
  jvmud_capture_session_input("receive_character_name", 0);
}

void receive_login_password(mixed value) {
  if (!jvmud_verify_password(value, password_hash)) {
    write("That password did not match.\n");
    avelorn_prompt("Password: ");
    jvmud_capture_session_input("receive_login_password", 1);
    return;
  }

  pending_password = "";
  enter_avelorn(1);
}

void receive_character_name(mixed value) {
  if (!valid_character_name(value)) {
    write("Use 2-24 letters, spaces, apostrophes, or dashes.\n");
    avelorn_prompt("Character name: ");
    jvmud_capture_session_input("receive_character_name", 0);
    return;
  }

  character_name = jvmud_capitalize_text(jvmud_lowercase_text(value));
  avelorn_prompt("Gender (male/female/non-binary): ");
  jvmud_capture_session_input("receive_gender", 0);
}

void receive_gender(mixed value) {
  string selected;

  selected = jvmud_lowercase_text(value);
  if (!valid_gender(selected)) {
    avelorn_prompt("Please choose male, female, or non-binary: ");
    jvmud_capture_session_input("receive_gender", 0);
    return;
  }

  gender = selected;
  avelorn_prompt("Class (fighter/ranger/mage/cleric): ");
  jvmud_capture_session_input("receive_class", 0);
}

void receive_class(mixed value) {
  string selected;

  selected = jvmud_lowercase_text(value);
  if (!valid_class(selected)) {
    avelorn_prompt("Please choose fighter, ranger, mage, or cleric: ");
    jvmud_capture_session_input("receive_class", 0);
    return;
  }

  character_class = selected;
  configure_starting_attributes();
  account_created = 1;
  inventory_materialized = 1;
  quests_materialized = 1;
  quest_stages = ([ ]);
  quest_counts = ([ ]);
  quest_flags = ([ ]);
  grant_starter_kit();
  recalculate_resources(1);
  save_character();
  enter_avelorn(0);
}

void enter_avelorn(int returning) {
  jvmud_enable_commands();
  materialize_inventory();
  materialize_quests();
  recalculate_resources(0);
  jvmud_bind_entity_alias(
      jvmud_current_lpc_object(),
      "entity",
      jvmud_lowercase_text(character_name));
  jvmud_bind_entity_alias(
      jvmud_current_lpc_object(),
      "living",
      jvmud_lowercase_text(character_name));

  if (returning) {
    write("\nWelcome back, " + character_name + ".\n\n");
  } else {
    write("\nYour name is entered upon the roll of the Company of the Lantern.\n");
    write("Welcome to Avelorn, " + character_name + ".\n\n");
  }
  look(0);
}

void end_session() {
  combat_target = 0;
  save_character();
  clear_materialized_inventory();
}

void save_character() {
  int saved;

  if (account_created && jvmud_size(account_id) > 0 && jvmud_size(password_hash) > 0) {
    pending_password = "";
    if (inventory_materialized) {
      snapshot_inventory();
    }
    if (quests_materialized) {
      snapshot_quests();
    }
    saved = jvmud_save_lpc_object_state("accounts/" + account_id);
    if (!saved) {
      write("Warning: Avelorn could not write your character snapshot to the host filesystem.\n");
    }
  }
}

void offer_interactions() {
  jvmud_add_action("look", "look");
  jvmud_add_action("look", "l");
  jvmud_add_action("score", "score");
  jvmud_add_action("inventory", "inventory");
  jvmud_add_action("inventory", "i");
  jvmud_add_action("equipment", "equipment");
  jvmud_add_action("equipment", "eq");
  jvmud_add_action("get_item", "get");
  jvmud_add_action("get_item", "take");
  jvmud_add_action("drop_item", "drop");
  jvmud_add_action("equip_item", "equip");
  jvmud_add_action("unequip_item", "unequip");
  jvmud_add_action("use_item", "use");
  jvmud_add_action("attack", "attack");
  jvmud_add_action("attack", "fight");
  jvmud_add_action("consider", "consider");
  jvmud_add_action("class_ability", "ability");
  jvmud_add_action("class_ability", "technique");
  jvmud_add_action("quests", "quests");
  jvmud_add_action("quests", "journal");
  jvmud_add_action("money", "money");
  jvmud_add_action("improve", "improve");
  jvmud_add_action("pronouns_command", "pronouns");
  jvmud_add_action("who", "who");
  jvmud_add_action("say", "say");
  jvmud_add_action("tell", "tell");
  jvmud_add_action("help", "help");
  jvmud_add_action("save_command", "save");
  jvmud_add_action("quit", "quit");
  jvmud_invoke_lpc_object("system/socials", "register", 0);
}

int look(mixed target) {
  object place;
  object entity;

  place = jvmud_entity_location(jvmud_current_lpc_object());
  if (!place) {
    write("You are nowhere in Avelorn.\n");
    return 1;
  }
  if (target) {
    entity = jvmud_find_entity(target, place);
    if (!entity) {
      entity = jvmud_find_entity(target, jvmud_current_lpc_object());
    }
    if (!entity) {
      write("You do not see that here.\n");
      return 1;
    }
    jvmud_invoke_lpc_object(entity, "describe", jvmud_current_lpc_object());
    return 1;
  }
  jvmud_invoke_lpc_object(place, "describe", jvmud_current_lpc_object());
  return 1;
}

int travel_to(string direction, string destination) {
  object new_place;

  if (combat_target) {
    write("You break away from combat and flee " + direction + ".\n");
    combat_target = 0;
  }
  avelorn_emit_except(
      jvmud_current_lpc_object(),
      character_name + " leaves " + direction + ".\n",
      jvmud_current_lpc_object());
  jvmud_move_entity(jvmud_current_lpc_object(), destination);
  new_place = jvmud_entity_location(jvmud_current_lpc_object());
  avelorn_emit_except(
      jvmud_current_lpc_object(),
      character_name + " arrives.\n",
      jvmud_current_lpc_object());
  jvmud_invoke_lpc_object(new_place, "describe", jvmud_current_lpc_object());
  return 1;
}

int score(mixed ignored) {
  write(character_name + ", level " + level + " " + character_class + "\n");
  write("Health " + health + "/" + max_health + "  ");
  write(resource_name() + " " + resource + "/" + max_resource + "\n");
  if (level < 10) {
    write("Experience " + experience + "/" + experience_for_next_level() + "\n");
  } else {
    write("Experience: level cap reached\n");
  }
  write("Strength " + strength + "  Dexterity " + dexterity);
  write("  Constitution " + constitution + "\n");
  write("Intelligence " + intelligence + "  Wisdom " + wisdom);
  write("  Charisma " + charisma + "\n");
  write("Unspent attribute points: " + attribute_points + "\n");
  write("Coin: " + money_text() + "\n");
  return 1;
}

int inventory(mixed ignored) {
  object item;
  int total_weight;

  item = jvmud_first_entity_at(jvmud_current_lpc_object());
  if (!item) {
    write("You are carrying nothing.\n");
    return 1;
  }
  write("You are carrying:\n");
  while (item) {
    if (jvmud_method_exists("query_blueprint", item)) {
      write("  " + jvmud_invoke_lpc_object(item, "short") + "\n");
      total_weight += jvmud_invoke_lpc_object(item, "query_weight");
    }
    item = jvmud_next_entity_at(item);
  }
  write("Carried weight: " + total_weight + "/" + carry_capacity() + ".\n");
  return 1;
}

int equipment(mixed ignored) {
  object item;
  int found;

  write("Equipped:\n");
  item = jvmud_first_entity_at(jvmud_current_lpc_object());
  while (item) {
    if (is_item(item) && jvmud_invoke_lpc_object(item, "query_equipped")) {
      write("  " + jvmud_invoke_lpc_object(item, "query_slot") + ": ");
      write(jvmud_invoke_lpc_object(item, "short") + "\n");
      found = 1;
    }
    item = jvmud_next_entity_at(item);
  }
  if (!found) {
    write("  Nothing.\n");
  }
  return 1;
}

int get_item(mixed target) {
  object place;
  object item;
  int item_weight;

  if (!target) {
    write("Get what?\n");
    return 1;
  }
  place = jvmud_entity_location(jvmud_current_lpc_object());
  item = jvmud_find_entity(target, place);
  if (!item || !is_item(item)) {
    write("You do not see a portable item by that name.\n");
    return 1;
  }
  item_weight = jvmud_invoke_lpc_object(item, "query_weight");
  if (carried_weight() + item_weight > carry_capacity()) {
    write("That would exceed your carrying capacity.\n");
    return 1;
  }
  jvmud_move_entity(item, jvmud_current_lpc_object());
  write("You take " + jvmud_invoke_lpc_object(item, "short") + ".\n");
  save_character();
  return 1;
}

int drop_item(mixed target) {
  object item;
  object place;

  if (!target) {
    write("Drop what?\n");
    return 1;
  }
  item = jvmud_find_entity(target, jvmud_current_lpc_object());
  if (!item || !is_item(item)) {
    write("You are not carrying that.\n");
    return 1;
  }
  if (jvmud_invoke_lpc_object(item, "query_equipped")) {
    write("Unequip it before dropping it.\n");
    return 1;
  }
  place = jvmud_entity_location(jvmud_current_lpc_object());
  jvmud_move_entity(item, place);
  write("You drop " + jvmud_invoke_lpc_object(item, "short") + ".\n");
  save_character();
  return 1;
}

int equip_item(mixed target) {
  object item;
  object other;
  string slot;
  string stat;
  int minimum;

  if (!target) {
    write("Equip what?\n");
    return 1;
  }
  item = jvmud_find_entity(target, jvmud_current_lpc_object());
  if (!item || !is_item(item)) {
    write("You are not carrying that item.\n");
    return 1;
  }
  slot = jvmud_invoke_lpc_object(item, "query_slot");
  if (slot == "none") {
    write("That item cannot be equipped.\n");
    return 1;
  }
  other = jvmud_first_entity_at(jvmud_current_lpc_object());
  while (other) {
    if (other != item && is_item(other)
        && jvmud_invoke_lpc_object(other, "query_equipped")
        && jvmud_invoke_lpc_object(other, "query_slot") == slot) {
      jvmud_invoke_lpc_object(other, "set_equipped", 0);
    }
    other = jvmud_next_entity_at(other);
  }
  jvmud_invoke_lpc_object(item, "set_equipped", 1);
  write("You equip " + jvmud_invoke_lpc_object(item, "short") + ".\n");
  stat = jvmud_invoke_lpc_object(item, "query_governing_stat");
  minimum = jvmud_invoke_lpc_object(item, "query_minimum_stat");
  if (minimum > 0 && stat_value(stat) < minimum) {
    write("You can use it, but your " + stat + " is below the recommended " + minimum);
    write(", so it will be less effective.\n");
  }
  save_character();
  return 1;
}

int unequip_item(mixed target) {
  object item;

  if (!target) {
    write("Unequip what?\n");
    return 1;
  }
  item = jvmud_find_entity(target, jvmud_current_lpc_object());
  if (!item || !is_item(item) || !jvmud_invoke_lpc_object(item, "query_equipped")) {
    write("You do not have that equipped.\n");
    return 1;
  }
  jvmud_invoke_lpc_object(item, "set_equipped", 0);
  write("You unequip " + jvmud_invoke_lpc_object(item, "short") + ".\n");
  save_character();
  return 1;
}

int use_item(mixed target) {
  object item;
  int amount;

  if (!target) {
    write("Use what?\n");
    return 1;
  }
  item = jvmud_find_entity(target, jvmud_current_lpc_object());
  if (!item || !is_item(item)) {
    write("You are not carrying that item.\n");
    return 1;
  }
  amount = jvmud_invoke_lpc_object(item, "query_restore_amount");
  if (amount <= 0) {
    write("That item is not consumable.\n");
    return 1;
  }
  health += amount;
  if (health > max_health) {
    health = max_health;
  }
  write("You use " + jvmud_invoke_lpc_object(item, "short"));
  write(" and recover " + amount + " health.\n");
  jvmud_destroy_lpc_object(item);
  save_character();
  return 1;
}

int money(mixed ignored) {
  write("You carry " + money_text() + ".\n");
  return 1;
}

int attack(mixed target) {
  object place;
  object opponent;

  if (!target) {
    write("Attack what?\n");
    return 1;
  }
  place = jvmud_entity_location(jvmud_current_lpc_object());
  opponent = jvmud_find_entity(target, place);
  if (!opponent || !jvmud_method_exists("query_hostile", opponent)
      || !jvmud_invoke_lpc_object(opponent, "query_hostile")) {
    write("That is not a hostile combatant.\n");
    return 1;
  }
  if (combat_target == opponent) {
    write("You are already fighting "
        + jvmud_invoke_lpc_object(opponent, "query_name") + ".\n");
    return 1;
  }
  combat_target = opponent;
  write("You engage " + jvmud_invoke_lpc_object(opponent, "query_name")
      + " in combat.\n");
  perform_combat_round();
  return 1;
}

void scheduled_update() {
  if (combat_target) {
    perform_combat_round();
  }
}

void perform_combat_round() {
  object place;
  object opponent;

  opponent = combat_target;
  if (!opponent) {
    return;
  }
  place = jvmud_entity_location(jvmud_current_lpc_object());
  if (!place
      || jvmud_entity_location(opponent) != place
      || !jvmud_method_exists("query_hostile", opponent)
      || !jvmud_invoke_lpc_object(opponent, "query_hostile")) {
    combat_target = 0;
    write("Your opponent is no longer within reach.\n");
    return;
  }
  jvmud_invoke_lpc_object(
      opponent,
      "receive_attack",
      jvmud_current_lpc_object(),
      attack_damage());
}

int consider(mixed target) {
  object place;
  object opponent;
  int opponent_level;
  int difference;
  string opponent_gender;
  string subject_word;
  string be_word;

  if (!target) {
    write("Consider what?\n");
    return 1;
  }
  place = jvmud_entity_location(jvmud_current_lpc_object());
  opponent = jvmud_find_entity(target, place);
  if (!opponent || !jvmud_method_exists("query_hostile", opponent)
      || !jvmud_invoke_lpc_object(opponent, "query_hostile")) {
    write("That is not a hostile combatant.\n");
    return 1;
  }
  opponent_level = jvmud_invoke_lpc_object(opponent, "query_level");
  difference = opponent_level - level;
  opponent_gender = jvmud_invoke_lpc_object(opponent, "query_gender");
  subject_word = jvmud_invoke_lpc_object("system/pronouns", "subject", opponent_gender);
  be_word = jvmud_invoke_lpc_object("system/pronouns", "be_present", opponent_gender);
  write(jvmud_invoke_lpc_object(opponent, "query_name") + " is level ");
  write(opponent_level + ". ");
  if (difference >= 3) {
    write(jvmud_capitalize_text(subject_word) + " " + be_word);
    write(" far beyond your present training, though you may still engage ");
    write(jvmud_invoke_lpc_object("system/pronouns", "object_form", opponent_gender) + ".\n");
  } else if (difference > 0) {
    write(jvmud_capitalize_text(subject_word) + " " + be_word);
    write(" a dangerous challenge for you, but not an impossible one.\n");
  } else if (difference < -2) {
    write(jvmud_capitalize_text(subject_word) + " should pose little danger to you.\n");
  } else {
    write(jvmud_capitalize_text(subject_word) + " " + be_word);
    write(" an appropriate challenge for your present training.\n");
  }
  return 1;
}

int class_ability(mixed target) {
  object place;
  object opponent;
  int cost;
  int damage;
  string technique;

  if (!target) {
    write("Use your technique against what?\n");
    return 1;
  }
  place = jvmud_entity_location(jvmud_current_lpc_object());
  opponent = jvmud_find_entity(target, place);
  if (!opponent || !jvmud_method_exists("query_hostile", opponent)
      || !jvmud_invoke_lpc_object(opponent, "query_hostile")) {
    write("That is not a hostile combatant.\n");
    return 1;
  }
  if (character_class == "fighter") {
    technique = "a disciplined mighty blow";
    cost = 8;
    damage = attack_damage() + level + strength / 2;
  } else if (character_class == "ranger") {
    technique = "a carefully aimed shot";
    cost = 8;
    damage = attack_damage() + level + dexterity / 2;
  } else if (character_class == "mage") {
    technique = "a bolt of blue-white arcane fire";
    cost = 10;
    damage = attack_damage() + level + intelligence / 2;
  } else {
    technique = "a radiant Lantern smite";
    cost = 10;
    damage = attack_damage() + level + wisdom / 2;
  }
  if (resource < cost) {
    write("You need " + cost + " " + jvmud_lowercase_text(resource_name()));
    write(" for that technique. Rest at a shrine to recover.\n");
    return 1;
  }
  resource -= cost;
  combat_target = opponent;
  write("You unleash " + technique + ".\n");
  return jvmud_invoke_lpc_object(
      opponent,
      "receive_attack",
      jvmud_current_lpc_object(),
      damage);
}

int receive_damage(int amount, string attacker_name) {
  int lost_copper;

  if (amount < 1) {
    amount = 1;
  }
  health -= amount;
  write(attacker_name + " strikes you for " + amount + " damage.\n");
  if (health > 0) {
    write("You have " + health + "/" + max_health + " health remaining.\n");
    return health;
  }

  avelorn_emit_except(
      jvmud_current_lpc_object(),
      character_name + " is overcome and carried to Sister Elara's care.\n",
      jvmud_current_lpc_object());
  combat_target = 0;
  lost_copper = copper / 10;
  copper -= lost_copper;
  recalculate_resources(1);
  jvmud_move_entity(jvmud_current_lpc_object(), "place/brindleford/shrine");
  write("You are overcome. Crown wardens bring you safely to the village shrine.\n");
  if (lost_copper > 0) {
    write("You lost "
        + jvmud_invoke_lpc_object("system/economy", "format_money", lost_copper)
        + " in the retreat.\n");
  }
  save_character();
  look(0);
  return 0;
}

void award_victory(int xp, int coins, string opponent_name, object opponent) {
  if (combat_target == opponent) {
    combat_target = 0;
  }
  avelorn_write_to(
      jvmud_current_lpc_object(),
      "You help defeat " + opponent_name + ".\n");
  if (coins > 0) {
    copper += coins;
    avelorn_write_to(
        jvmud_current_lpc_object(),
        "The recovered bounty is worth "
            + jvmud_invoke_lpc_object("system/economy", "format_money", coins) + ".\n");
  }
  gain_experience(xp);
  save_character();
}

int offer_quest(string quest_id) {
  int stage;
  int recommended_level;

  materialize_quests();
  stage = quest_stage(quest_id);
  if (stage == 3) {
    write("You have already completed " + quest_title(quest_id) + ".\n");
    return 1;
  }
  if (stage > 0) {
    write("That assignment is already recorded in your journal.\n");
    return 1;
  }
  recommended_level = jvmud_invoke_lpc_object("system/quests", "recommended_level", quest_id);
  write(quest_title(quest_id) + " (recommended level " + recommended_level + ")\n");
  write(jvmud_invoke_lpc_object("system/quests", "description", quest_id) + "\n");
  if (level < recommended_level) {
    write("This is beyond your current training, but the Company will not forbid the attempt.\n");
  }
  quest_stages[quest_id] = 1;
  quest_counts[quest_id] = 0;
  write("You accept the assignment.\n");
  save_character();
  return 1;
}

void record_quest_defeat(string quest_tag) {
  string quest_id;
  string flag_id;
  mapping flags;
  int required;
  int count;

  if (!quest_tag || jvmud_size(quest_tag) == 0) {
    return;
  }
  materialize_quests();
  quest_id = jvmud_invoke_lpc_object("system/quests", "quest_for_defeat_tag", quest_tag);
  if (!quest_id || quest_stage(quest_id) != 1) {
    return;
  }
  if (!jvmud_invoke_lpc_object("system/quests", "repeatable_defeat_tag", quest_tag)) {
    if (jvmud_member(quest_flags, quest_id)) {
      flags = quest_flags[quest_id];
    } else {
      flags = ([ ]);
    }
    flag_id = "defeat:" + quest_tag;
    if (jvmud_member(flags, flag_id)) {
      return;
    }
    flags[flag_id] = 1;
    quest_flags[quest_id] = flags;
  }
  count = quest_count(quest_id) + 1;
  required = jvmud_invoke_lpc_object("system/quests", "required_count", quest_id);
  quest_counts[quest_id] = count;
  avelorn_write_to(
      jvmud_current_lpc_object(),
      quest_title(quest_id) + ": " + count + "/" + required + ".\n");
  if (count >= required) {
    quest_stages[quest_id] = 2;
    avelorn_write_to(
        jvmud_current_lpc_object(),
        "The assignment is ready to report.\n");
  }
  save_character();
}

int record_quest_action(string action_tag) {
  string quest_id;
  mapping flags;
  int count;
  int required;

  materialize_quests();
  quest_id = jvmud_invoke_lpc_object("system/quests", "quest_for_action_tag", action_tag);
  if (!quest_id || quest_stage(quest_id) == 0 || quest_stage(quest_id) == 3) {
    write("You make a careful inspection, but no current Company assignment concerns it.\n");
    return 1;
  }
  if (jvmud_member(quest_flags, quest_id)) {
    flags = quest_flags[quest_id];
  } else {
    flags = ([ ]);
  }
  if (jvmud_member(flags, action_tag)) {
    write("You have already recorded this objective.\n");
    return 1;
  }
  if (quest_stage(quest_id) != 1) {
    write("That assignment is already ready to report.\n");
    return 1;
  }
  flags[action_tag] = 1;
  quest_flags[quest_id] = flags;
  count = quest_count(quest_id) + 1;
  quest_counts[quest_id] = count;
  required = jvmud_invoke_lpc_object("system/quests", "required_count", quest_id);
  write(quest_title(quest_id) + ": " + count + "/" + required + ".\n");
  if (count >= required) {
    quest_stages[quest_id] = 2;
    write("The assignment is ready to report.\n");
  }
  save_character();
  return 1;
}

int turn_in_quest(string quest_id) {
  object reward_item;
  int stage;
  int xp;
  int coins;

  materialize_quests();
  stage = quest_stage(quest_id);
  if (stage == 0) {
    write("You have not accepted that assignment. Ask for work first.\n");
    return 1;
  }
  if (stage == 1) {
    write("The assignment is not yet complete.\n");
    return 1;
  }
  if (stage == 3) {
    write("That assignment has already been reported and rewarded.\n");
    return 1;
  }

  quest_stages[quest_id] = 3;
  xp = jvmud_invoke_lpc_object("system/quests", "experience_reward", quest_id);
  coins = jvmud_invoke_lpc_object("system/quests", "copper_reward", quest_id);
  copper += coins;
  write("You complete " + quest_title(quest_id) + ".\n");
  write("The Company awards "
      + jvmud_invoke_lpc_object("system/economy", "format_money", coins) + ".\n");
  gain_experience(xp);
  if (jvmud_size(jvmud_invoke_lpc_object("system/quests", "item_reward", quest_id)) > 0) {
    reward_item = jvmud_invoke_lpc_object(
        "system/items",
        "create",
        jvmud_invoke_lpc_object("system/quests", "item_reward", quest_id));
    if (reward_item) {
      jvmud_move_entity(reward_item, jvmud_current_lpc_object());
      write("You receive " + jvmud_invoke_lpc_object(reward_item, "short") + ".\n");
    }
  }
  if (quest_id == "rekindle-western-lantern") {
    write("Blue fire runs from Ashenwatch through every western wardstone. ");
    write("The Lantern Crown shines whole above a safe and grateful kingdom.\n");
  }
  save_character();
  return 1;
}

int quests(mixed ignored) {
  mixed *ids;
  int index;
  int stage;
  string quest_id;

  materialize_quests();
  ids = jvmud_mapping_keys(quest_stages);
  if (jvmud_size(ids) == 0) {
    write("Your Company journal contains no assignments.\n");
    return 1;
  }
  write("Company assignments:\n");
  index = 0;
  while (index < jvmud_size(ids)) {
    quest_id = ids[index];
    stage = quest_stage(quest_id);
    write("  " + quest_title(quest_id) + " — " + quest_stage_text(stage));
    if (stage == 1 || stage == 2) {
      write(" (" + quest_count(quest_id) + "/");
      write(jvmud_invoke_lpc_object("system/quests", "required_count", quest_id) + ")");
    }
    write("\n");
    index += 1;
  }
  return 1;
}

int purchase_blueprint(string blueprint) {
  object item;
  int price;
  int item_weight;

  item = jvmud_invoke_lpc_object("system/items", "create", blueprint);
  if (!item) {
    write("That stock entry is unavailable.\n");
    return 1;
  }
  price = jvmud_invoke_lpc_object(item, "query_value");
  item_weight = jvmud_invoke_lpc_object(item, "query_weight");
  if (copper < price) {
    write("You do not have enough Crown coin.\n");
    jvmud_destroy_lpc_object(item);
    return 1;
  }
  if (carried_weight() + item_weight > carry_capacity()) {
    write("You cannot carry that much weight.\n");
    jvmud_destroy_lpc_object(item);
    return 1;
  }
  copper -= price;
  jvmud_move_entity(item, jvmud_current_lpc_object());
  write("You buy " + jvmud_invoke_lpc_object(item, "short") + " for ");
  write(jvmud_invoke_lpc_object("system/economy", "format_money", price) + ".\n");
  save_character();
  return 1;
}

int sell_item(mixed target) {
  object item;
  int sale_price;
  string item_name;

  item = jvmud_find_entity(target, jvmud_current_lpc_object());
  if (!item || !is_item(item)) {
    write("You are not carrying that item.\n");
    return 1;
  }
  if (jvmud_invoke_lpc_object(item, "query_equipped")) {
    write("Unequip it before selling it.\n");
    return 1;
  }
  sale_price = jvmud_invoke_lpc_object(item, "query_value") / 2;
  item_name = jvmud_invoke_lpc_object(item, "short");
  jvmud_destroy_lpc_object(item);
  copper += sale_price;
  write("You sell " + item_name + " for ");
  write(jvmud_invoke_lpc_object("system/economy", "format_money", sale_price) + ".\n");
  save_character();
  return 1;
}

int improve(mixed target) {
  string stat;

  if (!target) {
    write("Improve which attribute?\n");
    return 1;
  }
  if (attribute_points <= 0) {
    write("You have no unspent attribute points.\n");
    return 1;
  }
  stat = jvmud_lowercase_text(target);
  if (stat == "strength") {
    strength += 1;
  } else if (stat == "dexterity") {
    dexterity += 1;
  } else if (stat == "constitution") {
    constitution += 1;
  } else if (stat == "intelligence") {
    intelligence += 1;
  } else if (stat == "wisdom") {
    wisdom += 1;
  } else if (stat == "charisma") {
    charisma += 1;
  } else {
    write("Choose strength, dexterity, constitution, intelligence, wisdom, or charisma.\n");
    return 1;
  }
  attribute_points -= 1;
  recalculate_resources(0);
  write("Your " + stat + " improves to " + stat_value(stat) + ".\n");
  save_character();
  return 1;
}

int complete_introductory_drill() {
  if (introductory_drill_completed) {
    write("You have already completed the introductory Company drill.\n");
    return 1;
  }
  introductory_drill_completed = 1;
  write("You complete the Company's measured course of footwork, signals, and field discipline.\n");
  gain_experience(100);
  save_character();
  return 1;
}

int rest_at_shrine() {
  recalculate_resources(1);
  write("You rest beneath the Seven Lamps and recover your health and "
      + jvmud_lowercase_text(resource_name()) + ".\n");
  save_character();
  return 1;
}

int pronouns_command(mixed ignored) {
  string subject_word;
  string be_word;
  string have_word;

  subject_word = pronoun("subject");
  be_word = pronoun("be_present");
  have_word = pronoun("have_present");
  write(character_name + " uses " + subject_word + "/" + pronoun("object_form") + " pronouns.\n");
  write(jvmud_capitalize_text(subject_word) + " " + be_word);
  write(" a sworn novice, and " + subject_word + " " + have_word);
  write(" begun " + pronoun("possessive_adjective") + " service to the Crown.\n");
  return 1;
}

int who(mixed ignored) {
  object *connected;
  object persona;
  string name;
  int index;
  int online;

  connected = jvmud_users();
  index = 0;
  while (index < jvmud_size(connected)) {
    if (is_online_adventurer(connected[index])) {
      online += 1;
    }
    index += 1;
  }

  write("Adventurers of the Lantern (" + online + " online):\n");
  index = 0;
  while (index < jvmud_size(connected)) {
    persona = connected[index];
    if (is_online_adventurer(persona)) {
      name = jvmud_invoke_lpc_object(persona, "query_name");
      write("  " + name + " - level ");
      write(jvmud_invoke_lpc_object(persona, "query_level") + " ");
      write(jvmud_invoke_lpc_object(persona, "query_class") + " (");
      write(jvmud_invoke_lpc_object(persona, "pronoun", "subject") + "/");
      write(jvmud_invoke_lpc_object(persona, "pronoun", "object_form") + ")\n");
    }
    index += 1;
  }
  return 1;
}

int say(mixed value) {
  string message;

  if (!value || jvmud_size(value) == 0) {
    write("Say what?\n");
    return 1;
  }
  message = jvmud_to_string(value);
  write("You say, \"" + message + "\"\n");
  avelorn_emit_except(
      jvmud_current_lpc_object(),
      character_name + " says, \"" + message + "\"\n",
      jvmud_current_lpc_object());
  return 1;
}

int tell(mixed value) {
  object *connected;
  object persona;
  object target;
  string input;
  string lowered_input;
  string message;
  string name;
  string lowered_name;
  int index;
  int name_length;
  int target_name_length;

  if (!value || jvmud_size(value) == 0) {
    write("Tell whom what?\n");
    return 1;
  }

  input = jvmud_to_string(value);
  lowered_input = jvmud_lowercase_text(input);
  connected = jvmud_users();
  index = 0;
  while (index < jvmud_size(connected)) {
    persona = connected[index];
    if (is_online_adventurer(persona)) {
      name = jvmud_invoke_lpc_object(persona, "query_name");
      lowered_name = jvmud_lowercase_text(name);
      name_length = jvmud_size(name);
      if (lowered_input == lowered_name
          || (jvmud_size(input) > name_length
              && jvmud_extract_text(lowered_input, 0, name_length - 1) == lowered_name
              && lowered_input[name_length] == ' ')) {
        if (!target || name_length > target_name_length) {
          target = persona;
          target_name_length = name_length;
          if (jvmud_size(input) > name_length) {
            message = jvmud_extract_text(input, name_length + 1);
          } else {
            message = "";
          }
        }
      }
    }
    index += 1;
  }

  if (!target) {
    write("No adventurer by that name is presently in Avelorn.\n");
    return 1;
  }
  name = jvmud_invoke_lpc_object(target, "query_name");
  if (jvmud_size(message) == 0) {
    write("Tell " + name + " what?\n");
    return 1;
  }
  if (target == jvmud_current_lpc_object()) {
    write("You consider telling yourself, then think better of it.\n");
    return 1;
  }

  write("You tell " + name + ", \"" + message + "\"\n");
  avelorn_write_to(
      target,
      character_name + " tells you, \"" + message + "\"\n");
  return 1;
}

int is_online_adventurer(object persona) {
  if (!persona
      || !jvmud_method_exists("query_name", persona)
      || !jvmud_method_exists("query_class", persona)
      || !jvmud_method_exists("query_level", persona)) {
    return 0;
  }
  return jvmud_size(jvmud_invoke_lpc_object(persona, "query_name")) > 0
      && jvmud_size(jvmud_invoke_lpc_object(persona, "query_class")) > 0;
}

int help(mixed ignored) {
  write("Avelorn commands:\n");
  write("  look, north/east/south/west, score, pronouns, who\n");
  write("  say <message>, tell <character> <message>\n");
  write("  inventory, equipment, get, drop, equip, unequip, use\n");
  write("  attack <target> begins timed combat; ability uses a technique\n");
  write("  consider, quests, money, list, buy, sell\n");
  write("  train, improve, emotes, save, quit, help\n");
  return 1;
}

int quit(mixed ignored) {
  combat_target = 0;
  save_character();
  write("You leave the road in the Company's keeping. Farewell.\n");
  clear_materialized_inventory();
  jvmud_destroy_lpc_object(jvmud_current_lpc_object());
  return 1;
}

int save_command(mixed ignored) {
  save_character();
  write("Your character has been saved.\n");
  return 1;
}

string pronoun(string form) {
  return jvmud_invoke_lpc_object("system/pronouns", form, gender);
}

string query_name() {
  return character_name;
}

string query_gender() {
  return gender;
}

string query_class() {
  return character_class;
}

int query_level() {
  return level;
}

void grant_starter_kit() {
  object item;
  string weapon;

  if (character_class == "fighter") {
    weapon = "weapon/crown-arming-sword";
  } else if (character_class == "ranger") {
    weapon = "weapon/ashwood-shortbow";
  } else if (character_class == "mage") {
    weapon = "weapon/oak-focus-staff";
  } else {
    weapon = "weapon/temple-mace";
  }
  item = jvmud_invoke_lpc_object("system/items", "create", weapon);
  jvmud_invoke_lpc_object(item, "set_equipped", 1);
  jvmud_move_entity(item, jvmud_current_lpc_object());

  item = jvmud_invoke_lpc_object("system/items", "create", "armor/travel-cloak");
  jvmud_invoke_lpc_object(item, "set_equipped", 1);
  jvmud_move_entity(item, jvmud_current_lpc_object());

  item = jvmud_invoke_lpc_object("system/items", "create", "consumable/healing-draught");
  jvmud_move_entity(item, jvmud_current_lpc_object());
}

void snapshot_inventory() {
  mixed *states;
  mapping state;
  object item;

  states = ({ });
  item = jvmud_first_entity_at(jvmud_current_lpc_object());
  while (item) {
    if (is_item(item)) {
      state = ([
        "blueprint": jvmud_invoke_lpc_object(item, "query_blueprint"),
        "equipped": jvmud_invoke_lpc_object(item, "query_equipped")
      ]);
      states += ({ state });
    }
    item = jvmud_next_entity_at(item);
  }
  inventory_state = jvmud_serialize_lpc_value(states);
}

void materialize_inventory() {
  mixed decoded;
  mixed *states;
  mapping state;
  object item;
  int index;

  if (inventory_materialized) {
    return;
  }
  inventory_materialized = 1;
  if (!inventory_state || jvmud_size(inventory_state) == 0) {
    return;
  }
  decoded = jvmud_deserialize_lpc_value(inventory_state);
  if (!jvmud_is_array(decoded)) {
    return;
  }
  states = decoded;
  index = 0;
  while (index < jvmud_size(states)) {
    state = states[index];
    if (jvmud_is_mapping(state)) {
      item = jvmud_invoke_lpc_object("system/items", "create", state["blueprint"]);
      if (item) {
        jvmud_invoke_lpc_object(item, "set_equipped", state["equipped"]);
        jvmud_move_entity(item, jvmud_current_lpc_object());
      }
    }
    index += 1;
  }
}

void clear_materialized_inventory() {
  object item;

  item = jvmud_first_entity_at(jvmud_current_lpc_object());
  while (item) {
    jvmud_destroy_lpc_object(item);
    item = jvmud_first_entity_at(jvmud_current_lpc_object());
  }
  inventory_materialized = 0;
}

void snapshot_quests() {
  mapping state;

  state = ([
    "stages": quest_stages,
    "counts": quest_counts,
    "flags": quest_flags
  ]);
  quest_state = jvmud_serialize_lpc_value(state);
}

void materialize_quests() {
  mixed decoded;
  mapping state;

  if (quests_materialized) {
    return;
  }
  quests_materialized = 1;
  quest_stages = ([ ]);
  quest_counts = ([ ]);
  quest_flags = ([ ]);
  if (!quest_state || jvmud_size(quest_state) == 0) {
    return;
  }
  decoded = jvmud_deserialize_lpc_value(quest_state);
  if (!jvmud_is_mapping(decoded)) {
    return;
  }
  state = decoded;
  if (jvmud_is_mapping(state["stages"])) {
    quest_stages = state["stages"];
  }
  if (jvmud_is_mapping(state["counts"])) {
    quest_counts = state["counts"];
  }
  if (jvmud_is_mapping(state["flags"])) {
    quest_flags = state["flags"];
  }
}

int quest_stage(string quest_id) {
  if (!jvmud_member(quest_stages, quest_id)) {
    return 0;
  }
  return quest_stages[quest_id];
}

int quest_count(string quest_id) {
  if (!jvmud_member(quest_counts, quest_id)) {
    return 0;
  }
  return quest_counts[quest_id];
}

string quest_title(string quest_id) {
  return jvmud_invoke_lpc_object("system/quests", "title", quest_id);
}

string quest_stage_text(int stage) {
  if (stage == 1) {
    return "active";
  }
  if (stage == 2) {
    return "ready to report";
  }
  if (stage == 3) {
    return "complete";
  }
  return "unknown";
}

int is_item(object candidate) {
  return candidate && jvmud_method_exists("query_blueprint", candidate);
}

int carried_weight() {
  object item;
  int total;

  item = jvmud_first_entity_at(jvmud_current_lpc_object());
  while (item) {
    if (is_item(item)) {
      total += jvmud_invoke_lpc_object(item, "query_weight");
    }
    item = jvmud_next_entity_at(item);
  }
  return total;
}

int attack_damage() {
  object item;
  int base;
  int penalty;
  int recommended;
  int minimum;
  string stat;

  if (character_class == "fighter") {
    base = strength;
  } else if (character_class == "ranger") {
    base = dexterity;
  } else if (character_class == "mage") {
    base = intelligence;
  } else {
    base = wisdom;
  }
  item = equipped_weapon();
  if (!item) {
    penalty = 3;
  } else {
    recommended = jvmud_invoke_lpc_object(item, "query_recommended_level");
    if (recommended > level) {
      penalty += recommended - level;
    }
    stat = jvmud_invoke_lpc_object(item, "query_governing_stat");
    minimum = jvmud_invoke_lpc_object(item, "query_minimum_stat");
    if (minimum > stat_value(stat)) {
      penalty += minimum - stat_value(stat);
    }
  }
  base = 3 + level + base / 3 + jvmud_random(4) - penalty;
  if (base < 1) {
    return 1;
  }
  return base;
}

object equipped_weapon() {
  object item;

  item = jvmud_first_entity_at(jvmud_current_lpc_object());
  while (item) {
    if (is_item(item)
        && jvmud_invoke_lpc_object(item, "query_equipped")
        && jvmud_invoke_lpc_object(item, "query_slot") == "weapon") {
      return item;
    }
    item = jvmud_next_entity_at(item);
  }
  return 0;
}

int carry_capacity() {
  return 20 + strength * 2;
}

string money_text() {
  return jvmud_invoke_lpc_object("system/economy", "format_money", copper);
}

void gain_experience(int amount) {
  if (amount <= 0 || level >= 10) {
    return;
  }
  experience += amount;
  avelorn_write_to(
      jvmud_current_lpc_object(),
      "You gain " + amount + " experience.\n");
  while (level < 10 && experience >= experience_for_next_level()) {
    experience -= experience_for_next_level();
    level += 1;
    if (level % 2 == 0) {
      attribute_points += 1;
    }
    recalculate_resources(1);
    avelorn_write_to(
        jvmud_current_lpc_object(),
        "You advance to level " + level + "!\n");
    if (level % 2 == 0) {
      avelorn_write_to(
          jvmud_current_lpc_object(),
          "You earn an attribute point. Use improve <attribute>.\n");
    }
  }
}

int experience_for_next_level() {
  return level * 100;
}

void recalculate_resources(int fill) {
  max_health = 30 + constitution * 4 + level * 6;
  if (character_class == "fighter") {
    max_resource = 20 + constitution * 2 + level * 3;
  } else if (character_class == "ranger") {
    max_resource = 25 + dexterity + constitution + level * 3;
  } else if (character_class == "mage") {
    max_resource = 20 + intelligence * 3 + level * 4;
  } else {
    max_resource = 20 + wisdom * 3 + level * 4;
  }
  if (fill || health <= 0) {
    health = max_health;
  } else if (health > max_health) {
    health = max_health;
  }
  if (fill || resource <= 0) {
    resource = max_resource;
  } else if (resource > max_resource) {
    resource = max_resource;
  }
}

string resource_name() {
  if (character_class == "mage") {
    return "Mana";
  }
  if (character_class == "cleric") {
    return "Faith";
  }
  return "Stamina";
}

int stat_value(string stat) {
  if (stat == "strength") {
    return strength;
  }
  if (stat == "dexterity") {
    return dexterity;
  }
  if (stat == "constitution") {
    return constitution;
  }
  if (stat == "intelligence") {
    return intelligence;
  }
  if (stat == "wisdom") {
    return wisdom;
  }
  if (stat == "charisma") {
    return charisma;
  }
  return 0;
}

void configure_starting_attributes() {
  strength = 10;
  dexterity = 10;
  constitution = 10;
  intelligence = 10;
  wisdom = 10;
  charisma = 10;

  if (character_class == "fighter") {
    strength = 14;
    constitution = 13;
    dexterity = 11;
  } else if (character_class == "ranger") {
    dexterity = 14;
    constitution = 12;
    wisdom = 12;
  } else if (character_class == "mage") {
    intelligence = 14;
    wisdom = 12;
    dexterity = 11;
  } else {
    wisdom = 14;
    constitution = 12;
    charisma = 12;
  }
}

string normalize_account_id(mixed value) {
  if (!value) {
    return "";
  }
  return jvmud_lowercase_text(value);
}

int valid_account_id(string value) {
  int index;
  int ch;

  if (jvmud_size(value) < 3 || jvmud_size(value) > 24) {
    return 0;
  }
  index = 0;
  while (index < jvmud_size(value)) {
    ch = value[index];
    if (!((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')
        || ch == '_' || ch == '-')) {
      return 0;
    }
    index = index + 1;
  }
  return 1;
}

int valid_character_name(mixed value) {
  int index;
  int ch;
  int saw_letter;

  if (!value || jvmud_size(value) < 2 || jvmud_size(value) > 24) {
    return 0;
  }
  index = 0;
  while (index < jvmud_size(value)) {
    ch = value[index];
    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
      saw_letter = 1;
    } else if (ch != ' ' && ch != '\'' && ch != '-') {
      return 0;
    }
    index = index + 1;
  }
  return saw_letter;
}

int valid_gender(string value) {
  return value == "male" || value == "female" || value == "non-binary";
}

int valid_class(string value) {
  return value == "fighter" || value == "ranger"
      || value == "mage" || value == "cleric";
}

mixed password_problem(mixed value) {
  int index;
  int ch;
  int upper;
  int lower;
  int number;
  int special;

  if (!value || jvmud_size(value) < 8) {
    return "Password must be at least 8 characters.";
  }
  if (jvmud_size(value) > 72) {
    return "Password must be 72 characters or fewer.";
  }

  index = 0;
  while (index < jvmud_size(value)) {
    ch = value[index];
    if (ch >= 'A' && ch <= 'Z') {
      upper = 1;
    } else if (ch >= 'a' && ch <= 'z') {
      lower = 1;
    } else if (ch >= '0' && ch <= '9') {
      number = 1;
    } else if (password_special(ch)) {
      special = 1;
    } else {
      return "Password contains an unsupported character.";
    }
    index = index + 1;
  }
  if (!upper || !lower || !number || !special) {
    return "Password needs uppercase, lowercase, number, and special characters.";
  }
  return 0;
}

int password_special(int ch) {
  return ch == '!' || ch == '@' || ch == '#' || ch == '$' || ch == '%'
      || ch == '^' || ch == '&' || ch == '*' || ch == '_' || ch == '.'
      || ch == '?' || ch == '+' || ch == '-';
}
