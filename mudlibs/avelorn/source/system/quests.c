void initialize(mixed first_load) {
}

string title(string quest_id) {
  if (quest_id == "millers-unwelcome-guests") {
    return "Miller's Unwelcome Guests";
  }
  if (quest_id == "light-for-the-road") {
    return "Light for the Road";
  }
  if (quest_id == "silent-patrol-bell") {
    return "The Silent Patrol Bell";
  }
  if (quest_id == "beneath-blackstone") {
    return "Beneath Blackstone";
  }
  if (quest_id == "rekindle-western-lantern") {
    return "Rekindle the Western Lantern";
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
  if (quest_id == "silent-patrol-bell") {
    return "Watch-Captain Ilyra asks you to restore the abandoned north-road patrol post by defeating the bell wraith silencing its alarm.";
  }
  if (quest_id == "beneath-blackstone") {
    return "Royal Surveyor Maelin asks you to defeat the flooded and ashbound guardians beneath Blackstone, then renew the central wardstone.";
  }
  if (quest_id == "rekindle-western-lantern") {
    return "Marshal Serin asks you to clear Ashenwatch's ash hound, ember knight, and Crown warden; align the eastern mirror; and rekindle the western Crown Lantern.";
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
  if (quest_id == "silent-patrol-bell") {
    return 4;
  }
  if (quest_id == "beneath-blackstone") {
    return 5;
  }
  if (quest_id == "rekindle-western-lantern") {
    return 7;
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
  if (quest_id == "silent-patrol-bell") {
    return 1;
  }
  if (quest_id == "beneath-blackstone") {
    return 3;
  }
  if (quest_id == "rekindle-western-lantern") {
    return 5;
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
  if (quest_id == "silent-patrol-bell") {
    return 300;
  }
  if (quest_id == "beneath-blackstone") {
    return 600;
  }
  if (quest_id == "rekindle-western-lantern") {
    return 1200;
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
  if (quest_id == "silent-patrol-bell") {
    return 150;
  }
  if (quest_id == "beneath-blackstone") {
    return 300;
  }
  if (quest_id == "rekindle-western-lantern") {
    return 600;
  }
  return 0;
}

string quest_for_defeat_tag(string tag) {
  if (tag == "granary-rat") {
    return "millers-unwelcome-guests";
  }
  if (tag == "bell-wraith") {
    return "silent-patrol-bell";
  }
  if (tag == "blackstone-water-guardian" || tag == "blackstone-ash-guardian") {
    return "beneath-blackstone";
  }
  if (tag == "ashenwatch-hound" || tag == "ashenwatch-knight"
      || tag == "ashenwatch-warden") {
    return "rekindle-western-lantern";
  }
  return "";
}

string quest_for_action_tag(string tag) {
  if (tag == "bridge-lantern" || tag == "shelter-lantern"
      || tag == "greyhaven-west-lantern") {
    return "light-for-the-road";
  }
  if (tag == "blackstone-wardstone") {
    return "beneath-blackstone";
  }
  if (tag == "ashenwatch-mirror" || tag == "ashenwatch-crown-lantern") {
    return "rekindle-western-lantern";
  }
  return "";
}

int repeatable_defeat_tag(string tag) {
  return tag == "granary-rat";
}

string item_reward(string quest_id) {
  if (quest_id == "rekindle-western-lantern") {
    return "trinket/lantern-crown-medal";
  }
  return "";
}
