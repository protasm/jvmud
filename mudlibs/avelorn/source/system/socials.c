string *verbs;
string *first_person;
string *third_person;

void initialize(mixed first_load) {
  verbs = ({
    "acknowledge", "admire", "applaud", "beam", "beckon",
    "blink", "blush", "bow", "cheer", "chuckle",
    "clap", "comfort", "congratulate", "agree", "cough",
    "curtsy", "dance", "daydream", "frown", "gasp",
    "giggle", "glance", "grin", "groan", "highfive",
    "hum", "laugh", "listen", "nod", "ponder",
    "point", "pose", "raisebrow", "salute", "shiver",
    "shrug", "sigh", "smile", "smirk", "snap",
    "sneeze", "stare", "stretch", "study", "tap",
    "thank", "think", "tilt", "wave", "wink",
    "yawn", "cheerup", "compliment", "encourage", "greet",
    "muse", "observe", "pace", "peer", "reflect",
    "relax", "respect", "reverie", "roll", "scrutinize",
    "shout", "sing", "squint", "twirl", "whistle",
    "whisper", "wonder", "worry", "breathe", "celebrate",
    "compose", "focus", "gesture", "glow", "hop",
    "jot", "kneel", "lean", "march", "mutter",
    "offer", "pause", "present", "question", "radiate",
    "reassure", "remember", "search", "settle", "sketch",
    "sparkle", "toast", "trace", "welcome", "zoom"
  });

  first_person = ({
    "acknowledge the moment", "look on admiringly", "applaud", "beam", "beckon",
    "blink", "blush", "bow", "cheer", "chuckle",
    "clap", "offer comfort", "offer congratulations", "agree", "cough",
    "curtsy", "dance", "daydream", "frown", "gasp",
    "giggle", "glance aside", "grin", "groan", "offer a high five",
    "hum", "laugh", "listen closely", "nod", "ponder",
    "point", "strike a pose", "raise an eyebrow", "salute", "shiver",
    "shrug", "sigh", "smile", "smirk", "snap your fingers",
    "sneeze", "stare", "stretch", "study your surroundings", "tap your foot",
    "give thanks", "think", "tilt your head", "wave", "wink",
    "yawn", "try to cheer everyone up", "offer a compliment", "offer encouragement", "offer a greeting",
    "muse", "observe quietly", "pace", "peer into the distance", "reflect",
    "relax", "show respect", "fall into a reverie", "roll your shoulders", "scrutinize your surroundings",
    "shout", "sing", "squint", "twirl", "whistle",
    "whisper", "wonder", "worry", "breathe deeply", "celebrate",
    "compose yourself", "focus", "gesture", "glow with pride", "hop",
    "jot a note", "kneel", "lean thoughtfully", "march in place", "mutter",
    "make an offering gesture", "pause", "make a courtly presentation", "look questioning", "radiate confidence",
    "offer reassurance", "remember", "search the area", "settle yourself", "sketch in the air",
    "sparkle with delight", "raise a toast", "trace a line in the air", "offer a warm welcome", "dash about excitedly"
  });

  third_person = ({
    "acknowledges the moment", "looks on admiringly", "applauds", "beams", "beckons",
    "blinks", "blushes", "bows", "cheers", "chuckles",
    "claps", "offers comfort", "offers congratulations", "agrees", "coughs",
    "curtsies", "dances", "daydreams", "frowns", "gasps",
    "giggles", "glances aside", "grins", "groans", "offers a high five",
    "hums", "laughs", "listens closely", "nods", "ponders",
    "points", "strikes a pose", "raises an eyebrow", "salutes", "shivers",
    "shrugs", "sighs", "smiles", "smirks", "snaps sharply",
    "sneezes", "stares", "stretches", "studies the surroundings", "taps one foot",
    "gives thanks", "thinks", "tilts to one side", "waves", "winks",
    "yawns", "tries to cheer everyone up", "offers a compliment", "offers encouragement", "offers a greeting",
    "muses", "observes quietly", "paces", "peers into the distance", "reflects",
    "relaxes", "shows respect", "falls into a reverie", "rolls both shoulders", "scrutinizes the surroundings",
    "shouts", "sings", "squints", "twirls", "whistles",
    "whispers", "wonders", "worries", "breathes deeply", "celebrates",
    "regains composure", "focuses", "gestures", "glows with pride", "hops",
    "jots a note", "kneels", "leans thoughtfully", "marches in place", "mutters",
    "makes an offering gesture", "pauses", "makes a courtly presentation", "looks questioning", "radiates confidence",
    "offers reassurance", "remembers", "searches the area", "settles down", "sketches in the air",
    "sparkles with delight", "raises a toast", "traces a line in the air", "offers a warm welcome", "dashes about excitedly"
  });
}

void register(mixed ignored) {
  int index;

  index = 0;
  while (index < jvmud_size(verbs)) {
    jvmud_add_action("social", verbs[index]);
    index += 1;
  }
  jvmud_add_action("list_emotes", "emotes");
}

