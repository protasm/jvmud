string info;
string name, alias_name;
string read_msg;
string short_desc, long_desc;
int value, local_weight;
/*
* This is a generic valuable object. Clone a copy, and
* setup local values.
*/

/*
* If you are going to copy this file, in the purpose of changing
* it a little to your own need, beware:
*
* First try one of the following:
*
* 1. Do clone_object(), and then configur it. This object is specially
*    prepared for configuration.
*
* 2. If you still is not pleased with that, create a new empty
*    object, and make an inheritance of this objet on the first line.
*    This will automatically copy all variables and functions from the
*    original object. Then, add the functions you want to change. The
*    original function can still be accessed with '::' prepended on the name.
*
* The maintainer of this LPmud might become sad with you if you fail
* to do any of the above. Ask other wizards if you are doubtful.
*
* The reason of this, is that the above saves a lot of memory.
*/
status get() {
  return 1;
}

status id(mixed str) {
  return str == name || str == alias_name;
}

void init() {
  if (!read_msg)
    return;

  add_action("read", "read");
}

void long() {
  write(long_desc);
}

string query_info() {
  return info;
}

int query_weight() {
  return local_weight;
}

status read(mixed str) {
  if (str != name &&  str != alias_name)
    return 0;

  write(read_msg);

  return 1;
}

void set_alias(mixed str) {
  alias_name = str;
}
int query_value() { return value; }

void set_id(mixed str) {
  local_weight = 1;
  name = str;
}

void set_info(mixed i) {
  info = i;
}

void set_long(mixed str) {
  long_desc = str;
}

void set_read(mixed str) {
  read_msg = str;
}

void set_short(mixed str) {
  short_desc = str;
  long_desc = "You see nothing special.\n";
}

void set_value(int v) {
  value = v;
}

void set_weight(int w) {
  local_weight = w;
}

string short() {
  return short_desc;
}
