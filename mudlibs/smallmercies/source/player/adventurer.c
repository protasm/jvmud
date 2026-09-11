// Guest characters last for one connection. Only this object schedules combat.
string name;
string gender;
string profession;
int strength;
int intelligence;
int dexterity;
int health;
int maximum;
int victories;
int ready;
object opponent;

void create() {
  name = "an undecided adventurer";
}

string query_name() {
  return name;
}

string short() {
  return name;
}

int id(string value) {
  return jvmud_lowercase_text(name) == jvmud_lowercase_text(value);
}

int receives_player_bound_messages() {
  return ready;
}

void describe(object viewer) {
  jvmud_write_to_lpc_object(viewer, name + ", a " + gender + " " + profession + ", looks cautiously heroic.\n");
}

void connect() {
  jvmud_write("Welcome to Small Mercies! Characters last for this visit.\nName (2-16 letters): ");

  jvmud_capture_session_input("choose_name", 0);
}

void choose_name(string value) {
  int i;
  object *people;

  value = jvmud_lowercase_text(value);

  if (jvmud_size(value) < 2 || jvmud_size(value) > 16) {
    jvmud_write("Please use 2-16 letters. Name: ");

    jvmud_capture_session_input("choose_name", 0);

    return;
  }

  for (i = 0; i < jvmud_size(value); i = i + 1) {
    if (value[i] < 'a' || value[i] > 'z') {
      jvmud_write("Letters only, please. Name: ");

      jvmud_capture_session_input("choose_name", 0);

      return;
    }
  }

  people = jvmud_users();

  for (i = 0; i < jvmud_size(people); i = i + 1) {
    if (people[i] != jvmud_current_lpc_object() && jvmud_lowercase_text(jvmud_invoke_lpc_object(people[i], "query_name")) == value) {
      jvmud_write("That name is visiting already. Name: ");

      jvmud_capture_session_input("choose_name", 0);

      return;
    }
  }

  name = jvmud_capitalize_text(value);

  jvmud_write("Gender (male/female): ");

  jvmud_capture_session_input("choose_gender", 0);
}

void choose_gender(string value) {
  value = jvmud_lowercase_text(value);

  if (value != "male" && value != "female") {
    jvmud_write("Choose male or female: ");

    jvmud_capture_session_input("choose_gender", 0);

    return;
  }

  gender = value;

  jvmud_write("Class (warrior/mage): ");

  jvmud_capture_session_input("choose_class", 0);
}

void choose_class(string value) {
  value = jvmud_lowercase_text(value);

  if (value != "warrior" && value != "mage") {
    jvmud_write("Choose warrior or mage: ");

    jvmud_capture_session_input("choose_class", 0);

    return;
  }

  profession = value;

  strength = 12;

  intelligence = 6;

  dexterity = 9;

  if (value == "mage") {
    strength = 6;

    intelligence = 12;
  }

  maximum = 20 + strength;

  health = maximum;

  ready = 1;

  jvmud_enable_commands();

  jvmud_write("Welcome, " + name + "! Type help. Your legend can wait until after tea.\n");

  jvmud_emit_perceivable_except(jvmud_current_lpc_object(), name + " arrives, reasonably prepared.\n", jvmud_current_lpc_object());

  look(0);
}

void init() {
  jvmud_add_action("look", "look");
  jvmud_add_action("look", "l");
  jvmud_add_action("score", "score");
  jvmud_add_action("help", "help");
  jvmud_add_action("go", "go");
  jvmud_add_action("direction", "north");
  jvmud_add_action("direction", "n");
  jvmud_add_action("direction", "south");
  jvmud_add_action("direction", "s");
  jvmud_add_action("direction", "east");
  jvmud_add_action("direction", "e");
  jvmud_add_action("direction", "west");
  jvmud_add_action("direction", "w");
  jvmud_add_action("direction", "up");
  jvmud_add_action("direction", "u");
  jvmud_add_action("direction", "down");
  jvmud_add_action("direction", "d");
  jvmud_add_action("say", "say");
  jvmud_add_action("who", "who");
  jvmud_add_action("social", "smile");
  jvmud_add_action("social", "wave");
  jvmud_add_action("social", "bow");
  jvmud_add_action("social", "laugh");
  jvmud_add_action("talk", "talk");
  jvmud_add_action("attack", "attack");
  jvmud_add_action("attack", "kill");
  jvmud_add_action("stop", "stop");
  jvmud_add_action("rest", "rest");
  jvmud_add_action("quit", "quit");
}

