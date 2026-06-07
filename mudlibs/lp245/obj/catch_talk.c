string match, fun, ob_str, type;
object next;
string super;
/*
short() { return type + "#" + match; }

*/
void collaps() {
  if(next)
    call_other(next, "collaps");

  destruct(this_object());
}

void link(object ob) {
  next = ob;
}

object remove_match(string str) {
  if (str == match) {
    destruct(this_object());

    return next;
  }

  if (next)
    next = call_other(next, "remove_match", str);

  return this_object();
}

/*
* The function to call.
*/
void set_function(string f) {
  fun = f;
}

/*
* The string to match.
*/
void set_match(string str) {
  match = str;
}

/*
* The object to call.
*/
void set_object(string ob) {  /* NOTE: a string */
  ob_str = ob;
}

/*
* The function to call.
*/
void set_type(string t) {
  type = t;
}

status test_match(string str) {
  string who,str1;

  if(sscanf(str,"%s " + type + match + " %s\n",who,str1) == 2 ||
    sscanf(str,"%s " + type + match + "\n",who) == 1 ||

  sscanf(str,"%s " + type + match + "%s\n",who,str1) == 2 ||
  sscanf(str,"%s " + type + " " + match + "\n",who) == 1 ||
  sscanf(str,"%s " + type + " " + match + " %s\n",who,str1) == 2) {
    return call_other(ob_str, fun, str);
  }
  if (next)
    return call_other(next, "test_match", str);

  else
    return 0;
}
status drop() { return 1; }
