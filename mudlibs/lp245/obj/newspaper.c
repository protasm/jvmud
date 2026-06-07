status id(string str) {
  return str == "newspaper" || str == "paper" || str == "news";
}

void init() {
  add_action("read", "read");
}

void long() {
  cat("/NEWSPAPER");
}

status read(string str) {
  if (!id(str))
    return 0;

  say(call_other(this_player(), "query_name") + " reads the newspaper.\n");
  long();

  return 1;
}

string short() {
  return "A newspaper" ;
}
int query_weight() { return 1; }

status get() { return 1; }

int query_value() { return 5; }
