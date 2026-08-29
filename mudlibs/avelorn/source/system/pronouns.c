void initialize(mixed first_load) {
}

string subject(string gender) {
  if (gender == "male") {
    return "he";
  }
  if (gender == "female") {
    return "she";
  }
  return "they";
}

string object_form(string gender) {
  if (gender == "male") {
    return "him";
  }
  if (gender == "female") {
    return "her";
  }
  return "them";
}

string possessive_adjective(string gender) {
  if (gender == "male") {
    return "his";
  }
  if (gender == "female") {
    return "her";
  }
  return "their";
}

string possessive_pronoun(string gender) {
  if (gender == "male") {
    return "his";
  }
  if (gender == "female") {
    return "hers";
  }
  return "theirs";
}

string reflexive(string gender) {
  if (gender == "male") {
    return "himself";
  }
  if (gender == "female") {
    return "herself";
  }
  return "themself";
}

string be_present(string gender) {
  if (gender == "non-binary") {
    return "are";
  }
  return "is";
}

string have_present(string gender) {
  if (gender == "non-binary") {
    return "have";
  }
  return "has";
}
