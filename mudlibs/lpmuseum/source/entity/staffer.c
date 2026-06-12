int step;
int emote_delay;

void reset(mixed first_load) {
  step = 0;
  emote_delay = 60 + random(60);
  bind_alias(this_object(), "entity", "staffer");
  bind_alias(this_object(), "entity", "museum staffer");
  if (object_name(this_object()) == "entity/staffer") {
    return;
  }
  set_heart_beat(1);
}

string short() {
  return "kind museum security staffer";
}

int id(mixed value) {
  return value == "staffer" || value == "security" || value == "guard"
      || value == "museum staffer" || value == "security staffer";
}

void describe(object viewer) {
  write("The museum security staffer wears a soft blue jacket and a patient expression.\n");
  write("He watches the galleries carefully, but his work is mostly directions, lost badges, and reassurance.\n");
  write("His gentle patrol is driven by LPMuseum's timed heartbeat, not by player input.\n");
}

void heart_beat() {
  step = step + 1;
  emote_delay = emote_delay - 1;

  if (emote_delay <= 0) {
    staff_emote();
    emote_delay = 60 + random(60);
  }

  if (step >= 30) {
    step = 0;
    wander();
  }
}

void wander() {
  object place;
  string place_id;
  string destination;
  string direction;
  int choice;

  place = environment(this_object());
  if (!place) {
    return;
  }

  place_id = object_name(place);
  destination = "place/concourse";
  direction = "toward the concourse";

  if (place_id == "place/concourse") {
    choice = random(3);
    if (choice == 0) {
      destination = "place/origins";
      direction = "north toward the Origins Gallery";
    } else if (choice == 1) {
      destination = "place/workshop";
      direction = "east toward the Creator Workshop";
    } else {
      destination = "place/archive";
      direction = "west toward the Archive";
    }
  } else if (place_id == "place/origins") {
    choice = random(2);
    if (choice == 0) {
      destination = "place/concourse";
      direction = "south toward the concourse";
    } else {
      destination = "place/portal_hall";
      direction = "east toward the Portal Hall";
    }
  } else if (place_id == "place/workshop") {
    destination = "place/concourse";
    direction = "west toward the concourse";
  } else if (place_id == "place/archive") {
    destination = "place/concourse";
    direction = "east toward the concourse";
  } else if (place_id == "place/portal_hall") {
    destination = "place/origins";
    direction = "west toward the Origins Gallery";
  }

  tell_place(place, "The kind museum security staffer heads " + direction + ".\n");
  move_object(this_object(), destination);
  tell_place(environment(this_object()), "The kind museum security staffer arrives, offering a quiet nod.\n");
}

void staff_emote() {
  object place;
  string place_id;
  int choice;

  place = environment(this_object());
  if (!place) {
    return;
  }

  place_id = object_name(place);
  choice = random(5);

  if (place_id == "place/concourse") {
    if (choice < 2) {
      tell_place(place, "The kind museum security staffer updates the directory with careful little taps.\n");
    } else if (choice < 4) {
      tell_place(place, "The kind museum security staffer offers directions before anyone has to ask twice.\n");
    } else {
      tell_place(place, "The kind museum security staffer watches the concourse doors with relaxed attention.\n");
    }
    return;
  }

  if (place_id == "place/origins") {
    if (choice < 2) {
      tell_place(place, "The kind museum security staffer studies the engine model with quiet pride.\n");
    } else if (choice < 4) {
      tell_place(place, "The kind museum security staffer brushes dust from the Player -> Session -> Persona plaque.\n");
    } else {
      tell_place(place, "The kind museum security staffer smiles at the clean JVMud vocabulary on the wall.\n");
    }
    return;
  }

  if (place_id == "place/workshop") {
    if (choice < 2) {
      tell_place(place, "The kind museum security staffer checks that the demo bench has enough room around it.\n");
    } else if (choice < 4) {
      tell_place(place, "The kind museum security staffer reads a builder note and nods approvingly.\n");
    } else {
      tell_place(place, "The kind museum security staffer gently returns a misplaced tool to its outline.\n");
    }
    return;
  }

  if (place_id == "place/archive") {
    if (choice < 2) {
      tell_place(place, "The kind museum security staffer checks the archive case latch without making a sound.\n");
    } else if (choice < 4) {
      tell_place(place, "The kind museum security staffer lowers his voice even further among the records.\n");
    } else {
      tell_place(place, "The kind museum security staffer pauses beside the archive case, fond but careful.\n");
    }
    return;
  }

  tell_place(place, "The kind museum security staffer pauses, listening for anyone who needs help.\n");
}
