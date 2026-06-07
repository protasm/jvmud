id(str) {
  return str == "newspaper" || str == "paper" || str == "news";
}

init() {
  add_action("read", "read");
}

long() {
  cat("/NEWSPAPER");
}

read(str) {
  if (!id(str))
    return 0;

  say(call_other(this_player(), "query_name") + " reads the newspaper.\n");
  long();

  return 1;
}

short() {
  return "A newspaper" ;
}
query_weight() { return 1; }

get() { return 1; }

query_value() { return 5; }
