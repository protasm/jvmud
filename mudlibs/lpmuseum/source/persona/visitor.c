string display_name;
string persona_name;
string account_id;
string password_hash;
string email;
string gender;
string pending_password;
int account_created;
int password_attempts;
int player_bound_messages_enabled;

void reset(mixed first_load) {
  if (!display_name) {
    display_name = "visitor";
  }
  if (!persona_name) {
    persona_name = "visitor";
  }
  if (!gender) {
    gender = "none";
  }
  if (!account_id) {
    account_id = "";
  }
  if (!password_hash) {
    password_hash = "";
  }
  if (!email) {
    email = "";
  }
  if (!pending_password) {
    pending_password = "";
  }
}

void connect() {
  player_bound_messages_enabled = 1;
}

void configure_account(mixed user_id, mixed persona, mixed account_gender, mixed account_email, mixed account_password_hash) {
  account_id = user_id;
  persona_name = lower_case(persona);
  display_name = capitalize(persona_name);
  gender = account_gender;
  email = account_email;
  password_hash = account_password_hash;
  account_created = 1;
  pending_password = "";
  password_attempts = 0;
}

void start_password_change(mixed value) {
  write("Current password: ");
  input_to("verify_current_password_for_change", 1);
}

void verify_current_password_for_change(mixed value) {
  if (!verify_password(value, password_hash)) {
    write("That password did not match. Password unchanged.\n");
    return;
  }

  write("New password: ");
  input_to("choose_replacement_password", 1);
}

void choose_replacement_password(mixed value) {
  mixed problem;

  problem = password_problem(value);
  if (problem) {
    write(problem + "\n");
    write("New password: ");
    input_to("choose_replacement_password", 1);
    return;
  }

  pending_password = value;
  write("New password again: ");
  input_to("confirm_replacement_password", 1);
}

void confirm_replacement_password(mixed value) {
  if (value != pending_password) {
    pending_password = "";
    write("Those passwords did not match.\n");
    write("New password: ");
    input_to("choose_replacement_password", 1);
    return;
  }

  password_hash = hash_password(value);
  pending_password = "";
  save_account();
  write("Password changed.\n");
}

void enter_museum() {
  player_bound_messages_enabled = 1;
  enable_commands();
  write("Hi, " + display_name + "! Welcome to LPMuseum.\n");
  tell_place(environment(this_object()), display_name + " enters LPMuseum through the museum doors.\n");
  write("This is a native JVMud mudlib. Type help for museum commands.\n\n");
}

void return_from_exhibit() {
  object place;

  player_bound_messages_enabled = 1;
  enable_commands();
  write("You step back into LPMuseum as " + display_name + ".\n\n");
  place = environment(this_object());
  if (place) {
    call_other(place, "describe", this_object());
  }
}

void disconnect() {
  save_account();
}

void save_account() {
  if (account_created && strlen(account_id) > 0 && strlen(password_hash) > 0) {
    pending_password = "";
    save_object("accounts/" + account_id);
  }
}

int receives_player_bound_messages() {
  return player_bound_messages_enabled;
}

string normalize_account_id(mixed value) {
  if (!value) {
    return "";
  }
  return lower_case(value);
}

