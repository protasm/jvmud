string display_name;
string gender;
string office;
string quest_id;
string *identities;

void initialize(mixed first_load) {
  if (!display_name) {
    display_name = "Company petitioner";
  }
  if (!gender) {
    gender = "non-binary";
  }
  if (!office) {
    office = "petitioner";
  }
  if (!quest_id) {
    quest_id = "";
  }
  if (!identities) {
    identities = ({ "petitioner" });
  }
  jvmud_bind_entity_alias(jvmud_current_lpc_object(), "entity", "petitioner");
}

void configure(string name, string selected_gender, string role, string assignment) {
  display_name = name;
  gender = selected_gender;
  office = role;
  quest_id = assignment;
  jvmud_bind_entity_alias(
      jvmud_current_lpc_object(),
      "entity",
      jvmud_lowercase_text(display_name));
}

void add_identity(string identity) {
  identities += ({ identity });
  jvmud_bind_entity_alias(jvmud_current_lpc_object(), "entity", identity);
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

void offer_interactions() {
  jvmud_add_action("request_work", "work");
  jvmud_add_action("report_work", "report");
}

int request_work(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "offer_quest", quest_id);
}

int report_work(mixed ignored) {
  return jvmud_invoke_lpc_object(jvmud_current_actor(), "turn_in_quest", quest_id);
}

string query_name() {
  return display_name;
}

string query_gender() {
  return gender;
}

string short() {
  return display_name;
}

void describe(object viewer) {
  string subject_word;
  string have_word;

  subject_word = jvmud_invoke_lpc_object("system/pronouns", "subject", gender);
  have_word = jvmud_invoke_lpc_object("system/pronouns", "have_present", gender);
  jvmud_write(display_name + " is the " + office + ". ");
  jvmud_write(jvmud_capitalize_text(subject_word) + " " + have_word);
  jvmud_write(" posted a measured request for Company assistance.\n");
  jvmud_write("Type work to accept the assignment or report when it is complete.\n");
}
