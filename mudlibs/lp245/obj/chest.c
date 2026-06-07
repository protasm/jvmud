int chest_is_open;
int local_weight;

int query_value() { return 200; }

int query_weight() { return 8; }

status get() { return 1; }

status can_put_and_get() { return chest_is_open; }
status add_weight(int w) {
  if (w + local_weight > 8)
    return 0;

  local_weight += w;
}

status close(string str) {
  if (!id(str))
    return 0;

  chest_is_open = 0;

  write("Ok.\n");

  return 1;
}

void init() {
  add_action("open", "open");
  add_action("close", "close");
}

void long() {
  write("A chest that seems to be of a high value.\n");

  if (chest_is_open)
    write("It is open.\n");

  else
    write("It is closed.\n");
}

status open(string str) {
  if (!id(str))
    return 0;

  chest_is_open = 1;

  write("Ok.\n");

  return 1;
}

void reset(string arg) {
  if (arg)
    return;

  chest_is_open = 0;
}
status id(string str) { return str == "chest"; }

string short() {
  return "chest";
}
