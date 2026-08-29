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
  if (!account_created && !copper) {
    copper = 120;
  }
}

void begin_session() {
  jvmud_write("\nThe western chapter of the Company of the Lantern welcomes you.\n");
  jvmud_write("Account ID: ");
  jvmud_capture_session_input("receive_account_id", 0);
}

void receive_account_id(mixed value) {
  string requested;

  requested = normalize_account_id(value);
  if (!valid_account_id(requested)) {
    jvmud_write("Use 3-24 letters, numbers, underscores, or dashes.\n");
    jvmud_write("Account ID: ");
    jvmud_capture_session_input("receive_account_id", 0);
    return;
  }

  account_id = requested;
  if (jvmud_restore_lpc_object_state("accounts/" + requested)) {
    account_id = requested;
    pending_password = "";
    inventory_materialized = 0;
    jvmud_write("Password: ");
    jvmud_capture_session_input("receive_login_password", 1);
    return;
  }

  jvmud_write("No account named " + requested + " exists. Create it? (yes/no) ");
  jvmud_capture_session_input("confirm_account_creation", 0);
}

void confirm_account_creation(mixed value) {
  string answer;

  answer = jvmud_lowercase_text(value);
  if (answer == "yes" || answer == "y") {
    jvmud_write("Choose a password: ");
    jvmud_capture_session_input("receive_new_password", 1);
    return;
  }
  if (answer == "no" || answer == "n") {
    account_id = "";
    jvmud_write("No account was created.\nAccount ID: ");
    jvmud_capture_session_input("receive_account_id", 0);
    return;
  }
  jvmud_write("Please answer yes or no: ");
  jvmud_capture_session_input("confirm_account_creation", 0);
}

void receive_new_password(mixed value) {
  mixed problem;

  problem = password_problem(value);
  if (problem) {
    jvmud_write(problem + "\nChoose a password: ");
    jvmud_capture_session_input("receive_new_password", 1);
    return;
  }

  pending_password = value;
  jvmud_write("Password again: ");
  jvmud_capture_session_input("confirm_new_password", 1);
}

void confirm_new_password(mixed value) {
  if (value != pending_password) {
    pending_password = "";
    jvmud_write("Those passwords did not match.\nChoose a password: ");
    jvmud_capture_session_input("receive_new_password", 1);
    return;
  }

  password_hash = jvmud_hash_password(value);
  pending_password = "";
  jvmud_write("Character name: ");
  jvmud_capture_session_input("receive_character_name", 0);
}

void receive_login_password(mixed value) {
  if (!jvmud_verify_password(value, password_hash)) {
    jvmud_write("That password did not match.\nPassword: ");
    jvmud_capture_session_input("receive_login_password", 1);
    return;
  }

  pending_password = "";
  enter_avelorn(1);
}

void receive_character_name(mixed value) {
  if (!valid_character_name(value)) {
    jvmud_write("Use 2-24 letters, spaces, apostrophes, or dashes.\n");
    jvmud_write("Character name: ");
    jvmud_capture_session_input("receive_character_name", 0);
    return;
  }

  character_name = jvmud_capitalize_text(jvmud_lowercase_text(value));
  jvmud_write("Gender (male/female/non-binary): ");
  jvmud_capture_session_input("receive_gender", 0);
}

void receive_gender(mixed value) {
  string selected;

  selected = jvmud_lowercase_text(value);
  if (!valid_gender(selected)) {
    jvmud_write("Please choose male, female, or non-binary: ");
    jvmud_capture_session_input("receive_gender", 0);
    return;
  }

  gender = selected;
  jvmud_write("Class (fighter/ranger/mage/cleric): ");
  jvmud_capture_session_input("receive_class", 0);
}

void receive_class(mixed value) {
  string selected;

  selected = jvmud_lowercase_text(value);
  if (!valid_class(selected)) {
    jvmud_write("Please choose fighter, ranger, mage, or cleric: ");
    jvmud_capture_session_input("receive_class", 0);
    return;
  }

  character_class = selected;
  configure_starting_attributes();
  account_created = 1;
  inventory_materialized = 1;
  grant_starter_kit();
  recalculate_resources(1);
  save_character();
  enter_avelorn(0);
}

