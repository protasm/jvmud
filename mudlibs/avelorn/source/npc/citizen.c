string display_name;
string gender;
string office;
string duty;
string *identities;

void initialize(mixed first_load) {
  if (!display_name) {
    display_name = "Avelorn citizen";
  }
  if (!gender) {
    gender = "non-binary";
  }
  if (!office) {
    office = "citizen";
  }
  if (!duty) {
    duty = "serves the community";
  }
  if (!identities) {
    identities = ({ "citizen" });
  }
  jvmud_bind_entity_alias(jvmud_current_lpc_object(), "entity", "citizen");
}

void configure(string name, string selected_gender, string role, string responsibility) {
  display_name = name;
  gender = selected_gender;
  office = role;
  duty = responsibility;
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
    index = index + 1;
  }
  return 0;
}

string short() {
  return display_name;
}

string query_name() {
  return display_name;
}

string query_gender() {
  return gender;
}

void describe(object viewer) {
  string subject_word;
  string be_word;
  string have_word;

  subject_word = pronoun("subject");
  be_word = pronoun("be_present");
  have_word = pronoun("have_present");
  jvmud_write(display_name + " is the " + office + ". ");
  jvmud_write(jvmud_capitalize_text(subject_word) + " " + be_word + " plainly dressed, ");
  jvmud_write("and " + subject_word + " " + have_word + " " + duty + ".\n");
}

string pronoun(string form) {
  return jvmud_invoke_lpc_object("system/pronouns", form, gender);
}
