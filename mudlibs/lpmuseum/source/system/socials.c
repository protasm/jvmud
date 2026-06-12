string *verbs;
string *third_person;

void reset(mixed first_load) {
  verbs = ({
    "acknowledge", "admire", "applaud", "beam", "beckon",
    "blink", "blush", "bow", "cheer", "chuckle",
    "clap", "comfort", "congratulate", "consider", "cough",
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

  third_person = ({
    "acknowledges", "admires", "applauds", "beams", "beckons",
    "blinks", "blushes", "bows", "cheers", "chuckles",
    "claps", "comforts", "congratulates", "considers", "coughs",
    "curtsies", "dances", "daydreams", "frowns", "gasps",
    "giggles", "glances", "grins", "groans", "high-fives",
    "hums", "laughs", "listens", "nods", "ponders",
    "points", "poses", "raises an eyebrow", "salutes", "shivers",
    "shrugs", "sighs", "smiles", "smirks", "snaps",
    "sneezes", "stares", "stretches", "studies", "taps",
    "thanks", "thinks", "tilts", "waves", "winks",
    "yawns", "cheers up", "compliments", "encourages", "greets",
    "muses", "observes", "paces", "peers", "reflects",
    "relaxes", "respects", "falls into a reverie", "rolls their shoulders", "scrutinizes",
    "shouts", "sings", "squints", "twirls", "whistles",
    "whispers", "wonders", "worries", "breathes", "celebrates",
    "composes themself", "focuses", "gestures", "glows", "hops",
    "jots a note", "kneels", "leans", "marches", "mutters",
    "offers", "pauses", "presents", "questions", "radiates",
    "reassures", "remembers", "searches", "settles", "sketches",
    "sparkles", "toasts", "traces a line", "welcomes", "zooms"
  });
}

void register(mixed ignored) {
  int index;

  index = 0;
  while (index < sizeof(verbs)) {
    add_action("social", verbs[index]);
    index = index + 1;
  }
}

int social(mixed target_name) {
  int index;
  string verb;
  string action;
  object actor;
  object place;
  object target;

  verb = query_verb();
  index = find_verb(verb);
  if (index < 0) {
    return 0;
  }

  actor = this_player();
  place = environment(actor);
  action = third_person[index];

  if (target_name) {
    target = present(target_name, place);
    if (!target) {
      target = present(target_name, actor);
    }
    if (!target) {
      write("You do not see " + target_name + " here.\n");
      return 1;
    }

    tell_place(place, call_other(actor, "query_name") + " " + action + " " + call_other(target, "short") + ".\n");
    return 1;
  }

  tell_place(place, call_other(actor, "query_name") + " " + action + ".\n");
  return 1;
}

int find_verb(string verb) {
  int index;

  index = 0;
  while (index < sizeof(verbs)) {
    if (verbs[index] == verb) {
      return index;
    }
    index = index + 1;
  }

  return -1;
}

int count() {
  return sizeof(verbs);
}
