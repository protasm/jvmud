void initialize(mixed first_load) {
}

string title(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return "Miller's Unwelcome Guests";
  }
  if (quest_id == "light-for-the-road") {
    return "Light for the Road";
  }
  return "Unknown assignment";
}

string description(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return "Miller Enid asks you to clear three granary rats from the damaged cellar stores.";
  }
  if (quest_id == "light-for-the-road") {
    return "Sister Elara asks you to tend the ward lanterns at Old Brindle Bridge, the Crown shelter, and Greyhaven's western approach.";
  }
  return "No description is available.";
}

int recommended_level(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return 1;
  }
  if (quest_id == "light-for-the-road") {
    return 2;
  }
  return 1;
}

int required_count(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return 3;
  }
  if (quest_id == "light-for-the-road") {
    return 3;
  }
  return 1;
}

int experience_reward(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return 75;
  }
  if (quest_id == "light-for-the-road") {
    return 150;
  }
  return 0;
}

int copper_reward(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return 40;
  }
  if (quest_id == "light-for-the-road") {
    return 75;
  }
  return 0;
}

string quest_for_defeat_tag(string tag) {
  if (tag == "granary-rat") {
    return "millers-unwelcome-guests";
  }
  return "";
}

string quest_for_action_tag(string tag) {
  if (tag == "bridge-lantern" || tag == "shelter-lantern"
      || tag == "greyhaven-west-lantern") {
    return "light-for-the-road";
  }
  return "";
}
