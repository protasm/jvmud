status id(string str) {
  return str == "list" || str == "top" || str == "top players" ||

  str == "list of top players" || str == "top list";
}

void init() {
  add_action("read", "read");
}

void long() {
  cat("/SORT_LEVEL");
}

status read(string str) {
  if (!id(str))
    return 0;

  say(call_other(this_player(), "query_name") + " reads the top list.\n");
  long();

  return 1;
}

string short() {
  return "A list of the top players" ;
}
int query_weight() { return 1; }

status get() { return 1; }

int query_value() { return 5; }
