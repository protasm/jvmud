#define MAX_WEIGTH  6
int local_weight;

add_weight(w) {
  if (local_weight + w > MAX_WEIGTH)
    return 0;

  local_weight += w;
  return 1;
}
can_put_and_get() { return 1; }

get() {
  return 1;
}

id(str) {
  return str == "bag";
}

long() {
  write("A bag. ");

  if (first_inventory(this_object()))
    write("There is something in it.\n");

  else
    write("You can put things in it.\n");
}

prevent_insert() {
  if (local_weight > 0) {
    write("You can't when there are things in the bag.\n");

    return 1;
  }

  return 0;
}

query_value() {
  return 12;
}

query_weight() {
  return 1;
}

reset(arg) {
  if (arg)
    return;

  local_weight = 0;
}

short() {
  return "bag";
}
