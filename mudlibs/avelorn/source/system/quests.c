void initialize(mixed first_load) {
}

string title(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return "Miller's Unwelcome Guests";
  }
  return "Unknown assignment";
}

string description(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return "Miller Enid asks you to clear three granary rats from the damaged cellar stores.";
  }
  return "No description is available.";
}

int recommended_level(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return 1;
  }
  return 1;
}

int required_count(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return 3;
  }
  return 1;
}

int experience_reward(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return 75;
  }
  return 0;
}

int copper_reward(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return 40;
  }
  return 0;
}

string quest_for_defeat_tag(string tag) {
  if (tag == "granary-rat") {
    return "millers-unwelcome-guests";
  }
  return "";
}
