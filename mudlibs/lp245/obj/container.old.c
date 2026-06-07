string name_of_container ,cap_name ,alt_name ,alias_name;
string short_desc, long_desc;
int value, max_weight, local_weight;

int query_weight() { return local_weight; }

int query_max_weight() { return max_weight; }
status add_weight(int w) {
  if (local_weight + w > max_weight)
    return 0;

  local_weight += w;
  return 1;
}
string short() { return short_desc; }

status id(string str) {
  return str == name_of_container || str == alt_name || str == alias_name;
}

void long() {
  write(long_desc);

  if (first_inventory(this_object()))
    write("There is something in it.\n");

  else
    write("You can put things in it.\n");
}
int query_value() { return value; }

status can_put_and_get() { return 1; }

status get() { return 1; }

status prevent_insert() {
  if (local_weight > 0) {
    write("You can't when there are things in the " + name_of_container + ".\n");

    return 1;
  }

  return 0;
}

void reset(string arg) {
  if (arg)
    return;

  local_weight = 0;
}
void set_weight(int w) { local_weight = w; }

void set_max_weight(int w) { max_weight = w; }

void set_value(int v) { value = v; }

void set_name(string n) {
  name_of_container = n;
  cap_name = capitalize(n);
  short_desc = cap_name;
  long_desc = cap_name;
}
void set_alt_name(string n) { alt_name = n; }

void set_alias(string n) { alias_name = n; }

void set_short(string sh) { short_desc = sh; long_desc = short_desc + "\n"; }

void set_long(string lo) { long_desc = lo; }
