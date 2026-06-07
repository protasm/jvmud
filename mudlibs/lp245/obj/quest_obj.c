string hint_string, name;
/*
* This is a standard quest object.
* Configure it to make it look the way you want.
*/
long() {
  write("This is the quest '" + name + "':\n");
  write(hint_string);
}

set_hint(h) {
  hint_string = h;
}

set_name(n) {
  name = n;
}
id(str) { return str == name || str == "quest"; }

short() {
  return name;
}
hint() { return hint_string; }
