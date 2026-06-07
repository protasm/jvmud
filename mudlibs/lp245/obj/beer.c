int full;

status drink(mixed str) {
  if (str && str != "beer" && str != "from bottle")
    return 0;

  if (!full)
    return 0;

  if (!call_other(this_player(), "drink_alcohol", 2))
    return 1;

  full = 0;

  write("It is really good beer!\n");

  say(call_other(this_player(), "query_name", 0) +
  " drinks a bottle of beer.\n");

  return 1;
}

status get() {
  return 1;
}

status id(mixed str) {
  if (str == "beer" && full)
    return 1;

  return str == "bottle";
}

void init() {
  add_action("drink", "drink");
}

void long() {
  write(short() + ".\n");
}

/* The shop only buys empty bottles ! */

int query_value() {
  if (!full) return 10;
  return 0;
}

int query_weight() {
  return 1;
}

void reset(mixed arg) {
  if (arg)
    return;

  full = 1;
}

string short() {
  if (full)
    return "bottle of beer";

  return "empty bottle";
}
