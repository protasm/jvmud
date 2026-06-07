string hint_string, name;
/*
* This is a standard quest object.
* Configure it to make it look the way you want.
*/
void long() {
  write("This is the quest '" + name + "':\n");
  write(hint_string);
}

void set_hint(mixed h) {
  hint_string = h;
}

void set_name(mixed n) {
  name = n;
}
status id(mixed str) { return str == name || str == "quest"; }

string short() {
  return name;
}
string hint() { return hint_string; }