int look(mixed target) {
  object item;

  if (!target) {
    jvmud_invoke_lpc_object(jvmud_entity_location(jvmud_current_lpc_object()), "describe", jvmud_current_lpc_object());

    return 1;
  }

  item = jvmud_find_entity(jvmud_lowercase_text(target), jvmud_entity_location(jvmud_current_lpc_object()));

  if (!item) jvmud_write("You do not see that here.\n");

  else jvmud_invoke_lpc_object(item, "describe", jvmud_current_lpc_object());

  return 1;
}

int score(mixed ignored) {
  jvmud_write(name + " - " + gender + " " + profession + "\n");

  jvmud_write("STR " + strength + "  INT " + intelligence + "  DEX " + dexterity + "\n");

  jvmud_write("HP " + health + "/" + maximum + "  Victories " + victories + "\n");

  if (opponent) jvmud_write("Fighting: " + jvmud_invoke_lpc_object(opponent, "short") + "\n");

  return 1;
}

int help(mixed ignored) {
  jvmud_write("look [name], score, who, say <message>, talk <npc>\n");

  jvmud_write("north/south/east/west/up/down (n/s/e/w/u/d), go <direction>\n");

  jvmud_write("smile, wave, bow, laugh, attack <npc> (or kill), stop, rest, quit\n");

  jvmud_write("Warriors swing using STR; mages cast sparks using INT. DEX helps dodge.\n");

  jvmud_write("Combat repeats every two seconds. Walk away or stop to end it.\n");

  jvmud_write("Defeat sends you safely to the inn. Rest there for full HP. No PvP.\n");

  return 1;
}

int direction(mixed ignored) {
  return go(jvmud_current_verb());
}

int go(mixed way) {
  string destination;

  if (!way) {
    jvmud_write("Go where?\n");

    return 1;
  }

  if (way == "n") way = "north";
  if (way == "s") way = "south";
  if (way == "e") way = "east";
  if (way == "w") way = "west";
  if (way == "u") way = "up";
  if (way == "d") way = "down";

  destination = jvmud_invoke_lpc_object(jvmud_entity_location(jvmud_current_lpc_object()), "destination", way);

  if (!destination) {
    jvmud_write("You cannot go that way. The map is on a strict budget.\n");

    return 1;
  }

  end_combat();

  jvmud_emit_perceivable_except(jvmud_current_lpc_object(), name + " leaves " + way + ".\n", jvmud_current_lpc_object());

  jvmud_move_entity(jvmud_current_lpc_object(), destination);

  jvmud_emit_perceivable_except(jvmud_current_lpc_object(), name + " arrives.\n", jvmud_current_lpc_object());

  return look(0);
}

int say(mixed text) {
  if (!text) jvmud_write("Say what?\n");

  else jvmud_emit_perceivable_at(jvmud_entity_location(jvmud_current_lpc_object()), name + " says: " + text + "\n");

  return 1;
}

int social(mixed ignored) {
  string action;

  action = jvmud_current_verb();

  if (action == "smile") action = "smiles optimistically";

  if (action == "wave") action = "waves with unnecessary ceremony";

  if (action == "bow") action = "bows, checking for dropped coins";

  if (action == "laugh") action = "laughs in the face of mild inconvenience";

  jvmud_emit_perceivable_at(jvmud_entity_location(jvmud_current_lpc_object()), name + " " + action + ".\n");

  return 1;
}

int who(mixed ignored) {
  object *people;
  int i;

  people = jvmud_users();

  jvmud_write("Visiting adventurers:\n");

  for (i = 0; i < jvmud_size(people); i = i + 1)
  if (jvmud_invoke_lpc_object(people[i], "receives_player_bound_messages")) jvmud_write("  " + jvmud_invoke_lpc_object(people[i], "query_name") + "\n");

  return 1;
}

