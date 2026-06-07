string code;
string type;

get() {
  return 1;
}

id( strang) {
  if ( ( strang == "key" )||( strang == type + " key")||( strang == "H_key") )
    return 1;

  return 0;
}
query_type() { return type; }
query_code() { return code; }

set_type( str) { type = str; }
set_code( str) { code = str; }

init() {
}

long() {
  write("\nThis a " + type + " key, wonder where it fits?\n");
}

query_value() {
  return 10;
}

reset( arg) {
  if(arg)
    return;

  type = 0;
  code = 0;
}

set_key_data( str) {
  if ( sscanf(str, "%s %s", type, code) == 2)
    return 1;

  return 2;
}

short() {
  return "A " + type + " key";
}
