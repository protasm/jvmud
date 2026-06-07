string code;
string type;

status get() {
  return 1;
}

status id(string strang) {
  if ( ( strang == "key" )||( strang == type + " key")||( strang == "H_key") )
    return 1;

  return 0;
}
string query_type() { return type; }
string query_code() { return code; }

void set_type(string str) { type = str; }
void set_code(string str) { code = str; }

void init() {
}

void long() {
  write("\nThis a " + type + " key, wonder where it fits?\n");
}

int query_value() {
  return 10;
}

void reset(string arg) {
  if(arg)
    return;

  type = 0;
  code = 0;
}

status set_key_data(string str) {
  if ( sscanf(str, "%s %s", type, code) == 2)
    return 1;

  return 2;
}

string short() {
  return "A " + type + " key";
}