int talk(mixed target) {
  object npc;

  if (target) npc = jvmud_find_entity(jvmud_lowercase_text(target), jvmud_entity_location(jvmud_current_lpc_object()));

  if (!npc || !jvmud_method_exists("talk", npc)) jvmud_write("Talk to which NPC here?\n");

  else jvmud_write(jvmud_invoke_lpc_object(npc, "talk"));

  return 1;
}

int attack(mixed target) {
  object npc;

  if (opponent) {
    jvmud_write("One bout at a time. Use stop first.\n");

    return 1;
  }

  if (target) npc = jvmud_find_entity(jvmud_lowercase_text(target), jvmud_entity_location(jvmud_current_lpc_object()));

  if (!npc || !jvmud_method_exists("engage", npc)) {
    jvmud_write("Choose a sparring NPC here: goose or rat.\n");

    return 1;
  }

  if (!jvmud_invoke_lpc_object(npc, "engage", jvmud_current_lpc_object())) {
    jvmud_write("They are friendly, resting, or already sparring.\n");

    return 1;
  }

  opponent = npc;

  jvmud_write("You begin sparring with " + jvmud_invoke_lpc_object(npc, "short") + ".\n");

  jvmud_schedule_recurring_tick(1, 2);

  return 1;
}

void end_combat() {
  if (opponent) jvmud_invoke_lpc_object(opponent, "release", jvmud_current_lpc_object());

  opponent = 0;

  jvmud_schedule_recurring_tick(0, 2);
}

int stop(mixed ignored) {
  end_combat();

  jvmud_write("You lower your guard. Hostilities adjourned.\n");

  return 1;
}

void tick() {
  int amount;
  string attack_text;

  if (!opponent || jvmud_entity_location(opponent) != jvmud_entity_location(jvmud_current_lpc_object())) {
    end_combat();

    return;
  }

  amount = 2 + strength / 3;

  attack_text = "You swing";

  if (profession == "mage") {
    amount = 2 + intelligence / 3;

    attack_text = "You cast a spark";
  }

  jvmud_write_to_lpc_object(jvmud_current_lpc_object(), attack_text + " for " + amount + " damage.\n");

  if (jvmud_invoke_lpc_object(opponent, "hit", jvmud_current_lpc_object(), amount)) {
    victories = victories + 1;

    jvmud_write_to_lpc_object(
      jvmud_current_lpc_object(), "Victory! Your opponent takes a dignified breather.\n"
    );

    end_combat();

    return;
  }

  if (jvmud_random(30) < dexterity) {
    jvmud_write_to_lpc_object(
      jvmud_current_lpc_object(), "You dodge with surprising dignity.\n"
    );

    return;
  }

  amount = jvmud_invoke_lpc_object(opponent, "attack_power");

  health = health - amount;

  jvmud_write_to_lpc_object(
    jvmud_current_lpc_object(), "Your opponent hits for " + amount + " damage.\n"
  );

  if (health <= 0) {
    end_combat();

    health = 1;

    jvmud_write_to_lpc_object(
      jvmud_current_lpc_object(), "Defeated! The innkeeper collects you with a wheelbarrow.\n"
    );

    jvmud_move_entity(jvmud_current_lpc_object(), "room/inn");

    jvmud_invoke_lpc_object(
      jvmud_entity_location(jvmud_current_lpc_object()), "describe", jvmud_current_lpc_object()
    );
  }
}

int rest(mixed ignored) {
  if (jvmud_entity_location(jvmud_current_lpc_object()) != jvmud_load_lpc_object("room/inn"))
    jvmud_write("Rest at the inn, west of the square.\n");
  else {
    end_combat();

    health = maximum;

    jvmud_write("Tea, toast, full health. The heroic essentials.\n");
  }

  return 1;
}

void disconnect() {
  end_combat();

  ready = 0;
}

int quit(mixed ignored) {
  disconnect();

  jvmud_write("Goodbye! Your legend will fit in the guest book.\n");

  jvmud_destroy_lpc_object(jvmud_current_lpc_object());

  return 1;
}