void enter_avelorn(int returning) {
  jvmud_enable_commands();
  materialize_inventory();
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
    jvmud_write("\nWelcome back, " + character_name + ".\n\n");
  } else {
    jvmud_write("\nYour name is entered upon the roll of the Company of the Lantern.\n");
    jvmud_write("Welcome to Avelorn, " + character_name + ".\n\n");
  }
  look(0);
}

void end_session() {
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
    saved = jvmud_save_lpc_object_state("accounts/" + account_id);
    if (!saved) {
      jvmud_write("Warning: Avelorn could not write your character snapshot to the host filesystem.\n");
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
  jvmud_add_action("money", "money");
  jvmud_add_action("improve", "improve");
  jvmud_add_action("pronouns_command", "pronouns");
  jvmud_add_action("help", "help");
  jvmud_add_action("save_command", "save");
}

int look(mixed target) {
  object place;
  object entity;

  place = jvmud_entity_location(jvmud_current_lpc_object());
  if (!place) {
    jvmud_write("You are nowhere in Avelorn.\n");
    return 1;
  }
  if (target) {
    entity = jvmud_find_entity(target, place);
    if (!entity) {
      entity = jvmud_find_entity(target, jvmud_current_lpc_object());
    }
    if (!entity) {
      jvmud_write("You do not see that here.\n");
      return 1;
    }
    jvmud_invoke_lpc_object(entity, "describe", jvmud_current_lpc_object());
    return 1;
  }
  jvmud_invoke_lpc_object(place, "describe", jvmud_current_lpc_object());
  return 1;
}

int travel_to(string direction, string destination) {
  object old_place;
  object new_place;

  old_place = jvmud_entity_location(jvmud_current_lpc_object());
  jvmud_emit_perceivable_except(
      old_place,
      character_name + " leaves " + direction + ".\n",
      jvmud_current_lpc_object());
  jvmud_move_entity(jvmud_current_lpc_object(), destination);
  new_place = jvmud_entity_location(jvmud_current_lpc_object());
  jvmud_emit_perceivable_except(
      new_place,
      character_name + " arrives.\n",
      jvmud_current_lpc_object());
  jvmud_invoke_lpc_object(new_place, "describe", jvmud_current_lpc_object());
  return 1;
}

int score(mixed ignored) {
  jvmud_write(character_name + ", level " + level + " " + character_class + "\n");
  jvmud_write("Health " + health + "/" + max_health + "  ");
  jvmud_write(resource_name() + " " + resource + "/" + max_resource + "\n");
  if (level < 10) {
    jvmud_write("Experience " + experience + "/" + experience_for_next_level() + "\n");
  } else {
    jvmud_write("Experience: level cap reached\n");
  }
  jvmud_write("Strength " + strength + "  Dexterity " + dexterity);
  jvmud_write("  Constitution " + constitution + "\n");
  jvmud_write("Intelligence " + intelligence + "  Wisdom " + wisdom);
  jvmud_write("  Charisma " + charisma + "\n");
  jvmud_write("Unspent attribute points: " + attribute_points + "\n");
  jvmud_write("Coin: " + money_text() + "\n");
  return 1;
}

int inventory(mixed ignored) {
  object item;
  int total_weight;

  item = jvmud_first_entity_at(jvmud_current_lpc_object());
  if (!item) {
    jvmud_write("You are carrying nothing.\n");
    return 1;
  }
  jvmud_write("You are carrying:\n");
  while (item) {
    if (jvmud_method_exists("query_blueprint", item)) {
      jvmud_write("  " + jvmud_invoke_lpc_object(item, "short") + "\n");
      total_weight += jvmud_invoke_lpc_object(item, "query_weight");
    }
    item = jvmud_next_entity_at(item);
  }
  jvmud_write("Carried weight: " + total_weight + "/" + carry_capacity() + ".\n");
  return 1;
}

int equipment(mixed ignored) {
  object item;
  int found;

  jvmud_write("Equipped:\n");
  item = jvmud_first_entity_at(jvmud_current_lpc_object());
  while (item) {
    if (is_item(item) && jvmud_invoke_lpc_object(item, "query_equipped")) {
      jvmud_write("  " + jvmud_invoke_lpc_object(item, "query_slot") + ": ");
      jvmud_write(jvmud_invoke_lpc_object(item, "short") + "\n");
      found = 1;
    }
    item = jvmud_next_entity_at(item);
  }
  if (!found) {
    jvmud_write("  Nothing.\n");
  }
  return 1;
}

int get_item(mixed target) {
  object place;
  object item;
  int item_weight;

  if (!target) {
    jvmud_write("Get what?\n");
    return 1;
  }
  place = jvmud_entity_location(jvmud_current_lpc_object());
  item = jvmud_find_entity(target, place);
  if (!item || !is_item(item)) {
    jvmud_write("You do not see a portable item by that name.\n");
    return 1;
  }
  item_weight = jvmud_invoke_lpc_object(item, "query_weight");
  if (carried_weight() + item_weight > carry_capacity()) {
    jvmud_write("That would exceed your carrying capacity.\n");
    return 1;
  }
  jvmud_move_entity(item, jvmud_current_lpc_object());
  jvmud_write("You take " + jvmud_invoke_lpc_object(item, "short") + ".\n");
  save_character();
  return 1;
}

int drop_item(mixed target) {
  object item;
  object place;

  if (!target) {
    jvmud_write("Drop what?\n");
    return 1;
  }
  item = jvmud_find_entity(target, jvmud_current_lpc_object());
  if (!item || !is_item(item)) {
    jvmud_write("You are not carrying that.\n");
    return 1;
  }
  if (jvmud_invoke_lpc_object(item, "query_equipped")) {
    jvmud_write("Unequip it before dropping it.\n");
    return 1;
  }
  place = jvmud_entity_location(jvmud_current_lpc_object());
  jvmud_move_entity(item, place);
  jvmud_write("You drop " + jvmud_invoke_lpc_object(item, "short") + ".\n");
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
    jvmud_write("Equip what?\n");
    return 1;
  }
  item = jvmud_find_entity(target, jvmud_current_lpc_object());
  if (!item || !is_item(item)) {
    jvmud_write("You are not carrying that item.\n");
    return 1;
  }
  slot = jvmud_invoke_lpc_object(item, "query_slot");
  if (slot == "none") {
    jvmud_write("That item cannot be equipped.\n");
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
  jvmud_write("You equip " + jvmud_invoke_lpc_object(item, "short") + ".\n");
  stat = jvmud_invoke_lpc_object(item, "query_governing_stat");
  minimum = jvmud_invoke_lpc_object(item, "query_minimum_stat");
  if (minimum > 0 && stat_value(stat) < minimum) {
    jvmud_write("You can use it, but your " + stat + " is below the recommended " + minimum);
    jvmud_write(", so it will be less effective.\n");
  }
  save_character();
  return 1;
}

int unequip_item(mixed target) {
  object item;

  if (!target) {
    jvmud_write("Unequip what?\n");
    return 1;
  }
  item = jvmud_find_entity(target, jvmud_current_lpc_object());
  if (!item || !is_item(item) || !jvmud_invoke_lpc_object(item, "query_equipped")) {
    jvmud_write("You do not have that equipped.\n");
    return 1;
  }
  jvmud_invoke_lpc_object(item, "set_equipped", 0);
  jvmud_write("You unequip " + jvmud_invoke_lpc_object(item, "short") + ".\n");
  save_character();
  return 1;
}

int use_item(mixed target) {
  object item;
  int amount;

  if (!target) {
    jvmud_write("Use what?\n");
    return 1;
  }
  item = jvmud_find_entity(target, jvmud_current_lpc_object());
  if (!item || !is_item(item)) {
    jvmud_write("You are not carrying that item.\n");
    return 1;
  }
  amount = jvmud_invoke_lpc_object(item, "query_restore_amount");
  if (amount <= 0) {
    jvmud_write("That item is not consumable.\n");
    return 1;
  }
  health += amount;
  if (health > max_health) {
    health = max_health;
  }
  jvmud_write("You use " + jvmud_invoke_lpc_object(item, "short"));
  jvmud_write(" and recover " + amount + " health.\n");
  jvmud_destroy_lpc_object(item);
  save_character();
  return 1;
}

int money(mixed ignored) {
  jvmud_write("You carry " + money_text() + ".\n");
  return 1;
}

int purchase_blueprint(string blueprint) {
  object item;
  int price;
  int item_weight;

  item = jvmud_invoke_lpc_object("system/items", "create", blueprint);
  if (!item) {
    jvmud_write("That stock entry is unavailable.\n");
    return 1;
  }
  price = jvmud_invoke_lpc_object(item, "query_value");
  item_weight = jvmud_invoke_lpc_object(item, "query_weight");
  if (copper < price) {
    jvmud_write("You do not have enough Crown coin.\n");
    jvmud_destroy_lpc_object(item);
    return 1;
  }
  if (carried_weight() + item_weight > carry_capacity()) {
    jvmud_write("You cannot carry that much weight.\n");
    jvmud_destroy_lpc_object(item);
    return 1;
  }
  copper -= price;
  jvmud_move_entity(item, jvmud_current_lpc_object());
  jvmud_write("You buy " + jvmud_invoke_lpc_object(item, "short") + " for ");
  jvmud_write(jvmud_invoke_lpc_object("system/economy", "format_money", price) + ".\n");
  save_character();
  return 1;
}

int sell_item(mixed target) {
  object item;
  int sale_price;
  string item_name;

  item = jvmud_find_entity(target, jvmud_current_lpc_object());
  if (!item || !is_item(item)) {
    jvmud_write("You are not carrying that item.\n");
    return 1;
  }
  if (jvmud_invoke_lpc_object(item, "query_equipped")) {
    jvmud_write("Unequip it before selling it.\n");
    return 1;
  }
  sale_price = jvmud_invoke_lpc_object(item, "query_value") / 2;
  item_name = jvmud_invoke_lpc_object(item, "short");
  jvmud_destroy_lpc_object(item);
  copper += sale_price;
  jvmud_write("You sell " + item_name + " for ");
  jvmud_write(jvmud_invoke_lpc_object("system/economy", "format_money", sale_price) + ".\n");
  save_character();
  return 1;
}

int improve(mixed target) {
  string stat;

  if (!target) {
    jvmud_write("Improve which attribute?\n");
    return 1;
  }
  if (attribute_points <= 0) {
    jvmud_write("You have no unspent attribute points.\n");
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
    jvmud_write("Choose strength, dexterity, constitution, intelligence, wisdom, or charisma.\n");
    return 1;
  }
  attribute_points -= 1;
  recalculate_resources(0);
  jvmud_write("Your " + stat + " improves to " + stat_value(stat) + ".\n");
  save_character();
  return 1;
}

int complete_introductory_drill() {
  if (introductory_drill_completed) {
    jvmud_write("You have already completed the introductory Company drill.\n");
    return 1;
  }
  introductory_drill_completed = 1;
  jvmud_write("You complete the Company's measured course of footwork, signals, and field discipline.\n");
  gain_experience(100);
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
  jvmud_write(character_name + " uses " + subject_word + "/" + pronoun("object_form") + " pronouns.\n");
  jvmud_write(jvmud_capitalize_text(subject_word) + " " + be_word);
  jvmud_write(" a sworn novice, and " + subject_word + " " + have_word);
  jvmud_write(" begun " + pronoun("possessive_adjective") + " service to the Crown.\n");
  return 1;
}

int help(mixed ignored) {
  jvmud_write("Avelorn commands:\n");
  jvmud_write("  look, north/east/south/west, score, pronouns\n");
  jvmud_write("  inventory, equipment, get, drop, equip, unequip, use\n");
  jvmud_write("  money, list, buy, sell, train, improve, save, help\n");
  return 1;
}

int save_command(mixed ignored) {
  save_character();
  jvmud_write("Your character has been saved.\n");
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
  jvmud_write("You gain " + amount + " experience.\n");
  while (level < 10 && experience >= experience_for_next_level()) {
    experience -= experience_for_next_level();
    level += 1;
    if (level % 2 == 0) {
      attribute_points += 1;
    }
    recalculate_resources(1);
    jvmud_write("You advance to level " + level + "!\n");
    if (level % 2 == 0) {
      jvmud_write("You earn an attribute point. Use improve <attribute>.\n");
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