int valid_account_id(mixed value) {
  int index;
  int ch;

  if (!value || strlen(value) < 3 || strlen(value) > 24) {
    return 0;
  }

  index = 0;
  while (index < strlen(value)) {
    ch = value[index];
    if (!((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')
        || ch == '_' || ch == '-')) {
      return 0;
    }
    index = index + 1;
  }
  return 1;
}

mixed password_problem(mixed value) {
  int index;
  int ch;
  int upper;
  int lower;
  int number;
  int special;

  if (!value || strlen(value) < 6) {
    return "Password must be at least 6 characters.";
  }
  if (strlen(value) > 72) {
    return "Password must be 72 characters or fewer.";
  }

  index = 0;
  while (index < strlen(value)) {
    ch = value[index];
    if (ch >= 'A' && ch <= 'Z') {
      upper = 1;
    } else if (ch >= 'a' && ch <= 'z') {
      lower = 1;
    } else if (ch >= '0' && ch <= '9') {
      number = 1;
    } else if (is_password_special(ch)) {
      special = 1;
    } else {
      return "Password may use letters, numbers, and ! @ # $ % ^ & * _ . ? + - only.";
    }
    index = index + 1;
  }

  if (!upper) {
    return "Password must include an uppercase letter.";
  }
  if (!lower) {
    return "Password must include a lowercase letter.";
  }
  if (!number) {
    return "Password must include a number.";
  }
  if (!special) {
    return "Password must include a special character.";
  }
  return 0;
}

int is_password_special(int ch) {
  return ch == '!' || ch == '@' || ch == '#' || ch == '$' || ch == '%'
      || ch == '^' || ch == '&' || ch == '*' || ch == '_' || ch == '.'
      || ch == '?' || ch == '+' || ch == '-';
}

int valid_email(mixed value) {
  int index;
  int at;
  int dot_after_at;
  int ch;

  at = -1;
  dot_after_at = 0;
  index = 0;
  while (index < strlen(value)) {
    ch = value[index];
    if (ch == '@') {
      if (at != -1 || index == 0 || index == strlen(value) - 1) {
        return 0;
      }
      at = index;
    } else if (ch == '.' && at != -1 && index > at + 1 && index < strlen(value) - 1) {
      dot_after_at = 1;
    } else if (!valid_email_char(ch)) {
      return 0;
    }
    index = index + 1;
  }
  return at > 0 && dot_after_at;
}

int valid_email_char(int ch) {
  return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')
      || (ch >= '0' && ch <= '9') || ch == '@' || ch == '.'
      || ch == '_' || ch == '%' || ch == '+' || ch == '-';
}

int valid_persona_name(mixed value) {
  int index;
  int ch;
  int saw_letter_or_number;

  if (!value || strlen(value) < 2 || strlen(value) > 24) {
    return 0;
  }

  index = 0;
  while (index < strlen(value)) {
    ch = value[index];
    if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
      saw_letter_or_number = 1;
    } else if (ch != ' ' && ch != '\'' && ch != '-') {
      return 0;
    }
    index = index + 1;
  }
  return saw_letter_or_number;
}

void init() {
  add_action("look", "look");
  add_action("look", "l");
  add_action("go", "go");
  add_action("direction", "north");
  add_action("direction", "n");
  add_action("direction", "east");
  add_action("direction", "e");
  add_action("direction", "south");
  add_action("direction", "s");
  add_action("direction", "west");
  add_action("direction", "w");
  add_action("direction", "up");
  add_action("direction", "u");
  add_action("direction", "down");
  add_action("direction", "d");
  add_action("examine", "examine");
  add_action("examine", "exa");
  add_action("take", "take");
  add_action("take", "get");
  add_action("drop", "drop");
  add_action("inventory", "inventory");
  add_action("inventory", "i");
  add_action("help", "help");
  add_action("who", "who");
  add_action("whoami", "whoami");
  add_action("password_command", "password");
  add_action("email_command", "email");
  add_action("quit", "quit");
  add_action("quit", "exit");
  add_action("say_command", "say");
  call_other("system/socials", "register", 0);
}

string query_name() {
  return display_name;
}

string query_real_name() {
  return display_name;
}

string short() {
  return display_name;
}

void describe(object viewer) {
  if (viewer == this_object()) {
    write("You are " + display_name + ", a visiting Persona exploring LPMuseum.\n");
  } else {
    write(display_name + " is a visiting Persona exploring LPMuseum.\n");
  }
}

int id(mixed value) {
  if (!value) {
    return 0;
  }
  value = lower_case(value);
  return value == persona_name || value == lower_case(display_name)
      || value == "visitor" || value == "persona" || value == "me";
}

int look(mixed target) {
  object place;
  object item;

  place = environment(this_object());
  if (!target || target == "around") {
    call_other(place, "describe", this_object());
    return 1;
  }

  item = present(target, place);
  if (!item) {
    item = present(target, this_object());
  }
  if (!item) {
    write("You do not see that here.\n");
    return 1;
  }

  call_other(item, "describe", this_object());
  return 1;
}

int examine(mixed target) {
  if (!target) {
    write("Examine what?\n");
    return 1;
  }

  return look(target);
}

int take(mixed target) {
  object place;
  object item;

  if (!target) {
    write("Take what?\n");
    return 1;
  }

  place = environment(this_object());
  item = present(target, place);
  if (!item) {
    write("You do not see that here.\n");
    return 1;
  }
  if (!call_other(item, "can_take", this_object())) {
    write("That is part of the museum display.\n");
    return 1;
  }

  move_object(item, this_object());
  write("You take " + call_other(item, "short") + ".\n");
  tell_place_except(place, display_name + " takes " + call_other(item, "short") + ".\n", this_object());
  return 1;
}

int drop(mixed target) {
  object place;
  object item;

  if (!target) {
    write("Drop what?\n");
    return 1;
  }

  item = present(target, this_object());
  if (!item) {
    write("You are not carrying that.\n");
    return 1;
  }

  place = environment(this_object());
  move_object(item, place);
  write("You drop " + call_other(item, "short") + ".\n");
  tell_place_except(place, display_name + " drops " + call_other(item, "short") + ".\n", this_object());
  return 1;
}

