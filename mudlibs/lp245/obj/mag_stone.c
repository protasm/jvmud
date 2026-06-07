int gived;

status cat_file(string path) {
  if (!path)
    return 0;

  cat(path);

  return 1;
}
int query_weight() { return 1; }

/* Prevent giving away this object */
status drop() {
  gived += 1;

  if (gived == 2)
    return 1;

  else
    return 0;
}

status drop_object(string str) {
  if (str == "all") {
    drop_object("black stone");

    return 0;
  }

  if (!str || !id(str))
    return 0;

  write("The stone dissapears.\n");
  say(call_other(this_player(), "query_name") + " drops a black stone. It dissapears.\n");
  call_other(this_player(), "add_weight", -1);
  destruct(this_object());

  return 1;
}

status id(string str) {
  return str == "stone" || str == "black stone";
}
status get() { return 1; }

void init() {
  add_action("list_peoples", "people");
  add_action("list_files", "ls");
  add_action("cat_file", "cat");
  add_action("drop_object", "drop");
}

status list_files(string path) {
  ls(path);

  return 1;
}

status list_peoples() {
  people();

  return 1;
}

void long() {
  write("The stone is completely black, and feels warm to the touch.\n");
  write("There seems to be somthing magic with it.\n");
}

string short() {
  return "A black stone";
}