int social(mixed target_name) {
  object actor;
  object place;
  object target;
  string actor_name;
  string first_target_phrase;
  string third_target_phrase;
  string target_name_text;
  string verb;
  int index;

  verb = jvmud_current_verb();
  index = find_verb(verb);
  if (index < 0) {
    return 0;
  }
  actor = jvmud_current_actor();
  place = jvmud_entity_location(actor);
  actor_name = jvmud_invoke_lpc_object(actor, "query_name");

  if (!target_name) {
    write("You " + first_person[index] + ".\n");
    avelorn_emit_except(actor, actor_name + " " + third_person[index] + ".\n", actor);
    return 1;
  }

  target = jvmud_find_entity(target_name, place);
  if (!target) {
    target = jvmud_find_entity(target_name, actor);
  }
  if (!target) {
    write("You do not see " + target_name + " here.\n");
    return 1;
  }
  first_target_phrase = directed_first_phrase(verb, index);
  third_target_phrase = directed_third_phrase(verb, index);
  if (jvmud_size(first_target_phrase) == 0) {
    write("That emote is not directed at anyone. Try it without a target.\n");
    return 1;
  }
  if (jvmud_method_exists("query_name", target)) {
    target_name_text = jvmud_invoke_lpc_object(target, "query_name");
  } else {
    target_name_text = jvmud_invoke_lpc_object(target, "short");
  }
  write("You " + first_target_phrase + target_name_text + ".\n");
  avelorn_emit_except(
      actor,
      actor_name + " " + third_target_phrase + target_name_text + ".\n",
      actor);
  return 1;
}

string directed_first_phrase(string verb, int index) {
  if (verb == "admire") { return "admire "; }
  if (verb == "comfort") { return "comfort "; }
  if (verb == "congratulate") { return "congratulate "; }
  if (verb == "compliment") { return "compliment "; }
  if (verb == "encourage") { return "encourage "; }
  if (verb == "greet") { return "greet "; }
  if (verb == "respect") { return "show respect to "; }
  if (verb == "study") { return "study "; }
  if (verb == "thank") { return "thank "; }
  if (verb == "welcome") { return "welcome "; }
  if (verb == "reassure") { return "reassure "; }
  if (verb == "glance") { return "glance at "; }
  if (verb == "highfive") { return "exchange a high five with "; }
  if (verb == "acknowledge") { return "acknowledge "; }
  if (verb == "beckon" || verb == "bow" || verb == "curtsy"
      || verb == "listen" || verb == "salute" || verb == "wave") {
    return first_person[index] + " to ";
  }
  if (verb == "beam" || verb == "frown" || verb == "glance"
      || verb == "grin" || verb == "point" || verb == "smile"
      || verb == "smirk" || verb == "stare" || verb == "wink") {
    return first_person[index] + " at ";
  }
  if (verb == "applaud" || verb == "celebrate" || verb == "cheer"
      || verb == "chuckle" || verb == "dance" || verb == "giggle"
      || verb == "highfive" || verb == "laugh" || verb == "sing"
      || verb == "toast") {
    return first_person[index] + " with ";
  }
  if (verb == "acknowledge" || verb == "gesture" || verb == "nod"
      || verb == "offer" || verb == "present") {
    return first_person[index] + " toward ";
  }
  return "";
}

string directed_third_phrase(string verb, int index) {
  if (verb == "admire") { return "admires "; }
  if (verb == "comfort") { return "comforts "; }
  if (verb == "congratulate") { return "congratulates "; }
  if (verb == "compliment") { return "compliments "; }
  if (verb == "encourage") { return "encourages "; }
  if (verb == "greet") { return "greets "; }
  if (verb == "respect") { return "shows respect to "; }
  if (verb == "study") { return "studies "; }
  if (verb == "thank") { return "thanks "; }
  if (verb == "welcome") { return "welcomes "; }
  if (verb == "reassure") { return "reassures "; }
  if (verb == "glance") { return "glances at "; }
  if (verb == "highfive") { return "exchanges a high five with "; }
  if (verb == "acknowledge") { return "acknowledges "; }
  if (verb == "beckon" || verb == "bow" || verb == "curtsy"
      || verb == "listen" || verb == "salute" || verb == "wave") {
    return third_person[index] + " to ";
  }
  if (verb == "beam" || verb == "frown" || verb == "grin"
      || verb == "point" || verb == "smile" || verb == "smirk"
      || verb == "stare" || verb == "wink") {
    return third_person[index] + " at ";
  }
  if (verb == "applaud" || verb == "celebrate" || verb == "cheer"
      || verb == "chuckle" || verb == "dance" || verb == "giggle"
      || verb == "laugh" || verb == "sing" || verb == "toast") {
    return third_person[index] + " with ";
  }
  if (verb == "gesture" || verb == "nod" || verb == "offer"
      || verb == "present") {
    return third_person[index] + " toward ";
  }
  return "";
}

int list_emotes(mixed ignored) {
  int index;

  write("Avelorn emotes (100):\n");
  index = 0;
  while (index < jvmud_size(verbs)) {
    write("  " + verbs[index]);
    if ((index + 1) % 5 == 0 || index == jvmud_size(verbs) - 1) {
      write("\n");
    }
    index += 1;
  }
  write("Many social emotes may also name someone present.\n");
  return 1;
}

int find_verb(string verb) {
  int index;

  index = 0;
  while (index < jvmud_size(verbs)) {
    if (verbs[index] == verb) {
      return index;
    }
    index += 1;
  }
  return -1;
}

int count() {
  return jvmud_size(verbs);
}