int go(mixed destination) {
  object place;

  if (!destination) {
    write("Go where?\n");
    return 1;
  }

  place = environment(this_object());
  return call_other(place, "go", destination);
}

int direction(mixed ignored) {
  return go(query_verb());
}

int move_player(mixed movement) {
  string destination;
  string direction;
  int separator;
  object old_place;
  object new_place;

  if (!movement) {
    return 0;
  }

  destination = movement;
  direction = movement;
  separator = 0;
  while (separator < strlen(destination) && destination[separator] != '#') {
    separator = separator + 1;
  }
  if (separator < strlen(destination)) {
    direction = destination[0..separator - 1];
    destination = destination[separator + 1..];
  }

  old_place = environment(this_object());
  tell_place_except(old_place, display_name + " leaves " + direction + ".\n", this_object());
  move_object(this_object(), destination);
  new_place = environment(this_object());
  tell_place_except(new_place, display_name + " arrives.\n", this_object());
  call_other(new_place, "describe", this_object());
  return 1;
}

int quit(mixed ignored) {
  save_account();
  write("You step away from LPMuseum.\n");
  destruct(this_object());
  return 1;
}

int say_command(mixed text) {
  string target_name;
  string message;
  int separator;
  object place;
  object target;

  if (!text) {
    write("Say what?\n");
    return 1;
  }

  place = environment(this_object());
  message = text;
  target = 0;

  if (strlen(text) > 3 && text[0..2] == "to ") {
    separator = 3;
    while (separator < strlen(text) && text[separator] != ' ') {
      separator = separator + 1;
    }
    if (separator >= strlen(text) - 1) {
      write("Say what to whom?\n");
      return 1;
    }

    target_name = text[3..separator - 1];
    message = text[separator + 1..];
    target = present(target_name, place);
    if (!target) {
      write("You do not see " + target_name + " here.\n");
      return 1;
    }
  }

  if (target) {
    tell_place(place, query_name() + " says to " + call_other(target, "short") + ": " + message + "\n");
  } else {
    tell_place(place, query_name() + " says: " + message + "\n");
  }
  return 1;
}

int inventory(mixed ignored) {
  object item;

  item = first_inventory(this_object());
  if (!item) {
    write("You are carrying nothing.\n");
    return 1;
  }

  write("You are carrying:\n");
  while (item) {
    write("  " + call_other(item, "short") + "\n");
    item = next_inventory(item);
  }
  return 1;
}

int help(mixed topic) {
  call_other("system/help", "show", topic);
  return 1;
}

int who(mixed ignored) {
  object *connected;
  object persona;
  int index;
  int count;

  connected = users();
  count = sizeof(connected);
  write("Connected Personas in LPMuseum: " + count + "\n");
  index = 0;
  while (index < count) {
    persona = connected[index];
    write("  " + call_other(persona, "query_name") + "  " + object_name(persona));
    write("  from " + query_ip_number(persona));
    write("  idle " + query_idle(persona) + "s\n");
    index = index + 1;
  }
  return 1;
}

int whoami(mixed ignored) {
  write("You are " + display_name + ", a JVMud Player with this Telnet Session bound to a museum Persona.\n");
  return 1;
}

int password_command(mixed arg) {
  if (arg == "change") {
    start_password_change(arg);
    return 1;
  }

  write("Your password is set. Use 'password change' to change it.\n");
  return 1;
}

int email_command(mixed arg) {
  if (!arg || strlen(arg) == 0) {
    if (strlen(email) == 0) {
      write("No email address is set. Use 'email name@example.com' to add one.\n");
    } else {
      write("Email address: " + email + "\n");
    }
    return 1;
  }

  if (arg == "clear") {
    email = "";
    save_account();
    write("Email address cleared.\n");
    return 1;
  }

  if (!valid_email(arg)) {
    write("That email address does not look valid.\n");
    return 1;
  }

  email = arg;
  save_account();
  write("Email address updated.\n");
  return 1;
}

int list_present_personas(object viewer) {
  object *connected;
  object persona;
  int index;
  int count;

  connected = users();
  index = 0;
  count = 0;
  while (index < sizeof(connected)) {
    persona = connected[index];
    if (persona != viewer && environment(persona) == environment(viewer)) {
      write(call_other(persona, "query_name") + " is here.\n");
      count = count + 1;
    }
    index = index + 1;
  }
  return count;
}

int list_vended_entities(object viewer) {
  object place;
  object item;
  int count;

  place = environment(viewer);
  item = first_inventory(place);
  count = 0;
  while (item) {
    if (item != viewer && call_other(item, "is_vended_entity")) {
      write(call_other(item, "short") + "\n");
      count = count + 1;
    }
    item = next_inventory(item);
  }
  return count;
}
