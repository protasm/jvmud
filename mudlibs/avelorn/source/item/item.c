string blueprint_id;
string display_name;
string keyword;
string details;
string equipment_slot;
string governing_stat;
int weight;
int copper_value;
int recommended_level;
int minimum_stat;
int restore_amount;
int equipped;

void initialize(mixed first_load) {
  if (!blueprint_id) {
    blueprint_id = "unknown-item";
  }
  if (!display_name) {
    display_name = "unremarkable item";
  }
  if (!keyword) {
    keyword = "item";
  }
  if (!details) {
    details = "It has not yet been described.";
  }
  if (!equipment_slot) {
    equipment_slot = "none";
  }
  if (!governing_stat) {
    governing_stat = "none";
  }
}

void configure(
    string id,
    string name,
    string identity,
    string description,
    string slot,
    int item_weight,
    int value,
    int suggested_level,
    string required_stat,
    int suggested_stat,
    int healing) {
  blueprint_id = id;
  display_name = name;
  keyword = identity;
  details = description;
  equipment_slot = slot;
  weight = item_weight;
  copper_value = value;
  recommended_level = suggested_level;
  governing_stat = required_stat;
  minimum_stat = suggested_stat;
  restore_amount = healing;
  jvmud_bind_entity_alias(jvmud_current_lpc_object(), "entity", keyword);
  jvmud_bind_entity_alias(
      jvmud_current_lpc_object(),
      "blueprint",
      blueprint_id);
}

int id(mixed value) {
  string normalized;

  if (!value) {
    return 0;
  }
  normalized = jvmud_lowercase_text(value);
  return normalized == keyword
      || normalized == jvmud_lowercase_text(display_name)
      || normalized == blueprint_id;
}

string short() {
  if (equipped) {
    return display_name + " (equipped)";
  }
  return display_name;
}

void describe(object viewer) {
  write(display_name + "\n");
  write(details + "\n");
  write("Weight: " + weight + ". Value: ");
  write(jvmud_invoke_lpc_object("system/economy", "format_money", copper_value) + ".\n");
  if (recommended_level > 1) {
    write("Recommended level: " + recommended_level + ".\n");
  }
  if (minimum_stat > 0) {
    write("Recommended " + governing_stat + ": " + minimum_stat + ".\n");
  }
}

int can_take(object actor) {
  return 1;
}

string query_blueprint() {
  return blueprint_id;
}

string query_slot() {
  return equipment_slot;
}

string query_governing_stat() {
  return governing_stat;
}

int query_weight() {
  return weight;
}

int query_value() {
  return copper_value;
}

int query_recommended_level() {
  return recommended_level;
}

int query_minimum_stat() {
  return minimum_stat;
}

int query_restore_amount() {
  return restore_amount;
}

int query_equipped() {
  return equipped;
}

void set_equipped(int value) {
  equipped = value ? 1 : 0;
}
